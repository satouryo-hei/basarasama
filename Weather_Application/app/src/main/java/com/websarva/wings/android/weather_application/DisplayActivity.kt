package com.websarva.wings.android.weather_application

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class DisplayActivity(context: Context, var mDisplayList: List<DisplayWeather>) : ArrayAdapter<DisplayWeather>(context, 0, mDisplayList) {

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // mHourlyWeatherListの取得
        val lDisplay = mDisplayList[position]

        // レイアウトの設定
        var view = convertView
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.activity_custom, parent, false)
        }

        // 各Viewの設定
        val TexView = view?.findViewById<ImageView>(R.id.IvWeatherInfoTex)
        TexView?.setImageResource(lDisplay.WeatherInfoTex)

        val WeatherInfo = view?.findViewById<TextView>(R.id.csTvWeather)
        WeatherInfo?.text = lDisplay.Weather

        return view!!
    }
}