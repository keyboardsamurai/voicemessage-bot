package com.keyboardsamurais.apps;

import com.keyboardsamurais.apps.notification.VoiceMessageBot;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class VoiceMessageBotApp {

    public static void main(String[] args) {
        log.info("Starting VoiceMessageBotApp");

        try {
            var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            VoiceMessageBot updatePostedHandler = new VoiceMessageBot();
            telegramBotsApi.registerBot(updatePostedHandler);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
