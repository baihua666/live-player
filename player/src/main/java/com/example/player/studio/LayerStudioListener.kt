package com.example.player.studio

import android.graphics.Bitmap
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

interface LayerStudioListener {
     fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)

     fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)

//     only debug
     fun onDrawFrame(bitmap: Bitmap)

    //    jni环境创建完成，可以调用jni方法，还不能调用依赖opengl沉浸相关的API
//    open fun onJniCreate() {}

//    底层渲染初始化完成，可以调用设置背景等渲染相关的API
//    fun onEGLSurfaceCreated()
}