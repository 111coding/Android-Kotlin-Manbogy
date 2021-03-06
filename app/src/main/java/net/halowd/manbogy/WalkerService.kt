package net.halowd.manbogy

import android.R.attr
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
import android.R.attr.y

import android.R.attr.x
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import android.R.attr.y

import android.R.attr.x
import androidx.room.Room
import androidx.room.RoomDatabase
import net.halowd.manbogy.room.DbInstance
import net.halowd.manbogy.room.WalkDatabase
import kotlin.math.round


class WalkerService : Service() {

    companion object {
        var WALKING_COUNT = 0
    }

    var db: WalkDatabase? = null

    private fun initDb(){
        db = DbInstance.walkDatabase(applicationContext)

        WALKING_COUNT = db!!.walkDao().getRecent(1645428688174)
    }


    // 센서 시작
    private var sensorManager: SensorManager? = null
    private var stepCountSensor: Sensor? = null
    private var mEventListener :SensorEventListener? = null


    private fun initSensor(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCountSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        val listner = object : StepListener{
            override fun onStep(step: Int) {
                updateWalkerCount(step)
            }
        }
        if(stepCountSensor == null){
            // 가속도센서
            stepCountSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            mEventListener = StepAccelerometer()
            (mEventListener as StepAccelerometer).setStepListener(listner)
        }else{
            // 걷기센서
            mEventListener = StepDetector()
            (mEventListener as StepDetector).setStepListener(listner)
        }

        sensorManager?.registerListener(mEventListener,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST)
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
        db!!.walkDao().insert(nextCnt)
        WALKING_COUNT += nextCnt

        remoteViews?.setTextViewText(R.id.tv_walker_count,"$WALKING_COUNT")
        remoteViews?.setTextViewText(R.id.tv_walker_km,"${String.format("%.1f", (WALKING_COUNT * 0.0065))}")
        remoteViews?.setTextViewText(R.id.tv_walker_cal,"${String.format("%.0f", WALKING_COUNT * 0.033)}")

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

        initDb()

        initSensor()

        initNotification()


        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(mEventListener)
        db?.close();
    }


}

