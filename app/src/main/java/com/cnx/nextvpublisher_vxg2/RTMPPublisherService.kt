package com.cnx.nextvpublisher_vxg2

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.work.*
import com.google.firebase.FirebaseApp
import veg.mediacapture.sdk.MediaCapture
import veg.mediacapture.sdk.MediaCaptureCallback
import veg.mediacapture.sdk.MediaCaptureConfig
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer


class RTMPPublisherService : LifecycleService(), MediaCaptureCallback {

    private lateinit var capturer: MediaCapture
    private lateinit var config: MediaCaptureConfig
    //var startTime = 0

    companion object {
        const val CONNECTION_URL = "rtmp://192.168.0.102:1935/live/rte"
    }

    inner class RTMPPublisherBinder : Binder() {
        val service: RTMPPublisherService
            get() = this@RTMPPublisherService
    }

    private var mBinder = RTMPPublisherBinder()

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        capturer = MediaCapture(this@RTMPPublisherService, null)

        //onTaskRemoved(intent)

        config = capturer.config
        config.cameraFacing = MediaCaptureConfig.CAMERA_FACING_FRONT

        config.isStreaming = true
        config.isRecording = false
        config.isTranscoding = false

        config.captureMode = MediaCaptureConfig.CaptureModes.PP_MODE_ALL.`val`()

        config.streamType = 1
        config.audioFormat = MediaCaptureConfig.TYPE_AUDIO_AAC
        config.audioBitrate = 64
        config.audioSamplingRate = 44100
        config.audioChannels = 2
        config.setUrl(
            MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTMP_PUBLISH.`val`(),
            CONNECTION_URL
        )

        //        config.setUrlSec(
        //            MediaCaptureConfig.StreamerTypes.STREAM_TYPE_RTSP_SERVER.`val`(),
        //            if (false) CONNECTION_URL else ""
        //        )

        config.videoFramerate = 30
        config.videoKeyFrameInterval = 1
        config.videoBitrate = 700
        config.videoBitrateMode = MediaCaptureConfig.BITRATE_MODE_ADAPTIVE
        config.videoSecBitrateMode = MediaCaptureConfig.BITRATE_MODE_ADAPTIVE
        config.videoMaxLatency = 500

        var resX = 720

        val listRes = config.videoSupportedRes
        if (listRes != null && listRes.size > 0) {
            for (vr in listRes) {
                val w =
                    if (config.videoOrientation == 0) config.getVideoWidth(vr) else config.getVideoHeight(
                        vr
                    )
                if (w < resX) {
                    resX = w
                }
                break
            }
        }

        when (resX) {
            3840 -> config.videoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_3840x2160
            1920 -> config.videoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_1920x1080
            1280 -> config.videoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_1280x720
            721 -> config.videoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_720x576
            720 -> config.videoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_720x480
            640 -> config.videoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_640x480
            352 -> config.videoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_352x288
            320 -> config.videoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_320x240
            176 -> config.videoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_176x144
        }


        if (config.videoWidth == 320 || config.videoWidth == 240) {
            config.secVideoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_720x480
        } else {
            config.secVideoResolution = MediaCaptureConfig.CaptureVideoResolution.VR_320x240
        }

        config.secVideoFramerate = 5
        config.secVideoKeyFrameInterval = 2

        config.transWidth = 320
        config.transHeight = 240
        config.transFps = 1.0
        config.transFormat = MediaCaptureConfig.TYPE_VIDEO_RAW
        config.captureSource = MediaCaptureConfig.CaptureSources.PP_MODE_CAMERA.`val`()

        capturer.Open(
            config,
            this
        )


        FirebasePublisherListener.shouldPublish().observe(this, Observer {
            it?.let {
                if (it) {
                    capturer.StartStreaming()
                } else {
                    capturer.StopStreaming()
                }
            }
        })

//        val transThread = Thread(Runnable {
//            Thread.sleep(2000)
//            startTime = (System.currentTimeMillis() / 1000).toInt()
//            capturer.StartTranscoding()
//        }).start()
        return START_STICKY
    }

    override fun onDestroy() {
        capturer.Stop()
        capturer.Close()
        super.onDestroy()
    }

//    override fun onTaskRemoved(rootIntent: Intent?) {
//        val restartServiceIntent = Intent(applicationContext, this.javaClass)
//        restartServiceIntent.setPackage(packageName)
//        startService(restartServiceIntent)
//        super.onTaskRemoved(rootIntent)
//    }

    override fun OnCaptureStatus(p0: Int): Int {
        Log.d("TESTING_STIGMA", "STA $p0" + "Stream Status - ${capturer.streamStatus}")
        return 0
    }

    override fun OnCaptureReceiveData(p0: ByteBuffer?, p1: Int, p2: Int, p3: Long): Int {
//        Log.d("TESTING_STIGMA", "REC")
//
//        if (p0 == null) {
//            Log.d("TESTING_STIGMA", "RETURNED")
//            return 0
//        }
//
//        if ((System.currentTimeMillis() / 1000).toInt() - startTime <= 30) {
//            return 0
//        }
//
//        val byteArray = ByteArray(p0.capacity())
//
//        Log.d("TESTING_STIGMA", "INSIDE")
//
//        val filePreview = File(getRecordPath() + "/preview.png")
//        val width = capturer.config.transWidth //320;
//        val height = capturer.config.transHeight //240;
//
//        var bos: BufferedOutputStream? = null
//        try {
//            bos = BufferedOutputStream(FileOutputStream(filePreview))
//            val bmp0 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//            bmp0.copyPixelsFromBuffer(p0)
//            val matrix = Matrix()
//            matrix.preScale(-1f, 1f)
//            matrix.preRotate(180f)
//            val bmp = Bitmap.createBitmap(bmp0, 0, 0, width, height, matrix, true)
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, bos)
//            bmp0.recycle()
//            bmp.recycle()
//            bos.close()
//
//            getImage(bmp0!!)
//
//        } catch (e: IOException) {
//
//        }
//
//        startTime = (System.currentTimeMillis() / 1000).toInt()

        return 0
    }

    fun getRecordPath(): String? {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ), "cnx_publisher"
        )
        if (!mediaStorageDir.exists()) {
            if (!(mediaStorageDir.mkdirs() || mediaStorageDir.isDirectory)) {
                return ""
            }
        }
        return mediaStorageDir.path
    }

    private fun getImage(bitmap: Bitmap?) {
        lateinit var uri: Uri
        bitmap?.let {
            try {
                uri = Uri.fromFile(File(getRecordPath() + "/preview.png"))

            } catch (e: IOException) {
                Log.d("TESTING_STIGMA", e.message!!)
            }

            val data = workDataOf("snapshot" to uri.toString())
            val oneTimeWorkRequest =
                OneTimeWorkRequest.Builder(FileUploader::class.java).setInputData(data)
                    .build()
            WorkManager.getInstance(this@RTMPPublisherService).enqueue(oneTimeWorkRequest)

            Log.d("TESTING_STIGMA", "ENQUED TO SAVE")

//            WorkManager.getInstance(this@RTMPPublisherService)
//                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
//                .observe(this, Observer {
//                    it?.let {
//                        //saved
//                        Log.d("TESTING_STIGMA", "SAVED")
//                    }
//                })

        }

    }

}