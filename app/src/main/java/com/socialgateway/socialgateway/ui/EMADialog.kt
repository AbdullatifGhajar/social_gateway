package com.socialgateway.socialgateway.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.widget.*
import com.socialgateway.socialgateway.ServerInterface
import com.socialgateway.socialgateway.SocialGatewayApp
import com.socialgateway.socialgateway.ui.AppGridActivity.Companion.userId
import socialgateway.socialgateway.R

/**
 * the dialog that pops up to answer the EMA of the day
 */
class EMADialog(
    private val activity: Activity,
    private val onSubmit: () -> Unit = {},
    private val onCancel: () -> Unit = {}
) {
    private val layouts = listOf(
        activity.layoutInflater.inflate(R.layout.ema_dialog1, null),
        activity.layoutInflater.inflate(R.layout.ema_dialog2, null),
        activity.layoutInflater.inflate(R.layout.ema_dialog3, null),
    )
    private lateinit var dialog: AlertDialog

    init {
        showDialog(0)
    }

    private fun showDialog(page: Int) {
        dialog = AlertDialog.Builder(activity, R.style.AlertDialogStyle).apply {
            setMessage("EMA of the day")
            setView(layouts[page])
            setNegativeButton(android.R.string.cancel) { _, _ ->
                onCancel()
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                if (page + 1 == layouts.size) {
                    onSubmit()
                    answers().forEach { (question, answer) ->
                        ServerInterface.sendTextAnswer("ema", userId, question, answer)
                    }
                } else {
                    showDialog(page + 1)
                }
            }
        }.create().apply {
            show()
        }
    }

    private fun answers(): Map<String, String> {
        val question1 = SocialGatewayApp.resources.getString(R.string.ema_question1)
        val question2 = SocialGatewayApp.resources.getString(R.string.ema_question2)
        val question3 = SocialGatewayApp.resources.getString(R.string.ema_question3)
        val question4 = SocialGatewayApp.resources.getString(R.string.ema_question4)
        val question5 = SocialGatewayApp.resources.getString(R.string.ema_question5)

        // collect checked options of question 3 (the one with checkboxes)
        val answerView3 = layouts[1].findViewById<LinearLayout>(R.id.ema_answer3)
        val checkedOptions = mutableListOf<String>()
        for (i in 0 until answerView3.childCount) {
            val checkbox = answerView3.getChildAt(i)
            if ((checkbox as CheckBox).isChecked) {
                checkedOptions.add(checkbox.text.toString())
            }
        }

        val answer1 = layouts[0].findViewById<TextView>(R.id.ema_answer1).text.toString()
        val answer2 = layouts[0].findViewById<TextView>(R.id.ema_answer2).text.toString()
        val answer3 = checkedOptions.joinToString(separator = " & ")
        val answer4 = layouts[1].findViewById<TextView>(R.id.ema_answer4).text.toString()
        val answer5 = layouts[2].findViewById<RatingBar>(R.id.ema_answer5).progress.toString()

        return mapOf(
            question1 to answer1,
            question2 to answer2,
            question3 to answer3,
            question4 to answer4,
            question5 to answer5,
        )
    }
}