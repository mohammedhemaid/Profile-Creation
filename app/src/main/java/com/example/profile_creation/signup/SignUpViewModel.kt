package com.example.profile_creation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profile_creation.R
import com.example.profile_creation.extension.isValidEmail
import com.example.profile_creation.extension.isValidPassword
import com.example.profile_creation.extension.isValidUrl
import com.example.profile_creation.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _showError = MutableSharedFlow<Int>()
    val showError: SharedFlow<Int> = _showError

    private val _navigateToConfirmation = MutableSharedFlow<Long>()
    val navigateToConfirmation: SharedFlow<Long> get() = _navigateToConfirmation

    var firstName = ""
    var email = ""
    var password = ""
    var imageUri = ""
    var website = ""

    fun submitUser() {
        val errorMessage = isInputValid()
        if (errorMessage != -1) {
            viewModelScope.launch {
                _showError.emit(errorMessage)
            }
            return
        }

        createNewUser()
    }

    private fun createNewUser() = viewModelScope.launch(Dispatchers.IO) {
        val userId = userRepository.createUser(
            firstName = firstName,
            email = email,
            password = password,
            imageUri = imageUri,
            website = website
        )
        if (userId != -1L) {
            _navigateToConfirmation.emit(userId)
        }
    }

    private fun isInputValid(): Int {
        if (firstName.isEmpty()) {
            return R.string.first_name_validation_message
        } else if (!email.isValidEmail()) {
            return R.string.email_validation_message
        } else if (!password.isValidPassword()) {
            return R.string.password_validation_message
        } else if (website.isNotEmpty() && !website.isValidUrl()) {
            return R.string.website_validation_message
        }
        return -1
    }
}
