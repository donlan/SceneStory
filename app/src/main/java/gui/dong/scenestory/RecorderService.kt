package gui.dong.scenestory

import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Binder
import android.os.Environment
import android.os.HandlerThread
import android.os.IBinder

import java.io.File
import java.io.IOException

class RecorderService : Service() {
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null

    var isRunning: Boolean = false
        private set
    private var width = 720
    private var height = 1080
    private var dpi: Int = 0
    var savePath: String? = null
        private set


    override fun onBind(intent: Intent): IBinder? {
        return RecordBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val serviceThread = HandlerThread("service_thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND)
        serviceThread.start()
        isRunning = false
        //Android提供的屏幕录制功能
        mediaRecorder = MediaRecorder()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun setMediaProject(project: MediaProjection) {
        mediaProjection = project
    }

    fun setConfig(width: Int, height: Int, dpi: Int) {
        this.width = width
        this.height = height
        this.dpi = dpi
    }

    /**
     * 开始录制
     * @return
     */
    fun startRecord(): Boolean {
        if (mediaProjection == null || isRunning) {
            return false
        }

        initRecorder()
        createVirtualDisplay()
        mediaRecorder!!.start()
        isRunning = true
        return true
    }

    /**
     * 结束录制
     * @return
     */
    fun stopRecord(): Boolean {
        if (!isRunning) {
            return false
        }
        isRunning = false
        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
        virtualDisplay!!.release()
        mediaProjection!!.stop()
        return true
    }

    private fun createVirtualDisplay() {
        virtualDisplay = mediaProjection!!.createVirtualDisplay("MainScreen", width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder!!.surface, null, null)
    }

    private fun initRecorder() {
        //初始化录制参数

        //录制视频保存的地址
        savePath = saveDirectory + System.currentTimeMillis() + ".mp4"
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setOutputFile(savePath)
        //宽高
        mediaRecorder!!.setVideoSize(width, height)
        //视频编码
        mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        //音频编码
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder!!.setVideoEncodingBitRate(5 * 1024 * 1024)
        //帧率
        mediaRecorder!!.setVideoFrameRate(30)
        try {
            mediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    inner class RecordBinder : Binder() {
        val recordService: RecorderService
            get() = this@RecorderService
    }

    companion object {

        /**
         *
         * @return 视频保存的目录
         */
        val saveDirectory: String?
            get() {
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    val rootDir = Environment.getExternalStorageDirectory().absolutePath + "/" + "story" + "/"

                    val file = File(rootDir)
                    if (!file.exists()) {
                        if (!file.mkdirs()) {
                            return null
                        }
                    }
                    return rootDir
                } else {
                    return null
                }
            }
    }
}
