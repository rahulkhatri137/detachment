package com.rk.detachment.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class DetachmentDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Detachment is actively enforcing parental controls. Deactivating device administrator will compromise child protection and notify the parent."
    }
}
