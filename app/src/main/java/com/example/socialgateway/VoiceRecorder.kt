package com.example.socialgateway

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageButton
import java.io.File


class VoiceRecorder(a: Activity, v: View) {
    private var activity = a
    private var view = v
    private var state = "initial"
        set(value) {
            field = value
            applyState()
        }

    private var startRecordingIB: ImageButton = view.findViewById(R.id.start_recording_button)
    private var stopRecordingIB: ImageButton = view.findViewById(R.id.stop_recording_button)
    private var deleteRecordingIB: ImageButton = view.findViewById(R.id.delete_recording_button)
    private var startPlayingIB: ImageButton = view.findViewById(R.id.start_playing_button)
    private var stopPlayingIB: ImageButton = view.findViewById(R.id.stop_playing_button)

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private var mFileName: String? = null

    init {
        startRecordingIB.setOnClickListener {
            state = "recording"
            startRecording()
        }
        stopRecordingIB.setOnClickListener {
            state = "recorded"
            stopRecording()
        }
        deleteRecordingIB.setOnClickListener {
            state = "initial"
            deletePlaying()
        }
        startPlayingIB.setOnClickListener {
            state = "playing"
            startPlaying()
        }
        stopPlayingIB.setOnClickListener {
            state = "recorded"
            stopPlaying()
        }

        state = "initial"
    }

    private fun startRecording() {
        if (hasPermissions()) {
            mRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setOutputFile(getAnswerAudioFile().absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }
        } else {
            requestPermissions()
        }
    }

    private fun stopRecording() {
        mRecorder?.stop()
        mRecorder?.release()
        mRecorder = null
    }

    private fun deletePlaying() {
        getAnswerAudioFile().delete()
    }

    private fun startPlaying() {
        mPlayer = MediaPlayer.create(activity, Uri.fromFile(getAnswerAudioFile()))
        mPlayer?.start()
    }

    private fun stopPlaying() {
        mPlayer?.release()
        mPlayer = null
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        // why 82? use other methods to request permissions
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            82
        )
    }

    private fun applyState() {
        val voiceRecorderState = voiceRecorderStates[state] ?: throw Exception()
        startRecordingIB.visibility =
            if (voiceRecorderState.startRecording) ImageButton.VISIBLE else ImageButton.GONE
        stopRecordingIB.visibility =
            if (voiceRecorderState.stopRecording) ImageButton.VISIBLE else ImageButton.GONE
        deleteRecordingIB.visibility =
            if (voiceRecorderState.deleteRecording) ImageButton.VISIBLE else ImageButton.GONE
        startPlayingIB.visibility =
            if (voiceRecorderState.startPlaying) ImageButton.VISIBLE else ImageButton.GONE
        stopPlayingIB.visibility =
            if (voiceRecorderState.stopPlaying) ImageButton.VISIBLE else ImageButton.GONE
    }

    fun getAnswerAudioFile(): File {
        return activity.cacheDir.resolve("social_gateway_answer_audio.aac")
    }
}

data class VoiceRecorderState(
    val startRecording: Boolean,
    val stopRecording: Boolean,
    val deleteRecording: Boolean,
    val startPlaying: Boolean,
    val stopPlaying: Boolean
)

val voiceRecorderStates = mapOf(
    "initial" to VoiceRecorderState(true, false, false, false, false),
    "recording" to VoiceRecorderState(false, true, false, false, false),
    "recorded" to VoiceRecorderState(false, false, true, true, false),
    "playing" to VoiceRecorderState(false, false, true, false, true),
    "hideAll" to VoiceRecorderState(false, false, false, false, false)
)