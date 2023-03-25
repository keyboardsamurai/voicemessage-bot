# VoiceMessage Telegram Bot

VoiceMessageBotApp is a Java application that utilizes the OpenAI and Telegram API to interact with users through a Telegram Bot. 
The primary functionality of the bot includes transcribing voice messages, audio files, and YouTube videos into text. 
Users can also request a summarized version of the transcribed text.                                                                                      

## Features

- Transcribe voice messages and audio files sent to the bot
- Transcribe audio from YouTube video URLs sent to the bot using existing subtitles or by downloading the video and converting it to audio
- Summarize the transcribed text upon user request
* Can be run locally, or as a Docker container
* Uses [Dotenv](https://github.com/cdimascio/dotenv-java) to load environment variables from a `.env` file, so it can 
be run locally without having to set environment variables

## Prerequisites

- Java 17 or higher
- Maven
- FFmpeg
- yt-dlp
- Telegram API Token
- OpenAI API Token
                                                     

## Usage

Follow these instructions on how to set up your own a Telegram bot: https://core.telegram.org/bots/features#botfather
then, copy the token and chat id from the botfather response and set them as environment variables.

The bot is intended to be run as a docker container. The following environment variables are required for it to work:

* `CHAT_ID`: The id of the chat to send the messages to
* `OPENAPI_TOKEN`: The token of the Whisper API
* `TELEGRAM_BOT_NAME`: The name of the telegram bot
* `TELEGRAM_BOT_TOKEN`: The token of the telegram bot
* `FFMPEG_PATH` The path to the ffmpeg executable
* `YTDLP_PATH` The path to the yt-dlp executable

## Building
                     
### For local usage
The bot can be built using maven. The following command will build an `app.jar` file in the `target` directory:

    mvn clean package

Feel free to place the following blank `.env` file in the root directory of the project and adjust to your needs:

```
CHAT_ID=MyTelegramChatId
TELEGRAM_BOT_NAME=MyTelegramBotName
TELEGRAM_BOT_TOKEN=MyTelegramBotToken
OPENAPI_TOKEN=MyOpenApiToken
FFMPEG_PATH=/usr/local/bin/ffmpeg
YTDLP_PATH=/usr/local/bin/yt-dlp
```

Or simply set the environment variables in the OS of your choice and run the jar file:

    java -jar target/app.jar
                                                                                                               
### For Docker
The bot can also be built as a docker container. The following command will build a docker image called `voicemessage-bot`:

    docker build -t voicemessage-bot .

Place these environment variables in the Dockerfile and adjust to your needs:                                

```
ENV CHAT_ID=MyTelegramChatId
ENV TELEGRAM_BOT_NAME=MyTelegramBotName
ENV TELEGRAM_BOT_TOKEN=MyTelegramBotToken
ENV OPENAPI_TOKEN=MyOpenApiToken
ENV FFMPEG_PATH=/usr/local/bin/ffmpeg
ENV YTDLP_PATH=/usr/local/bin/yt-dlp
```

## Disclaimer

This bot is not affiliated with Telegram, OpenAI or YouTube in any way. It is a research project and not intended to be used in 
any way that violates either company's ToS.
