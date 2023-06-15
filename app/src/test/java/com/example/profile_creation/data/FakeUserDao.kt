package com.example.profile_creation.data

class FakeUserDao : UserDao {

    private var _users: MutableMap<Long, User>? = mutableMapOf()
    var users: List<User>?
        get() = _users?.values?.toList()
        set(newUser) {
            _users = newUser?.associateBy { it.id!! }?.toMutableMap()
        }

    override fun getUserById(userId: Long) = _users?.get(userId)

    override fun insertUser(user: User): Long {
        val userId = (0..10).random().toLong()
        user.id = userId
        _users?.put(userId, user)
        return userId
    }
}