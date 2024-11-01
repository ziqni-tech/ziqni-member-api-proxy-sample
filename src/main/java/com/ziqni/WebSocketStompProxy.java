package com.ziqni;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.WebSocket;

public class WebSocketStompProxy {

    private static final String ZIQNI_MEMBER_API = "https://member-api.ziqni.com";
    private final Vertx vertx;
    private final ZiqniApiHandler ziqniApiHandler;

    public WebSocketStompProxy(Vertx vertx) {
        this.vertx = vertx;
        this.ziqniApiHandler = new ZiqniApiHandler(vertx);
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        WebSocketStompProxy proxy = new WebSocketStompProxy(vertx);
        proxy.startProxyServer();
    }

    public void startProxyServer() {
        HttpServer server = vertx.createHttpServer();

        server.webSocketHandler(clientWebSocket -> {
            // Call Ziqni API to get the passcode (jwtToken)
            String[] origins = {"origin1", "origin2"};
            ziqniApiHandler.postToZiqniApi("someApiKey", true, origins, "someMember", 72)
                    .onSuccess(response -> {
                        String jwtToken = response.getJsonObject("data").getString("jwtToken");

                        // Mock a STOMP CONNECT message to authenticate using jwtToken as passcode
                        String stompAuthMessage = "CONNECT\naccept-version:1.2\nhost:example.com\nlogin:bearer\npasscode:" + jwtToken + "\n\n\0";

                        // Establish connection to the target WebSocket server
                        HttpClient httpClient = vertx.createHttpClient();
                        httpClient.webSocket(443, "target-websocket-server.com", "/", targetWebSocketAsyncResult -> {
                            if (targetWebSocketAsyncResult.succeeded()) {
                                WebSocket targetWebSocket = targetWebSocketAsyncResult.result();

                                // Send the STOMP CONNECT message to authenticate
                                targetWebSocket.write(Buffer.buffer(stompAuthMessage));

                                // Forward messages from client to target server
                                clientWebSocket.handler(data -> targetWebSocket.write(data));

                                // Forward messages from target server back to client
                                targetWebSocket.handler(data -> clientWebSocket.write(data));

                                // Close handlers
                                clientWebSocket.closeHandler(v -> targetWebSocket.close());
                                targetWebSocket.closeHandler(v -> clientWebSocket.close());
                            } else {
                                clientWebSocket.close();
                            }
                        });
                    })
                    .onFailure(err -> {
                        // Close the client connection if Ziqni API request failed
                        clientWebSocket.close((short) 1008, "Authentication failed: " + err.getMessage());
                    });
        }).listen(8080);
    }
}
