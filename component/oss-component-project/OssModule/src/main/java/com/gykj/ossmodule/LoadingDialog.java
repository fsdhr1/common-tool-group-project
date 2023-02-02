package com.gykj.ossmodule;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by zy on 2018/2/14.
 */

public class LoadingDialog extends Dialog {

    private Activity activity;
    private LinearLayout llLoading, llError;

    private TextView tv_msg;
    private String msg = "";

    public LoadingDialog(Context context) {
        super(context);
        activity = (Activity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oss_widget_dialog_loading_layout);
        setCanceledOnTouchOutside(false);
        getWindow().setDimAmount(0f);

        llLoading = findViewById(R.id.llLoading);
        llError = findViewById(R.id.llError);
        llLoading.setVisibility(View.VISIBLE);
        llError.setVisibility(View.GONE);
        tv_msg = findViewById(R.id.tv_msg);
        if (!msg.equals("")) {
            tv_msg.setText(msg);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismiss();
        activity.finish();
    }

    public void error() {
        llLoading.setVisibility(View.GONE);
        llError.setVisibility(View.VISIBLE);
    }

    public void setMsg(String msg1) {
        this.msg = msg1;
        if(tv_msg!=null)
            tv_msg.setText(msg);
    }
}
