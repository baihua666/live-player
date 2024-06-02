package com.example.player.layer

import org.json.JSONObject

open class BaseLayerImpl : BaseLayer() {

//    var inEdit = false

    private var propertiesJson: String? = null

    //如果不考虑旋转，用坐标加宽高就可以同步上层操作和底层渲染，如果需要考虑旋转，需要同步矩阵
    var matrixPoints = FloatArray(9)

    fun updatePropertiesJson(json: String) {
        propertiesJson = json
        val obj = JSONObject(json)
        this.x = obj.getInt("x")
        this.y = obj.getInt("y")
        this.width = obj.getInt("width")
        this.height = obj.getInt("height")
    }

    open fun stop() {

    }
}