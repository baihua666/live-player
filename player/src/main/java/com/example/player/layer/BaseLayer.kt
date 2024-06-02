package com.example.player.layer


open class BaseLayer {

    public var layerId:Int = 0
    var order:Int = 0

    var scaleType: ScaleType = ScaleType.FIT_XY

    var visible = true

    var touchEnable = true

    var x: Int? = null
    var y: Int? = null
    var width: Int? = null
    var height: Int? = null
//    var scaleX: Float? = null
    var rotate: Int? = null

}