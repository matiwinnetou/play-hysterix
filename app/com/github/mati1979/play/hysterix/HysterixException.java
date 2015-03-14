package com.github.mati1979.play.hysterix;

public class HysterixException extends RuntimeException {

    public HysterixException(final String message) {
        super(message);
    }

    public HysterixException(final String message, final Throwable t) {
        super(message, t);
    }

}
