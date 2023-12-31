package com.example.profile_creation.signup

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.profile_creation.R
import com.example.profile_creation.common.PermissionUtils
import com.example.profile_creation.databinding.FragmentSignUpBinding
import com.example.profile_creation.photo.PhotoImportManager
import com.example.profile_creation.photo.PhotoImportOptionsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up),
    PhotoImportManager.Listener,
    PhotoImportOptionsDialog.Delegate {

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2
    }

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by viewModels()

    private lateinit var photoImportManager: PhotoImportManager
    private lateinit var photoOptionsDialog: PhotoImportOptionsDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignUpBinding.bind(view)

        photoImportManager = PhotoImportManager(this)
        photoOptionsDialog = PhotoImportOptionsDialog(requireContext(), this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateToConfirmation.collect { userId ->
                navigateToDetailsScreen(userId)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showError.collect { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        if (viewModel.imageUri.isNotEmpty()) {
            binding.imageEmptyState.isVisible = false
            binding.image.setImageURI(Uri.parse(viewModel.imageUri))
        }

        onPhotoClickListener()
        onFirstNameChanged()
        onEmailChanged()
        onPasswordChanged()
        onWebsiteChanged()
        onSubmitClickListener()
    }

    private fun onPhotoClickListener() {
        binding.imageCard.setOnClickListener {
            photoOptionsDialog.showPhotoOptions()
        }
    }

    private fun onSubmitClickListener() {
        binding.submit.setOnClickListener {
            viewModel.submitUser()
        }
    }

    private fun navigateToDetailsScreen(userId: Long) {
        val action =
            SignUpFragmentDirections.actionSignUpFragmentToSignUpConfirmationFragment(userId)
        findNavController().navigate(action)
    }

    private fun onFirstNameChanged() {
        binding.firstName.doOnTextChanged { text, _, _, _ ->
            viewModel.firstName = text.toString().trim()
        }
    }

    private fun onEmailChanged() {
        binding.email.doOnTextChanged { text, _, _, _ ->
            viewModel.email = text.toString().trim()
        }
    }

    private fun onPasswordChanged() {
        binding.password.doOnTextChanged { text, _, _, _ ->
            viewModel.password = text.toString().trim()
        }
    }

    private fun onWebsiteChanged() {
        binding.website.doOnTextChanged { text, _, _, _ ->
            viewModel.website = text.toString().trim()
        }
    }

    override fun addWithGallery() {
        if (PermissionUtils.isPermissionGranted(getReadStoragePermission(), requireContext())) {
            openFilePicker()
        } else {
            requestStoragePermissionLauncher.launch(getReadStoragePermission())
        }
    }

    private fun getReadStoragePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    override fun addWithCamera() {
        if (PermissionUtils.isPermissionGranted(Manifest.permission.CAMERA, requireContext())) {
            startCameraPage()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCameraPage() {
        val takePhotoIntent: Intent? = photoImportManager.getPhotoTakingIntent(requireContext())
        if (takePhotoIntent == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.take_photo_with_camera_failed),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            startActivityForResult(
                takePhotoIntent,
                CAMERA_REQUEST_CODE
            )
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(
            intent, GALLERY_REQUEST_CODE
        )
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startCameraPage()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.camera_permission_denied,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                addWithGallery()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.gallery_permission_denied,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> if (resultCode == AppCompatActivity.RESULT_OK) {
                Toast.makeText(requireContext(), R.string.processing_your_photo, Toast.LENGTH_SHORT)
                    .show()
                photoImportManager.processTakenPhoto(requireContext())
            } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                photoImportManager.deleteLastTakenPhoto()
            }
            GALLERY_REQUEST_CODE -> if (resultCode == AppCompatActivity.RESULT_OK) {
                Toast.makeText(requireContext(), R.string.processing_your_photo, Toast.LENGTH_SHORT)
                    .show()
                photoImportManager.processSelectedPhoto(requireContext(), data)
            }
        }
    }

    override fun onAddPhotoFailure() {
        Toast.makeText(requireContext(), R.string.photo_processing_failure, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onAddPhotoSuccess(takenPhotoUri: Uri) {
        requireActivity().runOnUiThread {
            binding.image.setImageURI(takenPhotoUri)
            binding.imageEmptyState.isVisible = false
            viewModel.imageUri = takenPhotoUri.toString()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}