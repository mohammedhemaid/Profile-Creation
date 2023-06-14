package com.example.profile_creation.photo

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.profile_creation.R
import android.content.DialogInterface

class PhotoImportOptionsDialog(private var context: Context, private var delegate: Delegate) {
    interface Delegate {
        fun addWithGallery()
        fun addWithCamera()
    }

    fun showPhotoOptions() {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.photo_options_dialog_title)
            .setItems(R.array.photo_import_options_without_upload) { dialog: DialogInterface?, position: Int ->
                when (position) {
                    0 -> delegate.addWithGallery()
                    1 -> delegate.addWithCamera()
                }
            }
            .setPositiveButton(R.string.cancel, null)
            .show()
    }
}