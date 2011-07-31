/*
 * Copyright (C) 2008 Google Inc.
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
 *  - moved from package com.google.common.primitives
 *  - removed unused methods
 *  - code formatting
 */

package com.bitgrind.websocket.util;

import java.util.Arrays;

/**
 * Static utility methods pertaining to {@code byte} primitives, that are not
 * already found in either {@link Byte} or {@link Arrays}, <i>and interpret
 * bytes as neither signed nor unsigned</i>. The methods which specifically
 * treat bytes as signed or unsigned are found in {@link SignedBytes} and {@link
 * UnsignedBytes}.
 *
 * @author Kevin Bourrillion
 * @since 2009.09.15 <b>tentative</b>
 */
public class Bytes {
  private Bytes() {
  }

  /**
   * Returns the values from each provided array combined into a single array.
   * For example, {@code concat(new byte[] a, b}, new byte[] {}, new
   * byte[] {c}} returns the array {@code a, b, c}}.
   *
   * @param arrays zero or more {@code byte} arrays
   * @return a single array containing all the values from the source arrays, in
   *         order
   */
  public static byte[] concat(byte[]... arrays) {
    int length = 0;
    for (byte[] array : arrays) {
      length += array.length;
    }
    byte[] result = new byte[length];
    int pos = 0;
    for (byte[] array : arrays) {
      System.arraycopy(array, 0, result, pos, array.length);
      pos += array.length;
    }
    return result;
  }

}
