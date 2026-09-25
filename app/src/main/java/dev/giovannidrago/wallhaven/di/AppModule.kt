package dev.giovannidrago.wallhaven.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.giovannidrago.wallhaven.data.db.WallhavenDao
import dev.giovannidrago.wallhaven.data.db.WallhavenDatabase
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.data.prefs.SecureApiKeyStore
import dev.giovannidrago.wallhaven.util.AppLogger
import javax.inject.Singleton

private val Context.wallhavenDataStore by preferencesDataStore(name = "wallhaven_filter_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
	@Provides
	@Singleton
	fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.wallhavenDataStore

	@Provides
	@Singleton
	fun provideDatabase(@ApplicationContext context: Context): WallhavenDatabase = Room.databaseBuilder(
		context,
		WallhavenDatabase::class.java,
		"wallhaven.db"
	)
		// The database is a browsable cache; favorites are the only user data and are restored by re-favoriting.
		.fallbackToDestructiveMigration(dropAllTables = true)
		.build()

	@Provides
	@Singleton
	fun provideDao(database: WallhavenDatabase): WallhavenDao = database.dao()

	@Provides
	@Singleton
	fun provideApiKeyStore(@ApplicationContext context: Context, logger: AppLogger): ApiKeyStore = SecureApiKeyStore(context, logger)
}
