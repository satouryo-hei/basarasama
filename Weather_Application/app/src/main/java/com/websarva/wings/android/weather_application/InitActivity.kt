package com.websarva.wings.android.weather_application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class InitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)
    }

    fun onButtonClick(view: View){
        //
        val intent = Intent(this@InitActivity,changeActivity::class.java)
        //
        startActivity(intent)
    }
}