/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications:
 *  - moved from package com.google.common.io
 *  - removed uneeded methods
 *  - code formatting
 */
package com.bitgrind.websocket.util;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides utility methods for working with byte arrays and I/O streams.
 * <p>
 * All method parameters must be non-null unless documented otherwise.
 *
 * @author Chris Nokleberg
 * @since 2009.09.15 <b>tentative</b>
 */
public class ByteStreams {
  /**
   * Discards {@code n} bytes of data from the input stream. This method
   * will block until the full amount has been skipped. Does not close the
   * stream.
   *
   * @param in the input stream to read from
   * @param n the number of bytes to skip
   * @throws EOFException if this stream reaches the end before skipping all
   *           the bytes
   * @throws IOException if an I/O error occurs, or the stream does not
   *           support skipping
   */
  public static void skipFully(InputStream in, long n) throws IOException {
    while (n > 0) {
      long amt = in.skip(n);
      if (amt == 0) {
        // Force a blocking read to avoid infinite loop
        if (in.read() == -1) {
          throw new EOFException();
        }
        n--;
      } else {
        n -= amt;
      }
    }
  }

  /**
   * Attempts to read enough bytes from the stream to fill the given byte array,
   * with the same behavior as {@link DataInput#readFully(byte[])}.
   * Does not close the stream.
   *
   * @param in the input stream to read from.
   * @param b the buffer into which the data is read.
   * @throws EOFException if this stream reaches the end before reading all
   *           the bytes.
   * @throws IOException if an I/O error occurs.
   */
  public static void readFully(InputStream in, byte[] b) throws IOException {
    readFully(in, b, 0, b.length);
  }

  /**
   * Attempts to read {@code len} bytes from the stream into the given array
   * starting at {@code off}, with the same behavior as
   * {@link DataInput#readFully(byte[], int, int)}. Does not close the
   * stream.
   *
   * @param in the input stream to read from.
   * @param b the buffer into which the data is read.
   * @param off an int specifying the offset into the data.
   * @param len an int specifying the number of bytes to read.
   * @throws EOFException if this stream reaches the end before reading all
   *           the bytes.
   * @throws IOException if an I/O error occurs.
   */
  private static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
    if (read(in, b, off, len) != len) {
      throw new EOFException();
    }
  }

  /**
   * Reads some bytes from an input stream and stores them into the buffer array
   * {@code b}. This method blocks until {@code len} bytes of input data have
   * been read into the array, or end of file is detected. The number of bytes
   * read is returned, possibly zero. Does not close the stream.
   * <p>
   * A caller can detect EOF if the number of bytes read is less than
   * {@code len}. All subsequent calls on the same stream will return zero.
   * <p>
   * If {@code b} is null, a {@code NullPointerException} is thrown. If
   * {@code off} is negative, or {@code len} is negative, or {@code off+len} is
   * greater than the length of the array {@code b}, then an
   * {@code IndexOutOfBoundsException} is thrown. If {@code len} is zero, then
   * no bytes are read. Otherwise, the first byte read is stored into element
   * {@code b[off]}, the next one into {@code b[off+1]}, and so on. The number
   * of bytes read is, at most, equal to {@code len}.
   *
   * @param in the input stream to read from
   * @param b the buffer into which the data is read
   * @param off an int specifying the offset into the data
   * @param len an int specifying the number of bytes to read
   * @return the number of bytes read
   * @throws IOException if an I/O error occurs
   */
  private static int read(InputStream in, byte[] b, int off, int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException("len is negative");
    }
    int total = 0;
    while (total < len) {
      int result = in.read(b, off + total, len - total);
      if (result == -1) {
        break;
      }
      total += result;
    }
    return total;
  }

}
