package com.websarva.wings.android.weather_application

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.graphics.colorSpace
import androidx.core.os.HandlerCompat
import androidx.core.view.get
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.Executors

class changeActivity : AppCompatActivity() {
    // クラス内のprivate定数を宣言するために
    companion object {
        // ログに記載するタグ用の文字列
        private const val DEBUG_TAG = "Debug:API通信Change"

        // お天気情報のURL
        private const val WEATHERINFO_URL = "https://api.openweathermap.org/data/2.5/weather?lang=ja"

        // お天気APIにアクセスすするためにAPIキー。
        private const val APP_ID = "76eafa6c7ef6c4b02799cf2857ad6d89"

        // データ取得時間
        private const val TIMEOUT = 1000

        // 初期化の数
        private const val INIT_NUM = 0

        // 回す回数
        private const val CITY_NUM = 10
    }

    // リストビューに表示させるリストデータ。(天気リストデータ)
    private var _Citylist: MutableList<MutableMap<String, String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change)

        val lTvSelection =  findViewById<TextView>(R.id.tvSelection)
        lTvSelection.text = "現在選択されていません"

        // 都市リストを生成
        _Citylist = createList()

        val lLvCityList = findViewById<ListView>(R.id.lscitylist)
        val from = arrayOf("name")
        val to = intArrayOf(android.R.id.text1)
        val adapter = SimpleAdapter(this@changeActivity,_Citylist,android.R.layout.simple_list_item_1,from,to)
        lLvCityList.adapter = adapter
        lLvCityList.onItemClickListener = ListChangeClickListener()
    }

    // リストビューに表示させる天気ポイントリストデータを生成するメソッド
    private  fun createList(): MutableList<MutableMap<String,String>> {
        var rList: MutableList<MutableMap<String, String>> = mutableListOf()

        var rCity = mutableMapOf("name" to "札幌", "q" to "Sapporo")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "東京", "q" to "Tokyo")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "大阪", "q" to "Osaka")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "北見", "q" to "Kitami")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "那覇", "q" to "Naha")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "旭川", "q" to "Asahikawa")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "京都", "q" to "Kyoto")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "名古屋", "q" to "Nagoya")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "神戸", "q" to "Kobe")
        rList.add(rCity)
        rCity = mutableMapOf("name" to "広島", "q" to "Hiroshima")
        rList.add(rCity)

        return rList
    }


    @UiThread
    // お天気情報の取得処理を行うメソッド
    private fun receiveWeatherInfo(nrlFull: String) {
        // ここに非同期で天気情報を取得する処理を記述する。
        var rHandler = HandlerCompat.createAsync(mainLooper)
        val lBackgroundReceiver = WeatherInfoBackgroundReceiver(rHandler, nrlFull)
        val lExecuteService = Executors.newSingleThreadExecutor()
        lExecuteService.submit(lBackgroundReceiver)
    }

    // 非同期でお天気情報APIにアクセスするためのクラス
    private inner class WeatherInfoBackgroundReceiver(handler: Handler, url: String) : Runnable {
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
                    it.requestMethod = "GET"
                    // 接続
                    it.connect()
                    // HttpURLConnectionオブジェクトからレスポンスデータを取得
                    val stream = it.inputStream
                    // レスポンスデータであるInputStreamを文字列に変換
                    rResult = is2String(stream)
                    // InputStreamオブジェクトを解放
                    stream.close()
                } catch (ex: SocketTimeoutException) {
                    Log.w(DEBUG_TAG, "通信タイムアウト", ex)
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
            Log.i(DEBUG_TAG,"経緯度:${coordJSON}")
            // 緯度情報文字列を取得
            val lLatitude = coordJSON.getString("lat")
            // 経度情報文字列を取得
            val lLongitude = coordJSON.getString("lon")

            val lTvSelection =  findViewById<TextView>(R.id.tvSelection)
            lTvSelection.text = "現在選択中の都市:${lCityName}"

            // "DataStore"という名前でインスタンスを生成
            val lSharedPref = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )

            // 文字列を書き込む
            val editor = lSharedPref.edit()
            editor.putString("CityName", "${lCityName}")
            Log.i(DEBUG_TAG,"都市の名前:${lCityName}")
            editor.putString("latitude", "${lLatitude}")
            Log.i(DEBUG_TAG,"経度:${lLatitude}")
            editor.putString("longitude", "${lLongitude}")
            Log.i(DEBUG_TAG,"緯度:${lLongitude}")
            editor.apply()
        }
    }



    // リストがタップされたときの処理が記述されたリスナクラス
    private inner class ListChangeClickListener : AdapterView.OnItemClickListener {

        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val lText = _Citylist.get(position)
            val lQ = lText.get("q")
            lQ?.let {
                val urlFull = "$WEATHERINFO_URL&q=$lQ&appid=$APP_ID"
                receiveWeatherInfo(urlFull)
            }

            SelectCangeColor(position,parent)

            // "DataStore"という名前でインスタンスを生成
            val sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )

            // 文字列を"Input"に書き込む
            val editor = sharedPref.edit()
            editor.putString("q", "${lQ}")
            editor.apply()
        }

        // 色変更を行う処理
        fun SelectCangeColor(position: Int, parent: ViewGroup) {
            // 任意の初期値を代入
            var i = INIT_NUM
            // 都市分while文を回す
            while (i <CITY_NUM) {// 都市リストの色を全て白にする
                // 色を白にする
                parent[i].setBackgroundColor(Color.WHITE)
                i++ // 次に進める
            }
            // 押された場所の色を黄色にする
            parent[position].setBackgroundColor(Color.YELLOW)
        }
    }

    // 戻るボタンが押されたときの処理
    fun onPreviousButtonClick(view: View){
        finish()
    }

    // 決定ボタンが押されたときの処理
    fun onDecisionButtonClick(view: View){
        // インテントオブジェクトを用意(どこに遷移するか)
        val intent = Intent(this@changeActivity, TopActivity::class.java)
        // アクティビティを起動(遷移開始)
        startActivity(intent)
    }
}