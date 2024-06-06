package com.example.player.studio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import com.example.player.BuildConfig
import com.example.player.layer.BaseLayer
import com.example.player.layer.BaseLayerImpl
import com.example.player.layer.BitmapLayer
import com.example.player.layer.ImageLayer
import com.example.player.layer.TextureLayer
import com.example.player.layer.VideoLayer
import com.example.player.view.LayerActionLayout
import com.tencent.mars.xlog.Log
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class LayerStudioImpl : LayerStudio, GLSurfaceView.Renderer {
    private val TAG = "[LayerStudioImpl]"


    private var listener: LayerStudioListener? = null

    private var render: LayerRender? = null

    public var context: Context? = null

    private var layerList: MutableList<BaseLayer> = mutableListOf()

    private var previewWidth = 0
    private var previewHeight = 0

    private var actionView: LayerActionLayout? = null

    private var mainHandler: Handler? = null

    init {
        if (BuildConfig.DEBUG) {
            Log.setLevel(Log.LEVEL_DEBUG, false)
        }
    }

    override fun setListener(listener: LayerStudioListener) {
        this.listener = listener
    }

    override fun startPreview(context: Context, surfaceView: GLSurfaceView, actionView: FrameLayout?) {
        this.context = context
        mainHandler = Handler(Looper.getMainLooper())

        render = LayerRender()
        render?.listener = this
        render?.startPreview(surfaceView)

        actionView?.let {
            this.actionView = LayerActionLayout(actionView)
            this.actionView!!.listener = layerActionListener
        }
    }

    override fun stopPreview() {
        render?.stopPreview()
    }

//    LayerStudio

    override fun releaseLayer(layer: BaseLayerImpl) {
        synchronized(layerList) {
            if (layerList.contains(layer)) {
                layerList.remove(layer)
            }
            else {
                Log.e(TAG, "releaseLayer: layer not found: $layer")
                assert(false) { "Layer not found: $layer" }
            }

        }
    }

    override fun addVideoLayer(filePath: String): VideoLayer? {
        if (!File(filePath).exists()) {
            assert(false) { "File not found: $filePath" }
            return null
        }
        val layer = VideoLayer()
        layer.filePath = filePath
        addVideoPlayer(layer)
        synchronized(layerList) {
            layerList.add(layer)
        }

        return layer
    }

    override fun addBitmapLayer(bitmap: Bitmap) : BitmapLayer? {
        val layer = BitmapLayer()
        runOnGLThread {
            layer.configure(bitmap)
            layer.updateCenterPosition(previewWidth / 2, previewHeight / 2)
        }

        synchronized(layerList) {
            layerList.add(layer)
        }
        return layer
    }

    override fun addImageLayer(filePath: String) : ImageLayer?  {
        if (!File(filePath).exists()) {
            assert(false) { "File not found: $filePath" }
            return null
        }
        val layer = ImageLayer()
        layer.filePath = filePath
        runOnGLThread {
            layer.configure()
        }

        synchronized(layerList) {
            layerList.add(layer)
        }
        return layer
    }

    override fun showActionView(layer: BaseLayerImpl) {
        runOnMainThread {
            actionView?.showActionView(layer)
        }
    }

    override fun setBackgroundColor(color: Color) {
        render?.backgroundColor = color
    }

//end of LayerStudio

    private fun runOnMainThread(runnable: Runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            runnable.run()
        }
        else {
            mainHandler?.post(runnable)
        }
    }

    private fun runOnGLThread(runnable: Runnable) {
        render?.surfaceView?.queueEvent(runnable)
    }


    private val layerActionListener: LayerActionLayout.LayerActionViewListener = object :
        LayerActionLayout.LayerActionViewListener {
        override fun onLayerActionViewDelete(layer: BaseLayerImpl) {
//            jni.removeLayer(mHandle, layer.layerId)
            Log.d(TAG, "onLayerActionViewDelete: $layer")
            releaseLayer(layer)
        }

        override fun onLayerActionViewMove(layer: BaseLayerImpl) {
//            jni.updateLayerMatrix(mHandle, JSON.toJSONString(layer))
            Log.d(TAG, "onLayerActionViewMove: $layer")
        }

        override fun onLayerActionViewRotate(layer: BaseLayerImpl, degrees: Float, px: Float, py: Float
        ) {
//            jni.updateLayerRotate(mHandle, layer.layerId, degrees.toInt(), px.toInt(), py.toInt())
            Log.d(TAG, "onLayerActionViewRotate: $layer")

        }
    };

    //    GLSurfaceView.Renderer
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        previewWidth = render?.surfaceView?.width ?: 0
        previewHeight = render?.surfaceView?.height ?: 0

        listener?.onSurfaceCreated(gl, config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        previewWidth = width
        previewHeight = height
        listener?.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (render == null) {
            return
        }
        synchronized(layerList) {
            for (layer in layerList) {
                if (!layer.visible) {
                    continue
                }
                if (layer is TextureLayer) {
                    drawTextureLayer(layer)
                }
            }
        }


    }
//    End of GLSurfaceView.Renderer

    private fun addVideoPlayer(videoLayer: VideoLayer) {
        runOnGLThread {
            videoLayer.addPlayer(context!!, previewWidth, previewHeight)
        }
    }

    private fun drawTextureLayer(videoLayer: TextureLayer) {
        videoLayer.targetDrawable?.let {
            if (render == null) {
                return
            }
            render!!.drawDrawable2DTarget(it)
        }
    }
}