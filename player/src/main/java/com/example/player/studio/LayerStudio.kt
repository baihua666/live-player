package com.example.player.studio

import android.content.Context
import android.opengl.GLSurfaceView
import com.example.player.layer.VideoLayer


interface LayerStudio {

    companion object {
        fun create(): LayerStudio {
            return LayerStudioImpl()
        }
    }


    open fun setListener(listener: LayerStudioListener)

    open fun startPreview(surfaceView: GLSurfaceView, context: Context)
//
//    open fun stopPreview()
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
//    open fun setBackgroundColor(red: Int, green: Int, blue: Int)
//
//    open fun setBackgroundImage(filePath: String?)
//
//    open fun addImageLayer(layer: ImageLayer)
//
    fun addVideoLayer(filePath: String) : VideoLayer?
//
//    open fun addCameraLayer() : CameraLayer?
}