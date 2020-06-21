package com.example.workalarmclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 今回はメインページを再度呼び出す
        // mainが呼ばれた時の条件としてセットした値を利用する
        val mainIntent = Intent(context, MainActivity::class.java)
                               .putExtra("onReceive", true)
                               .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(mainIntent)
    }
}
