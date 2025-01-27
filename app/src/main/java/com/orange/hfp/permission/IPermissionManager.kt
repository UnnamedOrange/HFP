// Copyright (c) UnnamedOrange. Licensed under the MIT License.
// See the LICENSE file in the repository root for full license text.

package com.orange.hfp.permission

import kotlinx.coroutines.flow.StateFlow

interface IPermissionManager {
    val isGranted: StateFlow<Boolean>

    fun requestPermissions()
    fun updateIsGranted()
}
