package com.example.profile_creation.signupconfirmation

import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.profile_creation.R
import com.example.profile_creation.databinding.FragmentSignUpConfirmationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpConfirmationFragment : Fragment(R.layout.fragment_sign_up_confirmation) {

    private var _binding: FragmentSignUpConfirmationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpConfirmationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignUpConfirmationBinding.bind(view)

        lifecycleScope.launch {
            viewModel.uiState.collect {
                updateContent(it)
            }
        }
    }

    private fun updateContent(uiState: SignUpConfirmationUiState) {
        binding.apply {
            confirmationTitle.text = getString(R.string.confirmation_title, uiState.firstName)
            firstName.text = uiState.firstName
            email.text = uiState.email
            website.text = uiState.website
            Linkify.addLinks(website, Linkify.WEB_URLS)
            uiState.imageUri?.let {
                image.setImageURI(Uri.parse(it))
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}