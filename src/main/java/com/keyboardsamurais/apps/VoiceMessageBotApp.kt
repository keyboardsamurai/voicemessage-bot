package com.keyboardsamurais.apps

import com.keyboardsamurais.apps.config.EnvUtils
import com.keyboardsamurais.apps.notification.VoiceMessageBot
import mu.KotlinLogging
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


private val log = KotlinLogging.logger {}
object VoiceMessageBotApp {
    @JvmStatic
    fun main(ignoredArgs: Array<String>) {
        log.info("Starting VoiceMessageBotApp")

        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        val options = DefaultBotOptions()
        val botToken = EnvUtils.getEnv("TELEGRAM_BOT_TOKEN")
        val updatePostedHandler = VoiceMessageBot(options, botToken)
        telegramBotsApi.registerBot(updatePostedHandler)

    }
}
