package com.gykj.commontool.networktest;

import java.util.HashMap;
import java.util.Map;

import com.gykj.commontool.networktest.beans.UserData;
import com.gykj.networkmodule.beans.DataResponse;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author zyp
 * 2021/7/15
 */
public interface UserApi {
    @POST("http://gykj123.cn:8100/open-platform-admin-server/api/v1/login")
    Observable<DataResponse<UserData>> userLogin(@Body Map<String, String> params);
}
