package de.lichessbyvoice

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment

class NullModelDialogFragment(private val parentActivity: AppCompatActivity) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(parentActivity)
            builder.setTitle(R.string.null_model_alert)
                .setMessage(R.string.null_model_alert_text)
                .setPositiveButton(R.string.mic_permission_button)
                { dialog, _ ->
                    dialog.dismiss()
                    parentActivity.finish()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {
        dialog.dismiss()
        parentActivity.finish()
    }
}
