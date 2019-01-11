package com.example.gislainy.webrtcubicare

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.gislainy.webrtcubicare.webrtc.VideoCallSession
import com.example.gislainy.webrtcubicare.webrtc.VideoCallStatus
import com.example.gislainy.webrtcubicare.webrtc.VideoRenderers
import org.webrtc.DataChannel
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import java.nio.ByteBuffer
import java.util.*

class VideoCallActivity : AppCompatActivity() {

    private var videoSession : VideoCallSession? = null
    private var statusTextView: TextView? = null
    private var remoteTextView: TextView? = null
    private var localVideoView: SurfaceViewRenderer? = null
    private var remoteVideoView: SurfaceViewRenderer? = null
    private var audioManager: AudioManager? = null
    private var savedMicrophoneState : Boolean? = null
    private var savedAudioMode: Int? = null
    private var channel: DataChannel? = null
    private var listaChannel: MutableList<DataChannel?> = mutableListOf();
    private var jaEnviouDados: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        statusTextView = findViewById(R.id.status_text)
        //remoteTextView = findViewById(R.id.remote_text)
        localVideoView = findViewById(R.id.pip_video)
        remoteVideoView = findViewById(R.id.remote_video)

        val hangup : Button = findViewById(R.id.hangup_button)
        hangup.setOnClickListener {
            finish()
        }

        audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        savedAudioMode = audioManager?.mode
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION

        savedMicrophoneState = audioManager?.isMicrophoneMute
        audioManager?.isMicrophoneMute = false

        handlePermissions()
    }

    override fun onDestroy() {
        super.onDestroy()

        videoSession?.terminate()
        localVideoView?.release()
        remoteVideoView?.release()

        if(savedAudioMode !== null) {
            audioManager?.mode = savedAudioMode!!
        }
        if(savedMicrophoneState != null) {
            audioManager?.isMicrophoneMute = savedMicrophoneState!!
        }
    }

    private fun onStatusChanged(newStatus: VideoCallStatus) {
        Log.d(TAG,"New call status: $newStatus")
        runOnUiThread {
            when(newStatus) {
                VideoCallStatus.FINISHED -> finish()
                else -> {
                    statusTextView?.text = resources.getString(newStatus.label)
                    statusTextView?.setTextColor(ContextCompat.getColor(this, newStatus.color))
                }
            }
        }
    }

    private fun handlePermissions() {
        val canAccessCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val canRecordAudio  = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if(!canAccessCamera || !canRecordAudio) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), CAMERA_AUDIO_PERMISSION_REQUEST)
        } else {
            startVideoSession()
        }
    }

    private fun startVideoSession() {
        videoSession = VideoCallSession.connect(this, BACKEND_URL, VideoRenderers(localVideoView, remoteVideoView), this::onMesasge, this::onSendCb, this::onStatusChanged)

        localVideoView?.init(videoSession?.renderContext, null)
        localVideoView?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        localVideoView?.setZOrderMediaOverlay(true)
        localVideoView?.setEnableHardwareScaler(true)

        remoteVideoView?.init(videoSession?.renderContext, null)
        remoteVideoView?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        remoteVideoView?.setEnableHardwareScaler(true)
    }
    private fun onSendCb(chan: DataChannel?) {
        if(chan != null) {
            listaChannel.add(chan)
            if(!jaEnviouDados)
                enviarDados()
        };
    }
    private fun onMesasge(string: String) {
        runOnUiThread {
            remoteTextView?.text = string
        }
    }
    private fun enviarDados() {
        runOnUiThread {
            val text = Date();
            Log.w(TAG, "DataChannel enviarDados")
            listaChannel.forEach{
                if (it?.state() == DataChannel.State.OPEN) {
                    val buffer = ByteBuffer.wrap(text.toString().toByteArray())
                    it.send(DataChannel.Buffer(buffer, false))
                    Log.w(TAG, "DataChannel enviarDados  Estou enviando == "+ text.toString())
                    remoteTextView?.text = "Estou enviando == " + text.toString()
                    /*Handler().postDelayed({
                         enviarDados()
                     }, 1000)*/
                    jaEnviouDados = true;
                } else {
                    Handler().postDelayed({
                        enviarDados()
                    }, 1000);
                }
            }


        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.w(TAG, "onRequestPermissionsResult: $requestCode $permissions $grantResults")
        when (requestCode) {
            CAMERA_AUDIO_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    startVideoSession()
                } else {
                    finish()
                }
                return
            }
        }
    }

    companion object {
        private val CAMERA_AUDIO_PERMISSION_REQUEST = 1
        private val TAG = "VideoCallActivity"
        private val BACKEND_URL = "ws://192.168.15.24:7000/" // Change HOST to your server's IP if you want to test
    }
}