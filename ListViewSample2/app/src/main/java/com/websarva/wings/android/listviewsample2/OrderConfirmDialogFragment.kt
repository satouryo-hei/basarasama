package com.websarva.wings.android.listviewsample2

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog

class OrderConfirmDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // アクティビティがnullではないならばダイアログオブジェクトを生成。
        val dialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            // ダイアログのタイトルを設定。
            builder.setTitle(R.string.dialog_title)
            // ダイアログのメッセージを設定。
            builder.setMessage(R.string.dialog_msg)
            // Positive Button を設定。
            builder.setPositiveButton(R.string.dialog_btn_ok, DialogButtonClickListener())
            // Negative Button を設定。
            builder.setNegativeButton(R.string.dialog_btn_ng, DialogButtonClickListener())
            // Neutral Button を設定。
            builder.setNeutralButton(R.string.dialog_btn_nu, DialogButtonClickListener())
            // ダイアログオブジェクトを生成。
            builder.create()
        }
        // 生成したダイアログオブジェクトをリターン。
        return dialog ?: throw IllegalAccessException("アクティビティがnullです")
    }

    // ダイアログのアクションボタンがタップされた時の処理が記述されたメンバクラス
    private inner class DialogButtonClickListener : DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface, which: Int) {
            // トーストメッセージ用文字列変数を用意
            var msg = ""
            // タップされたアクションボタンで分岐。
            when(which){
                // Positive Button ならば・・・
                DialogInterface.BUTTON_POSITIVE->
                    // 注文用のメッセージを格納
                    msg = getString(R.string.dialog_ok_toast)
                // Negative Button ならば・・・
                DialogInterface.BUTTON_NEGATIVE->
                    // キャンセル用のメッセージを格納。
                    msg = getString(R.string.dialog_ng_toast)
                // Neutral Button ならば・・・
                DialogInterface.BUTTON_NEUTRAL->
                    // 間合い用のメッセージを格納。
                    msg = getString(R.string.dialog_nu_toast)
            }
            // トーストの表示
            Toast.makeText(activity,msg,Toast.LENGTH_LONG).show()
        }
    }
}