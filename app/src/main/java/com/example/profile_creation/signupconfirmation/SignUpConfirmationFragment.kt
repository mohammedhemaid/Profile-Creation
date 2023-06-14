package com.example.profile_creation.signupconfirmation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.profile_creation.R
import com.example.profile_creation.databinding.FragmentSignUpBinding
import com.example.profile_creation.databinding.FragmentSignUpConfirmationBinding


class SignUpConfirmationFragment : Fragment(R.layout.fragment_sign_up_confirmation) {

    private var _binding: FragmentSignUpConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignUpConfirmationBinding.bind(view)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}