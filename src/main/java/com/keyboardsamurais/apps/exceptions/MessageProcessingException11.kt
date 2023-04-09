package com.keyboardsamurais.apps.exceptions

open class MessageProcessingException : RuntimeException {
    constructor(e: Exception?) : super(e)
    constructor(msg: String?) : super(msg)
    constructor(tex: Throwable?) : super(tex)
}
