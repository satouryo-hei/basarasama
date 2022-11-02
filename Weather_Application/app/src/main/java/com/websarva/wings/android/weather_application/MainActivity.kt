package com.websarva.wings.android.weather_application

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
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

class MainActivity : AppCompatActivity() {
    // クラス内のprivate定数を宣言するために
    companion object {
        // ログに記載するタグ用の文字列
        private const val DEBAG_TAG = "Debag:API通信MAIN"

        // お天気情報のURL
        private const val WEATHERINFO_URL = "https://api.openweathermap.org/data/2.5/forecast?lang=ja"

        // お天気APIにアクセスすするためにAPIキー。
        private const val APP_ID = "76eafa6c7ef6c4b02799cf2857ad6d89"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

      //  val intent = Intent(this@MainActivity, InitActivity::class.java)
//        startActivity(intent)

        changeWeatherInfo(true)
    }

    // 次の画面を表示するためのボタンを押したときの処理
    fun onButtonClick(view: View){
        // インテントオブジェクトを用意
        val lIntent = Intent(this@MainActivity,InitActivity::class.java)
        // アクティビティを起動
        startActivity(lIntent)
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

    private fun changeWeatherInfo(change: Boolean) {

        val lSharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        // "Input"から読み出す
        val lLan = lSharedPref.getString("latitude", "NoData")
        lLan?.let {

            val lLon = lSharedPref.getString("longitude", "NoData")

            Log.i(DEBAG_TAG,"${lLan}")
            Log.i(DEBAG_TAG,"${lLon}")

            val urlFull = "$WEATHERINFO_URL&lat=${lLan}&lon=${lLon}&appid=$APP_ID"
            receiveWeatherInfo(urlFull)
        }
    }

    // 非同期でお天気情報を取得した後にUIスレッドでその情報を表示するためのクラス
    private inner class WeatherInfoPostExecutor(result: String) : Runnable {
        // 取得したお天気情報JSON文字列。
        private val _result = result

        @UiThread
        override fun run() {
            // ここにUIスレッドを行う処理コードを記述
            // 天気情報を表示するTextViewを取得
            val tvWeatherTelop = findViewById<TextView>(R.id.tvWeatherTelop)
            val tvWeatherDesc = findViewById<TextView>(R.id.tvWeatherDesc)
            val tvTemp = findViewById<TextView>(R.id.tvTemp)
            val tvTempMin = findViewById<TextView>(R.id.tvTempMin)
            val tvTempMax = findViewById<TextView>(R.id.tvTempMax)

            // ルートJSONオブジェクトを生成
            val lRootJSON = JSONObject(_result)

            // 天気情報JSON配列オブジェクトを取得
            val lRootJSONArray = lRootJSON.getJSONArray("list")
            // 天気情報JSON配列オブジェクトを取得
            val lRootJSONArrayAny = lRootJSONArray.getJSONObject(0)


            // 都市情報JSONオブジェクトを取得を取得
            val lCityJSON = lRootJSON.getJSONObject("city")

            // 都市名文字列。を取得
            val lCityName = lCityJSON.getString("name")
            Log.i(DEBAG_TAG,"${lCityName}")
            // 天気情報JSON配列オブジェクトを取得
            val lWeatherJSONArray = lRootJSONArrayAny.getJSONArray("weather")
            // 現在の天気情報JSONオブジェクトを取得
            val lWeatherIndex = lWeatherJSONArray.getJSONObject(0)
            // 現在の天気情報文字列を取得
            val lWeather = lWeatherIndex.getString("description")
            Log.i(DEBAG_TAG,"${lWeather}")

            // 気温情報JSONオブジェクトを取得
            val lMain = lRootJSONArrayAny.getJSONObject("main")
            // 天気情報を表示
            tvWeatherTelop.text = "${lCityName}の天気"
            tvWeatherDesc.text = "現在は${lWeather}です。"
            tvTemp.text = "現在の気温${lMain.getInt("temp")-273}"
            // 最低気温情報文字列を取得、表示｛C(摂氏)＝K(ケルビン)-273｝
            tvTempMin.text ="最低気温${lMain.getInt("temp_min")-273}"
            // 最高気温情報文字列を取得、表示 ｛C(摂氏)＝K(ケルビン)-273｝
            tvTempMax.text ="最高気温${lMain.getInt("temp_max")-273}"
        }
    }
}