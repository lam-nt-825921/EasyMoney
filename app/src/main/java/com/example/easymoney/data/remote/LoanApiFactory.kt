//package com.example.easymoney.data.remote
//
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object LoanApiFactory {
//    const val MOCK_BASE_URL = "https://mockapi.easymoney.dev/"
//
//    fun create(baseUrl: String = MOCK_BASE_URL): LoanApiService {
//        val logger = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(logger)
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl(baseUrl)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(LoanApiService::class.java)
//    }
//}
//
