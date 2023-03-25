package com.keyboardsamurais.apps.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtils {
    public static Dotenv dotenv;

    public static String getEnv(String key) {
        if (dotenv == null) {
            dotenv = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load();
        }
        return dotenv.get(key);
    }
}
