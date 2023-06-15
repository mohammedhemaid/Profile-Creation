package com.example.profile_creation.photo

import android.os.HandlerThread
import android.content.Intent
import android.provider.MediaStore
import com.example.profile_creation.photo.PictureUtils
import androidx.core.content.FileProvider
import android.content.pm.ResolveInfo
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import com.example.profile_creation.common.Constants
import java.io.IOException

/**
 * Utility class to take photos via a camera intent or pick a photo from storage.
 * Also responsible for the necessary post-processing (undoing rotation).
 */
class PhotoImportManager(private val listener: Listener) {
    interface Listener {
        fun onAddPhotoFailure()
        fun onAddPhotoSuccess(takenPhotoUri: Uri)
    }

    private val backgroundHandler: Handler
    private var currentPhotoUri: Uri? = null

    init {
        val handlerThread = HandlerThread("Photo Processor")
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
    }

    fun getPhotoTakingIntent(context: Context): Intent? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val currentPhotoFile = PictureUtils.createImageFile(context)
        if (currentPhotoFile != null) {
            currentPhotoUri = FileProvider.getUriForFile(
                context,
                Constants.FILE_PROVIDER_AUTHORITY,
                currentPhotoFile
            )

            // Grant access to content URI so camera app doesn't crash
            val resolvedIntentActivities = context.packageManager
                .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolvedIntentInfo in resolvedIntentActivities) {
                val packageName = resolvedIntentInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName, currentPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            return takePictureIntent
        }
        return null
    }

    fun processTakenPhoto(context: Context) {
        currentPhotoUri?.let { photoUri ->
            backgroundHandler.post {
                try {
                    currentPhotoUri = PictureUtils.processImage(context, photoUri, true)
                    if (currentPhotoUri == null) {
                        listener.onAddPhotoFailure()
                    } else {
                        listener.onAddPhotoSuccess(currentPhotoUri!!)
                    }
                } catch (exception: IOException) {
                    listener.onAddPhotoFailure()
                }
            }
        }
    }

    @SuppressLint("WrongConstant")
    fun processSelectedPhoto(context: Context, data: Intent?) {
        if (data != null && data.data != null) {
            backgroundHandler.post {
                // Persist ability to read from this file
                val takeFlags = (data.flags
                        and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                context.contentResolver.takePersistableUriPermission(data.data!!, takeFlags)
                try {
                    currentPhotoUri = PictureUtils.processImage(context, data.data!!, false)
                    if (currentPhotoUri == null) {
                        listener.onAddPhotoFailure()
                    } else {
                        listener.onAddPhotoSuccess(currentPhotoUri!!)
                    }
                } catch (exception: IOException) {
                    listener.onAddPhotoFailure()
                }
            }
        } else {
            listener.onAddPhotoFailure()
        }
    }

    fun deleteLastTakenPhoto() {
        if (currentPhotoUri != null) {
            PictureUtils.deleteFileWithUri(currentPhotoUri.toString())
        }
    }
}