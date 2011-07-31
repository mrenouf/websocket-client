package com.bitgrind.websocket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import com.bitgrind.websocket.WebSocketClient.WebSocketListener;

public class WebSocketClientDemo {
  public static void main(String[] args) throws IOException, URISyntaxException {
    @SuppressWarnings("unused")
    WebSocketClient client = new WebSocketClient("ws://websocket.mtgox.com/mtgox", new WebSocketListener() {
      @Override
      public void onOpen() {
        System.out.println("onOpen");
      }

      @Override
      public void onMessage(String message) {
        System.out.println("onMessage: " + message);
      }

      @Override
      public void onMessage(byte[] message) {
        System.out.println("onMessage: " + Arrays.toString(message));
      }

      @Override
      public void onError(Throwable error) {
        System.out.println("onError: " + error);
      }

      @Override
      public void onClose() {
        System.out.println("onClose");
      }
    });
  }
}
