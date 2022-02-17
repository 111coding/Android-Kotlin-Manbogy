package net.halowd.manbogy

import android.app.*
import android.content.Intent
import android.os.IBinder

import androidx.core.app.NotificationCompat

import android.content.Context
import android.os.Build
import android.widget.RemoteViews


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import java.util.*
import android.hardware.SensorManager



var WALKING_COUNT = 0


class WalkerService : Service(), SensorEventListener {

    private var preferences: SharedPreferences? = null
    private var preferencesEditor: SharedPreferences.Editor? = null
    private val SHARED_PREFERENCES_NAME = "walk_count_shared_preferences_name"
    private val SHARED_PREFERENCES_KEY = "walk_count_shared_preferences_key"


    private fun initPreferences(){
        preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if(preferences != null){
            preferencesEditor = preferences!!.edit()
            WALKING_COUNT = preferences!!.getInt(SHARED_PREFERENCES_KEY,0)
        }
    }

    private fun saveValue(){
        preferencesEditor?.putInt(SHARED_PREFERENCES_KEY, WALKING_COUNT)
        preferencesEditor?.commit()
    }

    // 센서 시작
    private var sensorManager: SensorManager? = null
    private var stepCountSensor: Sensor? = null

    private fun initSensor(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCountSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        sensorManager?.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event!=null){
            val nextCount : Int = event.values[0].toInt()
            updateWalkerCount(nextCount)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
    }
    // 센서 끝

    // 노티피케이션 시작
    private val CHANNEL_ID : String = "walker_service"
    private val CHANNEL_NAME : String = "walker"
    private val NOTIF_ID = 1

    private var notificationManager: NotificationManager? = null
    private var notification:Notification? = null
    private var remoteViews:RemoteViews? = null


    private fun updateWalkerCount(nextCnt : Int){
        WALKING_COUNT += nextCnt;
        saveValue()
        remoteViews?.setTextViewText(R.id.tv_walker_count,"$WALKING_COUNT")
        notificationManager?.notify(NOTIF_ID,notification)
    }

    private fun initNotification(){
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 채널 먼저 만들기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager?.createNotificationChannel(channel)
        }

        // 뷰
        remoteViews = RemoteViews(
            packageName,
            R.layout.walker_notification
        )
        remoteViews?.setTextViewText(R.id.tv_walker_count,"$WALKING_COUNT")

        val mBuilder  = NotificationCompat.Builder(this,  CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContent(remoteViews)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            // 클릭 시 메인 액티비티 가게
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(applicationContext, MainActivity::class.java), PendingIntent.FLAG_CANCEL_CURRENT))

        notification = mBuilder.build()

        startForeground(NOTIF_ID, notification)
    }
    // 노티피케이션 끝



    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        initPreferences()

        initSensor()

        initNotification()


        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }


}

