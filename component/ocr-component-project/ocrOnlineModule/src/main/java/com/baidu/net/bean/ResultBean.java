package com.baidu.net.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by fs on 2019-08-28.
 * 动物植物果蔬返回结果
 */

public class ResultBean implements Serializable {

    public long log_id  ;
    public List<Result> result;

    public class Result implements Serializable {
        public Double score  ;
        public String name  ;
        public int bank_card_type  ;
        public Baike_info baike_info  ;
    }
    public class Baike_info implements Serializable {
        public String baike_url  ;
        public String image_url  ;
        public String description  ;
    }
}
