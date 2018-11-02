package com.example.gislainy.webrtcubicare

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Principal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        val connectButton: Button = findViewById(R.id.connect_button)
        connectButton.setOnClickListener {
            startVideoCall()
        }

    }
    private fun startVideoCall() {
        startActivity(Intent(this, VideoCallActivity::class.java))
    }
}
