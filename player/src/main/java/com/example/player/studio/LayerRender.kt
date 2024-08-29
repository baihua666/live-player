package com.example.player.studio

import android.graphics.Color
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.player.gles.Drawable2dTarget
import com.example.player.gles.GlUtil
import com.example.player.gles.Texture2dProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LayerRender: GLSurfaceView.Renderer {
    var surfaceView: GLSurfaceView? = null

    public var listener: GLSurfaceView.Renderer? = null

    private var texProgram: Texture2dProgram? = null
    private val displayProjectionMatrix = FloatArray(16)

    public var backgroundColor: Color? = null

    fun startPreview(surfaceView: GLSurfaceView) {
        this.surfaceView = surfaceView
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setRenderer(this)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        surfaceView.holder.setFormat(PixelFormat.RGBA_8888)
//        surfaceView.setZOrderOnTop(true)
        surfaceView.setBackgroundColor(Color.TRANSPARENT)

        surfaceView.requestRender()
    }

    fun stopPreview() {
        surfaceView?.queueEvent {
            surfaceView?.setRenderer(null)
            surfaceView?.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
    }


    fun drawDrawable2DTarget(target: Drawable2dTarget) {
        target.draw(texProgram!!, displayProjectionMatrix)
    }


    private fun prepareGl() {
        texProgram = Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D)

        // Set the background color.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Disable depth testing -- we're 2D only.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)


        // Don't need backface culling.  (If you're feeling pedantic, you can turn it on to
        // make sure we're defining our shapes correctly.)
        GLES20.glDisable(GLES20.GL_CULL_FACE)

    }

    private fun updateGL(width: Int, height: Int) {
        // Use full window.

        GLES20.glViewport(0, 0, width, height)

        // Simple orthographic projection, with (0,0) in lower-left corner.
        Matrix.orthoM(
            displayProjectionMatrix,
            0,
            0f,
            width.toFloat(),
            0f,
            height.toFloat(),
            -1f,
            1f
        )

        //        int smallDim = Math.min(width, height);
//        mTargetImage.setPosition(width / 2.0f - 100, height / 2.0f + 100)
//        mTargetVideo0.setPosition(width / 2.0f, height / 2.0f)
//        mTargetVideo1.setPosition(width / 2.0f, height / 2.0f)
    }

    //    GLSurfaceView.Renderer
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        prepareGl()
        listener?.onSurfaceCreated(gl, config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        updateGL(width, height)
        listener?.onSurfaceChanged(gl, width, height)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDrawFrame(gl: GL10?) {
        GlUtil.checkGlError("draw start")


        // Clear to a non-black color to make the content easily differentiable from
        // the pillar-/letter-boxing.
        if (backgroundColor != null) {
            GLES20.glClearColor(backgroundColor!!.red(), backgroundColor!!.green(), backgroundColor!!.blue(), backgroundColor!!.alpha())
        }
        else {
            GLES20.glClearColor(0f, 0f, 0f, 1.0f)
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)


        // Textures may include alpha, so turn blending on.
//        debug
//        if (BuildConfig.DEBUG) {
//
//        }
//        else
//        {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//        }


        listener?.onDrawFrame(gl)

        GLES20.glDisable(GLES20.GL_BLEND)

        GlUtil.checkGlError("draw done")
    }
//    end GLSurfaceView.Renderer
}