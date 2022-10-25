package com.websarva.wings.android.lifecyclesample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("LifeCycleSample","Main onCrate() called.")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    public override fun onStart(){
        Log.i("LifeCycleSample","Main onStart() called.")
        super.onStart()
    }

    public override fun onResume() {
        Log.i("LifeCycleSample","Main onResume() called.")
        super.onResume()
    }

    public override fun onStop() {
        Log.i("LifeCycleSample","Main onStop() called.")
        super.onStop()
    }

    public override fun onDestroy() {
        Log.i("LifeCycleSample","Main onDestroy() called.")
        super.onDestroy()
    }

    // 「次の画面を表示」ボタンがタップされた時の処理
    fun onButtonClick(view: View){
        // インテントオブジェクトを用意
        val intent = Intent(this@MainActivity,SubActivity::class.java)
        // アクティビティを起動
        startActivity(intent)
    }
}