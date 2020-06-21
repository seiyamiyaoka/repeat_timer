package com.example.workalarmclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import android.text.format.DateFormat;
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), TimeAleartDialog.Listener, DatePickerFragment.OnDateSelectedListener, TimePickerFragment.OnTimeSelectedListener {
    private var mediaPlayer: MediaPlayer? = null

    inner class MyCountDownTimer(millisInfuture: Long, countDownInterval: Long, var mediaPlayer: MediaPlayer?) : CountDownTimer(millisInfuture, countDownInterval) {
        var isRunning = false
        override fun onTick(millisInfuture: Long) {
            print(millisInfuture)
        }

        override fun onFinish() {
            mediaPlayer?.stop()
            println("countdown終了")
        }
    }

    override fun onSelected(year: Int, month: Int, date: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, date)
        dateText.text = DateFormat.format("yyyy/MM/dd", c)
    }

    override fun onSelected(hourOfDay: Int, minute: Int) {
        timeText.text = "%1$02d:%2$02d".format(hourOfDay, minute)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if(intent?.getBooleanExtra("onReceive", false) == true) {
            val dialog = TimeAleartDialog()
            dialog.show(supportFragmentManager, "alert_dialog")
            // 音楽これで実行される
            mediaPlayer = MediaPlayer.create(this, R.raw.test_music)

            mediaPlayer?.start()
            val timer = MyCountDownTimer(60 * 1000, 100, mediaPlayer)
            timer.isRunning = when (timer.isRunning) {
                true -> {
                    timer.cancel()
                    false
                }
                false -> {
                    timer.start()
                    true
                }
            }

        }

        setContentView(R.layout.activity_main)

        setAlarm.setOnClickListener {
            val date = "${dateText.text} ${timeText.text}".toDate()

            when {
                date != null -> {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    useAlarmManager(1, calendar)
                }
                else -> {
                    Toast.makeText(
                        this, "日付の設定正しくないよ",
                        Toast.LENGTH_SHORT
                    )
                }
            }

        }
        cancelAlarm.setOnClickListener {
            useAlarmManager(0, null)
        }
        dateText.setOnClickListener {
            val dialog = DatePickerFragment()
            dialog.show(supportFragmentManager, "date_dialog")
        }
        timeText.setOnClickListener {
            val dialog = TimePickerFragment()
            dialog.show(supportFragmentManager, "time_dialog")
        }
    }

    override fun getUp() {
        Toast.makeText(this, "起きるが押されたよ", Toast.LENGTH_SHORT).show()
    }

    override fun snooze() {
        Toast.makeText(this, "あと5分がクリックされたよ", Toast.LENGTH_SHORT).show()
    }

    private fun useAlarmManager(flag: Int, calendar: Calendar?) {

        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // ページ起動時に時刻とreceiverを登録している
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 0, intent,0)
        when {
            flag == 0 -> am.cancel(pending)
            flag == 1 && calendar != null -> {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                        am.setInexactRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                            pending
                        )
//                        am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        val info = AlarmManager.AlarmClockInfo(calendar.timeInMillis, null)
                        am.setAlarmClock(info, pending)
                    }
                    else -> {
                        am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
                    }
                }
            }
        }
    }
      private fun String.toDate(pattern: String = "yyyy/MM/dd HH:mm") : Date? {
          return try {
              SimpleDateFormat(pattern).parse(this)
          } catch (e: IllegalArgumentException) {
              return null
          } catch (e: ParseException) {
              return null
          }
      }
}