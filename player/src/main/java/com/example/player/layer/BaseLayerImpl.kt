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
        this.x = obj.getDouble("x").toFloat()
        this.y = obj.getDouble("y").toFloat()
        this.width = obj.getDouble("width").toFloat()
        this.height = obj.getDouble("height").toFloat()
    }

    open fun stop() {

    }

    fun contentRectReady() : Boolean {
        return width != null && height != null && x != null && y != null
    }

    fun checkContentRectReady() {
        if (width != null && height != null && x != null && y != null) {
            onContentRectReady()
        }
    }

    open fun onContentRectReady() {

    }

    internal open fun updatePosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    internal open fun move(dx: Int, dy: Int) {
        if (x == null || y == null) {
            return
        }

        updatePosition(x!! + dx, y!! + dy)
    }

    internal open fun updateSize(layerWith: Float, layerHeight: Float) {
        width = layerWith
        height = layerHeight
    }

    open fun updateRotation(rotate: Float) {
        this.rotate = rotate
    }
}