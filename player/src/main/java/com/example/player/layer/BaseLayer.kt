package com.example.player.layer


open class BaseLayer {

    public var layerId:Int = 0
    var order:Int = 0

    var scaleType: ScaleType = ScaleType.FIT_XY

    var visible = true

    var touchEnable = true

    var x: Float? = null
    var y: Float? = null
    var width: Float? = null
    var height: Float? = null
    var rotate: Float? = null

    var color: Int? = null

    fun getCenterX(): Float {
        return x!! + width!! / 2
    }

    fun getCenterY(): Float {
        return y!! + height!! / 2
    }
}