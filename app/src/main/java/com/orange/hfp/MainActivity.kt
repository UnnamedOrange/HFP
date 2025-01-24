package com.orange.hfp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

import com.orange.hfp.permission.MyPermissionManager
import com.orange.hfp.ui.theme.HFPTheme

class MainActivity : ComponentActivity() {
    private val permissionManager by lazy { MyPermissionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager.updateIsGranted()
        if (!permissionManager.isGranted.value) {
            permissionManager.requestPermissions()
        }

        enableEdgeToEdge()
        setContent {
            HFPTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Text("Hello, Android")
                    }
                }
            }
        }
    }
}
