package com.websarva.wings.android.intentsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 画面部品ListView を取得。
        val lvMenu = findViewById<ListView>(R.id.lvMenu)
        // SimpleAdapter で使用する MutableList オブジェクトを用意
        val menuList: MutableList<MutableMap<String,String>> = mutableListOf()
        // 「から揚げ定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        var menu = mutableMapOf("name" to "から揚げ定食","price" to "800円")
        menuList.add(menu)
        // 「ハンバーグ定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "ハンバーグ定食","price" to "850円")
        menuList.add(menu)
        // 「焼肉定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "焼肉定食","price" to "1050円")
        menuList.add(menu)
        // 「カレー定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "カレー定食","price" to "820円")
        menuList.add(menu)
        // 「カツカレー定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "カツカレー定食","price" to "950円")
        menuList.add(menu)
        // 「キーマカレー定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "キーマカレー定食","price" to "870円")
        menuList.add(menu)
        // 「かつ丼定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "かつ丼定食","price" to "1100円")
        menuList.add(menu)
        // 「オムライス定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "オムライス定食","price" to "970円")
        menuList.add(menu)
        // 「寿司定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "寿司定食","price" to "1200円")
        menuList.add(menu)
        // 「お好み焼き定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "お好み焼き定食","price" to "1050円")
        menuList.add(menu)
        // 「焼きそば定食」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "焼きそば定食","price" to "1050円")
        menuList.add(menu)
        // 「釜めし」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "釜めし","price" to "1050円")
        menuList.add(menu)
        // 「パフェのフルコース」のデータを格納するMapオブジェクトの用意とmenuListへのデータ登録。
        menu = mutableMapOf("name" to "パフェのフルコース","price" to "1050円")
        menuList.add(menu)


        // SimpleAdapter第4引数 from 用データ用意
        val from = arrayOf("name","price")
        // SimpleAdapter第5引数 to 用データ用意
        val to = intArrayOf(android.R.id.text1,android.R.id.text2)
        // SimpleAdapterを生成。
        val adapter = SimpleAdapter(this@MainActivity,menuList,android.R.layout.simple_list_item_2,
        from,to)
        // アダプタの登録
        lvMenu.adapter = adapter

        // リストタップのリスナクラス登録
        lvMenu.onItemClickListener = ListItemClickListener()
    }

    // リスナがタップされた時の処理が記述されたメンバクラス
    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

            // タップされた行のデータを取得。SimpleAdapterでは1行分のデータはMutableMap型
            val item = parent.getItemAtPosition(position) as MutableMap<String, String>
            // 定食名と金額を取得。
            val menuName = item["name"]
            val menuPrice = item["price"]
            // インテントオブジェクトを生成。
            val intent2MenuThanks = Intent(this@MainActivity, MenuThankActivity::class.java)
            // 第2画面に送るデータを格納
            intent2MenuThanks.putExtra("menuName", menuName)
            intent2MenuThanks.putExtra("menuPrice", menuPrice)
            // 第2画面の起動
            startActivity(intent2MenuThanks)
        }
    }
}