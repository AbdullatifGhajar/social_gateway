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

    private var mFile = File.createTempFile("record", ".acc")

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
        // log("before recording: " + mFile.length()/1024)
        if (hasPermissions()) {
            // TODO use scopes
            mRecorder = MediaRecorder()
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            mRecorder!!.setOutputFile(mFile.absolutePath)
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mRecorder!!.prepare()
            mRecorder!!.start()
        } else {
            requestPermissions()
        }
    }

    private fun stopRecording() {
        // TODO use scopes
        mRecorder?.stop()
        mRecorder?.release()
        mRecorder = null
        // log("after recording: " + mFile.length()/1024)
    }

    private fun deletePlaying() {
        mFile.delete()
        mFile = File.createTempFile("record", ".acc")
    }

    private fun startPlaying() {
        // TODO use scopes
        mPlayer = MediaPlayer()
        mPlayer!!.setDataSource(mFile.absolutePath);
        mPlayer!!.prepare();
        mPlayer!!.start()
    }

    private fun stopPlaying() {
        mPlayer?.release()
        mPlayer = null
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        // why 82? use other methods to request permissions
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(RECORD_AUDIO),
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
}

data class VoiceRecorderState(
    val startRecording: Boolean,
    val stopRecording: Boolean,
    val deleteRecording: Boolean,
    val startPlaying: Boolean,
    val stopPlaying: Boolean
)

val voiceRecorderStates = mapOf(
    "initial" to VoiceRecorderState(
        startRecording = true,
        stopRecording = false,
        deleteRecording = false,
        startPlaying = false,
        stopPlaying = false
    ),
    "recording" to VoiceRecorderState(
        startRecording = false,
        stopRecording = true,
        deleteRecording = false,
        startPlaying = false,
        stopPlaying = false
    ),
    "recorded" to VoiceRecorderState(
        startRecording = false,
        stopRecording = false,
        deleteRecording = true,
        startPlaying = true,
        stopPlaying = false
    ),
    "playing" to VoiceRecorderState(
        startRecording = false,
        stopRecording = false,
        deleteRecording = true,
        startPlaying = false,
        stopPlaying = true
    ),
    "hideAll" to VoiceRecorderState(
        startRecording = false,
        stopRecording = false,
        deleteRecording = false,
        startPlaying = false,
        stopPlaying = false
    )
)