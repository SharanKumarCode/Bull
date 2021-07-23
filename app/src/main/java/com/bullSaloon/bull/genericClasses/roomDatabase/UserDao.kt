package com.bullSaloon.bull.genericClasses.roomDatabase

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntityDataClass")
    fun getUserData(): LiveData<UserEntityDataClass>

    @Insert
    fun insert(user: UserEntityDataClass)

    @Update
    fun update(user: UserEntityDataClass)

    @Delete
    fun delete(user: UserEntityDataClass)

}