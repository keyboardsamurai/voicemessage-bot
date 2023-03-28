FROM alpine:3.17

RUN apk add openjdk17-jre-headless ffmpeg yt-dlp

# Set the working directory in the container
WORKDIR /app

USER nobody

# Copy the locally compiled code to the container
COPY --chown=nobody:nobody target/app.jar .

ENV CHAT_ID=MyTelegramChatId
ENV TELEGRAM_BOT_NAME=MyTelegramBotName
ENV TELEGRAM_BOT_TOKEN=MyTelegramBotToken
ENV OPENAPI_TOKEN=MyOpenApiToken
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV YTDLP_PATH=/usr/bin/yt-dlp

# Run the app
CMD ["java", "-jar", "app.jar"]
