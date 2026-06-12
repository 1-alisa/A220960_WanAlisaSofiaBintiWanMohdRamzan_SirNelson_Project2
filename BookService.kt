package com.example.a220960_sirnelson_lab01

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- MODELS (DTO) ---
data class AdviceResponse(
    @SerializedName("slip") val slip: AdviceSlip?
)

data class AdviceSlip(
    @SerializedName("id") val id: Int,
    @SerializedName("advice") val advice: String
)

// --- API SERVICE ---
interface BookApiService {
    @GET("advice")
    suspend fun getRandomAdvice(): Response<AdviceResponse>
}

// --- RETROFIT CLIENT ---
object RetrofitClient {
    private const val BASE_URL = "https://api.adviceslip.com/"

    val apiService: BookApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookApiService::class.java)
    }
}