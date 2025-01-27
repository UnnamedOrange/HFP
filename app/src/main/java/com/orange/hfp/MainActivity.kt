// Copyright (c) UnnamedOrange. Licensed under the MIT License.
// See the LICENSE file in the repository root for full license text.

package com.orange.hfp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import com.orange.hfp.permission.MyPermissionManager
import com.orange.hfp.ui.MainScreen
import com.orange.hfp.ui.theme.HFPTheme

import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val permissionManager by lazy { MyPermissionManager(this) }
    private var hfpEnabler = MutableStateFlow<HfpEnabler?>(null);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager.updateIsGranted()
        if (!permissionManager.isGranted.value) {
            permissionManager.requestPermissions()
        }

        enableEdgeToEdge()
        setContent {
            HFPTheme {
                val enabler by hfpEnabler.collectAsState()
                MainScreen(
                    isHfpEnabled = enabler != null,
                    enableHfp = { enableHfp() },
                    disableHfp = { disableHfp() },
                )
            }
        }
    }

    override fun onDestroy() {
        disableHfp()

        super.onDestroy()
    }

    private fun enableHfp(): Boolean {
        permissionManager.updateIsGranted()
        if (permissionManager.isGranted.value) {
            hfpEnabler.value = HfpEnabler(this as Context)
            return true
        } else {
            return false
        }
    }

    private fun disableHfp() {
        hfpEnabler.value?.close()
        hfpEnabler.value = null
    }
}
