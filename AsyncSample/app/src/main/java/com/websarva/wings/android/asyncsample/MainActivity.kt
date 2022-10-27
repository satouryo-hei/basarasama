package com.websarva.wings.android.asyncsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.os.Handler
import android.util.Log
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
import java.util.concurrent.Executors
import java.net.URL
import java.util.stream.Stream

class MainActivity : AppCompatActivity() {

    // クラス内のprivate定数を宣言するために
    companion object{
        // ログに記載するタグ用の文字列
        private const val DEBAG_TAG ="AsyncSample"
        // お天気情報のURL
        private const val WEATHERINFO_URL = "https://api.openweathermap.org/data/2.5/weather?lang=ja"
        // お天気APIにアクセスすするためにAPIキー。
        private const val APP_ID = "76eafa6c7ef6c4b02799cf2857ad6d89"
    }

    // リストビューに表示させるリストデータ。
    private var _list:MutableList<MutableMap<String,String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _list = createList()

        val lvCityList = findViewById<ListView>(R.id.lvCityList)
        val from = arrayOf("name")
        val to = intArrayOf(android.R.id.text1)
        val adapter = SimpleAdapter(this@MainActivity,_list,android.R.layout.simple_list_item_1,from,to)
        lvCityList.adapter = adapter
        lvCityList.onItemClickListener = ListItemClickListener()
    }

    // リストビューに表示させる天気ポイントリストデータを生成するメソッド
    private fun createList(): MutableList<MutableMap<String,String>>{
        var list: MutableList<MutableMap<String,String>> = mutableListOf()

        var city = mutableMapOf("name" to "大阪", "q" to "Osaka")
        list.add(city)
        city =  mutableMapOf("name" to "神戸", "q" to "kode")
        list.add(city)
        city =  mutableMapOf("name" to "札幌", "q" to "Sapporo")
        list.add(city)

        return list
    }

    @UiThread
    // お天気情報の取得処理を行うメソッド
    private fun receiveWeatherInfo(nrlFull: String){
        // ここに非同期で天気情報を取得する処理を記述する。
        var handler = HandlerCompat.createAsync(mainLooper)
        val backgroundReceiver = WeathetInfoBackgroundReceiver(handler,nrlFull)
        val executeService = Executors.newSingleThreadExecutor()
        executeService.submit(backgroundReceiver)
    }

    // 非同期でお天気情報APIにアクセスするためのクラス
    private inner class WeathetInfoBackgroundReceiver(handler: Handler,url:String):Runnable{
        // ハンドラオブジェクト
        private val _handler = handler
        // お天気情報を取得するURL
        private val _url = url

        @WorkerThread
        override fun run() {
            // ここにweb APIにアクセスするコードを記述
            // 天気情報サービスから取得したJSON文字列。天気情報が格納されている。
            var result = ""
            // URLオブジェクトを生成
            val url = URL(_url)
            // URLオブジェクトからHttpURLConnectionオブジェクトを取得。
            val con = url.openConnection() as? HttpURLConnection
            // conがnullじゃないならば・・・
            con?.let{
                try {
                    // 接続に使ってよい時間を設定
                    it.connectTimeout = 1000
                    // データ取得に使ってもよい時間。
                    it.readTimeout = 1000
                    // HTTP接続メソッドをGETに設定
                    it.requestMethod
                    // 接続
                    it.connect()
                    // HttpURLConnectionオブジェクトからレスポンスデータを取得
                    val stream = it.inputStream
                    // レスポンスデータであるInputStreamを文字列に変換
                    result = is2Stering(stream)
                    // InputStreamオブジェクトを解放
                    stream.close()
                }
                catch (ex: SocketTimeoutException){
                    Log.w(DEBAG_TAG,"通信タイムアウト",ex)
                }
                // HttpURLConnectionオブジェクトを解放
                it.disconnect()
            }
            val postExecutor = WeatherInfoPostExecutor(result)
            _handler.post(postExecutor)
        }

        private fun is2Stering(stream: InputStream):String{
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream,"UTF-8"))
            var line = reader.readLine()
            while (line !=null)
            {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }

    // 非同期でお天気情報を取得した後にUIスレッドでその情報を表示するためのクラス
    private inner class WeatherInfoPostExecutor(result: String): Runnable{
        // 取得したお天気情報JSON文字列。
        private val _result = result

        @UiThread
        override fun run() {
            // ここにUIスレッドを行う処理コードを記述

            // ルートJSONオブジェクトを生成
            val rootJSON = JSONObject(_result)
            // 都市名文字列。を取得
            val cityName = rootJSON.getString("name")
            // 経緯度情報JOSNオブジェクトを取得
            val coordJSON = rootJSON.getJSONObject("coord")
            // 緯度情報文字列を取得
            val langitude = coordJSON.getString("lat")
            // 経度情報文字列を取得
            val longitude = coordJSON.getString("lon")
            // 天気情報JSON配列オブジェクトを取得
            val weatherJSONArray = rootJSON.getJSONArray("wearher")
            // 現在の天気情報JSONオブジェクトを取得
            val weatherJSON = weatherJSONArray.getJSONObject(0)
            // 現在の天気情報文字列を取得
            val weather = weatherJSON.getString("description")
            // 画面に表示する「〇〇の天気」文字列を生成
            val telop = "${cityName}に天気"
            // 天気の詳細情報を表示する文字列を生成
            val desc = "現在は${weather}です。\n緯度は${langitude}度で経度は${longitude}度です。"
            // 天気情報を表示するTextViewを取得
            val tvWeatherTelop = findViewById<TextView>(R.id.tvWeatherTelop)
            val tvWeatherDesc = findViewById<TextView>(R.id.tvWeatherDesc)
            // 天気情報を表示
            tvWeatherTelop.text = telop
            tvWeatherDesc.text = desc
        }
    }

    // リストがタップされたときの処理が記述されたリスナクラス
    private inner class ListItemClickListener:AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val item = _list.get(position)
            val q = item.get("q")
            q?.let {
                val urlFull = "$WEATHERINFO_URL&q=$q&appid=$APP_ID"
                receiveWeatherInfo(urlFull)
            }
        }
    }
}