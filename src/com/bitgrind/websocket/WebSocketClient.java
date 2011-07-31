/*
 * Copyright 2011 Mark Renouf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bitgrind.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.primitives.Bytes;


public class WebSocketClient {

  public static interface WebSocketListener {
    void onOpen();
    void onMessage(String message);
    void onMessage(byte[] message);
    void onError(Throwable error);
    void onClose();
  }

  private static final byte[] keyChars = Bytes.concat(range(0x21, 0x2f), range(0x3a, 0x7e));
  private static final Random rnd = new Random();

  private final Charset UTF8 = Charset.forName("UTF-8");
  private final MessageDigest MD5;

  private URI uri;
  private InetSocketAddress endpoint;
  private Socket socket;
  private final WebSocketListener listener;

  public WebSocketClient(String wsUrl, WebSocketListener listener) throws IOException, URISyntaxException {
    this.listener = listener;
    try {
      MD5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }

    URI tmp = new URI(wsUrl);
    int port = tmp.getPort();
    if (tmp.getPort() == -1)
      port = 80;

    String path = tmp.getPath();
    if (path.equals(""))
      path = "/";

    this.uri = new URI(tmp.getScheme(), "", tmp.getHost(), port, path, "", "");
    endpoint = new InetSocketAddress(uri.getHost(), uri.getPort());
    socket = new Socket();
    socket.connect(endpoint);
    PushbackInputStream input = new PushbackInputStream(socket.getInputStream());
    OutputStream output = socket.getOutputStream();
    doHandShake(input, output);
    listener.onOpen();
    Thread thread = new Thread(new WebSocketTask(socket, input, output));
    thread.start();
  }

  void onMessage(String message) {
    listener.onMessage(message);
  }

  public void onMessage(byte[] message) {
    listener.onMessage(message);
  }

  private class WebSocketTask implements Runnable {
    private final InputStream input;
    private final OutputStream output;

    public WebSocketTask(Socket socket, InputStream input, OutputStream output) {
      this.input = input;
      this.output = output;
    }

    @Override
    public void run() {
      boolean closing = false;
      boolean closed = false;
      boolean error = false;
      ByteBuffer messageBuffer = ByteBuffer.allocate(4096);
      while (!(closed || error)) {
        try {
          int frameType = input.read();
          long length = 0;
          if ((frameType & 0x80) == 0x80) {
            int b;
            do {
              b = input.read();
              length *= 128;
              length += b & 0x7f;
            } while ((b & 0x80) == 0x80);
            if (frameType == 0xff && length == 0) {
              if (closing) {
                closed = true;
                listener.onClose();
                break;
              } else {
                closing = true;
                output.write(0xff);
                output.write(0x00);
                output.flush();
                Closeables.closeQuietly(output);
              }
            } else if (length < Integer.MAX_VALUE) {
              byte[] buffer = new byte[(int) length];
              ByteStreams.readFully(input, buffer);
              ReadableByteChannel channel = Channels.newChannel(input);
              do {
                if (messageBuffer.capacity() < length) {
                  messageBuffer = expand(messageBuffer, 1 - (messageBuffer.capacity() / length));
                }
                channel.read(messageBuffer);
              } while (messageBuffer.position() < length);
              messageBuffer.flip();
              onMessage(buffer);
              messageBuffer.clear();
            } else {
              ByteStreams.skipFully(input, length);
            }
          } else {
            int b;
            while ((b = input.read()) != 0xff) {
              messageBuffer.put((byte) b);
              if (messageBuffer.remaining() == 0) {
                messageBuffer = expand(messageBuffer, 1.6f);
              }
            }
            messageBuffer.flip();
            String message = UTF8.newDecoder().decode(messageBuffer).toString();
            messageBuffer.clear();
            if (frameType == 0x00) {
              onMessage(message);
            }
          }
        } catch (IOException e) {
          error = true;
          listener.onError(e);
        }
      }
      Closeables.closeQuietly(input);
      try {
        socket.close();
      } catch (IOException e) {}
    }

    private ByteBuffer expand(ByteBuffer buffer, float scaleFactor) {
      int newCapacity = (int) (buffer.capacity() * scaleFactor) + 1;
      ByteBuffer resized = ByteBuffer.allocate(newCapacity);
      resized.put(buffer.array(), 0, buffer.limit());
      resized.position(buffer.position());
      resized.limit(buffer.capacity());
      return resized;
    }
  }

  private void doHandShake(PushbackInputStream source, OutputStream output) throws IOException {
    PushbackInputStream input = new PushbackInputStream(source, 2000);
    StringBuilder request = new StringBuilder();
    request.append(String.format("GET %s HTTP/1.1\r\n", uri.getPath()));
    request.append("Upgrade: WebSocket\r\n");
    request.append("Connection: Upgrade\r\n");
    request.append(String.format("Host: %s\r\n", uri.getHost()));
    request.append(String.format("Origin: %s:%d\r\n", uri.getHost(), uri.getPort()));

    int spaces1 = rnd.nextInt(12) + 1;
    int spaces2 = rnd.nextInt(12) + 1;

    long key1 = rnd.nextInt(Integer.MAX_VALUE / spaces1);
    long key2 = rnd.nextInt(Integer.MAX_VALUE / spaces2);

    request.append(String.format("Sec-WebSocket-Key1: %s\r\n", generateKey(key1, spaces1)));
    request.append(String.format("Sec-WebSocket-Key2: %s\r\n", generateKey(key2, spaces2)));
    request.append("\r\n");
    try {
      ByteBuffer encoded = UTF8.newEncoder().encode(CharBuffer.wrap(request.toString()));
      output.write(encoded.array(), encoded.position(), encoded.limit());
    } catch (CharacterCodingException e) {
      throw new AssertionError(e);
    }
    byte[] key3 = new byte[8];
    rnd.nextBytes(key3);

    // make sure MSB is 0 due to buggy servers that
    // don't handle key3 > 0x7fffffffffffffff (That's *you* MtGox!)
    key3[0] &= 0x7f;

    output.write(key3);

    // stuff 'em all together, key1+key2+key3, big-endian order
    BigInteger merged =
        BigInteger.valueOf(key1)
            .shiftLeft(32)
            .or(BigInteger.valueOf(key2))
            .shiftLeft(64)
            .or(new BigInteger(key3));

    // The server is expected to send back the MD5 of this
    byte[] expectedResponse = MD5.digest(merged.toByteArray());

    HttpResponseParser parser = new HttpResponseParser();
    @SuppressWarnings("unused")
    HttpResponse response = parser.parse(input);
    // TODO verify response makes sense for a WebSocket connection

    byte[] serverResponse = new byte[16];
    input.read(serverResponse);

    if (!Arrays.equals(serverResponse, expectedResponse)) {
      System.err.println("Handshake failure!");
    }
  }

  private static byte[] range(int start, int stop) {
    int len = stop - start;
    byte[] arr = new byte[len];
    for (int i = 0; i < len; i++) {
      arr[i] = (byte) (start + i);
    }
    return arr;
  }

  private static String generateKey(long v, int divisor) {
    final int fillCount = 12;
    int spaces = divisor;

    BigInteger number = new BigInteger(Long.toString(v));
    number = number.multiply(new BigInteger(Integer.toString(spaces)));
    StringBuilder s = new StringBuilder(number.toString());
    for (int i = 0; i < fillCount; i++) {
      int pos = rnd.nextInt(s.length() - 1) + 1;
      s.insert(pos, (char) keyChars[rnd.nextInt(keyChars.length)]);
    }
    for (int i = 0; i < spaces; i++) {
      int pos = rnd.nextInt(s.length() - 1) + 1;
      s.insert(pos, " ");
    }
    return s.toString();
  }
}
