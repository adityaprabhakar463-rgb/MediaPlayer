package com.celestial.player.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        FavouriteEntity::class,
        RecentEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class CelestialDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        @Volatile
        private var instance: CelestialDatabase? = null

        fun get(context: Context): CelestialDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CelestialDatabase::class.java,
                    "celestial-library.db",
                ).build().also { instance = it }
            }
    }
}
