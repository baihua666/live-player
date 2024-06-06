package com.example.player.layer

import android.graphics.BitmapFactory


class ImageLayer : BitmapLayer() {
    var filePath:String? = null

    fun configure() {
        if (filePath == null) {
            return
        }
        val bmOptions = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeFile(filePath, bmOptions)

        configure(bitmap)
    }
}