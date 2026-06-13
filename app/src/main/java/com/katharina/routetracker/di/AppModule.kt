package com.katharina.routetracker.di

import android.content.Context
import androidx.room.Room
import com.katharina.routetracker.data.FusedLocationSource
import com.katharina.routetracker.data.LocationSource
import com.katharina.routetracker.data.SessionStore
import com.katharina.routetracker.data.room.AppDatabase
import com.katharina.routetracker.data.room.RoomSessionStore
import com.katharina.routetracker.data.room.SessionDao
import com.katharina.routetracker.data.room.TrackPointConverter
import com.katharina.routetracker.repository.TrackingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "route_tracker.db",
        ).fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()

    @Provides
    @Singleton
    fun provideSessionStore(dao: SessionDao): SessionStore =
        RoomSessionStore(dao, TrackPointConverter())

    @Provides
    @Singleton
    fun provideLocationSource(@ApplicationContext context: Context): LocationSource =
        FusedLocationSource(context)

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideTrackingRepository(
        store: SessionStore,
        locationSource: LocationSource,
        scope: CoroutineScope,
        @ApplicationContext context: Context
    ): TrackingRepository = TrackingRepository(store, locationSource, scope, context)
}
