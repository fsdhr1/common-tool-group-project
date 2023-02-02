package com.grandtech.orm.spatia.util

import com.google.gson.JsonObject

/**
 * @ClassName PropertyUtil
 * @Description TODO Feature属性转成Object
 * @Author: fs
 * @Date: 2021/8/24 9:34
 * @Version 2.0
 */
object  PropertyUtil {
    fun getFeatureAttributeAsObject(property: JsonObject?, attrName: String?): Any? {
        if (property == null || attrName == null || !property.has(attrName)) return null
        val jsonElement = property[attrName] ?: return null
        if (jsonElement.isJsonNull) return null
        if (jsonElement.isJsonPrimitive) {
            val jsonPrimitive = jsonElement.asJsonPrimitive
            if (jsonPrimitive.isString) {
                return jsonPrimitive.asString
            } else if (jsonPrimitive.isNumber) {
                return jsonPrimitive.asNumber
            } else if (jsonPrimitive.isBoolean) {
                return jsonPrimitive.asBoolean
            }
        }
        return null
    }
}