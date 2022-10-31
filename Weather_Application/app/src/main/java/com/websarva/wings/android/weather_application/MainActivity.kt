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

class MainActivity : AppCompatActivity() {
    // クラス内のprivate定数を宣言するために
    companion object {
        // ログに記載するタグ用の文字列
        private const val DEBAG_TAG = "AsyncSample"

        // お天気情報のURL
        private const val WEATHERINFO_URL = "https://api.openweathermap.org/data/2.5/weather?lang=ja"

        // お天気APIにアクセスすするためにAPIキー。
        private const val APP_ID = "76eafa6c7ef6c4b02799cf2857ad6d89"
    }

    // リストビューに表示させるリストデータ。
    private var _list: MutableList<MutableMap<String, String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _list = createList()

        val lSharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        // "Input"から読み出す
        val LStr = lSharedPref.getString("DataString", "NoData")
        val llvCityList = findViewById<ListView>(R.id.lvCityList)
        val lFrom = arrayOf("name")
        val lTo = intArrayOf(android.R.id.text1)
        val lAdapter =
            SimpleAdapter(this@MainActivity, _list, android.R.layout.simple_list_item_1, lFrom, lTo)
        llvCityList.adapter = lAdapter
        llvCityList.onItemClickListener = ListItemClickListener()
    }

    fun onButtonClick(view: View){
        //
        val lIntent = Intent(this@MainActivity,changeActivity::class.java)
        //
        startActivity(lIntent)
    }


    // リストビューに表示させる天気ポイントリストデータを生成するメソッド
    private fun createList(): MutableList<MutableMap<String, String>> {
        var rList: MutableList<MutableMap<String, String>> = mutableListOf()

        var rCity = mutableMapOf("name" to "大阪", "q" to "Osaka")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "神戸", "q" to "kobe")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "札幌", "q" to "Sapporo")
        rList.add(rCity)

        return rList
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
                    rResult = is2Stering(stream)
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

        private fun is2Stering(stream: InputStream): String {
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
            // 天気情報を表示するTextViewを取得
            val tvWeatherTelop = findViewById<TextView>(R.id.tvWeatherTelop)
            val tvWeatherDesc = findViewById<TextView>(R.id.tvWeatherDesc)
            val tvTemp = findViewById<TextView>(R.id.tvTemp)
            val tvTempMin = findViewById<TextView>(R.id.tvTempMin)
            val tvTempMax = findViewById<TextView>(R.id.tvTempMax)

            // ルートJSONオブジェクトを生成
            val rootJSON = JSONObject(_result)
            // 都市名文字列。を取得
            val lCityName = rootJSON.getString("name")
            // 天気情報JSON配列オブジェクトを取得
            val lWeatherJSONArray = rootJSON.getJSONArray("weather")
            // 現在の天気情報JSONオブジェクトを取得
            val lWeatherJSON = lWeatherJSONArray.getJSONObject(0)
            // 現在の天気情報文字列を取得
            val lWeather = lWeatherJSON.getString("description")
            // 気温情報JSONオブジェクトを取得
            val lMain = rootJSON.getJSONObject("main")
            // 天気情報を表示
            tvWeatherTelop.text = "${lCityName}に天気"
            tvWeatherDesc.text = "現在は${lWeather}です。"
            tvTemp.text = "現在の気温${lMain.getInt("temp")-273}"
            // 最低気温情報文字列を取得、表示｛C(摂氏)＝K(ケルビン)-273｝
            tvTempMin.text ="最低気温${lMain.getInt("temp_min")-273}"
            // 最高気温情報文字列を取得、表示 ｛C(摂氏)＝K(ケルビン)-273｝
            tvTempMax.text ="最高気温${lMain.getInt("temp_max")-273}"
        }
    }



    // リストがタップされたときの処理が記述されたリスナクラス
    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val item = _list.get(position)
            val q = item.get("q")
            q?.let {
                // "DataStore"という名前でインスタンスを生成
                val sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE)

                // 文字列を"Input"に書き込む
                val editor = sharedPref.edit()
                editor.putString("DataString", "${q}")

                editor.apply()

                val urlFull = "$WEATHERINFO_URL&q=$q&appid=$APP_ID"
                receiveWeatherInfo(urlFull)
            }
        }
    }
}