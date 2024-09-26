package com.example.jikimi.data.network.di

import com.example.jikimi.data.network.INDOOR_EVACUATION_API_BASE
import com.example.jikimi.data.network.IndoorEvacuationService
import com.example.jikimi.data.network.OUTDOOR_EVACUATION_API_BASE
import com.example.jikimi.data.network.OutdoorEvacuationService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }


    @Singleton
    @Provides
    @Named("OutdoorEvacuation")
    fun evacuationRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(OUTDOOR_EVACUATION_API_BASE)
            .build()
    }


    @Singleton
    @Provides
    @Named("OutdoorEvacuationService")
    fun provideOutdoorEvacuation(
        @Named("OutdoorEvacuation") retrofit: Retrofit
    ): OutdoorEvacuationService {
        return retrofit.create(OutdoorEvacuationService::class.java)
    }




    @Singleton
    @Provides
    @Named("IndoorEvacuation")
    fun indoorEvacuationRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(INDOOR_EVACUATION_API_BASE)
            .build()
    }

    @Singleton
    @Provides
    @Named("IndoorEvacuationService")
    fun provideIndoorEvacuation(
        @Named("IndoorEvacuation") retrofit: Retrofit
    ): IndoorEvacuationService {
        return retrofit.create(IndoorEvacuationService::class.java)
    }


}











