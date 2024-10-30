package com.example.jikimi.data.network.di

import com.example.jikimi.data.network.INDOOR_EVACUATION_API_BASE
import com.example.jikimi.data.network.service.IndoorEvacuationService
import com.example.jikimi.data.network.NATURALDISASTER_API_BASE
import com.example.jikimi.data.network.OUTDOOR_EVACUATION_API_BASE
import com.example.jikimi.data.network.SOCIAL_DISASTER_API_BASE
import com.example.jikimi.data.network.service.NaturalDisasterService
import com.example.jikimi.data.network.service.OutdoorEvacuationService
import com.example.jikimi.data.network.service.SocialDisasterService
import com.google.gson.GsonBuilder
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
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


    @Singleton
    @Provides
    @Named("NaturalDisaster")
    fun NaturalRetrofit(okHttpClient: OkHttpClient) : Retrofit{
        return Retrofit.Builder()
            .addConverterFactory(
                TikXmlConverterFactory
                    .create(TikXml.Builder().exceptionOnUnreadXml(false).build())
            )
            .client(okHttpClient)
            .baseUrl(NATURALDISASTER_API_BASE)
            .build()
    }

    @Singleton
    @Provides
    @Named("NaturalDisasterService")
    fun provideNaturalDisaster(
        @Named("NaturalDisaster") retrofit: Retrofit
    ) : NaturalDisasterService{
        return retrofit.create(NaturalDisasterService::class.java)
    }



    @Singleton
    @Provides
    @Named("SocialDisaster")
    fun SocialRetrofit(okHttpClient: OkHttpClient) : Retrofit{
        return Retrofit.Builder()
            .addConverterFactory(
                TikXmlConverterFactory
                    .create(TikXml.Builder().exceptionOnUnreadXml(false).build())
            )
            .client(okHttpClient)
            .baseUrl(SOCIAL_DISASTER_API_BASE)
            .build()
    }


    @Singleton
    @Provides
    @Named("SocialDisasterService")
    fun provideSocialDisaster(
        @Named("SocialDisaster") retrofit: Retrofit
    ) : SocialDisasterService{
        return retrofit.create(SocialDisasterService::class.java)
    }
}











