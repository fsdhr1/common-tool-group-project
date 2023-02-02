package com.gykj.commontool.networktest.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AdministrativeResultBean implements Serializable {
    private String msg;
    private int code;
    private List<Xzqh> xzqh;

    public static class Xzqh implements Serializable,Comparable {
        private String xzqhdm;
        private String layername;
        private String xzqhmc;

        public void setXzqhdm(String xzqhdm) {
            this.xzqhdm = xzqhdm;
        }

        public String getXzqhdm() {
            return this.xzqhdm;
        }

        public void setLayername(String layername) {
            this.layername = layername;
        }

        public String getLayername() {
            return this.layername;
        }

        public void setXzqhmc(String xzqhmc) {
            this.xzqhmc = xzqhmc;
        }

        public String getXzqhmc() {
            return this.xzqhmc;
        }
      
       @Override
       public int compareTo(Object o) {
          if(o instanceof Xzqh){
             Xzqh xzqh = (Xzqh) o;
             if(xzqh.xzqhdm == null) xzqh.xzqhdm = "";
             if(xzqhdm == null) xzqhdm = "";
             return xzqhdm.length() - xzqh.xzqhdm.length();
          }
          return 0;
       }
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

   public List<Xzqh> getXzqh() {
      return xzqh;
   }

   public void setXzqh(List<Xzqh> xzqh) {
      this.xzqh = xzqh;
   }
   
   public String getAdministrative(){
       if (xzqh != null && !xzqh.isEmpty()){
          Set<Xzqh> xzqhs = new TreeSet<>(xzqh);
          StringBuilder stringBuilder = new StringBuilder();
          for (Xzqh xzqh1 : xzqhs) {
             stringBuilder.append(xzqh1.xzqhmc);
          }
          return stringBuilder.toString();
       }
       return "";
   }
}