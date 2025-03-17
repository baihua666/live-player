package com.example.liveplayer

import android.graphics.BitmapFactory
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.GridView
import android.widget.SimpleAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.liveplayer.layer.LayerManager
import com.example.player.layer.BaseLayerImpl
import com.example.player.studio.LayerStudioListener
import com.example.player.view.LayerActionLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity(), LayerStudioListener {

    private lateinit var glSurfaceView: GLSurfaceView

    private lateinit var layerManager: LayerManager
    private lateinit var actionView: LayerActionLayout
    private var testLayer: BaseLayerImpl? = null
    private var isReady: Boolean = false
    private lateinit var gridView: GridView
    private lateinit var adapter: SimpleAdapter

    private lateinit var gridLayout: GridLayout


    private val dataList = ArrayList<Map<String, Any>>()

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
        GlobalScope.launch(Dispatchers.Main) {
            addTestButtons()
        }
    }

    private fun addTestButtons() {
        var hashMap : HashMap<String, Any>
                = HashMap()
        hashMap["text"] = "Video"
        hashMap["action"] = { testVideo() }
        dataList.add(hashMap)

        hashMap= HashMap()
        hashMap["text"] = "Image"
        hashMap["action"] = { testImage() }
        dataList.add(hashMap)

        hashMap= HashMap()
        hashMap["text"] = "Rotation"
        hashMap["action"] = { testRotation() }
        dataList.add(hashMap)

        gridLayout = findViewById(R.id.gl_test_view)
        val columnCount = 3
        for (i in dataList.indices) {
            val bn = Button(this)
            bn.text = dataList[i]["text"] as String
            bn.setOnClickListener {
                (dataList[i]["action"] as Function0<*>).invoke()
            }
            bn.setPadding(5, 35, 5, 35)
            val rowSpec = GridLayout.spec(i / columnCount + 2)
            val columSpace = GridLayout.spec(i % columnCount)
            val  params = GridLayout.LayoutParams(rowSpec, columSpace)
            params.setGravity(Gravity.LEFT)
            gridLayout.addView(bn, params)
        }
    }

    private fun testVideo() {
        addVideoLayer()
    }

    private fun testImage() {
        addImageLayer()
    }

    private fun testRotation() {
        addImageLayer()
        startTimer()
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
        testLayer?.rotate = 10f
        testLayer?.x = 50.0f
        testLayer?.y = 100.0f
        testLayer?.let {
            layerManager.showActionView(it)
        }

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        isReady = true
        startTest()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    fun startTimer() {
        testLayer?.rotate = 0f
        Timer().schedule(object : TimerTask() {
            override fun run() {
                var rotate = testLayer!!.rotate!!.plus(10f)
                if (rotate >= 360) {
                    rotate = 0f
                }
                testLayer?.updateRotation(rotate)
            }
        }, 100, 5000)
    }
}