package com.github.mati1979.play.hysterix;

import java.util.concurrent.TimeUnit;

public class HysterixResponse<T> {

    private T response;
    private HysterixResponseMetadata metadata;

    private HysterixResponse(final T response, final HysterixResponseMetadata metadata) {
        this.response = response;
        this.metadata = metadata;
    }

    public static <T> HysterixResponse create(final T response, final HysterixResponseMetadata metadata) {
        return new HysterixResponse(response, metadata);
    }

    public T getResponse() {
        return response;
    }

    public boolean isExecutionComplete() {
        return metadata.isExecutionComplete();
    }

    public boolean isSuccessfulExecution() {
        return metadata.isSuccessfulExecution();
    }

    public boolean isShortCircuited() {
        return metadata.isShortCircuited();
    }

    public boolean isError() {
        return metadata.isError();
    }

    public boolean isFailedExecution() {
        return metadata.isFailedExecution();
    }

    public boolean isResponseFromFallback() {
        return metadata.isResponseFromCache();
    }

    public boolean isResponseTimeout() {
        return metadata.isResponseTimeout();
    }

    public boolean isResponseFromCache() {
        return metadata.isResponseFromCache();
    }

    public long getExecutionTime(final TimeUnit timeUnit) {
        return metadata.getExecutionTime(timeUnit);
    }

    @Override
    public String toString() {
        return "HysterixResponse{" +
                "response=" + response +
                ", metadata=" + metadata +
                '}';
    }

}
