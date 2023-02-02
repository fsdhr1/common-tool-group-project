package com.gykj.commontool.networktest.beans;

import java.io.Serializable;
import java.util.List;

public class WeatherResultBean implements Serializable {
    public int status;
    public String desc;
    public Data data;

    public class Data implements Serializable {
        public String city;
        public String ganmao;
        public String wendu;
        public Yesterday yesterday;
        
        public class Yesterday implements Serializable {
            public String date;
            public String high;
            public String fx;
            public String low;
            public String fl;
            public String type;

            public void setDate(String date) {
                this.date = date;
            }

            public String getDate() {
                return this.date;
            }

            public void setHigh(String high) {
                this.high = high;
            }

            public String getHigh() {
                return this.high;
            }

            public void setFx(String fx) {
                this.fx = fx;
            }

            public String getFx() {
                return this.fx;
            }

            public void setLow(String low) {
                this.low = low;
            }

            public String getLow() {
                return this.low;
            }

            public void setFl(String fl) {
                this.fl = fl;
            }

            public String getFl() {
                return this.fl;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getType() {
                return this.type;
            }

        }

        public List<Forecast> forecast;

        public class Forecast implements Serializable {
            public String date;
            public String high;
            public String fengli;
            public String low;
            public String fengxiang;
            public String type;

            public void setDate(String date) {
                this.date = date;
            }

            public String getDate() {
                return this.date;
            }

            public void setHigh(String high) {
                this.high = high;
            }

            public String getHigh() {
                return this.high;
            }

            public void setFengli(String fengli) {
                this.fengli = fengli;
            }

            public String getFengli() {
                return this.fengli;
            }

            public void setLow(String low) {
                this.low = low;
            }

            public String getLow() {
                return this.low;
            }

            public void setFengxiang(String fengxiang) {
                this.fengxiang = fengxiang;
            }

            public String getFengxiang() {
                return this.fengxiang;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getType() {
                return this.type;
            }

        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCity() {
            return this.city;
        }

        public void setGanmao(String ganmao) {
            this.ganmao = ganmao;
        }

        public String getGanmao() {
            return this.ganmao;
        }

        public void setWendu(String wendu) {
            this.wendu = wendu;
        }

        public String getWendu() {
            return this.wendu;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "city='" + city + '\'' +
                    ", ganmao='" + ganmao + '\'' +
                    ", wendu='" + wendu + '\'' +
                    ", yesterday=" + yesterday +
                    ", forecast=" + forecast +
                    '}';
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

    @Override
    public String toString() {
        return "WeatherResultBean{" +
                "status=" + status +
                ", desc='" + desc + '\'' +
                ", data=" + data +
                '}';
    }
}