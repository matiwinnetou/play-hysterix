package com.github.mati1979.play.hysterix;

import play.libs.F;

/**
 * Created by mszczap on 27.05.14.
 */
public class HysterixResponse<T> {

    private T response;
    private HysterixResponseMetadata metadata;

    private HysterixResponse(T response, HysterixResponseMetadata metadata) {
        this.response = response;
        this.metadata = metadata;
    }

    public T getResponse() {
        return response;
    }

    public HysterixResponseMetadata getMetadata() {
        return metadata;
    }

    public static <T> HysterixResponse create(T response, HysterixResponseMetadata metadata) {
        return new HysterixResponse(response, metadata);
    }

}
