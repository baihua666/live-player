package com.example.player.studio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.util.Size
import com.example.player.layer.BaseLayer
import com.example.player.layer.BaseLayerImpl
import com.example.player.layer.BitmapLayer
import com.example.player.layer.ImageLayer
import com.example.player.layer.VideoLayer
import com.example.player.view.LayerActionLayout


interface LayerStudio {

    companion object {
        fun create(): LayerStudio {
            return LayerStudioImpl()
        }
    }


    fun setListener(listener: LayerStudioListener)

    fun startPreview(context: Context, surfaceView: GLSurfaceView, viewPortSize: Size, actionView: LayerActionLayout?)
//
    fun stopPreview()
//
//    /**
//     * 设置预览显示类型
//     * @param renderType FIX_XY 显示整个屏幕,同时裁剪显示内容, CROP按原比例显示,同时上下留黑
//     */
//    open fun setRenderType(renderType: RenderType)
//
//    /**
//     * 设置画幅
//     * 目前画幅有: 垂直 横向 1:1方形
//     */
//    open fun setFrame(frame: Frame)
//
    fun setBackgroundColor(color: Color)
//
//    open fun setBackgroundImage(filePath: String?)
//

    fun releaseLayer(layer: BaseLayerImpl)

    fun showActionView(layer: BaseLayerImpl)

    fun addImageLayer(filePath: String) : ImageLayer?

    fun addBitmapLayer(bitmap: Bitmap) : BitmapLayer?
//
    fun addVideoLayer(filePath: String) : VideoLayer?
//
//    open fun addCameraLayer() : CameraLayer?

    fun getLayerList(): List<BaseLayer>

    fun getViewPortWidth(): Int

    fun getViewPortHeight(): Int

    fun setViewPortSize(width: Int, height: Int)
}