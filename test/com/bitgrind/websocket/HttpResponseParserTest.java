package com.bitgrind.websocket;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;

import junit.framework.TestCase;

public class HttpResponseParserTest extends TestCase {
  private static final String CRLF = "\r\n";

  /** Live headers returned from a curl of http://www.google.com */
  private static final String GOOGLE_RESPONSE =
      "HTTP/1.1 200 OK" + CRLF +
      "Date: Sat, 09 Jul 2011 15:14:26 GMT" + CRLF +
      "Expires: -1" + CRLF +
      "Cache-Control: private, max-age=0" + CRLF +
      "Content-Type: text/html; charset=ISO-8859-1" + CRLF +
      "Set-Cookie: PREF=ID=27e7db90c2727d40:FF=0:TM=1310224466:LM=1310224466:S=RIOQ4J_GU1nd-QV4; expires=Mon, 08-Jul-2013 15:14:26 GMT; path=/; domain=.google.com" + CRLF +
      "Set-Cookie: NID=48=GF1_0RyvAMyVtt8V5E3NamWVHKVeIGl0yZGkY27RQ4JnhrkgP6kRgbUhL3_kR-DBFcuaahzPKx4kcgKCSKc5YQ4h8lRNBT9nB-kWD9RqRjW9nOmnDA4kTr6STgBoZlzB; expires=Sun, 08-Jan-2012 15:14:26 GMT; path=/; domain=.google.com; HttpOnly" + CRLF +
      "Server: gws" + CRLF +
      "X-XSS-Protection: 1; mode=block" + CRLF +
      "" + CRLF +
      "===================== arbitrary body content ======================";

  /**
   * A byte channel backed by a string, with the added ability to artbitrily
   * fragment the message into as many parts as desired.
   */
  class StringByteChannel implements ReadableByteChannel {
    boolean open = true;
    private final byte[] bytes;
    private int offset = 0;
    private int readSize;

    public StringByteChannel(String s, int numReads) {
      this.bytes = s.getBytes();
      readSize = bytes.length / numReads;
    }

    @Override
    public boolean isOpen() {
      return open;
    }

    @Override
    public void close() throws IOException {
      open = false;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
      if (!open)
        throw new ClosedChannelException();
      if (offset >= bytes.length)
        return -1;
      int read = Math.min(bytes.length - offset, readSize);
      dst.put(bytes, offset, read);
      offset += read;
      return read;
    }
  }

  /**
   * Attempt parsing the test response multiple times adjusting the number of
   * reads used each time. (Should help catch and buffering errors)
   */
  public void test() throws IOException {
    for (int i = 1; i < 10; i++) {
      HttpResponseParser parser = new HttpResponseParser();
      @SuppressWarnings("unused")
      HttpResponse response =
          parser.parse(new PushbackInputStream(Channels.newInputStream(new StringByteChannel(GOOGLE_RESPONSE, i))));
    }
  }
}
