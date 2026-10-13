package dev.giovannidrago.wallhaven.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
	entities = [
		WallpaperEntity::class,
		GalleryEntryEntity::class,
		RemoteKeyEntity::class,
		AlbumEntity::class,
		AlbumEntryEntity::class,
		AlbumImageEntity::class
	],
	version = 2,
	exportSchema = false
)
abstract class WallhavenDatabase : RoomDatabase() {
	abstract fun dao(): WallhavenDao

	abstract fun albumDao(): AlbumDao
}

/**
 * Adds the album tables and the wallpaper resolution flag. Non-destructive on purpose: existing
 * favorites and browsed wallpapers MUST survive the upgrade (constitution Principle II).
 */
val DB_MIGRATION_1_2 = object : Migration(1, 2) {
	override fun migrate(db: SupportSQLiteDatabase) {
		db.execSQL(
			"ALTER TABLE wallpapers ADD COLUMN metadataResolved INTEGER NOT NULL DEFAULT 1"
		)
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS `albums` (" +
				"`id` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
				"`updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
		)
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS `album_entries` (" +
				"`albumId` TEXT NOT NULL, `wallpaperId` TEXT NOT NULL, `position` INTEGER NOT NULL, " +
				"`addedAt` INTEGER NOT NULL, PRIMARY KEY(`albumId`, `wallpaperId`), " +
				"FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
				"FOREIGN KEY(`wallpaperId`) REFERENCES `wallpapers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
		)
		db.execSQL(
			"CREATE INDEX IF NOT EXISTS `index_album_entries_wallpaperId` ON `album_entries`(`wallpaperId`)"
		)
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS `album_images` (" +
				"`wallpaperId` TEXT NOT NULL, `localPath` TEXT, `status` TEXT NOT NULL, " +
				"`bytes` INTEGER, `cachedAt` INTEGER, `lastError` TEXT, PRIMARY KEY(`wallpaperId`), " +
				"FOREIGN KEY(`wallpaperId`) REFERENCES `wallpapers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
		)
	}
}
