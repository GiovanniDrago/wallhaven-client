package dev.giovannidrago.wallhaven.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.giovannidrago.wallhaven.BuildConfig
import dev.giovannidrago.wallhaven.data.api.WallhavenApi
import dev.giovannidrago.wallhaven.util.AppLogger
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

private const val BASE_URL = "https://wallhaven.cc/api/v1/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
	@Provides
	@Singleton
	fun provideJson(): Json = Json {
		ignoreUnknownKeys = true
		explicitNulls = false
		coerceInputValues = true
	}

	@Provides
	@Singleton
	fun provideOkHttpClient(logger: AppLogger): OkHttpClient = OkHttpClient.Builder()
		.connectTimeout(30, TimeUnit.SECONDS)
		.readTimeout(60, TimeUnit.SECONDS)
		.writeTimeout(30, TimeUnit.SECONDS)
		.apply {
			if (BuildConfig.DEBUG) {
				val loggingInterceptor = HttpLoggingInterceptor { message -> logger.debug(LOG_TAG, message) }
					.apply {
						level = HttpLoggingInterceptor.Level.BASIC
						// The key travels in a header; redact it in case a future level logs headers too.
						redactHeader("X-API-Key")
					}
				addInterceptor(loggingInterceptor)
			}
		}
		.build()

	@Provides
	@Singleton
	fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
		.baseUrl(BASE_URL)
		.client(okHttpClient)
		.addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
		.build()

	@Provides
	@Singleton
	fun provideWallhavenApi(retrofit: Retrofit): WallhavenApi = retrofit.create(WallhavenApi::class.java)
}

private const val LOG_TAG = "OkHttp"
