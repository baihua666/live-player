package com.example.player.studio

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.player.gles.GlUtil
import com.example.player.layer.BaseLayer
import com.example.player.layer.VideoLayer
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

    override fun setListener(listener: LayerStudioListener) {
        this.listener = listener
    }

    override fun startPreview(surfaceView: GLSurfaceView, context: Context) {
        this.context = context
        render = LayerRender()
        render?.listener = this
        render?.startPreview(surfaceView)
    }

//    LayerStudio
    override fun addVideoLayer(filePath: String): VideoLayer? {
        if (!File(filePath).exists()) {
            assert(false) { "File not found: $filePath" }
            return null
        }
        val layer = VideoLayer()
        layer.filePath = filePath
        addVideoPlayer(layer)
        layerList.add(layer)
        return layer
    }

//end of LayerStudio

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
        for (layer in layerList) {
            if (!layer.visible) {
                continue
            }
            if (layer is VideoLayer) {
                drawVideoLayer(layer)
            }
        }

    }
//    End of GLSurfaceView.Renderer

    private fun addVideoPlayer(videoLayer: VideoLayer) {
        videoLayer.addPlayer(context!!, previewWidth, previewHeight)


    }

    private fun drawVideoLayer(videoLayer: VideoLayer) {
        videoLayer.targetDrawable?.let {
            if (render == null) {
                return
            }
            render!!.drawDrawable2DTarget(it)
        }
    }
}