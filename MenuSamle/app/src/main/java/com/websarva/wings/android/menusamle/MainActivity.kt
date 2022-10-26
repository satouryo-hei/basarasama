package com.websarva.wings.android.menusamle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter

class MainActivity : AppCompatActivity() {
    // リストビューに表示するリストデータ
    private var _menuList: MutableList<MutableMap<String, Any>> = mutableListOf()
    // SimpleAdapterの第4引数fromに使用するプロパティ
    private var _from = arrayOf("name", "price")
    // SimpleAdapterの第5引数toに使用するプロパティ
    private  var _to = intArrayOf(R.id.tvMenuNameRow,R.id.tvMenuPriceRow)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 定食メニューListオブジェクトをprivateメソッドを利用して用意し、プロパティに格納
        _menuList = crateTeishokuList()
        // 画面部品ListViewを取得。
        val lvMenu = findViewById<ListView>(R.id.lvMenu)
        // SimpleAdapterを生成。
        val adapter = SimpleAdapter(this@MainActivity, _menuList, R.layout.row,_from,_to)
        // アダプタの登録。
        lvMenu.adapter = adapter
        // リストタップクラス登録。
        lvMenu.onItemClickListener = ListItemClickListener()
    }

    private fun crateTeishokuList():MutableList<MutableMap<String,Any>>{
            // 定食メニューリスト用のlistオブジェクトを用意。
            val menuList: MutableList<MutableMap<String,Any>> = mutableListOf()

            //「から揚げ定食」のデータを格納するMap オブジェクトの用意とmenuList へのデータ登録。
            var menu = mutableMapOf<String,Any>("name" to "から揚げ定食","price" to 800,"desc" to
            "若鳥のから揚げにサラダ、ご飯とお味噌汁が付きます。")
            menuList.add(menu)

            //「ハンバーグ定食」のデータを格納するMap オブジェクトの用意とmenuList へのデータ登録。
            menu = mutableMapOf("name" to "ハンバーグ定食", "price" to 850, "desc" to
                    "手ごねハンバーグにサラダ、ご飯とお味噌汁が付きます。")
            menuList.add(menu)

            //「ハンバーグ定食」のデータを格納するMap オブジェクトの用意とmenuList へのデータ登録。
            menu = mutableMapOf("name" to "かつ定食", "price" to 1050, "desc" to
                    "手ごねハンバーグにサラダ、ご飯とお味噌汁が付きます。")
            menuList.add(menu)

            return menuList
    }

    // リスナがタップされた時の処理が記述されたメンバクラス
    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

            // タップされた行のデータを取得。SimpleAdapterでは1行分のデータはMutableMap型
            val item = parent.getItemAtPosition(position) as MutableMap<String, Any>
            // 定食名と金額を取得。
            val menuName = item["name"] as String
            val menuPrice = item["price"] as Int
            // インテントオブジェクトを生成。
            val intent2MenuThanks = Intent(this@MainActivity, MenuThanksAcitivity::class.java)
            // 第2画面に送るデータを格納
            intent2MenuThanks.putExtra("menuName", menuName)
            intent2MenuThanks.putExtra("menuPrice", "${menuPrice}円")
            // 第2画面の起動
            startActivity(intent2MenuThanks)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // オプションメニュー用xmlファイルをインフレイト
        menuInflater.inflate(R.menu.menu_options_menu_list,menu)
        return true
    }
}