package com.cnx.nextvpublisher_vxg2

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cnx.nextvpublisher_vxg2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object{
        lateinit var binding: ActivityMainBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val intent = Intent(applicationContext, RTMPPublisherService::class.java)
        startService(intent)

    }

}