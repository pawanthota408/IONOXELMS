package com.Ionoxetechlms.data.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val TAG = "ApiClient"

    /**
     * BASE_URL must end with a trailing slash.
     *
     * IMPORTANT: Match this to where your PHP API files actually live.
     * Your API endpoint is course_lesson.php, so if that file is at:
     *   https://ionox.in/lms/course_lesson.php       → keep as "/lms/"
     *   https://ionox.in/lms/api/course_lesson.php   → change to "/lms/api/"
     *
     * Verify by opening the URL in a browser first.
     */
    private const val BASE_URL = "https://ionox.in/lms/"

    /**
     * Logging interceptor with Log.d output — critical for debugging.
     * Default HttpLoggingInterceptor() prints to stdout, which is often
     * invisible in adb logcat. This redirects to Log.d with a tag.
     */
    private val logging = HttpLoggingInterceptor { message ->
        Log.d("$TAG-HTTP", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Headers added to every request — mimics a mobile browser so the
     * server doesn't reject the API call or serve HTML fallbacks.
     */
    private val headerInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            )
            .header("Accept", "application/json, text/plain, */*")
            .header("X-Requested-With", "XMLHttpRequest")
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: LmsApiService = retrofit.create(LmsApiService::class.java)

    init {
        // Fail fast if BASE_URL is misconfigured
        check(BASE_URL.endsWith("/")) {
            "BASE_URL must end with a trailing slash. Current: $BASE_URL"
        }
        Log.d(TAG, "ApiClient initialized with BASE_URL=$BASE_URL")
    }
}