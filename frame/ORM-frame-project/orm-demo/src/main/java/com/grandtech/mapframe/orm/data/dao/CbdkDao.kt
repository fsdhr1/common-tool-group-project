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

package com.grandtech.mapframe.orm.data.dao

import androidx.room.*
import com.grandtech.mapframe.orm.data.model.CKDK
import com.grandtech.mapframe.orm.data.model.Post
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object (DAO) for [dev.shreyaspatil.foodium.data.local.FoodiumPostsDatabase]
 */
@Dao
interface CbdkDao {


    /**
     * Get Spatia lite version
     */
    @Query("SELECT spatialite_version()")
    @SkipQueryVerification
    fun getSpatiaVersion(): String

    @Query("SELECT ASText(shape) as shapes from dt_cbdk a  limit 1")
    @SkipQueryVerification
    fun getShape(): CKDK

    /**
     * Get Proj4 version
     */
    @Query("SELECT proj4_version()")
    @SkipQueryVerification
    fun getProj4Version(): String

    /**
     * Get Geos version
     */
    @Query("SELECT geos_version()")
    @SkipQueryVerification
    fun getGeosVersion(): String

    /**
     * Create a polyline from String and return String
     */
    @Query("""
        SELECT ASText( 
            MakePolygon(
                GeomFromText('LINESTRING(0 0, 100 0, 100 100, 0 100, 0 0)') 
            ) 
        ) as line
    """)
    @SkipQueryVerification
    fun getMakePolyline(): String

    /**
     * Get distance between two points in meters
     */
    @Query("""
        SELECT ST_Distance(
            Transform(MakePoint(-72.1235, 42.3521, 4326), 26986),
            Transform(MakePoint(-71.1235, 42.1521, 4326), 26986)
        ) as distance
    """)
    @SkipQueryVerification
    fun getDistance(): Double



}