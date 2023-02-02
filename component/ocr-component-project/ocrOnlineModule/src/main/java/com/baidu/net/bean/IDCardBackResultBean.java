package com.baidu.net.bean;

import java.io.Serializable;

/**
 * Created by jyh on 2019-08-13.
 */

public class IDCardBackResultBean implements Serializable {

    public long log_id  ;
    public int words_result_num  ;
    public String image_status  ;
    public int direction  ;
    public Words_result words_result;

    public class Words_result implements Serializable  {
        public  SXRQ 失效日期;
        public class SXRQ implements Serializable  {
            public String words  ;
            public  Location location;
            public class Location implements Serializable  {
                public int top  ;
                public int left  ;
                public int width  ;
                public int height  ;
            }
        }

        public  QFJG 签发机关;
        public class QFJG implements Serializable  {
            public String words  ;
            public  Location location;
            public class Location implements Serializable  {
                public int top  ;
                public int left  ;
                public int width  ;
                public int height  ;
            }
        }

        public  QFRQ 签发日期;
        public class QFRQ implements Serializable  {
            public String words  ;
            public  Location location;
            public class Location implements Serializable  {
                public int top  ;
                public int left  ;
                public int width  ;
                public int height  ;
            }
        }
    }
}
