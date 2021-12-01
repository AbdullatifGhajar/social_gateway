package com.example.socialgateway

import android.Manifest.permission.RECORD_AUDIO
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageButton
import java.io.File

private const val PERMISSIONS_RECORD_AUDIO_SOCIAL_GATEWAY = 1

class VoiceRecorder(a: Activity, v: View) {
    private var activity = a
    private var view = v
    private var state = "initial"

    private fun setState(newState: String) {
        state = newState
        applyState()
    }

    private var startRecordingIB: ImageButton = view.findViewById(R.id.start_recording_button)
    private var stopRecordingIB: ImageButton = view.findViewById(R.id.stop_recording_button)
    private var deleteRecordingIB: ImageButton = view.findViewById(R.id.delete_recording_button)
    private var startPlayingIB: ImageButton = view.findViewById(R.id.start_playing_button)
    private var stopPlayingIB: ImageButton = view.findViewById(R.id.stop_playing_button)

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null

    private var mFile: File? = null

    fun recordingFile(): File {
        if (mFile == null)
            mFile = File.createTempFile("record", ".3gp")
        return mFile!!
    }


    init {
        if(!hasPermissions()){
            requestPermissions()
        }

        startRecordingIB.setOnClickListener {
            startRecording()
        }
        stopRecordingIB.setOnClickListener {
            stopRecording()
        }
        deleteRecordingIB.setOnClickListener {
            deletePlaying()
        }
        startPlayingIB.setOnClickListener {
            startPlaying()
        }
        stopPlayingIB.setOnClickListener {
            stopPlaying()
        }

        setState("initial")
    }

    private fun startRecording() {
        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(recordingFile().absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }
        setState("recording")
    }

    private fun stopRecording() {
        mRecorder?.run {
            stop()
            release()
        }
        mRecorder = null
        setState("recorded")
    }

    private fun deletePlaying() {
        stopRecording()
        stopPlaying()
        mFile?.delete()
        mFile = null
        setState("initial")
    }

    private fun startPlaying() {
        mPlayer = MediaPlayer().apply {
            setDataSource(recordingFile().absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                stopPlaying()
            }
        }
        setState("playing")
    }

    private fun stopPlaying() {
        mPlayer?.release()
        mPlayer = null
        setState("recorded")
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(RECORD_AUDIO),
            PERMISSIONS_RECORD_AUDIO_SOCIAL_GATEWAY
        )
    }

    fun stop() {
        stopRecording()
        stopPlaying()
    }

    private fun applyState() {
        val recorderState = voiceRecorderStates[state] ?: throw Exception()
        val buttonStates = mapOf(
            startRecordingIB to recorderState.startRecording,
            stopRecordingIB to recorderState.stopRecording,
            deleteRecordingIB to recorderState.deleteRecording,
            startPlayingIB to recorderState.startPlaying,
            stopPlayingIB to recorderState.stopPlaying
        )

        for ((button, active) in buttonStates) {
            if (!active) {
                button.alpha = .5f
                button.isClickable = false
            } else {
                button.alpha = 1f
                button.isClickable = true
            }
        }
    }
}

data class RecorderState(
    val startRecording: Boolean,
    val stopRecording: Boolean,
    val deleteRecording: Boolean,
    val startPlaying: Boolean,
    val stopPlaying: Boolean
)

val voiceRecorderStates = mapOf(
    "initial" to RecorderState(
        startRecording = true,
        stopRecording = false,
        deleteRecording = false,
        startPlaying = false,
        stopPlaying = false
    ),
    "recording" to RecorderState(
        startRecording = false,
        stopRecording = true,
        deleteRecording = false,
        startPlaying = false,
        stopPlaying = false
    ),
    "recorded" to RecorderState(
        startRecording = false,
        stopRecording = false,
        deleteRecording = true,
        startPlaying = true,
        stopPlaying = false
    ),
    "playing" to RecorderState(
        startRecording = false,
        stopRecording = false,
        deleteRecording = true,
        startPlaying = false,
        stopPlaying = true
    ),
    "hideAll" to RecorderState(
        startRecording = false,
        stopRecording = false,
        deleteRecording = false,
        startPlaying = false,
        stopPlaying = false
    )
)