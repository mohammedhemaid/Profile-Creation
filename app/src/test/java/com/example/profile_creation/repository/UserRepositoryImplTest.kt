package com.example.profile_creation.repository

import com.example.profile_creation.data.FakeUserDao
import com.example.profile_creation.data.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private const val NEW_USER_FIRST_NAME = "firstname"
private const val NEW_USER_EMAIL = "mohammed@out.com"
private const val NEW_USER_PASSWORD = "123456"
private const val NEW_USER_IMAGE = "image"
private const val NEW_USER_WEBSITE = "www.website.com"

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private val newUser = User(
        firstName = NEW_USER_FIRST_NAME,
        email = NEW_USER_EMAIL,
        password = NEW_USER_PASSWORD,
        imageUri = NEW_USER_IMAGE,
        website = NEW_USER_WEBSITE
    )

    private lateinit var SUT: UserRepositoryImpl
    private lateinit var dataSource: FakeUserDao

    private var testDispatcher = UnconfinedTestDispatcher()
    private var testScope = TestScope(testDispatcher)

    @Before
    fun createRepository() {
        dataSource = FakeUserDao()
        SUT = UserRepositoryImpl(dataSource)
    }

    @Test
    fun createUser_savesToLocal_true(): Unit = testScope.runTest {
        val newUserId = SUT.createUser(
            newUser.firstName,
            newUser.email,
            newUser.password,
            newUser.imageUri,
            newUser.website
        )

        MatcherAssert.assertThat(dataSource.users?.map { it.id }?.contains(newUserId), `is`(true))
    }

    @Test
    fun createUser_isSameUser_true(): Unit = testScope.runTest {
        val newUserId = SUT.createUser(
            newUser.firstName,
            newUser.email,
            newUser.password,
            newUser.imageUri,
            newUser.website
        )

        val user = dataSource.users?.find { it.id == newUserId }!!

        MatcherAssert.assertThat(user.firstName, `is`(newUser.firstName))
        MatcherAssert.assertThat(user.email, `is`(newUser.email))
        MatcherAssert.assertThat(user.password, `is`(newUser.password))
        MatcherAssert.assertThat(user.imageUri, `is`(newUser.imageUri))
        MatcherAssert.assertThat(user.website, `is`(newUser.website))
    }

    @Test
    fun createUser_isInputsValid_true(): Unit = testScope.runTest {
        val newUserId = SUT.createUser(
            newUser.firstName,
            newUser.email,
            newUser.password,
            newUser.imageUri,
            newUser.website
        )

        val user = dataSource.users?.find { it.id == newUserId }!!

        MatcherAssert.assertThat(user.firstName, not(""))
        MatcherAssert.assertThat(user.email, not(""))
        MatcherAssert.assertThat(user.password, not(""))
        MatcherAssert.assertThat(user.password.length > 5, `is`(true))
    }
}
