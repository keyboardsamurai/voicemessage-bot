package com.keyboardsamurais.apps.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.keyboardsamurais.apps.config.EnvUtils
import com.keyboardsamurais.apps.exceptions.MessageProcessingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import okhttp3.*
import org.apache.commons.lang3.StringUtils
import retrofit2.*
import retrofit2.Call
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.io.*
import java.nio.file.Files
import java.util.concurrent.TimeUnit


private val log = KotlinLogging.logger {}
class OpenAIClient {
    private val objectMapper: ObjectMapper = ObjectMapper()


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
        val requestBodyJson = String.format(
            """
                        {"prompt": %s,"max_tokens": 1024,"stop": ["###"], "model":"text-davinci-003"}
                        """.trimIndent(), createJsonWithEscapedString(prompt)
        )
        log.debug(requestBodyJson)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson)
        return withContext(Dispatchers.IO) {
            val response = gptApi.getCompletion(requestBody)?.execute()
            if (!response!!.isSuccessful) {
                throw HttpException(response)
            }
            val jsonNode = response.body()
            log.debug("jsonNode: {}", jsonNode!!.toPrettyString())
            StringUtils.trim(jsonNode["choices"][0]["text"].asText())
        }
    }


    /**
     * Transcribe voice file to text through the OpenAI Whisper API.
     *
     * @param voiceFile audio file in 'm4a', 'mp3', 'webm', 'mp4', 'mpga', 'wav', 'mpeg' formats
     * @return the text transcription of the voice file
     */
    suspend fun whisperTranscribeAudio(voiceFile: File): String {
        if (voiceFile.length() > 25 * 1024 * 1024) {
            throw MessageProcessingException(
                "File size is too large. Max size is 25MB, but is %d bytes".formatted(
                    voiceFile.length()
                )
            )
        }
        val client = OkHttpClient().newBuilder()
            .readTimeout(HTTP_CONNECTION_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .connectTimeout(HTTP_CONNECTION_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()
        val mimeType: String = try {
            Files.probeContentType(voiceFile.toPath())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
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
                throw IOException("Unexpected code $response")
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
        return try {
            objectMapper.writeValueAsString(rootNode["text"])
        } catch (e: JsonProcessingException) {
            throw MessageProcessingException(e)
        }
    }

    companion object {
        private const val BASE_URL = "https://api.openai.com/"
        private const val HTTP_CONNECTION_TIMEOUT = 120 // uploads to whisper of up to 25 mbyte  might take a while
        const val PROMPT_SIZE_LIMIT = 4096
    }
}

interface GptApi {
    @Headers("Content-Type: application/json")
    @POST("v1/completions")
    fun getCompletion(@Body requestBody: RequestBody?): Call<JsonNode?>?
}
