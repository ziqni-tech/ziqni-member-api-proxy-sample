ZIQNI Member API
=========================
### Sample webSocket proxy using Vert.x

Vert.x, is well-suited for creating WebSocket proxy applications. Here's a simplified version using Vert.x:

    import io.vertx.core.Vertx;
    import io.vertx.core.http.HttpClient;
    import io.vertx.core.http.HttpServer;
    import io.vertx.core.http.WebSocket;
    
    public class WebSocketProxy {
    public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    HttpServer server = vertx.createHttpServer();

        server.webSocketHandler(clientWebSocket -> {
            HttpClient httpClient = vertx.createHttpClient();
            httpClient.webSocket(443, "target-websocket-server.com", "/", targetWebSocketAsyncResult -> {
                if (targetWebSocketAsyncResult.succeeded()) {
                    WebSocket targetWebSocket = targetWebSocketAsyncResult.result();

                    // Forward messages from client to target server
                    clientWebSocket.handler(data -> targetWebSocket.writeBinaryMessage(data));

                    // Forward messages from target server back to client
                    targetWebSocket.handler(data -> clientWebSocket.writeBinaryMessage(data));

                    clientWebSocket.closeHandler(v -> targetWebSocket.close());
                    targetWebSocket.closeHandler(v -> clientWebSocket.close());
                } else {
                    clientWebSocket.close();
                }
            });
        }).listen(8080);
    }
}

##### In this example:

Vert.x is used to create a WebSocket server and client.
The WebSocket client (httpClient.webSocket) connects to the target WebSocket server.
Messages are forwarded between the client and server sockets.

In the Vert.x WebSocket proxy example provided, each client connecting to your server will get its own connection to the proxied server. When a client connects to your server, a new WebSocket connection to the target (proxied) server is established specifically for that client. This approach ensures that each client has an independent communication channel to the target server.

##### Here is how it works in the example:

Client Connection: When a client initiates a WebSocket connection to your server, the server.webSocketHandler() gets called, creating a new connection handler for that client.

Target WebSocket Connection: Inside this handler, a new connection is established to the proxied server using httpClient.webSocket(...). This connection is unique for each client.

Message Handling: Any message received from the client is forwarded to the target server through this dedicated connection, and any message received from the target server is sent back to the respective client.

This means that every client will get its own WebSocket connection to both your proxy server and the downstream server, providing isolation and ensuring that client communications are not shared.
