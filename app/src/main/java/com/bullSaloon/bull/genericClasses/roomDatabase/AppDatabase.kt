package com.bullSaloon.bull.genericClasses.roomDatabase

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserEntityDataClass::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}