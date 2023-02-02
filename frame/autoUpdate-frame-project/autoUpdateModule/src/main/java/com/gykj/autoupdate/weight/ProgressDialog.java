package com.gykj.autoupdate.weight;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.gykj.autoupdate.R;


public class ProgressDialog extends Dialog {
    private static ProgressDialog progressDialog = null;

    public ProgressDialog(Context context) {
        super(context);
    }

    public ProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static ProgressDialog createDialog(Context context) {
        progressDialog = new ProgressDialog(context, R.style.ProgressDialog);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        return progressDialog;
    }

    public void onWindowFocusChanged(boolean hasFocus) {

        if (progressDialog == null) {
            return;
        }

        ImageView imageView = (ImageView) progressDialog.findViewById(R.id.imageView1);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.start();
    }

    /**
     * [Summary] setMessage 提示内容
     *
     * @param strMessage
     * @return
     */
    public ProgressDialog setMessage(String strMessage) {
        TextView tvMsg = findViewById(R.id.textView1);

        if (tvMsg != null) {
            tvMsg.setText(strMessage);
        }

        return progressDialog;
    }

    @SuppressLint("NewApi")
    public static void showProgressDialog(Context activity, String paramString) {
        if (progressDialog == null && isLiving((Activity) activity)) {
            progressDialog = ProgressDialog.createDialog(activity);
            progressDialog.setMessage(paramString);
            progressDialog.setCancelable(true);
            progressDialog.show();
            WindowManager windowManager = ((Activity) activity).getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = progressDialog.getWindow().getAttributes();
            Point size = new Point();
            display.getSize(size);
            lp.width = size.x;
            lp.height = size.y;
            progressDialog.getWindow().setAttributes(lp);
        }
    }

    /**
     * 判断activity是否存活
     */
    private static boolean isLiving(Activity activity) {

        if (activity == null) {
            Log.d("wisely", "activity == null");
            return false;
        }

        if (activity.isFinishing()) {
            Log.d("wisely", "activity is finishing");
            return false;
        }

        return true;
    }

    @SuppressLint("NewApi")
    public static void showIrrevocableProgressDialog(Context activity, String paramString) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.createDialog(activity);
            progressDialog.setMessage(paramString);
            progressDialog.setCancelable(false);
            progressDialog.show();
            WindowManager windowManager = ((Activity) activity).getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = progressDialog.getWindow().getAttributes();
            Point size = new Point();
            display.getSize(size);
            lp.width = size.x;
            lp.height = size.y;
            progressDialog.getWindow().setAttributes(lp);
        }
    }

    public static ProgressDialog getDialog() {
        return progressDialog;
    }

    @SuppressLint("NewApi")
    public static void setDialog(Context activity, String paramString) {
        progressDialog = ProgressDialog.createDialog(activity);
        progressDialog.setMessage(paramString);
        progressDialog.setCancelable(false);
        WindowManager windowManager = ((Activity) activity).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = progressDialog.getWindow().getAttributes();
        Point size = new Point();
        display.getSize(size);
        lp.width = size.x;
        lp.height = size.y;
        progressDialog.getWindow().setAttributes(lp);
    }

    public static void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
        return super.onKeyDown(keyCode, event);
    }
}
