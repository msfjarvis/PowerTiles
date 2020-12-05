package dev.msfjarvis.powertiles

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.TileService

class SettingsPanelTile : TileService() {
    override fun onClick() {
        super.onClick()
        startActivityAndCollapse(Intent(Settings.Panel.ACTION_WIFI).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
