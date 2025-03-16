package com.example.liveplayer

import android.graphics.BitmapFactory
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.liveplayer.layer.LayerManager
import com.example.player.layer.BaseLayerImpl
import com.example.player.studio.LayerStudioListener
import java.util.Timer
import java.util.TimerTask
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity(), LayerStudioListener {

    private lateinit var glSurfaceView: GLSurfaceView

    private lateinit var layerManager: LayerManager
    private lateinit var actionView:FrameLayout
    private var testLayer: BaseLayerImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initPlayer()
    }

    private fun startTest() {
        testRotation()
    }

    private fun testVideo() {
        addVideoLayer()
    }

    private fun testRotation() {
        addImageLayer()
//        startTimer()
    }

//    @RequiresApi(Build.VERSION_CODES.O)
    private fun initPlayer() {
        layerManager = LayerManager()
        layerManager.setListener(this)

        glSurfaceView = findViewById(R.id.glSurfaceView)
        actionView = findViewById(R.id.layer_studio_view)
        layerManager.startPreview(this, glSurfaceView, actionView)

        val color:Color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Color.valueOf(1.0f, 0.0f, 0.0f, 0.1f)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        layerManager.setBackgroundColor(color)

    }

    private fun addVideoLayer() {
        var filePath: String = FileUtil.copyAssetFileToCache(this, "test1.mp4")
        var layer = layerManager.addVideoLayer(filePath)
        layerManager.showActionView(layer!!)

//        filePath = FileUtil.copyAssetFileToCache(this, "green_video.mp4")
        filePath = FileUtil.copyAssetFileToCache(this, "green_test1.mp4")

        layer = layerManager.addVideoLayer(filePath)
        layer?.enableMattingGreen = true
    }

    private fun addImageLayer() {
        val bitmap = BitmapFactory.decodeResource(
            resources, R.drawable.test
        )
        testLayer = layerManager.addBitmapLayer(bitmap)
        testLayer?.color = Color.argb(255, 0, 250, 0)
        testLayer?.rotate = 10
        testLayer?.x = 50.0f
        testLayer?.y = 100.0f
        testLayer?.let {
            layerManager.showActionView(it)
        }

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        startTest()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    fun startTimer() {
        testLayer?.rotate = 0
        Timer().schedule(object : TimerTask() {
            override fun run() {
                var rotate = testLayer!!.rotate!!.plus(10)
                if (rotate >= 360) {
                    rotate = 0
                }
                testLayer?.updateRotation(rotate)
            }
        }, 100, 5000)
    }
}