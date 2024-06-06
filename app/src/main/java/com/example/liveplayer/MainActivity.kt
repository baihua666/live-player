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
import com.example.player.studio.LayerStudioListener
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity(), LayerStudioListener {

    private lateinit var glSurfaceView: GLSurfaceView

    private lateinit var layerManager: LayerManager
    private lateinit var actionView:FrameLayout

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

        filePath = FileUtil.copyAssetFileToCache(this, "green_video.mp4")
        layer = layerManager.addVideoLayer(filePath)
        layer?.enableMattingGreen = true
    }

    private fun addImageLayer() {
        val bitmap = BitmapFactory.decodeResource(
            resources, R.drawable.ic_theme_play_arrow
        )
        var layer = layerManager.addBitmapLayer(bitmap)
        layer?.let {
            layerManager.showActionView(it)
        }

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//            addVideoLayer()
            addImageLayer()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }
}