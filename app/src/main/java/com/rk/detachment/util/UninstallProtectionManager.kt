package com.rk.detachment.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.rk.detachment.receiver.DetachmentDeviceAdminReceiver

object UninstallProtectionManager {
    fun getAdminComponent(context: Context): ComponentName {
        return ComponentName(context, DetachmentDeviceAdminReceiver::class.java)
    }

    fun isDeviceAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        return dpm.isAdminActive(getAdminComponent(context))
    }

    fun createDeviceAdminIntent(context: Context): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getAdminComponent(context))
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Activate Device Administrator to prevent unauthorized uninstallation or tampering by children while parental control is active."
        )
        return intent
    }

    fun deactivateDeviceAdmin(context: Context) {
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            dpm?.removeActiveAdmin(getAdminComponent(context))
        } catch (e: Exception) {
        }
    }
}
