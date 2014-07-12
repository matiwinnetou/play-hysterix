package com.github.mati1979.play.hysterix;

/**
 * Created by mati on 12/07/2014.
 */
public class HysterixException extends RuntimeException {

    public HysterixException(final String message) {
        super(message);
    }

    public HysterixException(final String message, final Throwable t) {
        super(message, t);
    }

}
