
package com.gykj.commontool.ocrModuletest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.net.bean.BankCardResultBean;
import com.baidu.net.bean.BusinessLicenseResultBean;
import com.baidu.net.bean.IDCardResultBean;
import com.baidu.net.bean.ResultBean;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.baidu.ocr.ui.util.FileUtil;
import com.baidu.ocr.ui.util.OcrUtils;
import com.baidu.utils.Global;
import com.baidu.utils.OcrOpenUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.gykj.commontool.R;

public class OcrModuleTestActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int REQUEST_CODE_CAMERA = 102;
    private static final int REQUEST_CODE_CAMERA_OUTLINE = 103;
    private static final int REQUEST_CODE_BANKCARD = 111;
    private static final int REQUEST_CODE_BANKCARD_OUTLINE = 114;
    private static final int REQUEST_CODE_BUSINESS_LICENSE = 106;
    private static final int REQUEST_CODE_ANIMALDETECT = 107;
    private static final int REQUEST_CODE_PLANTDETECT = 108;
    private static final int REQUEST_CODE_INGREDIENT = 109;
    private boolean hasGotToken = false;
    private AlertDialog.Builder alertDialog;

    Button id_card_front_button_outline, id_card_back_button_outline, bankcard_button_outline,id_card_front_button, id_card_back_button, bankcard_button, business_license_button, animalDetect_button, plantDetect_button, ingredient_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_module_test);
        // 申请权限
        PermissionUtils.permission(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request();
        //
        OCR.getInstance(this).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                Global.OCR_TOKEN = accessToken.getAccessToken();
                hasGotToken = true;
                Log.e("onResult: ", " --- onResult --- ");
            }

            @Override
            public void onError(OCRError ocrError) {
                ocrError.getMessage();
                Log.e("onResult: ", " --- ocrError --- ");
            }
        }, this);
        //
        init();
    }

    private void init() {
        alertDialog = new AlertDialog.Builder(this);

        id_card_front_button = (Button) findViewById(R.id.id_card_front_button);
        id_card_back_button = (Button) findViewById(R.id.id_card_back_button);
        bankcard_button = (Button) findViewById(R.id.bankcard_button);
        business_license_button = (Button) findViewById(R.id.business_license_button);
        animalDetect_button = (Button) findViewById(R.id.animalDetect_button);
        plantDetect_button = (Button) findViewById(R.id.plantDetect_button);
        ingredient_button = (Button) findViewById(R.id.ingredient_button);
        id_card_front_button_outline = (Button)findViewById(R.id.id_card_front_button_outline);
        id_card_back_button_outline = (Button)findViewById(R.id.id_card_back_button_outline);
        bankcard_button_outline = (Button)findViewById(R.id.bankcard_button_outline);

        id_card_front_button.setOnClickListener(this);
        id_card_back_button.setOnClickListener(this);
        bankcard_button.setOnClickListener(this);
        id_card_front_button_outline.setOnClickListener(this);
        id_card_back_button_outline.setOnClickListener(this);
        bankcard_button_outline.setOnClickListener(this);
        business_license_button.setOnClickListener(this);
        animalDetect_button.setOnClickListener(this);
        plantDetect_button.setOnClickListener(this);
        ingredient_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String filePath = FileUtil.getSaveFile(getApplication()).getAbsolutePath();
        if (!checkTokenStatus()) {
            return;
        }
        if (v.getId() == R.id.id_card_front_button_outline) {
            OcrOpenUtils.getInstance().recIdCardFront(OcrModuleTestActivity.this, filePath, REQUEST_CODE_CAMERA_OUTLINE);
            return;
        } else if (v.getId() == R.id.id_card_back_button_outline) {
            OcrOpenUtils.getInstance().recIdCardBack(OcrModuleTestActivity.this, filePath, REQUEST_CODE_CAMERA_OUTLINE);
            return;
        } else if (v.getId() == R.id.bankcard_button_outline) {
            OcrOpenUtils.getInstance().recBankCard(OcrModuleTestActivity.this, filePath, REQUEST_CODE_BANKCARD_OUTLINE);
            return;
        } else if (v.getId() == R.id.id_card_front_button) {
            OcrOpenUtils.getInstance().recIdCardFront(OcrModuleTestActivity.this, filePath, REQUEST_CODE_CAMERA);
            return;
        } else if (v.getId() == R.id.id_card_back_button) {
            OcrOpenUtils.getInstance().recIdCardBack(OcrModuleTestActivity.this, filePath, REQUEST_CODE_CAMERA);
            return;
        } else if (v.getId() == R.id.bankcard_button) {
            OcrOpenUtils.getInstance().recBankCard(OcrModuleTestActivity.this, filePath, REQUEST_CODE_BANKCARD);
            return;
        } else if (v.getId() == R.id.business_license_button) {
            OcrOpenUtils.getInstance().recBusinessLicense(OcrModuleTestActivity.this, filePath, REQUEST_CODE_BUSINESS_LICENSE);
            return;
        } else if (v.getId() == R.id.animalDetect_button) {
            OcrOpenUtils.getInstance().recAnimalDetect(OcrModuleTestActivity.this, filePath, REQUEST_CODE_ANIMALDETECT);
            return;
        } else if (v.getId() == R.id.plantDetect_button) {
            OcrOpenUtils.getInstance().recPlantDetect(OcrModuleTestActivity.this, filePath, REQUEST_CODE_PLANTDETECT);
            return;
        } else if (v.getId() == R.id.ingredient_button) {
            OcrOpenUtils.getInstance().recIngredient(OcrModuleTestActivity.this, filePath, REQUEST_CODE_INGREDIENT);
            return;
        }
    }

    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            Toast.makeText(getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasGotToken;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
            String filePath = data.getStringExtra(CameraActivity.KEY_OUTPUT_FILE_PATH);

            if (requestCode == REQUEST_CODE_CAMERA_OUTLINE && resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (!TextUtils.isEmpty(contentType)) {
                        if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                            OcrUtils.scanIDResult( filePath, "front",  new OcrUtils.OnIDCardOcrResultListenerAli(){
                                @Override
                                public void onOcrResultIDCard(IDCardResultBean idCardResult) {
                                    if (idCardResult == null) return;
                                    alertText("", GsonUtils.toJson(idCardResult));
                                }

                                @Override
                                public void onError(com.baidu.net.bean.OCRError ocrError) {
                                    ocrError.getErrorMessage();
                                }
                            });
                        } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                            OcrUtils.scanIDResult( filePath, "back",new OcrUtils.OnIDCardOcrResultListenerAli() {
                                @Override
                                public void onOcrResultIDCard(IDCardResultBean idCardResult) {
                                    if (idCardResult == null) return;
                                    alertText("", GsonUtils.toJson(idCardResult));
                                }

                                @Override
                                public void onError(com.baidu.net.bean.OCRError ocrError) {
                                    ocrError.getErrorMessage();
                                }
                            });
                        }
                    }
                }
            }

            if (requestCode == REQUEST_CODE_BANKCARD_OUTLINE && resultCode == Activity.RESULT_OK) {
                OcrUtils.scanBcrResult( filePath, new OcrUtils.OnBankCardOcrResultListenerAli() {
                    @Override
                    public void onOcrResultBankCard(BankCardResultBean bankCardResult) {
                        if (bankCardResult == null) return;
                        alertText("", GsonUtils.toJson(bankCardResult));
                    }

                    @Override
                    public void onError(com.baidu.net.bean.OCRError ocrError) {
                        ocrError.getErrorMessage();
                    }
                });
            }
            if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
                if (!TextUtils.isEmpty(contentType)) {
                    if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                        OcrUtils.recIDCard(true, filePath, "front", Global.getToken(), new OcrUtils.OnIDCardOcrResultListenerAli() {
                            @Override
                            public void onOcrResultIDCard(IDCardResultBean idCardResult) {
                                if (idCardResult == null) return;
                                alertText("", GsonUtils.toJson(idCardResult));
                            }

                            @Override
                            public void onError(com.baidu.net.bean.OCRError ocrError) {
                                ocrError.getErrorMessage();
                            }
                        });
                    } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                        OcrUtils.recIDCard(true, filePath, "back", Global.getToken(), new OcrUtils.OnIDCardOcrResultListenerAli() {
                            @Override
                            public void onOcrResultIDCard(IDCardResultBean idCardResult) {
                                if (idCardResult == null) return;
                                alertText("", GsonUtils.toJson(idCardResult));
                            }

                            @Override
                            public void onError(com.baidu.net.bean.OCRError ocrError) {
                                ocrError.getErrorMessage();
                            }
                        });
                    }
                }
            }

            // 识别成功回调，银行卡识别
            else if (requestCode == REQUEST_CODE_BANKCARD && resultCode == Activity.RESULT_OK) {
                OcrUtils.recBankCard(true, filePath, Global.getToken(), new OcrUtils.OnBankCardOcrResultListenerAli() {
                    @Override
                    public void onOcrResultBankCard(BankCardResultBean bankCardResult) {
                        if (bankCardResult == null) return;
                        alertText("", GsonUtils.toJson(bankCardResult));
                    }

                    @Override
                    public void onError(com.baidu.net.bean.OCRError ocrError) {
                        ocrError.getErrorMessage();
                    }
                });
            }
            // 识别成功回调，营业执照识别
            else if (requestCode == REQUEST_CODE_BUSINESS_LICENSE && resultCode == Activity.RESULT_OK) {
                OcrUtils.recBusinessLicense(true, filePath, Global.getToken(), new OcrUtils.OnBusinessLicenseOcrResultListenerAli() {
                    @Override
                    public void OnOcrResultBusinessLicense(BusinessLicenseResultBean businessLicenseResult) {
                        if (businessLicenseResult == null) return;
                        alertText("", GsonUtils.toJson(businessLicenseResult));
                    }

                    @Override
                    public void onError(com.baidu.net.bean.OCRError ocrError) {
                        ocrError.getErrorMessage();
                    }
                });
            }

            // 识别成功回调，动物识别
            else if (requestCode == REQUEST_CODE_ANIMALDETECT && resultCode == Activity.RESULT_OK) {
                OcrUtils.animalDetect(true, filePath, Global.getToken(), new OcrUtils.OnAnimalDetectResultListenerAi() {
                    @Override
                    public void onAnimalDetectLicense(ResultBean resultBean) {
                        if (resultBean == null) return;
                        alertText("", GsonUtils.toJson(resultBean));
                    }

                    @Override
                    public void onError(com.baidu.net.bean.OCRError ocrError) {
                        ocrError.getErrorMessage();
                    }
                });
            }
            // 识别成功回调，植物识别
            else if (requestCode == REQUEST_CODE_PLANTDETECT && resultCode == Activity.RESULT_OK) {
                OcrUtils.plantDetect(true, filePath, Global.getToken(), new OcrUtils.OnPlantDetectResultListenerAi() {
                    @Override
                    public void onPlantDetectLicense(ResultBean resultBean) {
                        if (resultBean == null) return;
                        alertText("", GsonUtils.toJson(resultBean));
                    }

                    @Override
                    public void onError(com.baidu.net.bean.OCRError ocrError) {
                        ocrError.getErrorMessage();
                    }
                });
            }

            // 识别成功回调，果蔬识别
            else if (requestCode == REQUEST_CODE_INGREDIENT && resultCode == Activity.RESULT_OK) {
                OcrUtils.ingredient(true, filePath, Global.getToken(), new OcrUtils.OnIngredientResultListenerAi() {
                    @Override
                    public void onIngredientLicense(ResultBean resultBean) {
                        if (resultBean == null) return;
                        alertText("", GsonUtils.toJson(resultBean));
                    }

                    @Override
                    public void onError(com.baidu.net.bean.OCRError ocrError) {
                        ocrError.getErrorMessage();
                    }
                });
            }
        }
    }

    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }


}