package com.socialgateway.socialgateway

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.socialgateway.socialgateway.MainActivity.Companion.userId
import socialgateway.socialgateway.R

class AnswerDialog(
    private val activity: Activity,
    private val socialApp: SocialApp?,
    private val prompt: Prompt,
    private val onSubmit: () -> Unit,
    private val onCancel: () -> Unit
) {

    private val typingLayout: View = activity.layoutInflater.inflate(R.layout.typing_dialog, null)
    private val recordingLayout: View =
        activity.layoutInflater.inflate(R.layout.recording_dialog, null)
    private var recorder: VoiceRecorder? = null
    private lateinit var dialog: AlertDialog
    private var dialogType = "default"

    init {
        typingLayout.findViewById<ImageButton>(R.id.record_button).setOnClickListener {
            recorder = VoiceRecorder(activity, recordingLayout)

            if (recorder!!.hasPermissions()) {

                dialog.dismiss()
                if (recordingLayout.parent != null)
                    (recordingLayout.parent as ViewGroup).removeView(recordingLayout)

                dialogType = "recording"
                showDialog()
            }
        }
        recordingLayout.findViewById<ImageButton>(R.id.text_button).setOnClickListener {
            dialog.dismiss()
            recorder?.stop()
            recorder = null
            if (recordingLayout.parent != null)
                (typingLayout.parent as ViewGroup).removeView(typingLayout)

            dialogType = "typing"
            showDialog()
        }

        if (prompt.answerable)
            dialogType = "typing"

        showDialog()
    }

    private fun showDialog() {
        dialog = AlertDialog.Builder(activity).apply {
            setMessage(prompt.content)
            when (dialogType) {
                "recording" -> setView(recordingLayout)
                "typing" -> setView(typingLayout)
                "default" -> {}
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                onCancel()
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                when (dialogType) {
                    "recording" -> ServerInterface(activity).sendAudioAnswer(
                        // TODO KATIE how should check-in work
                        socialApp?.name ?: "check-in",
                        userId,
                        prompt.content,
                        recorder!!.recordingFile()
                    )
                    "typing" -> ServerInterface(activity).sendTextAnswer(
                        // TODO KATIE how should check-in work
                        socialApp?.name ?: "check-in",
                        userId,
                        prompt.content,
                        typingLayout.findViewById<TextView>(R.id.answer_edit_text).text.toString()
                    )
                    "default" -> {}
                }
                onSubmit()
            }
        }.create().apply {
            show()
        }
    }

}