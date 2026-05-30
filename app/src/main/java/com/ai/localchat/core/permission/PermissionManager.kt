package com.ai.localchat.core.permission

import android.app.Activity
import android.Manifest
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class PermissionManager(private val activity: Activity) {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var onPermissionResult: ((Boolean) -> Unit)? = null

    init {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val allGranted = result.all { it.value }
            onPermissionResult?.invoke(allGranted)
        }
    }

    // 请求基础权限：存储、媒体、网络
    fun requestBasePermission(callback: (Boolean) -> Unit) {
        onPermissionResult = callback
        val permissionList = mutableListOf<String>()

        permissionList.add(Manifest.permission.INTERNET)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissionList.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        permissionLauncher.launch(permissionList.toTypedArray())
    }
}

