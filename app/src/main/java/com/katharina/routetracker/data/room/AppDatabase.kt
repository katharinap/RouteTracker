package com.katharina.routetracker.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SessionEntity::class], version = 1, exportSchema = false)
@TypeConverters(TrackPointConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
