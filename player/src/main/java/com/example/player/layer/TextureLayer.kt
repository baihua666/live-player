package com.example.player.layer

import com.example.player.gles.Drawable2dTarget
import com.example.player.gles.Texture2dProgram

open class TextureLayer : BaseLayerImpl() {


    public var targetDrawable: Drawable2dTarget? = null

    fun draw(program: Texture2dProgram, projectionMatrix: FloatArray ) {
        if (targetDrawable == null || targetDrawable!!.textureId < 0) {
            return
        }
        targetDrawable!!.draw(program, projectionMatrix)
    }
}