package dev.msfjarvis.powertiles

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.ControlAction
import android.widget.Toast
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.ReplayProcessor
import org.reactivestreams.FlowAdapters
import java.util.concurrent.Flow
import java.util.function.Consumer

class ControlsService : ControlsProviderService() {
    private lateinit var updatePublisher: ReplayProcessor<Control>

    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> {
        val controls = mutableListOf<Control>()
        val i = Intent()
        val pi = PendingIntent.getActivity(
            this,
            CONTROL_REQUEST_CODE,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val control = Control.StatelessBuilder(CONTROL_BUTTON_ID, pi)
            .setTitle(getString(R.string.settings_button_title))
            .setSubtitle(getString(R.string.settings_button_summary))
            .setCustomIcon(Icon.createWithResource(this, R.drawable.ic_outline_settings_24))
            .setDeviceType(DeviceTypes.TYPE_SWITCH)
            .build()
        controls.add(control)
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls))
    }

    override fun performControlAction(
        controlId: String,
        action: ControlAction,
        consumer: Consumer<Int>
    ) {
        val context = baseContext
        if (action is BooleanAction) {
            consumer.accept(ControlAction.RESPONSE_OK)
            Toast.makeText(this, "newState=${action.newState}", Toast.LENGTH_LONG).show()
            val i = Intent(this, DummyActivity::class.java)
            val pi =
                PendingIntent.getActivity(
                    context,
                    CONTROL_REQUEST_CODE,
                    i,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            val control = Control.StatefulBuilder(CONTROL_BUTTON_ID, pi)
                .setTitle(getString(R.string.settings_button_title))
                .setSubtitle(getString(R.string.settings_button_summary))
                .setCustomIcon(Icon.createWithResource(this, R.drawable.ic_outline_settings_24))
                .setDeviceType(DeviceTypes.TYPE_SWITCH)
                .setStatus(Control.STATUS_OK)
                .build()
            updatePublisher.onNext(control)
        }
    }

    override fun createPublisherFor(controlIds: MutableList<String>): Flow.Publisher<Control> {
        val context = baseContext
        val i = Intent(this, DummyActivity::class.java)
        val pi =
            PendingIntent.getActivity(
                context,
                CONTROL_REQUEST_CODE,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val controls = mutableListOf<Control>()
        updatePublisher = ReplayProcessor.create()
        if (controlIds.contains(CONTROL_BUTTON_ID)) {
            val control = Control.StatefulBuilder(CONTROL_BUTTON_ID, pi)
                .setTitle(getString(R.string.settings_button_title))
                .setSubtitle(getString(R.string.settings_button_summary))
                .setCustomIcon(Icon.createWithResource(this, R.drawable.ic_outline_settings_24))
                .setDeviceType(DeviceTypes.TYPE_SWITCH)
                .setStatus(Control.STATUS_OK)
                .build()
            controls.add(control)
            updatePublisher.onNext(control)
        }
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls))
    }

    companion object {
        private const val CONTROL_REQUEST_CODE = 100
        private const val CONTROL_BUTTON_ID = "button_id"
    }
}
