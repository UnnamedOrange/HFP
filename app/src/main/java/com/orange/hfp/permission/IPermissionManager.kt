package com.orange.hfp.permission

import kotlinx.coroutines.flow.StateFlow

interface IPermissionManager {
    val isGranted: StateFlow<Boolean>

    fun requestPermissions()
    fun updateIsGranted()
}
