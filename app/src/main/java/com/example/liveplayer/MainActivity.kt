package com.example.liveplayer

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.liveplayer.layer.LayerManager
import com.example.player.studio.LayerStudioListener
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity(), LayerStudioListener {

    private lateinit var mGLSurfaceView: GLSurfaceView

    private lateinit var layerManager: LayerManager


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

    private fun initPlayer() {
        layerManager = LayerManager()
        layerManager.setListener(this)

        mGLSurfaceView = findViewById(R.id.glSurfaceView)
        layerManager.startPreview(mGLSurfaceView, this)
    }

    private fun addVideoLayer() {
        var filePath: String = FileUtil.copyAssetFileToCache(this, "test1.mp4")
        var layer = layerManager.addVideoLayer(filePath)

        filePath = FileUtil.copyAssetFileToCache(this, "green_video.mp4")
        layer = layerManager.addVideoLayer(filePath)
        layer?.enableMattingGreen = true
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        addVideoLayer()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }
}