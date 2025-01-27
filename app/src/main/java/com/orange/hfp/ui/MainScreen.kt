// Copyright (c) UnnamedOrange. Licensed under the MIT License.
// See the LICENSE file in the repository root for full license text.

package com.orange.hfp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import com.orange.hfp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isHfpEnabled: Boolean,
    enableHfp: () -> Boolean,
    disableHfp: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ElevatedButton(onClick = {
                if (isHfpEnabled) {
                    disableHfp()
                } else {
                    enableHfp()
                }
            }) {
                val text = if (isHfpEnabled) {
                    "Disable HFP"
                } else {
                    "Enable HFP"
                }
                Text(text)
            }
        }
    }
}
