package com.example.player.studio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.Size
import com.example.player.BuildConfig
import com.example.player.gles.GlUtil
import com.example.player.layer.BaseLayer
import com.example.player.layer.BaseLayerImpl
import com.example.player.layer.BitmapLayer
import com.example.player.layer.ImageLayer
import com.example.player.layer.TextureLayer
import com.example.player.layer.VideoLayer
import com.example.player.view.LayerActionLayout
import com.tencent.mars.xlog.Log
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class LayerStudioImpl : LayerStudio, GLSurfaceView.Renderer {
    private val TAG = "[LayerStudioImpl]"


    private var listener: LayerStudioListener? = null

    private var render: LayerRender? = null

    public var context: Context? = null

    private var layerList: MutableList<BaseLayer> = mutableListOf()
    private var needSourLayerList = false

    private var previewWidth = 0
    private var previewHeight = 0

    private var actionView: LayerActionLayout? = null

    private var mainHandler: Handler? = null

    private var viewPortWidth = 0
    private var viewPortHeight = 0

    init {
        if (BuildConfig.DEBUG) {
            Log.setLevel(Log.LEVEL_DEBUG, false)
        }
    }

    private fun sortLayerList() {
        if (!needSourLayerList) {
            return
        }
//        从小到大排序
        layerList.sortBy { it.order }
        needSourLayerList = false
    }

    private fun addLayerToList(layer: BaseLayer) {
        synchronized(layerList) {
            layerList.add(layer)
        }

        needSourLayerList = true
        sortLayerList()

    }

    override fun setViewPortSize(width: Int, height: Int) {
        viewPortWidth = width
        viewPortHeight = height
    }

    override fun getViewPortWidth(): Int {
        return viewPortWidth
    }

    override fun getViewPortHeight(): Int {
        return viewPortHeight
    }

    override fun setListener(listener: LayerStudioListener) {
        this.listener = listener
    }

    override fun getLayerList(): List<BaseLayer> {
        sortLayerList()
        return layerList
    }

    override fun startPreview(context: Context, surfaceView: GLSurfaceView, viewPortSize: Size,
                              actionView: LayerActionLayout?) {
        this.context = context
        mainHandler = Handler(Looper.getMainLooper())

        setViewPortSize(viewPortSize.width, viewPortSize.height)

        render = LayerRender()
        render?.listener = this
        render?.startPreview(surfaceView)

        actionView?.layerStudio = this
        this.actionView = actionView
//        actionView?.let {
//            this.actionView = LayerActionLayout(actionView)
//            this.actionView!!.listener = layerActionListener
//        }
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
        var layer:VideoLayer? = null
        layer = object : VideoLayer() {
            override fun onContentRectReady() {
                super.onContentRectReady()
                //仍然在显示当前layer，没有被其它layer覆盖
                if (actionView?.actionViewLayer() == layer) {
                    showActionView(layer!!)
                }
            }
        }
        layer.filePath = filePath
        addVideoPlayer(layer)
        addLayerToList(layer)

        return layer
    }

    override fun addBitmapLayer(bitmap: Bitmap) : BitmapLayer? {
        val layer = BitmapLayer()
        layer.updateSize(bitmap.width.toFloat(), bitmap.height.toFloat())
        //放在中间位置
        layer.updateCenterPosition(previewWidth / 2.0f, previewHeight / 2.0f)
        layer.updateCenterPosition(bitmap.width / 2.0f, previewHeight / 2.0f)
        runOnGLThread {
            layer.configure(bitmap)
        }

        layer.setOnTextureLayerListener(object : TextureLayer.OnTextureLayerListener {
            override fun onModelViewMatrixChanged(matrix16Points: FloatArray) {
                Log.d(TAG, "onModelViewMatrixChanged")
                //如果有TouchView，则更新TouchView的位置
                if (actionView?.touchViewLayer() == layer) {
                    actionView?.touchView?.setModelViewMatrix(matrix16Points)
                }
                else {
                    Log.d(TAG, "onModelViewMatrixChanged, no touch view")
                }
            }
        })

        addLayerToList(layer)
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

        addLayerToList(layer)
        return layer
    }

    override fun showActionView(layer: BaseLayerImpl) {
        runOnMainThread {
            actionView?.showActionView(layer)
            if (layer.contentRectReady()) {

            }
            else {
                assert(layer is VideoLayer) { "Content rect not ready: $layer" }

            }

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
            sortLayerList()
            for (layer in layerList) {
                if (!layer.visible) {
                    continue
                }
                if (layer is TextureLayer) {
                    drawTextureLayer(layer)
                }
            }
            readPixels()
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

    private fun readPixels() {
        val width: Int = previewWidth
        val height: Int = previewHeight
        val buf = ByteBuffer.allocateDirect(width * height * 4)
        buf.order(ByteOrder.LITTLE_ENDIAN)

        val startWhen = System.currentTimeMillis()
        // Try to ensure that rendering has finished.
        GLES20.glFinish()
        GLES20.glReadPixels(
            0, 0, width, height,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf
        )
        // Time individual extraction.  Ideally we'd be timing a bunch of these calls
        // and measuring the aggregate time, but we want the isolated time, and if we
        // just read the same buffer repeatedly we might get some sort of cache effect.


        var totalTime = System.currentTimeMillis() - startWhen
//        android.util.Log.d("glReadPixels:", "ms:" + totalTime)

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bmp.copyPixelsFromBuffer(buf)
        bmp.recycle()

        GlUtil.checkGlError("glReadPixels")
        buf.rewind()


    }
}