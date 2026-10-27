package com.materialdesign.nestrivo_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.materialdesign.nestrivo_app.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Nestrivoapp)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)

    }
}