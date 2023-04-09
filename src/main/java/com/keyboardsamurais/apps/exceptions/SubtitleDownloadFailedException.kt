package com.keyboardsamurais.apps.exceptions

class SubtitleDownloadFailedException : MessageProcessingException {
    constructor(e: Exception?) : super(e)
    constructor(msg: String?) : super(msg)
}
