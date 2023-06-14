package com.example.profile_creation.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: Long): User?

    @Insert
    fun insertUser(user: User): Long
}