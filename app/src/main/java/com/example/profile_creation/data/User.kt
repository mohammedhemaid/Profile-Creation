package com.example.profile_creation.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "user"
)
data class User(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var firstName: String,
    var email: String,
    var password: String,
    var imageUri: String?,
    var website: String?,
    var createdUpdatedTime: Long = System.currentTimeMillis()
)