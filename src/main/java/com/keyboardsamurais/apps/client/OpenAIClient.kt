package com.keyboardsamurais.apps.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.keyboardsamurais.apps.config.EnvUtils
import com.keyboardsamurais.apps.exceptions.MessageProcessingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import okhttp3.*
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}
class OpenAIClient {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    suspend fun gptCompletionRequest(prompt: String): String {
        if (prompt.length > PROMPT_SIZE_LIMIT) {
            throw MessageProcessingException("Prompt length must be less than $PROMPT_SIZE_LIMIT characters")
        }
        val client = OkHttpClient.Builder()
            .readTimeout(HTTP_CONNECTION_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(HTTP_CONNECTION_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .addInterceptor(createAuthorizationInterceptor(EnvUtils.getEnv("OPENAPI_TOKEN")))
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
        val gptApi = retrofit.create(GptApi::class.java)

        // Modify max_tokens as needed
        val requestBodyJson = """
            {
                "prompt": ${createJsonWithEscapedString(prompt)},
                "max_tokens": 1024,
                "stop": ["###"],
                "model": "text-davinci-003"
            }
        """.trimIndent()

        log.debug(requestBodyJson)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson)
        return withContext(Dispatchers.IO) {
            val response = gptApi.getCompletion(requestBody)?.execute()
            if (!response!!.isSuccessful) {
                throw HttpException(response)
            }
            val jsonNode = response.body()
            log.debug("jsonNode: {}", jsonNode!!.toPrettyString())
            jsonNode["choices"][0]["text"].asText().trim()
        }
    }


    /**
     * Transcribe voice file to text through the OpenAI Whisper API.
     *
     * @param voiceFile audio file in 'm4a', 'mp3', 'webm', 'mp4', 'mpga', 'wav', 'mpeg' formats
     * @return the text transcription of the voice file
     */
    suspend fun whisperTranscribeAudio(voiceFile: File): String {
        if(voiceFile.length() <= 25 * 1024 * 1024) {
            "File size is too large. Max size is 25MB, but is ${voiceFile.length()} bytes"
        }
        val client = OkHttpClient().newBuilder()
            .readTimeout(HTTP_CONNECTION_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(HTTP_CONNECTION_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()
        val mimeType: String = Files.probeContentType(voiceFile.toPath())
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", voiceFile.name,
                RequestBody.create(MediaType.parse(mimeType), voiceFile)
            )
            .addFormDataPart("model", "whisper-1")
            .build()
        val request = Request.Builder()
            .url(BASE_URL + "v1/audio/transcriptions")
            .post(requestBody)
            .addHeader("Authorization", "Bearer " + EnvUtils.getEnv("OPENAPI_TOKEN"))
            .addHeader("Content-Type", "multipart/form-data")
            .build()
        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Unexpected code ${response.code()}")
            }
            val jsonNode = objectMapper.readTree(response.body()!!.string())
            jsonNode["text"].asText()
        }
    }

    private fun createAuthorizationInterceptor(apiKey: String?): Interceptor {
        return Interceptor { chain: Interceptor.Chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(newRequest)
        }
    }

    private fun createJsonWithEscapedString(input: String?): String {
        val rootNode = objectMapper.createObjectNode()
        rootNode.put("text", input)
        return objectMapper.writeValueAsString(rootNode["text"])
    }

    companion object {
        private const val BASE_URL = "https://api.openai.com/"
        private const val HTTP_CONNECTION_TIMEOUT = 120 // uploads to whisper of up to 25 mbyte might take a while
        const val PROMPT_SIZE_LIMIT = 4096
    }

    interface GptApi {
        @Headers("Content-Type: application/json")
        @POST("v1/completions")
        fun getCompletion(@Body requestBody: RequestBody?): Call<JsonNode?>?
    }
}
