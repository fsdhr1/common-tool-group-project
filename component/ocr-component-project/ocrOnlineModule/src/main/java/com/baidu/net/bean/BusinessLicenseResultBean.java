package com.baidu.net.bean;

import java.io.Serializable;

/**
 * Created by jyh on 2019-08-06.
 * 营业执照返回结果
 */

public class BusinessLicenseResultBean {

    public long log_id;
    public int words_result_num;
    public Words_result words_result;

    public long getLog_id() {
        return log_id;
    }

    public void setLog_id(long log_id) {
        this.log_id = log_id;
    }

    public int getWords_result_num() {
        return words_result_num;
    }

    public void setWords_result_num(int words_result_num) {
        this.words_result_num = words_result_num;
    }


    public class Words_result implements Serializable {
        public CerNum 社会信用代码;

        public class CerNum implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public ZCXS 组成形式;

        public class ZCXS implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public JYFW 经营范围;

        public class JYFW implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public FR 法人;

        public class FR implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public CLRQ 成立日期;

        public class CLRQ implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public ZCZB 注册资本;

        public class ZCZB implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public ZJBH 证件编号;

        public class ZJBH implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public DZ 地址;

        public class DZ implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public DWMC 单位名称;

        public class DWMC implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public LX 类型;

        public class LX implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }

        public YXQ 有效期;

        public class YXQ implements Serializable {
            public String words;
            public Location location;

            public class Location implements Serializable {
                public int top;
                public int left;
                public int width;
                public int height;

                public void setTop(int top) {
                    this.top = top;
                }

                public int getTop() {
                    return this.top;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getLeft() {
                    return this.left;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getWidth() {
                    return this.width;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getHeight() {
                    return this.height;
                }

            }

            public void setWords(String words) {
                this.words = words;
            }

            public String getWords() {
                return this.words;
            }

        }
    }
}
