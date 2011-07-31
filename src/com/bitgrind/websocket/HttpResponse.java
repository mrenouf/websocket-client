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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class HttpResponse {
  private String version;
  private int statusCode;
  private String statusMessage;
  private Map<String, String> headers;

  public HttpResponse(String statusLine) {
    Scanner scanner = new Scanner(statusLine);
    scanner.useDelimiter("\\s+");

    if (!scanner.hasNext())
      throw new IllegalArgumentException("Invalid status line");

    String protocolVersion = scanner.next();

    if (!protocolVersion.startsWith("HTTP/") || protocolVersion.length() <= 5)
      throw new IllegalArgumentException("Invalid status line, missing protocol or version");

    version = protocolVersion.substring(5);
    statusCode = scanner.nextInt();
    if (scanner.hasNext())
      statusMessage = scanner.nextLine().trim();

    headers = new LinkedHashMap<String, String>();
  }

  public String getVersion() {
    return version;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public void addHeader(String name, String value) {
    headers.put(name, value);
  }

  public boolean hasHeader(String name) {
    return headers.containsKey(name);
  }

  public String getHeaderValue(String name) {
    return headers.get(name);
  }

  @Override
  public String toString() {
    return String.format("HttpResponse [version=%s, statusCode=%s, statusMessage=%s, headers=%s]", version, statusCode, statusMessage, headers);
  }
}
