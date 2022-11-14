package com.websarva.wings.android.weather_application

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomActivity(context: Context, var mHourlyWeatherList: List<HourlyWeather>) : ArrayAdapter<HourlyWeather>(context, 0, mHourlyWeatherList) {

    // クラス内のprivate定数を宣言するために
    companion object {
        // 摂氏へ変化するときの数
        private const val KELVIN_DIFF = 273
    }

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // mHourlyWeatherListの取得
        val lHourlyWeather = mHourlyWeatherList[position]

        // レイアウトの設定
        var view = convertView
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.activity_custom, parent, false)
        }

        // 各Viewの設定
        val WeatherInfo = view?.findViewById<TextView>(R.id.csTvWeatherInfo)
        WeatherInfo?.text = lHourlyWeather.WeatherInfo

        val WeatherName = view?.findViewById<TextView>(R.id.csTvWeatherComment)
        WeatherName?.text = lHourlyWeather.WeatherComment

        val TempInfo = view?.findViewById<TextView>(R.id.csTvTempInfo)
        TempInfo?.text = "${lHourlyWeather.TempInfo-KELVIN_DIFF} ℃"

        val TempName = view?.findViewById<TextView>(R.id.csTvTempComment)
        TempName?.text = lHourlyWeather.TempComment

        return view!!
    }
}