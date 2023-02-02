/*
 * MIT License
 *
 * Copyright (c) 2020 Shreyas Patil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.grandtech.mapframe.orm.data.model

import androidx.room.*
import androidx.room.ColumnInfo.BINARY
import com.grandtech.mapframe.orm.data.model.CKDK.Companion.TABLE_NAME
import java.math.BigDecimal
import java.util.*

/**
 * Data class for Database entity and Serialization.
 */
@Entity(tableName = TABLE_NAME)
data class CKDK(

    @PrimaryKey

    var objectid: Int? = 0,
    @Ignore
    var shapes: String? = null,
    @TypeConverters(Converters::class)
    var shape:GEOMETRY? = null,
    var dkzlqhdm:String? = null,
    var dkbm:String? = null,
    var uuid:String? = null,
    var qhdm:String? = null,
    var edit_status:Int? = null,
    var dkmc:String? = null,
    var mj:Double? = null,
    var status:Int? = null,
    var dkzlqhmc:String? = null,
    var provincecode:String? = null,
    var nf:String? = null,
    var zh:String? = null,
    var bxgsdm:String? = null,
    var tilestatus:Int? = null,
    var zdxlh:String? = null,
    var dkzb:String? = null,
    var updatetime:Long? = null,
    var datagroup:Int? = null,
    var bxgsmc:String? = null,
    var qhmc:String? = null,
    var szzb:String? = null,
    var citycode:String? = null,
    var dkzlqtqh:String? = null,
    var countycode:String? = null,
    var datasource:Int? = null,
    var bz:String? = null
) {
    companion object {
        const val TABLE_NAME = "dt_cbdk"
    }
}