package com.websarva.wings.android.weather_application

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.os.HandlerCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.Executors

class TopActivity : AppCompatActivity(){

    // クラス内のprivate定数を宣言するために
    companion object {
        // ログに記載するタグ用の文字列
        private const val DEBUG_TAG = "Debug:API通信Top"
    }

    // リストビューに表示させるリストデータ。(都市リストデータ)
    private var _Citylist: MutableList<MutableMap<String, String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)

        // 都市リストの生成
        _Citylist = createCityList()
        Log.i(DEBUG_TAG, "Citylist:${_Citylist}")
        val lvCityList = findViewById<ListView>(R.id.lvSelectWeather)
        val from = arrayOf("name")
        val to = intArrayOf(android.R.id.text1)
        val lCityAdapter = SimpleAdapter(
            this@TopActivity,
            _Citylist,
            android.R.layout.simple_list_item_1,
            from,
            to
        )
        lvCityList.adapter = lCityAdapter
        lvCityList.onItemClickListener = ListCityClickListener()
    }

    // リストビューに表示させる天気ポイントリストデータを生成するメソッド
    private fun createCityList(): MutableList<MutableMap<String, String>> {
        var list: MutableList<MutableMap<String, String>> = mutableListOf()

        val lSharedPref =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        lSharedPref?.let {
            // "SharedPref"で情報を読み出す
            val lCityName = lSharedPref.getString("CityName", "NoData")
            val lQ = lSharedPref.getString("q", "NoData")

            Log.i(DEBUG_TAG, "${lCityName}")
            Log.i(DEBUG_TAG, "${lQ}")

            var city = mutableMapOf("name" to "${lCityName}", "q" to "${lQ}")
            list.add(city)
        }
        return list
    }


    // リストがタップされたときの処理が記述されたリスナクラス
    private inner class ListCityClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            // インテントオブジェクトを用意(どこに遷移するか)
            val lIntent = Intent(this@TopActivity, MainActivity::class.java)
            // アクティビティを起動(遷移開始)
            startActivity(lIntent)
        }
    }

    // 前の画面を表示するためのボタンを押したときの処理
    fun onButtonClick(view: View){
        finish()
    }
}