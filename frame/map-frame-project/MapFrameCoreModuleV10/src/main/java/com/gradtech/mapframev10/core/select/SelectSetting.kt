package com.gradtech.mapframev10.core.select

import com.gradtech.mapframev10.core.rules.Rules
import org.jetbrains.annotations.NotNull
import java.util.*

/**
 * @ClassName SelectSetting
 * @Description TODO
 * @Author: fs
 * @Date: 2022/9/8 15:22
 * @Version 2.0
 */
class SelectSetting:Rules {

    var layerIds: MutableList<String> = mutableListOf()

    private val maxSelectCount = 20

    private var isMulti = true

    /**
     * 是否优先选择
     */
    private var isPriority = false

    /**
     * 是否渲染结果集
     */
    private var isRender = true

    /**
     * 是否支持反选
     */
    private var canCancelSelected = true

     constructor(@NotNull layerIds: List<String>) {
        if (layerIds == null) {
            return
        }
        this.layerIds.addAll(layerIds)
    }

     constructor(@NotNull vararg layerIds: String) {
        if (layerIds == null) {
            return
        }
        for (layerId in layerIds) {
            this.layerIds.add(layerId)
        }

    }


     constructor(@NotNull isPriority: Boolean, @NotNull layerIds: List<String>) {
        if (layerIds == null) {
            return
        }
        this.isPriority = isPriority
        this.layerIds.addAll(layerIds)
    }

     constructor(@NotNull _isMulti: Boolean, @NotNull isPriority: Boolean, @NotNull vararg layerIds: String) {
        if (layerIds == null) {
            return
        }
        isMulti = _isMulti
        this.isPriority = isPriority
        for (layerId in layerIds) {
            this.layerIds.add(layerId)
        }

    }
    @JvmOverloads
    constructor(isMulti: Boolean, isRender: Boolean, isPriority: Boolean, layerIds: MutableList<String>?) {
        if (layerIds == null) {
            return
        }
        this.isMulti = isMulti
        this.isRender = isRender
        this.isPriority = isPriority
        this.layerIds.addAll(layerIds)
    }
    @JvmOverloads
     constructor(isMulti: Boolean, isRender: Boolean, isPriority: Boolean, @NotNull vararg layerIds: String) {
        if (layerIds == null) {
            return
        }
        this.isMulti = isMulti
        this.isRender = isRender
        this.isPriority = isPriority
        for (layerId in layerIds) {
            this.layerIds.add(layerId)
        }
    }

    @JvmOverloads
    constructor(isMulti: Boolean, isRender: Boolean, isPriority: Boolean, canCancelSelected :Boolean,layerIds: MutableList<String>?) {
        if (layerIds == null) {
            return
        }
        this.isMulti = isMulti
        this.isRender = isRender
        this.isPriority = isPriority
        this.canCancelSelected = canCancelSelected
        this.layerIds.addAll(layerIds)
    }
    @JvmOverloads
    constructor(isMulti: Boolean, isRender: Boolean, isPriority: Boolean,canCancelSelected :Boolean, @NotNull vararg layerIds: String) {
        if (layerIds == null) {
            return
        }
        this.isMulti = isMulti
        this.isRender = isRender
        this.isPriority = isPriority
        this.canCancelSelected = canCancelSelected
        for (layerId in layerIds) {
            this.layerIds.add(layerId)
        }
    }


     constructor(isRender: Boolean, isPriority: Boolean, layerIds: List<String>?) {
        if (layerIds == null) {
            return
        }
        this.isRender = isRender
        this.isPriority = isPriority
        this.layerIds.addAll(layerIds)
    }



    fun getLayersAsList(): List<String>? {
        return layerIds
    }

    fun isPriority(): Boolean {
        return isPriority
    }

    fun isRender(): Boolean {
        return isRender
    }

    fun isMulti(): Boolean {
        return isMulti
    }

    fun getMaxSelectCount(): Int {
        return maxSelectCount
    }
    fun canCancelSelected(): Boolean {
        return canCancelSelected
    }

}