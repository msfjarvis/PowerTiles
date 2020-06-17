package dev.msfjarvis.powertiles

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.ControlAction
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.ReplayProcessor
import org.reactivestreams.FlowAdapters
import java.util.concurrent.Flow
import java.util.function.Consumer

class ControlsService : ControlsProviderService() {
    private lateinit var updatePublisher: ReplayProcessor<Control>
    private val settingsPendingIntent by lazy {
        val context: Context = baseContext
        val i = Intent(Settings.ACTION_SETTINGS)
        PendingIntent.getActivity(
            context,
            CONTROL_REQUEST_CODE,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> {
        val controls = mutableListOf<Control>()
        val control = Control.StatefulBuilder(CONTROL_BUTTON_ID, settingsPendingIntent)
            .setTitle(getString(R.string.settings_button_title))
            .setSubtitle(getString(R.string.settings_button_summary))
            .setStructure(getString(R.string.structure_home))
            .setCustomIcon(Icon.createWithResource(this, R.drawable.ic_outline_settings_24))
            .setDeviceType(DeviceTypes.TYPE_SWITCH)
            .setStatus(Control.STATUS_OK)
            .build()
        controls.add(control)
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls))
    }

    override fun performControlAction(
        controlId: String,
        action: ControlAction,
        consumer: Consumer<Int>
    ) {
        if (action is BooleanAction) {
            consumer.accept(ControlAction.RESPONSE_OK)
            val control = Control.StatefulBuilder(CONTROL_BUTTON_ID, settingsPendingIntent)
                .setTitle(getString(R.string.settings_button_title))
                .setSubtitle(getString(R.string.settings_button_summary))
                .setStructure(getString(R.string.structure_home))
                .setCustomIcon(Icon.createWithResource(this, R.drawable.ic_outline_settings_24))
                .setDeviceType(DeviceTypes.TYPE_SWITCH)
                .setStatus(Control.STATUS_OK)
                .build()
            updatePublisher.onNext(control)
        }
    }

    override fun createPublisherFor(controlIds: MutableList<String>): Flow.Publisher<Control> {
        updatePublisher = ReplayProcessor.create()
        val controls = mutableListOf<Control>()
        if (controlIds.contains(CONTROL_BUTTON_ID)) {
            val control = Control.StatefulBuilder(CONTROL_BUTTON_ID, settingsPendingIntent)
                .setTitle(getString(R.string.settings_button_title))
                .setSubtitle(getString(R.string.settings_button_summary))
                .setStructure(getString(R.string.structure_home))
                .setCustomIcon(Icon.createWithResource(this, R.drawable.ic_outline_settings_24))
                .setDeviceType(DeviceTypes.TYPE_SWITCH)
                .setStatus(Control.STATUS_OK)
                .build()
            updatePublisher.onNext(control)
            controls.add(control)
        }
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls))
    }

    companion object {
        private const val CONTROL_REQUEST_CODE = 100
        private const val CONTROL_BUTTON_ID = "button_id"
    }
}
