package com.baidu.net.bean;

import java.io.Serializable;

/**
 * Created by jyh on 2019-08-06.
 * 银行卡返回结果
 */

public class BankCardResultBean implements Serializable {

    public long log_id  ;
    public Result result;

    public static class Result implements Serializable {
        public String valid_date  ;
        public String bank_card_number  ;
        public int bank_card_type  ;
        public String bank_name  ;

        public String getValid_date() {
            return valid_date;
        }

        public void setValid_date(String valid_date) {
            this.valid_date = valid_date;
        }

        public String getBank_card_number() {
            return bank_card_number;
        }

        public void setBank_card_number(String bank_card_number) {
            this.bank_card_number = bank_card_number;
        }

        public int getBank_card_type() {
            return bank_card_type;
        }

        public void setBank_card_type(int bank_card_type) {
            this.bank_card_type = bank_card_type;
        }

        public String getBank_name() {
            return bank_name;
        }

        public void setBank_name(String bank_name) {
            this.bank_name = bank_name;
        }
    }

    public long getLog_id() {
        return log_id;
    }

    public void setLog_id(long log_id) {
        this.log_id = log_id;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
