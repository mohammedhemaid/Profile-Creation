package com.example.profile_creation.repository

import com.example.profile_creation.data.User

interface UserRepository {

    suspend fun getUser(id: Long): User?

    suspend fun createUser(firstName: String, email: String, password:String, imageUri: String?, website:String?): Long

}