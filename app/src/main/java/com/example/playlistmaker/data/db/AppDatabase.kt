package com.example.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playlistmaker.data.db.dao.TrackDao
import com.example.playlistmaker.data.db.entity.TrackEntity

@Database(entities = [TrackEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}