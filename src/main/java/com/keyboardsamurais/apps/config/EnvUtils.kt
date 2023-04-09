package com.keyboardsamurais.apps.config

import io.github.cdimascio.dotenv.dotenv

object EnvUtils {
    private val dotenv = dotenv {
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }

    fun getEnv(key: String): String {
        return dotenv[key]
    }
}
