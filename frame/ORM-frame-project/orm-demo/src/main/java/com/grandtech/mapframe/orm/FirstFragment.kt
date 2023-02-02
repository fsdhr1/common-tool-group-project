package com.grandtech.mapframe.orm

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.grandtech.mapframe.orm.data.SpatiaDatabase
import com.grandtech.mapframe.orm.data.SpatiaEncryptionDatabase
import com.grandtech.mapframe.orm.data.model.Post
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Polygon
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableSubscriber
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Cancellable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_first.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var flag = activity?.intent?.getIntExtra("flag",0);


        if(flag == 0){
            var appDatabase = SpatiaDatabase.getInstance(requireContext())
            AsyncTask.execute(Runnable {

                val post1 = Post(1, "prueba", "darwin", "spatia", "test.img")
                val post2 = Post(2, "prueba2", null, "spatia2", "test2.img")

                val listPost = listOf(post1,post2)

                appDatabase.getPostsDao().insertPosts(listPost)

                val spatiaVersion = appDatabase.getPostsDao().getSpatiaVersion()
                val proj4Version = appDatabase.getPostsDao().getProj4Version()
                val geosVersion = appDatabase.getPostsDao().getGeosVersion()
                val makePoliline = appDatabase.getPostsDao().getMakePolyline()
                val distance = appDatabase.getPostsDao().getDistance()
                val post = appDatabase.getPostsDao().getAllPosts()

                activity?.runOnUiThread(Runnable {
                    spatia_version.text = spatiaVersion
                    proj4_version.text = proj4Version
                    geos_version.text = geosVersion
                    polyline_txt.text = makePoliline
                    distance_txt.text = distance.toString()
                })


            })
        }
        if(flag == 1){
            var appDatabase : SpatiaEncryptionDatabase = SpatiaEncryptionDatabase.getInstance(requireContext())
            //SpatiaEncryptionDatabase.getInstance(requireContext()).openHelper.writableDatabase.
          //  appDatabase.getSpBaseDao().insert(com.mapbox.geojson.Feature.fromGeometry(null),IOnq)
            AsyncTask.execute(Runnable {

               // val post1 = Post(1, "prueba", "darwin", "spatia", "test.img")
               // val post2 = Post(2, "prueba2", null, "spatia2", "test2.img")

               // val listPost = listOf(post1,post2)

              //  appDatabase.getPostsDao().insertPosts(listPost)
                val shapr = appDatabase.getCbdkDao().getShape();
                val spatiaVersion = appDatabase.getPostsDao().getSpatiaVersion()
                val proj4Version = appDatabase.getPostsDao().getProj4Version()
                val geosVersion = appDatabase.getPostsDao().getGeosVersion()
                val makePoliline = appDatabase.getPostsDao().getMakePolyline()
                val distance = appDatabase.getPostsDao().getDistance()
                //val post = appDatabase.getCbdkDao().getAllPostsList()
                activity?.runOnUiThread(Runnable {
                    spatia_version.text = spatiaVersion
                    proj4_version.text = proj4Version
                    geos_version.text = geosVersion
                    polyline_txt.text = makePoliline
                    distance_txt.text = shapr.toString()
                })


            })
        }
        if(flag == 2){
            var appDatabase : SpatiaEncryptionDatabase = SpatiaEncryptionDatabase.getInstance(requireContext())
            btn_update.setOnClickListener {
                //更新
                var geo = "{\"type\":\"Polygon\",\"coordinates\":[[[115.0968162,35.9232154],[115.0968545,35.9237406],[115.0971143,35.9237292],[115.0971055,35.9235763],[115.0970797,35.9231227],[115.0970811,35.9228782],[115.0970722,35.9227526],[115.0967897,35.922556],[115.0968162,35.9232154]]]}";
                var polygon = Polygon.fromJson(geo)
                var ps2 = "{\"dkzb\":\"115.096854,35.923741;115.096816,35.923215;115.096790,35.922556;115.097072,35.922753;115.097081,35.922878;115.097080,35.923123;115.097106,35.923576;115.097114,35.923729;115.096854,35.923741\",\"countycode\":\"410922\",\"bxgsdm\":\"6\",\"uuid\":\"5c670c94d9bb42f8b74cff6d3532c841\",\"datagroup\":1,\"qhdm\":\"410922100005\",\"citycode\":\"4109\",\"dkpd\":0,\"qhmc\":\"高庄\",\"tilestatus\":0,\"mj\":4.37,\"bxgsmc\":\"北京世纪国源科技股份有限公司更新\",\"dkzlqhdm\":\"410922100217\",\"dkzlqhmc\":\"八角寨\",\"zdxlh\":\"a18c999b-8cc1-49ba-8a32-2754f33a71c6\",\"zh\":\"gykjqfx_test\",\"dkmc\":\"0050\",\"provincecode\":\"41\",\"ltmark\":0,\"datasource\":0,\"nf\":\"2022\",\"szzb\":\"W:115.0967897;S:35.922556;E:115.0971143;N:35.9237406\",\"dkbm\":\"2022064109221000050050\",\"objectid\":25445360,\"status\":2,\"EXTEND\":\"{\\\"bdh\\\":\\\"202206410922100005750916242507\\\",\\\"bdzlt\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"0050\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"2022064109221000050050\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/2022064109221000050050_ZLT.jpg\\\\\\\"}]\\\",\\\"bf\\\":196.65,\\\"bxfl\\\":5.0,\\\"bxgsdm\\\":\\\"6\\\",\\\"bxgsmc\\\":\\\"北京世纪国源科技股份有限公司1111\\\",\\\"bxje\\\":3933.0,\\\"bz\\\":\\\"\\\",\\\"cbsj\\\":\\\"2021-06-20 19:23:45\\\",\\\"citycode\\\":\\\"4109\\\",\\\"countycode\\\":\\\"410922\\\",\\\"datagroup\\\":1,\\\"datasource\\\":0,\\\"dkbm\\\":\\\"2022064109221000050050\\\",\\\"dkmc\\\":\\\"0050\\\",\\\"dkmj\\\":4.37,\\\"dkxh\\\":\\\"0\\\",\\\"dwbe\\\":900.0,\\\"dwbf\\\":45.0,\\\"gmtCreate\\\":\\\"2021-06-03 18:16:57\\\",\\\"isChangeData\\\":0,\\\"jd\\\":123.390343,\\\"jldwdm\\\":\\\"05\\\",\\\"jldwmc\\\":\\\"亩\\\",\\\"jnbl\\\":15.0,\\\"nf\\\":\\\"2022\\\",\\\"nhdm\\\":\\\"530701197509162425\\\",\\\"nhjn\\\":29.5,\\\"nhxm\\\":\\\"丁兰\\\",\\\"picbasepath\\\":\\\"{\\\\\\\"accessKeyId\\\\\\\":\\\\\\\"LTAIP816Jan0Nvjq\\\\\\\",\\\\\\\"bucket\\\\\\\":\\\\\\\"ram-agriinsurance-prod\\\\\\\",\\\\\\\"endpoint\\\\\\\":\\\\\\\"https://oss-cn-zhangjiakou.aliyuncs.com\\\\\\\"}\\\",\\\"provincecode\\\":\\\"41\\\",\\\"qhdm\\\":\\\"410922100005\\\",\\\"qhmc\\\":\\\"高庄\\\",\\\"qztp\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"农户签字\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"农户签字\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/02/03/13a936f928ec4964882b26e5d170afdc/QM_FARMER.jpg\\\\\\\"}]\\\",\\\"sfdh\\\":1,\\\"sfpkh\\\":0,\\\"szzb\\\":\\\"W:115.09679;S:35.922556;E:115.097114;N:35.923741\\\",\\\"tbmj\\\":4.37,\\\"tbxzdm\\\":\\\"04109\\\",\\\"tbxzmc\\\":\\\"河南优质小麦完全成本保险\\\",\\\"uuid\\\":\\\"baefd94476f249f0af45900adaad0d02\\\",\\\"wd\\\":41.894848,\\\"xczp\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：未知方向;正在获取信息;正在获取信息;正在获取信息;时间：2021-04-07 13:28:15;拍照点是否在地块内：否\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/04/07/29b3ed33a33942b7841455ddcc784c62/1617772867609_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西北;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:05\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715366727_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西南;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:19\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715380764_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西南;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:31\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715391829_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西北;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:38\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715399415_410922_XCZP.jpg\\\\\\\"}]\\\",\\\"zdxlh\\\":\\\"29acf55d-991e-4251-9be4-aa866f9f3f81\\\",\\\"zh\\\":\\\"gykjqfx_test\\\",\\\"zltzb\\\":\\\"115.096816,35.923215;115.096855,35.923741;115.097114,35.923729;115.097105,35.923576;115.097080,35.923123;115.097081,35.922878;115.097072,35.922753;115.096790,35.922556;115.096816,35.923215\\\",\\\"zwdm\\\":\\\"07\\\",\\\"zwmc\\\":\\\"冬小麦\\\"}\"}"
                val returnData2: JsonObject = JsonParser().parse(ps2).getAsJsonObject()
                var feature2:Feature = Feature.fromGeometry(polygon,returnData2)
                var con = mutableListOf<String>();
                con.add("dkbm");
                var observer2 =appDatabase.getSpBaseDao().update("dt_cbdk",feature2,con,null)
                observer2.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Long> {

                            override fun onComplete() {
                                //    distance_txt.text = "onComplete"
                            }

                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(t: Long) {
                                geos_version.text = "更新成功"
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }


                        })
            }
           //删除根据主键或者where条件
            btn_delete.setOnClickListener {
                //更新
                var geo = "{\"type\":\"Polygon\",\"coordinates\":[[[115.0968162,35.9232154],[115.0968545,35.9237406],[115.0971143,35.9237292],[115.0971055,35.9235763],[115.0970797,35.9231227],[115.0970811,35.9228782],[115.0970722,35.9227526],[115.0967897,35.922556],[115.0968162,35.9232154]]]}";
                var polygon = Polygon.fromJson(geo)
                var ps2 = "{\"dkzb\":\"115.096854,35.923741;115.096816,35.923215;115.096790,35.922556;115.097072,35.922753;115.097081,35.922878;115.097080,35.923123;115.097106,35.923576;115.097114,35.923729;115.096854,35.923741\",\"countycode\":\"410922\",\"bxgsdm\":\"6\",\"uuid\":\"5c670c94d9bb42f8b74cff6d3532c841\",\"datagroup\":1,\"qhdm\":\"410922100005\",\"citycode\":\"4109\",\"dkpd\":0,\"qhmc\":\"高庄\",\"tilestatus\":0,\"mj\":4.37,\"bxgsmc\":\"北京世纪国源科技股份有限公司更新\",\"dkzlqhdm\":\"410922100217\",\"dkzlqhmc\":\"八角寨\",\"zdxlh\":\"a18c999b-8cc1-49ba-8a32-2754f33a71c6\",\"zh\":\"gykjqfx_test\",\"dkmc\":\"0050\",\"provincecode\":\"41\",\"ltmark\":0,\"datasource\":0,\"nf\":\"2022\",\"szzb\":\"W:115.0967897;S:35.922556;E:115.0971143;N:35.9237406\",\"dkbm\":\"2022064109221000050050\",\"objectid\":25445360,\"status\":2,\"EXTEND\":\"{\\\"bdh\\\":\\\"202206410922100005750916242507\\\",\\\"bdzlt\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"0050\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"2022064109221000050050\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/2022064109221000050050_ZLT.jpg\\\\\\\"}]\\\",\\\"bf\\\":196.65,\\\"bxfl\\\":5.0,\\\"bxgsdm\\\":\\\"6\\\",\\\"bxgsmc\\\":\\\"北京世纪国源科技股份有限公司1111\\\",\\\"bxje\\\":3933.0,\\\"bz\\\":\\\"\\\",\\\"cbsj\\\":\\\"2021-06-20 19:23:45\\\",\\\"citycode\\\":\\\"4109\\\",\\\"countycode\\\":\\\"410922\\\",\\\"datagroup\\\":1,\\\"datasource\\\":0,\\\"dkbm\\\":\\\"2022064109221000050050\\\",\\\"dkmc\\\":\\\"0050\\\",\\\"dkmj\\\":4.37,\\\"dkxh\\\":\\\"0\\\",\\\"dwbe\\\":900.0,\\\"dwbf\\\":45.0,\\\"gmtCreate\\\":\\\"2021-06-03 18:16:57\\\",\\\"isChangeData\\\":0,\\\"jd\\\":123.390343,\\\"jldwdm\\\":\\\"05\\\",\\\"jldwmc\\\":\\\"亩\\\",\\\"jnbl\\\":15.0,\\\"nf\\\":\\\"2022\\\",\\\"nhdm\\\":\\\"530701197509162425\\\",\\\"nhjn\\\":29.5,\\\"nhxm\\\":\\\"丁兰\\\",\\\"picbasepath\\\":\\\"{\\\\\\\"accessKeyId\\\\\\\":\\\\\\\"LTAIP816Jan0Nvjq\\\\\\\",\\\\\\\"bucket\\\\\\\":\\\\\\\"ram-agriinsurance-prod\\\\\\\",\\\\\\\"endpoint\\\\\\\":\\\\\\\"https://oss-cn-zhangjiakou.aliyuncs.com\\\\\\\"}\\\",\\\"provincecode\\\":\\\"41\\\",\\\"qhdm\\\":\\\"410922100005\\\",\\\"qhmc\\\":\\\"高庄\\\",\\\"qztp\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"农户签字\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"农户签字\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/02/03/13a936f928ec4964882b26e5d170afdc/QM_FARMER.jpg\\\\\\\"}]\\\",\\\"sfdh\\\":1,\\\"sfpkh\\\":0,\\\"szzb\\\":\\\"W:115.09679;S:35.922556;E:115.097114;N:35.923741\\\",\\\"tbmj\\\":4.37,\\\"tbxzdm\\\":\\\"04109\\\",\\\"tbxzmc\\\":\\\"河南优质小麦完全成本保险\\\",\\\"uuid\\\":\\\"baefd94476f249f0af45900adaad0d02\\\",\\\"wd\\\":41.894848,\\\"xczp\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：未知方向;正在获取信息;正在获取信息;正在获取信息;时间：2021-04-07 13:28:15;拍照点是否在地块内：否\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/04/07/29b3ed33a33942b7841455ddcc784c62/1617772867609_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西北;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:05\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715366727_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西南;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:19\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715380764_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西南;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:31\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715391829_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西北;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:38\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715399415_410922_XCZP.jpg\\\\\\\"}]\\\",\\\"zdxlh\\\":\\\"29acf55d-991e-4251-9be4-aa866f9f3f81\\\",\\\"zh\\\":\\\"gykjqfx_test\\\",\\\"zltzb\\\":\\\"115.096816,35.923215;115.096855,35.923741;115.097114,35.923729;115.097105,35.923576;115.097080,35.923123;115.097081,35.922878;115.097072,35.922753;115.096790,35.922556;115.096816,35.923215\\\",\\\"zwdm\\\":\\\"07\\\",\\\"zwmc\\\":\\\"冬小麦\\\"}\"}"
                val returnData2: JsonObject = JsonParser().parse(ps2).getAsJsonObject()
                var feature2:Feature = Feature.fromGeometry(polygon,returnData2)
                //根据主键删
                var observer2 =appDatabase.getSpBaseDao().delete("dt_cbdk",feature2)
                //根据where条件删
                // var observer2 =appDatabase.getSpBaseDao().delete("dt_cbdk","objectid = '25445360'")
                observer2.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Long> {

                            override fun onComplete() {
                                //    distance_txt.text = "onComplete"
                            }

                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(t: Long) {
                                geos_version.text = "删除成功"+t
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }


                        })
            }
            btn_query.setOnClickListener {
                //获取feature
                var observer1 = appDatabase.getSpBaseDao().queryByCondition("dt_cbdk","dkbm = '2022064109221000050050'",null)
                observer1.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<MutableList<Feature?>> {

                            override fun onComplete() {
                                //    distance_txt.text = "onComplete"
                            }

                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(t: MutableList<Feature?>) {
                                proj4_version.text = t.get(0)!!.getStringProperty("bxgsmc")
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }


                        })
            }

            var geo = "{\"type\":\"Polygon\",\"coordinates\":[[[115.0968162,35.9232154],[115.0968545,35.9237406],[115.0971143,35.9237292],[115.0971055,35.9235763],[115.0970797,35.9231227],[115.0970811,35.9228782],[115.0970722,35.9227526],[115.0967897,35.922556],[115.0968162,35.9232154]]]}";
            var polygon = Polygon.fromJson(geo)
            var ps = "{\"dkzb\":\"115.096854,35.923741;115.096816,35.923215;115.096790,35.922556;115.097072,35.922753;115.097081,35.922878;115.097080,35.923123;115.097106,35.923576;115.097114,35.923729;115.096854,35.923741\",\"countycode\":\"410922\",\"bxgsdm\":\"6\",\"uuid\":\"5c670c94d9bb42f8b74cff6d3532c841\",\"datagroup\":1,\"qhdm\":\"410922100005\",\"citycode\":\"4109\",\"dkpd\":0,\"qhmc\":\"高庄\",\"tilestatus\":0,\"mj\":4.37,\"bxgsmc\":\"北京世纪国源科技股份有限公司\",\"dkzlqhdm\":\"410922100217\",\"dkzlqhmc\":\"八角寨\",\"zdxlh\":\"a18c999b-8cc1-49ba-8a32-2754f33a71c6\",\"zh\":\"gykjqfx_test\",\"dkmc\":\"0050\",\"provincecode\":\"41\",\"ltmark\":0,\"datasource\":0,\"nf\":\"2022\",\"szzb\":\"W:115.0967897;S:35.922556;E:115.0971143;N:35.9237406\",\"dkbm\":\"2022064109221000050050\",\"objectid\":25445360,\"status\":2,\"EXTEND\":\"{\\\"bdh\\\":\\\"202206410922100005750916242507\\\",\\\"bdzlt\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"0050\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"2022064109221000050050\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/2022064109221000050050_ZLT.jpg\\\\\\\"}]\\\",\\\"bf\\\":196.65,\\\"bxfl\\\":5.0,\\\"bxgsdm\\\":\\\"6\\\",\\\"bxgsmc\\\":\\\"北京世纪国源科技股份有限公司\\\",\\\"bxje\\\":3933.0,\\\"bz\\\":\\\"\\\",\\\"cbsj\\\":\\\"2021-06-20 19:23:45\\\",\\\"citycode\\\":\\\"4109\\\",\\\"countycode\\\":\\\"410922\\\",\\\"datagroup\\\":1,\\\"datasource\\\":0,\\\"dkbm\\\":\\\"2022064109221000050050\\\",\\\"dkmc\\\":\\\"0050\\\",\\\"dkmj\\\":4.37,\\\"dkxh\\\":\\\"0\\\",\\\"dwbe\\\":900.0,\\\"dwbf\\\":45.0,\\\"gmtCreate\\\":\\\"2021-06-03 18:16:57\\\",\\\"isChangeData\\\":0,\\\"jd\\\":123.390343,\\\"jldwdm\\\":\\\"05\\\",\\\"jldwmc\\\":\\\"亩\\\",\\\"jnbl\\\":15.0,\\\"nf\\\":\\\"2022\\\",\\\"nhdm\\\":\\\"530701197509162425\\\",\\\"nhjn\\\":29.5,\\\"nhxm\\\":\\\"丁兰\\\",\\\"picbasepath\\\":\\\"{\\\\\\\"accessKeyId\\\\\\\":\\\\\\\"LTAIP816Jan0Nvjq\\\\\\\",\\\\\\\"bucket\\\\\\\":\\\\\\\"ram-agriinsurance-prod\\\\\\\",\\\\\\\"endpoint\\\\\\\":\\\\\\\"https://oss-cn-zhangjiakou.aliyuncs.com\\\\\\\"}\\\",\\\"provincecode\\\":\\\"41\\\",\\\"qhdm\\\":\\\"410922100005\\\",\\\"qhmc\\\":\\\"高庄\\\",\\\"qztp\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"农户签字\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"农户签字\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/02/03/13a936f928ec4964882b26e5d170afdc/QM_FARMER.jpg\\\\\\\"}]\\\",\\\"sfdh\\\":1,\\\"sfpkh\\\":0,\\\"szzb\\\":\\\"W:115.09679;S:35.922556;E:115.097114;N:35.923741\\\",\\\"tbmj\\\":4.37,\\\"tbxzdm\\\":\\\"04109\\\",\\\"tbxzmc\\\":\\\"河南优质小麦完全成本保险\\\",\\\"uuid\\\":\\\"baefd94476f249f0af45900adaad0d02\\\",\\\"wd\\\":41.894848,\\\"xczp\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：未知方向;正在获取信息;正在获取信息;正在获取信息;时间：2021-04-07 13:28:15;拍照点是否在地块内：否\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/04/07/29b3ed33a33942b7841455ddcc784c62/1617772867609_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西北;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:05\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715366727_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西南;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:19\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715380764_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西南;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:31\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715391829_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西北;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:38\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715399415_410922_XCZP.jpg\\\\\\\"}]\\\",\\\"zdxlh\\\":\\\"29acf55d-991e-4251-9be4-aa866f9f3f81\\\",\\\"zh\\\":\\\"gykjqfx_test\\\",\\\"zltzb\\\":\\\"115.096816,35.923215;115.096855,35.923741;115.097114,35.923729;115.097105,35.923576;115.097080,35.923123;115.097081,35.922878;115.097072,35.922753;115.096790,35.922556;115.096816,35.923215\\\",\\\"zwdm\\\":\\\"07\\\",\\\"zwmc\\\":\\\"冬小麦\\\"}\"}"
            var ps1 = "{\"dkzb\":\"115.096854,35.923741;115.096816,35.923215;115.096790,35.922556;115.097072,35.922753;115.097081,35.922878;115.097080,35.923123;115.097106,35.923576;115.097114,35.923729;115.096854,35.923741\",\"countycode\":\"410922\",\"bxgsdm\":\"6\",\"uuid\":\"5c670c94d9bb42f8b74cff6d3532c841\",\"datagroup\":1,\"qhdm\":\"410922100005\",\"citycode\":\"4109\",\"dkpd\":0,\"qhmc\":\"高庄\",\"tilestatus\":0,\"mj\":4.37,\"bxgsmc\":\"北京世纪国源科技股份有限公司\",\"dkzlqhdm\":\"410922100217\",\"dkzlqhmc\":\"八角寨\",\"zdxlh\":\"a18c999b-8cc1-49ba-8a32-2754f33a71c6\",\"zh\":\"gykjqfx_test\",\"dkmc\":\"0050\",\"provincecode\":\"41\",\"ltmark\":0,\"datasource\":0,\"nf\":\"2022\",\"szzb\":\"W:115.0967897;S:35.922556;E:115.0971143;N:35.9237406\",\"dkbm\":\"2022064109221000050050\",\"objectid\":25445359,\"status\":2,\"EXTEND\":\"{\\\"bdh\\\":\\\"202206410922100005750916242507\\\",\\\"bdzlt\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"0050\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"2022064109221000050050\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/2022064109221000050050_ZLT.jpg\\\\\\\"}]\\\",\\\"bf\\\":196.65,\\\"bxfl\\\":5.0,\\\"bxgsdm\\\":\\\"6\\\",\\\"bxgsmc\\\":\\\"北京世纪国源科技股份有限公司\\\",\\\"bxje\\\":3933.0,\\\"bz\\\":\\\"\\\",\\\"cbsj\\\":\\\"2021-06-20 19:23:45\\\",\\\"citycode\\\":\\\"4109\\\",\\\"countycode\\\":\\\"410922\\\",\\\"datagroup\\\":1,\\\"datasource\\\":0,\\\"dkbm\\\":\\\"2022064109221000050050\\\",\\\"dkmc\\\":\\\"0050\\\",\\\"dkmj\\\":4.37,\\\"dkxh\\\":\\\"0\\\",\\\"dwbe\\\":900.0,\\\"dwbf\\\":45.0,\\\"gmtCreate\\\":\\\"2021-06-03 18:16:57\\\",\\\"isChangeData\\\":0,\\\"jd\\\":123.390343,\\\"jldwdm\\\":\\\"05\\\",\\\"jldwmc\\\":\\\"亩\\\",\\\"jnbl\\\":15.0,\\\"nf\\\":\\\"2022\\\",\\\"nhdm\\\":\\\"530701197509162425\\\",\\\"nhjn\\\":29.5,\\\"nhxm\\\":\\\"丁兰\\\",\\\"picbasepath\\\":\\\"{\\\\\\\"accessKeyId\\\\\\\":\\\\\\\"LTAIP816Jan0Nvjq\\\\\\\",\\\\\\\"bucket\\\\\\\":\\\\\\\"ram-agriinsurance-prod\\\\\\\",\\\\\\\"endpoint\\\\\\\":\\\\\\\"https://oss-cn-zhangjiakou.aliyuncs.com\\\\\\\"}\\\",\\\"provincecode\\\":\\\"41\\\",\\\"qhdm\\\":\\\"410922100005\\\",\\\"qhmc\\\":\\\"高庄\\\",\\\"qztp\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"农户签字\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"农户签字\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/02/03/13a936f928ec4964882b26e5d170afdc/QM_FARMER.jpg\\\\\\\"}]\\\",\\\"sfdh\\\":1,\\\"sfpkh\\\":0,\\\"szzb\\\":\\\"W:115.09679;S:35.922556;E:115.097114;N:35.923741\\\",\\\"tbmj\\\":4.37,\\\"tbxzdm\\\":\\\"04109\\\",\\\"tbxzmc\\\":\\\"河南优质小麦完全成本保险\\\",\\\"uuid\\\":\\\"baefd94476f249f0af45900adaad0d02\\\",\\\"wd\\\":41.894848,\\\"xczp\\\":\\\"[{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：未知方向;正在获取信息;正在获取信息;正在获取信息;时间：2021-04-07 13:28:15;拍照点是否在地块内：否\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/04/07/29b3ed33a33942b7841455ddcc784c62/1617772867609_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西北;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:05\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715366727_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西南;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:19\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715380764_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西南;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:31\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715391829_410922_XCZP.jpg\\\\\\\"},{\\\\\\\"desc\\\\\\\":\\\\\\\"方向：西北;北纬：34.795424;东经：113.803094;地点：金水区兴达路街道办事处时埂;时间：2021-06-03 18:16:38\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"现场照片\\\\\\\",\\\\\\\"path\\\\\\\":\\\\\\\"mobile/2021/06/03/a8b70044174843299321110d7106068c/1622715399415_410922_XCZP.jpg\\\\\\\"}]\\\",\\\"zdxlh\\\":\\\"29acf55d-991e-4251-9be4-aa866f9f3f81\\\",\\\"zh\\\":\\\"gykjqfx_test\\\",\\\"zltzb\\\":\\\"115.096816,35.923215;115.096855,35.923741;115.097114,35.923729;115.097105,35.923576;115.097080,35.923123;115.097081,35.922878;115.097072,35.922753;115.096790,35.922556;115.096816,35.923215\\\",\\\"zwdm\\\":\\\"07\\\",\\\"zwmc\\\":\\\"冬小麦\\\"}\"}"
            val returnData: JsonObject = JsonParser().parse(ps).getAsJsonObject()
            var feature:Feature = Feature.fromGeometry(polygon,returnData)
            var list:MutableList<Feature> = mutableListOf();
            list.add(feature);
            var feature1:Feature = Feature.fromGeometry(polygon,JsonParser().parse(ps1).getAsJsonObject())
            list.add(feature1);
            var oservable= appDatabase.getSpBaseDao().inserts("dt_cbdk",list,true)
            oservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Long> {

                        override fun onComplete() {
                        //    distance_txt.text = "onComplete"
                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(t: Long) {
                            distance_txt.text = t.toString()
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }


                    })

            //获取feature 直接传sql 如果是空间字段必须 AsGeoJSON(空间字段名) as GEOJSON
           var observer = appDatabase.getSpBaseDao().queryBySql("select *,AsGeoJSON(shape) as GEOJSON from dt_cbdk where shape is not null and dkbm = '2022064109221000050050'")
            observer.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<MutableList<Feature?>> {

                        override fun onComplete() {
                            //    distance_txt.text = "onComplete"
                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(t: MutableList<Feature?>) {
                            spatia_version.text = t.size.toString()
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }


                    })


            //获取feature
            var observer1 = appDatabase.getSpBaseDao().queryByCondition("dt_cbdk","dkbm = '2022064109221000050050'",null)
            observer1.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<MutableList<Feature?>> {

                        override fun onComplete() {
                            //    distance_txt.text = "onComplete"
                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(t: MutableList<Feature?>) {
                            proj4_version.text = t.size.toString()
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }


                    })


        }



    }

    override fun onDestroy() {
        super.onDestroy()

    }
}
