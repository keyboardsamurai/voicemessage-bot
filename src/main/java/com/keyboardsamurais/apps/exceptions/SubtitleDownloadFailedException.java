package com.keyboardsamurais.apps.exceptions;

public class SubtitleDownloadFailedException extends MessageProcessingException {
    public SubtitleDownloadFailedException(final Exception e) {
        super(e);
    }

    public SubtitleDownloadFailedException(final String msg) {
        super(msg);
    }
}
