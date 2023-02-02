package com.baidu.net.bean;

import java.io.Serializable;

import com.baidu.ocr.ui.util.IdCardType;

/**
 * Created by jyh on 2019-08-06.
 * 身份证返回结果
 */
public class IDCardResultBean implements Serializable  {

    public long log_id;
    public int words_result_num;
    public String image_status;
    public int direction;
    public Words_result words_result;
    @IdCardType public String contentType;
    
    public class Words_result implements Serializable  {
        public  XM 姓名;
        public class XM implements Serializable  {
            public String words  ;
            public  Location location;
            public class Location implements Serializable  {
                public int top  ;
                public int left  ;
                public int width  ;
                public int height  ;
            }
        }

        public  MZ 民族;
        public class MZ implements Serializable  {
            public String words  ;
            public  Location location;
            public class Location implements Serializable  {
                public int top  ;
                public int left  ;
                public int width  ;
                public int height  ;
            }
        }

        public  Address 住址;
        public class Address implements Serializable  {
            public String words  ;
            public  Location location;
            public class Location implements Serializable  {
                public int top  ;
                public int left  ;
                public int width  ;
                public int height  ;
            }
        }

        public  SFZH 公民身份号码;
        public class SFZH implements Serializable  {
            public String words  ;
            public  Location location;
            public class Location implements Serializable  {
                public int top  ;
                public int left  ;
                public int width  ;
                public int height  ;
            }
        }

        public  CSRQ 出生;
        public class CSRQ implements Serializable  {
            public String words  ;
            public  Location location;
            public class Location implements Serializable  {
                public int top  ;
                public int left  ;
                public int width  ;
                public int height  ;
            }
        }

        public  XB 性别;
        public class XB implements Serializable  {
            public String words  ;
            public  Location location;
            public class Location implements Serializable  {
                public int top  ;
                public int left  ;
                public int width  ;
                public int height  ;
            }
        }

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
