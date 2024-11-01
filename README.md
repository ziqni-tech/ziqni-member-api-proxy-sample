WebSocket STOMP Proxy with Dynamic Authentication Token
=======================================================

### Overview

In this implementation, we created a WebSocket proxy using Vert.x, which also handles a dynamic STOMP authentication. The proxy intercepts the initial WebSocket message to send a STOMP CONNECT message containing a passcode that is dynamically retrieved from a REST API (ZiqniApiHandler). This guide explains how this integration works step by step.

### Key Components

* ZiqniApiHandler: Handles the HTTP POST request to the ZIQNI API to retrieve a JWT token used for authentication.
* WebSocketStompProxy: The WebSocket proxy server that intercepts incoming WebSocket connections, retrieves an authentication token, and sends a STOMP CONNECT message.

### _Steps Explained_

#### 1. Creating ZiqniApiHandler

We created a class named ZiqniApiHandler that handles interactions with the ZIQNI member API (https://member-api.ziqni.com). The main purpose of this class is to make a POST request to the API and return a Future<JsonObject> containing the authentication information.

* Method: postToZiqniApi(String apiKey, boolean isReferenceId, String[] origins, String member, int expires)
* Request Payload:
* apiKey: The API key for authentication.
* isReferenceId: A boolean flag.
* origins, member, expires: Additional parameters required by the API.
* Response: Returns the jwtToken used for subsequent authentication.

#### 2. Setting up the WebSocket Proxy Server

* We implemented WebSocketStompProxy to act as a WebSocket proxy server that connects clients to a target WebSocket server.
* HTTP Server Setup: A Vert.x HTTP server listens for incoming WebSocket connections.
* Intercept Initial Client Connection: When a client connects, we call ZiqniApiHandler.postToZiqniApi() to retrieve the jwtToken for authentication.
* Construct the STOMP CONNECT Message:
  *  Once the token is retrieved successfully, we construct a STOMP CONNECT message:
  *  String stompAuthMessage = "CONNECT\naccept-version:1.2\nhost:example.com\nlogin:username\npasscode:" + jwtToken + "\n\n\0";
  *  This message includes a dynamically generated passcode (jwtToken) obtained from ZiqniApiHandler.

#### 3. Handling the WebSocket Proxy Logic

* Connect to Target WebSocket Server: After retrieving the jwtToken, the proxy establishes a connection to the target WebSocket server.
* Send the STOMP CONNECT Message: The stompAuthMessage is sent to the target server to initiate the STOMP protocol authentication.
* Message Forwarding: Subsequent messages between the client and the target server are forwarded bi-directionally:
  * Client to Target Server: Messages from the client are sent to the target WebSocket server.
  * Target Server to Client: Messages from the target server are forwarded back to the client.

#### 4. Error Handling

* If the ZiqniApiHandler.postToZiqniApi() call fails (e.g., due to authentication issues), the client connection is immediately closed, and an error message is provided.
* If the target WebSocket server connection fails, the client connection is also closed.

#### Summary

This solution allows for a dynamic authentication mechanism when proxying WebSocket connections. The main steps involved are:

* Retrieve a jwtToken from the ZIQNI member API using ZiqniApiHandler.
* Use this token as the passcode in a STOMP CONNECT message.
* Establish and maintain the WebSocket proxy connection, forwarding messages between the client and the target server.

The design ensures that each client connection is authenticated using a unique token retrieved dynamically, making the solution more secure and adaptable to different client sessions.
