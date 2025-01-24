package com.orange.hfp.permission

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manage permissions for an [ActivityResultCaller] and [Context].
 *
 * In `OnCreate`, call [updateIsGranted] to fetch the state of the permissions.
 * Then use [isGranted] to determine whether all the [permissions] are granted.
 * Call [requestPermissions] to request all the [permissions].
 */
class PermissionManager(
    private val context: Context,
    caller: ActivityResultCaller,
    private val permissions: Array<String>,
) : IPermissionManager {

    companion object {
        private const val TAG = "PermissionManager"
    }

    private val requestMultiplePermissions = caller.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allGranted = true
        permissions.entries.forEach {
            Log.d(TAG, "${it.key} = ${it.value}")
            if (!it.value) {
                allGranted = false
            }
        }
        _isGranted.value = allGranted
    }

    private val _isGranted = MutableStateFlow(false)
    override val isGranted = _isGranted.asStateFlow()

    override fun requestPermissions() {
        updateIsGranted()
        if (_isGranted.value) {
            return
        }
        requestMultiplePermissions.launch(permissions)
    }

    override fun updateIsGranted() {
        if (checkIsGranted()) {
            _isGranted.value = true
        }
    }

    private fun checkIsGranted(): Boolean {
        for (permission in permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
