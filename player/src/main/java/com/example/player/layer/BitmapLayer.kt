package com.example.player.layer

import android.graphics.Bitmap
import com.example.player.gles.Drawable2d
import com.example.player.gles.Drawable2dTarget
import com.example.player.gles.GlUtil


open class BitmapLayer : TextureLayer() {

    fun configure(bitmap: Bitmap) {
        val drawable2d = Drawable2d(Drawable2d.Prefab.RECTANGLE)
        val mImageTexture: Int = GlUtil.createImageTexture(bitmap, color ?: -1)
        targetDrawable = Drawable2dTarget(drawable2d)
//        targetDrawable?.setColor(0.9f, 0.1f, 0.1f)
        targetDrawable?.setTexture(mImageTexture)

        updateDrawable()
    }
}