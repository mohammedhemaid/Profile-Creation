package com.example.profile_creation.signupconfirmation

import androidx.lifecycle.SavedStateHandle
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

data class SignUpConfirmationUiState(
    val firstName: String = "",
    val email: String = "",
    val imageUri: String? = null,
    val website: String? = null,
)

@HiltViewModel
class SignUpConfirmationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpConfirmationUiState())
    val uiState: StateFlow<SignUpConfirmationUiState> = _uiState.asStateFlow()

    companion object {
        const val USER_ID_ARG = "userId"
    }

    private val userId: Long? = savedStateHandle[USER_ID_ARG]!!

    init {
        if (userId != null) {
            loadUser(userId)
        }
    }

    private fun loadUser(userId: Long) = viewModelScope.launch(Dispatchers.IO) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUser(userId)?.let { user ->
                _uiState.update {
                    it.copy(
                        firstName = user.firstName,
                        email = user.email,
                        imageUri = user.imageUri,
                        website = user.website,
                    )
                }
            }
        }
    }
}