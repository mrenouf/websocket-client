package com.bitgrind.websocket;

import junit.framework.TestCase;

/**
 * Various tests of the HTTP response line parsing done in the HttpResponse
 * constructor
 */
public class HttpResponseTest extends TestCase {

  public void testHttpResponse() {
    HttpResponse response = new HttpResponse("HTTP/1.1 200 OK");
    assertEquals("1.1", response.getVersion());
    assertEquals(200, response.getStatusCode());
    assertEquals("OK", response.getStatusMessage());
  }

  public void testHttpResponse2() {
    HttpResponse response = new HttpResponse("HTTP/1.1 401 You shall not pass!");
    assertEquals("1.1", response.getVersion());
    assertEquals(401, response.getStatusCode());
    assertEquals("You shall not pass!", response.getStatusMessage());
  }

  public void testHttpResponse3() {
    HttpResponse response = new HttpResponse("HTTP/1.1 404           Not            Found");
    assertEquals("1.1", response.getVersion());
    assertEquals(404, response.getStatusCode());
    assertEquals("Not            Found", response.getStatusMessage());
  }

  public void testHttpResponse4() {
    HttpResponse response = new HttpResponse("HTTP/wacky 999999");
    assertEquals("wacky", response.getVersion());
    assertEquals(999999, response.getStatusCode());
    assertNull(response.getStatusMessage());
  }

  public void testHttpResponse5() {
    try {
      @SuppressWarnings("unused")
      HttpResponse response = new HttpResponse("HTTP/");
    } catch (IllegalArgumentException ex) {
      return; // success
    }
    fail("Expected " + IllegalArgumentException.class);
  }

  public void testHttpResponse6() {
    try {
      @SuppressWarnings("unused")
      HttpResponse response = new HttpResponse("");
    } catch (IllegalArgumentException ex) {
      return; // success
    }
    fail("Expected " + IllegalArgumentException.class);
  }

  public void testHttpResponse7() {
    try {
      @SuppressWarnings("unused")
      HttpResponse response = new HttpResponse("http");
    } catch (IllegalArgumentException ex) {
      return; // success
    }
    fail("Expected " + IllegalArgumentException.class);
  }
}
