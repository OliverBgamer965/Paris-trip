package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeather?,
    @Json(name = "daily") val daily: DailyWeather?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "temperature") val temperature: Double,
    @Json(name = "windspeed") val windspeed: Double?,
    @Json(name = "weathercode") val weatherCode: Int
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    @Json(name = "time") val time: List<String>?,
    @Json(name = "temperature_2m_max") val tempMax: List<Double>?,
    @Json(name = "temperature_2m_min") val tempMin: List<Double>?,
    @Json(name = "weathercode") val weatherCode: List<Int>?
)

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getParisWeather(
        @Query("latitude") latitude: Double = 48.8566,
        @Query("longitude") longitude: Double = 2.3522,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weathercode",
        @Query("timezone") timezone: String = "Europe/Paris"
    ): WeatherResponse
}

object WeatherClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: WeatherApi = retrofit.create(WeatherApi::class.java)
}
