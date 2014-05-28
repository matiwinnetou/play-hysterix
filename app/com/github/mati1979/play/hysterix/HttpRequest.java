package com.github.mati1979.play.hysterix;

import play.libs.F;

import java.util.UUID;

/**
 * Created by mati on 25/05/2014.
 */
public class HttpRequest {

    private String requestId;

    private HttpRequestsCache httpRequestsCache;

    public HttpRequest(final HttpRequestsCache httpRequestsCache, final HysterixCommand command) {
        this.requestId = UUID.randomUUID().toString();
        this.httpRequestsCache = httpRequestsCache.addRequest(requestId, command);
    }

    public String getRequestId() {
        return requestId;
    }

    public F.Promise executeRequest() {
        return httpRequestsCache.execute(requestId);
    }

}
