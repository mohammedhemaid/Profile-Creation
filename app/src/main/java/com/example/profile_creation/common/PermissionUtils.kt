package com.example.profile_creation.common

import android.content.Context
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

object PermissionUtils {
    fun isPermissionGranted(permission: String?, context: Context?): Boolean {
        return (ContextCompat.checkSelfPermission(context!!, permission!!)
                == PackageManager.PERMISSION_GRANTED)
    }
}