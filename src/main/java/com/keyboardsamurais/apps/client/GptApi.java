package com.keyboardsamurais.apps.client;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GptApi {
    @Headers("Content-Type: application/json")
    @POST("v1/completions")
    Call<JsonNode> getCompletion(@Body RequestBody requestBody);
}
