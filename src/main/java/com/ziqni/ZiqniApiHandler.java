package com.ziqni;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.core.json.JsonObject;

public class ZiqniApiHandler {

    private static final String ZIQNI_MEMBER_TOKEN = "https://member-api.ziqni.com/member-token";
    private final WebClient webClient;

    public ZiqniApiHandler(Vertx vertx) {
        this.webClient = WebClient.create(vertx);
    }

    public Future<JsonObject> postToZiqniApi(String apiKey, boolean isReferenceId, String[] origins, String member, int expires) {
        JsonObject requestBody = new JsonObject()
                .put("apiKey", apiKey)
                .put("isReferenceId", isReferenceId)
                .put("origins", origins)
                .put("member", member)
                .put("expires", expires);

        HttpRequest<JsonObject> request = webClient
                .postAbs(ZIQNI_MEMBER_TOKEN)
                .as(BodyCodec.jsonObject());

        return request.sendJsonObject(requestBody)
                .compose(response -> handleResponse(response));
    }

    private Future<JsonObject> handleResponse(HttpResponse<JsonObject> response) {
        if (response.statusCode() == 200) {
            return Future.succeededFuture(response.body());
        } else {
            return Future.failedFuture("Request failed with status code: " + response.statusCode() + ", message: " + response.statusMessage());
        }
    }
}
