package dev.giovannidrago.wallhaven.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
	entities = [
		WallpaperEntity::class,
		GalleryEntryEntity::class,
		RemoteKeyEntity::class
	],
	version = 1,
	exportSchema = true
)
abstract class WallhavenDatabase : RoomDatabase() {
	abstract fun dao(): WallhavenDao
}
