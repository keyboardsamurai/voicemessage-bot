package com.keyboardsamurais.apps.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.keyboardsamurais.apps.config.EnvUtils;
import com.keyboardsamurais.apps.exceptions.MessageProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.*;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OpenAIClient {

    private static final String BASE_URL = "https://api.openai.com/";

    private static final int HTTP_CONNECTION_TIMEOUT = 120; // uploads to whisper of up to 25 mbyte  might take a while
    public static final int PROMPT_SIZE_LIMIT = 4096;
    private final ObjectMapper objectMapper;

    public OpenAIClient() {
        objectMapper = new ObjectMapper();
    }

    public CompletableFuture<String> gptCompletionRequest(String prompt) {
        if (prompt.length() > PROMPT_SIZE_LIMIT) {
            throw new MessageProcessingException("Prompt length must be less than " + PROMPT_SIZE_LIMIT + " characters");
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(createAuthorizationInterceptor(EnvUtils.getEnv("OPENAPI_TOKEN")))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        GptApi gptApi = retrofit.create(GptApi.class);

        // Modify max_tokens as needed
        String requestBodyJson = String.format(
                """
                        {"prompt": %s,"max_tokens": 1024,"stop": ["###"], "model":"text-davinci-003"}""", createJsonWithEscapedString(prompt)
        );
        log.debug(requestBodyJson);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson);

        Call<JsonNode> gptCall = gptApi.getCompletion(requestBody);
        CompletableFuture<String> future = new CompletableFuture<>();
        gptCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<JsonNode> call, Response<JsonNode> response) {
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new HttpException(response));
                    return;
                }

                JsonNode jsonNode = response.body();
                log.debug("jsonNode: {}", jsonNode.toPrettyString());
                future.complete(StringUtils.trim(jsonNode.get("choices").get(0).get("text").asText()));
            }

            @Override
            public void onFailure(Call<JsonNode> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    /**
     * Transcribe voice file to text through the OpenAI Whisper API.
     *
     * @param voiceFile audio file in 'm4a', 'mp3', 'webm', 'mp4', 'mpga', 'wav', 'mpeg' formats
     * @return the text transcription of the voice file
     */
    public CompletableFuture<String> transcribeAudio(File voiceFile) {
        if (voiceFile.length() > 25 * 1024 * 1024) {
            throw new MessageProcessingException("File size is too large. Max size is 25MB, but is %d bytes".formatted(voiceFile.length()));
        }
        var client = new OkHttpClient().newBuilder()
                .readTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .build();

        String mimeType;
        try {
            mimeType = Files.probeContentType(voiceFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", voiceFile.getName(),
                        RequestBody.create(MediaType.parse(mimeType), voiceFile))
                .addFormDataPart("model", "whisper-1")
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "v1/audio/transcriptions")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + EnvUtils.getEnv("OPENAPI_TOKEN"))
                .addHeader("Content-Type", "multipart/form-data")
                .build();

        CompletableFuture<String> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                    return;
                }

                JsonNode jsonNode = objectMapper.readTree(response.body().string());
                future.complete(jsonNode.get("text").asText());
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    private Interceptor createAuthorizationInterceptor(String apiKey) {
        return chain -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + apiKey)
                    .build();
            return chain.proceed(newRequest);
        };
    }

    public String createJsonWithEscapedString(String input) {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("text", input);
        try {
            return objectMapper.writeValueAsString(rootNode.get("text"));
        } catch (JsonProcessingException e) {
            throw new MessageProcessingException(e);
        }
    }
}
