package com.gykj.mvpbasemodule.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import com.gykj.mvpbasemodule.R;

/**
 * Created by zy on 2018/2/14.
 */

public class LoadingDialog extends Dialog {

    private LinearLayout llLoading, llError;

    private TextView tv_msg;
    private String msg = "";
    private boolean isInit;
    private @ColorInt int mProgressColor;
    public LoadingDialog(@NonNull Context context, @ColorInt int progressColor) {
        super(context);
        mProgressColor = progressColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isInit) {
            isInit = true;
            setContentView(R.layout.widget_dialog_loading_layout);
            setCanceledOnTouchOutside(false);
            llLoading = findViewById(R.id.llLoading);
            llError = findViewById(R.id.llError);
            tv_msg = findViewById(R.id.tv_msg);
            ProgressBar progressBar = findViewById(R.id.progressbar);
            if (!msg.equals("")) {
                tv_msg.setText(msg);
            }
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getWindow().getDecorView().setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
            progressBar.getIndeterminateDrawable().setColorFilter(mProgressColor, PorterDuff.Mode.SRC_ATOP);

        }
        llLoading.setVisibility(View.VISIBLE);
        llError.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismiss();
    }

    public void error() {
        llLoading.setVisibility(View.GONE);
        llError.setVisibility(View.VISIBLE);
    }

    public void setMsg(String msg1) {
        this.msg = msg1;
    }

    public void setLoadingMsg(String msg) {
        if (!TextUtils.isEmpty(msg) && tv_msg != null)
            tv_msg.setText(msg);

    }
}
