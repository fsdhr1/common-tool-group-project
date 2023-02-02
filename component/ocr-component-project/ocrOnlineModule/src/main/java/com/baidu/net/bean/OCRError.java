package com.baidu.net.bean;

import com.baidu.ocr.ui.util.IdCardType;

/**
 * Created by jyh on 2019-08-14.
 */

public class OCRError {

    protected int errorCode;
    protected String errorMessage;
    public @IdCardType String contentType;
    
    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
