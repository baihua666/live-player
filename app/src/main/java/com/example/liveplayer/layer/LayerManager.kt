package com.example.liveplayer.layer

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Size
import com.example.player.layer.VideoLayer
import com.example.player.studio.LayerStudio
import com.example.player.studio.LayerStudioListener

class LayerManager {
    public var outputSize: Size? = null

    private val layerStudio: LayerStudio = LayerStudio.create()

    fun setListener(listener: LayerStudioListener) {
        layerStudio.setListener(listener)
    }

    public fun startPreview(surfaceView: GLSurfaceView, context: Context) {
        layerStudio.startPreview(surfaceView, context)
    }

    public fun stopPreview() {

    }

    fun addVideoLayer(filePath: String): VideoLayer? {
        return layerStudio.addVideoLayer(filePath)
    }

}