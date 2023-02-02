package com.baidu.ocr.ui.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;

/**
 * @author zyp
 * 8.4.20
 */


@StringDef({OcrUtils.IDCARD_FRONT,OcrUtils.IDCARD_BACK})
@Retention(RetentionPolicy.SOURCE)
public @interface IdCardType {
}
