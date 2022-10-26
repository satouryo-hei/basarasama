package com.websarva.wings.android.menusamle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.widget.DecorContentParent

class MenuThanksAcitivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_thanks_acitivity)

            // リスト画面から渡されたデータの取得。
            val menuName = intent.getStringExtra("menuName")
            val menuPrice = intent.getStringExtra("menuPrice")

            // 定食名と金額を表示させる TextView　を取得。
            val tvMenuName = findViewById<TextView>(R.id.tvMenuName)
            val tvMenuPrice = findViewById<TextView>(R.id.tvMenuPrice)

            // TextView　に定食名と金額を表示
            tvMenuName.text = menuName
            tvMenuPrice.text = menuPrice
        }

        // 戻るボタンをタップした時の処理
        fun onBackButtonClick(view:View){
            finish()
        }
}