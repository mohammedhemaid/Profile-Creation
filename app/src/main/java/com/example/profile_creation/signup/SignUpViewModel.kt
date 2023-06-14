package com.example.profile_creation.signup

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profile_creation.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val firstName: String = "",
    val email: String = "",
    val password: String = "",
    val imageUri: String? = null,
    val website: String? = null,
    val createdUserId: Long = -1L,
    @StringRes val errorMessage: Int? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun createNewUser() = viewModelScope.launch(Dispatchers.IO) {
        val userId = userRepository.createNote(
            firstName = uiState.value.firstName,
            email = uiState.value.email,
            password = uiState.value.password,
            imageUri = uiState.value.imageUri,
            website = uiState.value.website
        )
        _uiState.update {
            it.copy(createdUserId = userId)
        }
    }

    fun updateFirstName(firstName: String) {
        _uiState.update {
            it.copy(firstName = firstName)
        }
    }

    fun updateEmail(email: String) {
        _uiState.update {
            it.copy(email = email)
        }
    }

    fun updatePassword(password: String) {
        _uiState.update {
            it.copy(password = password)
        }
    }

    fun updateImage(imageUri: String?) {
        _uiState.update {
            it.copy(imageUri = imageUri)
        }
    }

    fun updateWebsite(website: String?) {
        _uiState.update {
            it.copy(website = website)
        }
    }
}