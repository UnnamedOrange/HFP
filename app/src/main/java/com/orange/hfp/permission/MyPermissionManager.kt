// Copyright (c) UnnamedOrange. Licensed under the MIT License.
// See the LICENSE file in the repository root for full license text.

package com.orange.hfp.permission

import android.Manifest
import android.os.Build.VERSION
import androidx.activity.ComponentActivity

/**
 * Provide my permissions for [PermissionManager].
 *
 * The usage is the same as that of [PermissionManager].
 */
class MyPermissionManager(activity: ComponentActivity) : IPermissionManager {
    companion object {
        private val permissions = if (VERSION.SDK_INT <= 30) {
            arrayOf(
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        }
    }

    private val permissionManager = PermissionManager(activity, activity, permissions)

    override val isGranted = permissionManager.isGranted

    override fun requestPermissions() = permissionManager.requestPermissions()
    override fun updateIsGranted() = permissionManager.updateIsGranted()
}
