package com.example.easymoney.di

import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.remote.ChatApiService
import com.example.easymoney.data.remote.EventApiService
import com.example.easymoney.data.remote.HomeApiService
import com.example.easymoney.data.remote.LoanApiService
import com.example.easymoney.data.remote.PaymentApiService
import com.example.easymoney.data.remote.RewardApiService
import com.example.easymoney.data.remote.TransactionHistoryApiService
import com.example.easymoney.data.remote.UserApiService
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, appPreferences: AppPreferences): Retrofit {
        // Base URL có thể đổi từ Sandbox, nên dùng giá trị từ preferences
        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        return Retrofit.Builder()
            .baseUrl(appPreferences.apiBaseUrl.ensureEndsWithSlash())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideLoanApiService(retrofit: Retrofit): LoanApiService =
        retrofit.create(LoanApiService::class.java)

    // Workflow #44 — reference service cho User profile/account security
    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    // Workflow #45–#48 — REMOTE services cho các repository còn lại
    @Provides
    @Singleton
    fun provideHomeApiService(retrofit: Retrofit): HomeApiService =
        retrofit.create(HomeApiService::class.java)

    @Provides
    @Singleton
    fun provideEventApiService(retrofit: Retrofit): EventApiService =
        retrofit.create(EventApiService::class.java)

    @Provides
    @Singleton
    fun provideRewardApiService(retrofit: Retrofit): RewardApiService =
        retrofit.create(RewardApiService::class.java)

    @Provides
    @Singleton
    fun provideTransactionHistoryApiService(retrofit: Retrofit): TransactionHistoryApiService =
        retrofit.create(TransactionHistoryApiService::class.java)

    @Provides
    @Singleton
    fun providePaymentApiService(retrofit: Retrofit): PaymentApiService =
        retrofit.create(PaymentApiService::class.java)

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService =
        retrofit.create(ChatApiService::class.java)

    private fun String.ensureEndsWithSlash(): String {
        return if (this.endsWith("/")) this else "$this/"
    }
}
