package com.example.gislainy.webrtcubicare

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Principal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        val videoButton: Button = findViewById(R.id.btnVideo)
        videoButton.setOnClickListener {
            startVideoCall()
        }
        val dadosButton: Button = findViewById(R.id.btnDados)
        dadosButton.setOnClickListener {
            startDadosCall()
        }

    }
    private fun startVideoCall() {
        startActivity(Intent(this, VideoCallActivity::class.java))
    }
    private fun startDadosCall() {
        startActivity(Intent(this, DadosActivity::class.java))
    }
}
