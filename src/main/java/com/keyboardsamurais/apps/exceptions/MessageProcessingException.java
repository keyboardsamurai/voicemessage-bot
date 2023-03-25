package com.keyboardsamurais.apps.exceptions;

public class MessageProcessingException extends RuntimeException {
    public MessageProcessingException(final Exception e) {
        super(e);
    }

    public MessageProcessingException(final String msg) {
        super(msg);
    }
}
