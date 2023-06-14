package com.example.profile_creation.repository

import com.example.profile_creation.data.User
import com.example.profile_creation.data.UserDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dataSource: UserDao,
) : UserRepository {

    override suspend fun getUser(id: Long): User? {
        return dataSource.getUserById(id)
    }

    override suspend fun createNote(
        firstName: String,
        email: String,
        password: String,
        imageUri: String?,
        website: String?
    ): Long {
        val user = User(
            firstName = firstName,
            email = email,
            password = password,
            imageUri = imageUri,
            website = website,
            createdUpdatedTime = System.currentTimeMillis()
        )
        return dataSource.insertUser(user)
    }
}