package dev.msfjarvis.powertiles

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.ControlAction
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.ReplayProcessor
import org.reactivestreams.FlowAdapters
import java.util.concurrent.Flow
import java.util.function.Consumer

private const val CONTROL_REQUEST_CODE = 100
private const val CONTROL_ID_SETTINGS = "settings_control"

class ControlsService : ControlsProviderService() {
    private val controls = hashMapOf<String, Control>()
    private lateinit var updatePublisher: ReplayProcessor<Control>
    private val settingsPendingIntent by lazy {
        val context: Context = baseContext
        val i = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        PendingIntent.getActivity(
            context,
            CONTROL_REQUEST_CODE,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onCreate() {
        super.onCreate()
        makeDefaultControls()
    }

    private fun makeDefaultControls() {
        controls[CONTROL_ID_SETTINGS] = makeSettingsControl()
    }

    private fun makeSettingsControl(): Control {
        return Control.StatefulBuilder(CONTROL_ID_SETTINGS, settingsPendingIntent)
            .setTitle(getString(R.string.settings_button_title))
            .setSubtitle(getString(R.string.settings_button_summary))
            .setStructure(getString(R.string.structure_home))
            .setCustomIcon(Icon.createWithResource(this, R.drawable.ic_outline_settings_24))
            .setDeviceType(DeviceTypes.TYPE_SWITCH)
            .setStatus(Control.STATUS_OK)
            .build()
    }

    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> {
        makeDefaultControls()
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls.map { it.value }))
    }

    override fun performControlAction(
        controlId: String,
        action: ControlAction,
        consumer: Consumer<Int>
    ) {
        when (controlId) {
            CONTROL_ID_SETTINGS -> {
                controls[CONTROL_ID_SETTINGS] = makeSettingsControl()
                updatePublisher.onNext(controls[CONTROL_ID_SETTINGS])
            }
        }
    }

    override fun createPublisherFor(controlIds: MutableList<String>): Flow.Publisher<Control> {
        makeDefaultControls()
        updatePublisher = ReplayProcessor.create()
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls.map { it.value }))
    }
}
