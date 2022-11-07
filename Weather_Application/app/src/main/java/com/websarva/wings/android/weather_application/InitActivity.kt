package com.websarva.wings.android.weather_application

import android.R.attr.data
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.Executors


class InitActivity : AppCompatActivity() {

    // クラス内のprivate定数を宣言するために
    companion object {
        // ログに記載するタグ用の文字列
        private const val DEBAG_TAG = "Debag:API通信INIT"

        // お天気情報のURL
        private const val WEATHERINFO_URL = "https://api.openweathermap.org/data/2.5/weather?lang=ja"

        // お天気APIにアクセスすするためにAPIキー。
        private const val APP_ID = "76eafa6c7ef6c4b02799cf2857ad6d89"

        // データ取得時間
        private const val TIMEOUT = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        val lSharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        // "Input"から読み出す
        if((lSharedPref.getBoolean("first", false)))
       {
           val intent = Intent(this@InitActivity, MainActivity::class.java)
           startActivity(intent)
       }



        val llvCityList = findViewById<ListView>(R.id.lsjacitylist)
        llvCityList.onItemClickListener = ListInitClickListener()
    }


    @UiThread
    // お天気情報の取得処理を行うメソッド
    private fun receiveWeatherInfo(nrlFull: String) {
        // ここに非同期で天気情報を取得する処理を記述する。
        var rHandler = HandlerCompat.createAsync(mainLooper)
        val lBackgroundReceiver = WeathetInfoBackgroundReceiver(rHandler, nrlFull)
        val lExecuteService = Executors.newSingleThreadExecutor()
        lExecuteService.submit(lBackgroundReceiver)
    }

    // 非同期でお天気情報APIにアクセスするためのクラス
    private inner class WeathetInfoBackgroundReceiver(handler: Handler, url: String) : Runnable {
        // ハンドラオブジェクト
        private val _handler = handler

        // お天気情報を取得するURL
        private val _url = url

        @WorkerThread
        override fun run() {
            // ここにweb APIにアクセスするコードを記述
            // 天気情報サービスから取得したJSON文字列。天気情報が格納されている。
            var rResult = ""
            // URLオブジェクトを生成
            val lUrl = URL(_url)
            // URLオブジェクトからHttpURLConnectionオブジェクトを取得。
            val lCon = lUrl.openConnection() as? HttpURLConnection
            // conがnullじゃないならば・・・
            lCon?.let {
                try {
                    // 接続に使ってよい時間を設定
                    it.connectTimeout = TIMEOUT
                    // データ取得に使ってもよい時間。
                    it.readTimeout = TIMEOUT
                    // HTTP接続メソッドをGETに設定
                    it.requestMethod
                    // 接続
                    it.connect()
                    // HttpURLConnectionオブジェクトからレスポンスデータを取得
                    val stream = it.inputStream
                    // レスポンスデータであるInputStreamを文字列に変換
                    rResult = is2String(stream)
                    // InputStreamオブジェクトを解放
                    stream.close()
                } catch (ex: SocketTimeoutException) {
                    Log.w(DEBAG_TAG, "通信タイムアウト", ex)
                }
                // HttpURLConnectionオブジェクトを解放
                it.disconnect()
            }
            val lPostExecutor = WeatherInfoPostExecutor(rResult)
            _handler.post(lPostExecutor)
        }

        // InputStreamオブジェクトを文字列に変換
        private fun is2String(stream: InputStream): String {
            val lSb = StringBuilder()
            val rReader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var rLine = rReader.readLine()
            while (rLine != null) {
                lSb.append(rLine)
                rLine = rReader.readLine()
            }
            rReader.close()
            return lSb.toString()
        }
    }

    // 非同期でお天気情報を取得した後にUIスレッドでその情報を表示するためのクラス
    private inner class WeatherInfoPostExecutor(result: String) : Runnable {
        // 取得したお天気情報JSON文字列。
        private val _result = result

        @UiThread
        override fun run() {
            // ここにUIスレッドを行う処理コードを記述
            // ルートJSONオブジェクトを生成
            val lRootJSON = JSONObject(_result)
            // 都市名文字列。を取得
            val lCityName = lRootJSON.getString("name")
            // 経緯度情報JSONオブジェクトを取得
            val coordJSON = lRootJSON.getJSONObject("coord")
            // 緯度情報文字列を取得
            val lLatitude = coordJSON.getString("lat")
            // 経度情報文字列を取得
            val lLongitude = coordJSON.getString("lon")

            // "DataStore"という名前でインスタンスを生成
            val sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )

            // 文字列を"Input"に書き込む
            val editor = sharedPref.edit()
            editor.putString("CityName", "${lCityName}")
            Log.i(DEBAG_TAG,"都市の名前:${lCityName}")
            editor.putString("latitude", "${lLatitude}")
            Log.i(DEBAG_TAG,"経度:${lLatitude}")
            editor.putString("longitude", "${lLongitude}")
            Log.i(DEBAG_TAG,"緯度:${lLongitude}")
            editor.putBoolean("first", false)
            editor.apply()
            // インテントオブジェクトを用意(どこに遷移するか)
            val intent = Intent(this@InitActivity, MainActivity::class.java)
            //intent.putExtra("Bool", true)
            // アクティビティを起動(遷移開始)
            startActivity(intent)

            //finish()
        }
    }



    // リストがタップされたときの処理が記述されたリスナクラス
    private inner class ListInitClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val Text = view as TextView
            val item = Text.text.toString()
            item?.let {
                val urlFull = "$WEATHERINFO_URL&q=$item&appid=$APP_ID"
                receiveWeatherInfo(urlFull)
            }
            Log.i("${DEBAG_TAG}:Item","${item}")

            // "DataStore"という名前でインスタンスを生成
            val sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )

            // 文字列を"Input"に書き込む
            val editor = sharedPref.edit()
            editor.putString("q", "${item}")
            editor.apply()
        }
    }
}