package net.halowd.manbogy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import android.app.ActivityManager
import android.content.Context


class MainActivity : AppCompatActivity() {

    companion object{
        private const val FOREGROUND_PERMISSION_CODE = 1
    }

    fun isPermissionGranted() : Boolean{
        val foreground = ContextCompat.checkSelfPermission(this,android.Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
        val recognition = ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        return  foreground &&  recognition
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isPermissionGranted()){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.FOREGROUND_SERVICE,android.Manifest.permission.ACTIVITY_RECOGNITION), FOREGROUND_PERMISSION_CODE)
        }else{
            runWalkerService()
        }

    }

    fun runWalkerService(){
        if(!isWalkerRunning()){
            startService(Intent(this@MainActivity, WalkerService::class.java))
        }
    }

    fun isWalkerRunning() : Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var isRunning = false
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (WalkerService::class.java.name == service.service.className) {
                isRunning = true
            }
        }
        return isRunning;
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(isPermissionGranted()){
            runWalkerService()
        }
//        if (requestCode == FOREGROUND_PERMISSION_CODE){
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(this, "FOREGROUND PERMISSION IS GRANTED", Toast.LENGTH_LONG).show()
//            } else{
//                Toast.makeText(this, "FOREGROUND PERMISSION IS DENIED", Toast.LENGTH_LONG).show()
//            }
//        }
    }



}

fun log(text: String){
    Log.w("JWLOG", text);
}