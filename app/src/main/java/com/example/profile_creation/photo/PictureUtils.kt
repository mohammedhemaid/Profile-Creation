package com.example.profile_creation.photo

import android.os.Environment
import kotlin.Throws
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import com.example.profile_creation.photo.PictureUtils
import androidx.core.content.FileProvider
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.text.TextUtils
import androidx.exifinterface.media.ExifInterface
import com.example.profile_creation.common.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

internal object PictureUtils {
    private const val MAX_IMAGE_HEIGHT = 1024
    private const val MAX_IMAGE_WIDTH = 1024
    fun createImageFile(context: Context): File? {
        val imageFile: File = try {
            // Create an image file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val imageFileName = "Profile_APP_" + timeStamp + "_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )
        } catch (exception: IOException) {
            return null
        }
        return imageFile
    }

    @Throws(IOException::class)
    fun processImage(context: Context, takenPhotoUri: Uri, fromCamera: Boolean): Uri? {
        val contentResolver = context.contentResolver
        val input = contentResolver.openInputStream(takenPhotoUri) ?: return null
        val exifInterface = ExifInterface(input)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
        val rotatedBitmap: Bitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(
                context,
                takenPhotoUri,
                90
            )
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(
                context,
                takenPhotoUri,
                180
            )
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(
                context,
                takenPhotoUri,
                270
            )
            else -> {
                // If photo was taken with camera and no rotation needed, just return it
                if (fromCamera) {
                    return takenPhotoUri
                }
                // Otherwise, we need to copy the contents of the passed in URI into a file we control
                val photoFile = createImageFile(context) ?: return null
                val targetUri = FileProvider.getUriForFile(
                    context,
                    Constants.FILE_PROVIDER_AUTHORITY,
                    photoFile
                )
                copyFromUriIntoFile(contentResolver, takenPhotoUri, targetUri)
                return targetUri
            }
        }

        // If rotation was necessary, write rotated bitmap into a new file and return the URI for that file
        if (rotatedBitmap != null) {
            val photoFile = createImageFile(context) ?: return null
            val out = FileOutputStream(photoFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            return FileProvider.getUriForFile(context, Constants.FILE_PROVIDER_AUTHORITY, photoFile)
        }
        return null
    }

    private fun copyFromUriIntoFile(
        contentResolver: ContentResolver,
        sourceUri: Uri,
        targetUri: Uri
    ) {
        try {
            contentResolver.openInputStream(sourceUri).use { inputStream ->
                contentResolver.openOutputStream(targetUri).use { outputStream ->
                    if (inputStream == null || outputStream == null) {
                        return
                    }
                    val buf = ByteArray(1024)
                    if (inputStream.read(buf) <= 0) {
                        return
                    }
                    do {
                        outputStream.write(buf)
                    } while (inputStream.read(buf) != -1)
                }
            }
        } catch (ignored: IOException) {
        }
    }

    private fun rotateImage(context: Context, takenPhotoUri: Uri, degree: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = getBitmapFromFileProviderUri(context, takenPhotoUri)
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(
            bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
        bitmap.recycle()
        return rotatedImg
    }

    private fun getBitmapFromFileProviderUri(context: Context, takenPhotoUri: Uri): Bitmap? {
        val contentResolver = context.contentResolver
        return try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            var imageStream = contentResolver.openInputStream(takenPhotoUri)
            BitmapFactory.decodeStream(imageStream, null, options)
            imageStream!!.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options)

            // Decode bitmap with inSampleSize set, capping it at 1024x1024 and decreasing chances of OOM
            options.inJustDecodeBounds = false
            imageStream = contentResolver.openInputStream(takenPhotoUri)
            BitmapFactory.decodeStream(imageStream, null, options)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate an inSampleSize for use in a [BitmapFactory.Options] object when decoding
     * bitmaps using the decode* methods from [BitmapFactory]. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     * method with inJustDecodeBounds==true
     * @return The value to be used for inSampleSize
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > MAX_IMAGE_HEIGHT || width > MAX_IMAGE_WIDTH) {
            // Calculate ratios of height and width to requested height and width
            val heightRatio = Math.round(height.toFloat() / MAX_IMAGE_HEIGHT.toFloat())
            val widthRatio = Math.round(width.toFloat() / MAX_IMAGE_WIDTH.toFloat())

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = Math.min(heightRatio, widthRatio)

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).
            val totalPixels = (width * height).toFloat()

            // Anything more than 2x the requested pixels we'll sample down further
            val totalReqPixelsCap = (MAX_IMAGE_WIDTH * MAX_IMAGE_HEIGHT * 2).toFloat()
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }
        }
        return inSampleSize
    }

    fun deleteFileWithUri(uri: String?) {
        if (TextUtils.isEmpty(uri)) {
            return
        }
        val filePath = uri!!.substring(uri.lastIndexOf('/'))
        val completePath = (Environment.getExternalStorageDirectory().path
                + Constants.FILE_PROVIDER_PATH
                + filePath)
        val imageFile = File(completePath)
        if (imageFile.exists()) {
            imageFile.delete()
        }
    }
}