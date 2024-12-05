package com.chat4osu.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = application
        val folder = File(context.getExternalFilesDir(null), "")
        if (!folder.exists())
            Log.d("MainActivity", "mkdir returns ${folder.mkdir()}")

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}