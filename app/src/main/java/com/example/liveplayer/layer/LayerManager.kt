package com.example.liveplayer.layer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.util.Size
import android.widget.FrameLayout
import com.example.player.layer.BaseLayerImpl
import com.example.player.layer.BitmapLayer
import com.example.player.layer.VideoLayer
import com.example.player.studio.LayerStudio
import com.example.player.studio.LayerStudioListener

class LayerManager {
    public var outputSize: Size? = null

    private val layerStudio: LayerStudio = LayerStudio.create()

    fun setListener(listener: LayerStudioListener) {
        layerStudio.setListener(listener)
    }

    public fun startPreview(context: Context, surfaceView: GLSurfaceView, actionView: FrameLayout?) {
        layerStudio.startPreview(context, surfaceView, actionView)
    }

    fun setBackgroundColor(color: Color) {
        layerStudio.setBackgroundColor(color)
    }

    public fun stopPreview() {

    }

    fun showActionView(layer: BaseLayerImpl) {
        layerStudio.showActionView(layer)
    }

    fun addBitmapLayer(bitmap: Bitmap) : BitmapLayer? {
        return layerStudio.addBitmapLayer(bitmap)
    }

    fun addVideoLayer(filePath: String): VideoLayer? {
        return layerStudio.addVideoLayer(filePath)
    }

}