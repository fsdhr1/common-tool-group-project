package com.gykj.autoupdate.weight;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.PathUtils;
import com.gykj.autoupdate.R;
import com.gykj.autoupdate.bean.SysVersion;
import com.gykj.autoupdate.net.DownloadListner;
import com.gykj.autoupdate.net.DownloadManager;
import com.gykj.autoupdate.utils.DensityUtil;
import com.gykj.autoupdate.utils.FileUtil;
import com.gykj.autoupdate.utils.InstallUtil;
import com.gykj.autoupdate.utils.ScreenUtil;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import androidx.annotation.NonNull;

import java.io.File;

public class UpdateDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private boolean forcedUpgrade;
    private Button update;
    private int dialogImage = -1;
    private int dialogButtonTextColor = -1;
    private int dialogButtonColor = -1;
    private LinearLayout llUpdate;
    private CircleProgressBar progressBar;
    private String url;
    private String DEFAULT_FILE_DIR;
    private LinearLayout llProgress;
    private ImageView ivBg;
    private View line;
    private View ibClose;
    private TextView title;
    private TextView description;
    private DownloadManager mDownloadManager;
    private boolean isMultithread;
    private CloseLisener mCloseLisener;

    public UpdateDialog(@NonNull Context context, String url, SysVersion sysVersion) {
        super(context, R.style.UpdateDialog);
        this.context = context;
        this.url = url;
        initDialogView(sysVersion);
    }

    public UpdateDialog(@NonNull Context context, String url, SysVersion sysVersion, boolean isMultithread) {
        super(context, R.style.UpdateDialog);
        this.context = context;
        this.url = url;
        this.isMultithread = isMultithread;
        initDialogView(sysVersion);
    }

    public UpdateDialog(@NonNull Context context, String url, SysVersion sysVersion, int dialogImg, int dialogButtonTextColor, int dialogButtonColor, boolean isMultithread) {
        super(context, R.style.UpdateDialog);
        this.context = context;
        this.url = url;
        this.dialogImage = dialogImg;
        this.dialogButtonTextColor = dialogButtonTextColor;
        this.dialogButtonColor = dialogButtonColor;
        this.isMultithread = isMultithread;
        initDialogView(sysVersion);
    }

    public UpdateDialog(@NonNull Context context, String url, SysVersion sysVersion, int dialogImg, int dialogButtonTextColor, int dialogButtonColor) {
        super(context, R.style.UpdateDialog);
        this.context = context;
        this.url = url;
        this.dialogImage = dialogImg;
        this.dialogButtonTextColor = dialogButtonTextColor;
        this.dialogButtonColor = dialogButtonColor;
        initDialogView(sysVersion);
    }

    /**
     * 初始化布局
     */
    private void initDialogView(SysVersion configuration) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_update_dialog, null);
        setContentView(view);
        setWindowSize(context);

        // 更新页面
        llUpdate = view.findViewById(R.id.ll_update);
        ivBg = view.findViewById(R.id.iv_bg);
        title = view.findViewById(R.id.tv_title);
        line = view.findViewById(R.id.line);
        description = view.findViewById(R.id.tv_description);
        // 更新按钮
        update = view.findViewById(R.id.btn_update);
        update.setOnClickListener(this);

        // 下载进度页面
        llProgress = view.findViewById(R.id.ll_progress);
        progressBar = view.findViewById(R.id.progressBar);
        // 关闭按钮
        ibClose = view.findViewById(R.id.ib_close);
        ibClose.setOnClickListener(this);


        if (!TextUtils.isEmpty(configuration.getVersionName())) {
            String newVersion = "发现新版本v%s可以下载啦！";
            title.setText(String.format(newVersion, configuration.getVersionName()));
        }
        description.setText(configuration.getVersionTip());
        // 是否强制更新
        forcedUpgrade = configuration.getUpdateType() == 1;
        if (forcedUpgrade) {
            line.setVisibility(View.GONE);
            ibClose.setVisibility(View.GONE);
            //屏蔽返回键
            setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            });
        }


        //自定义
        if (dialogImage != -1) {
            ivBg.setBackgroundResource(dialogImage);
        } else {
            ivBg.setBackgroundResource(R.drawable.ic_dialog_default);
        }
        if (dialogButtonTextColor != -1) {
            update.setTextColor(dialogButtonTextColor);
        } else {
            update.setTextColor(this.getContext().getResources().getColor(R.color.white));
        }
        if (dialogButtonColor != -1) {
            StateListDrawable drawable = new StateListDrawable();
            GradientDrawable colorDrawable = new GradientDrawable();
            colorDrawable.setColor(dialogButtonColor);
            colorDrawable.setCornerRadius(DensityUtil.dip2px(context, 3));
            drawable.addState(new int[]{android.R.attr.state_pressed}, colorDrawable);
            drawable.addState(new int[]{}, colorDrawable);
            update.setBackgroundDrawable(drawable);
        } else {
            StateListDrawable drawable = new StateListDrawable();
            GradientDrawable colorDrawable = new GradientDrawable();
            colorDrawable.setColor(this.getContext().getResources().getColor(R.color.color_dialog_defult));
            colorDrawable.setCornerRadius(DensityUtil.dip2px(context, 3));
            drawable.addState(new int[]{android.R.attr.state_pressed}, colorDrawable);
            drawable.addState(new int[]{}, colorDrawable);
            update.setBackgroundDrawable(drawable);
        }

    }

    public void setCloseClickListener(CloseLisener listener){
        this.mCloseLisener = listener;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ib_close) {// 关闭弹窗
            if (mCloseLisener!=null){
                mCloseLisener.closed();
            }
            dismiss();

        } else if (i == R.id.btn_update) {
            update.setEnabled(false);
            update.setText("正在后台下载新版本…");
            // 显示进度页面
            llUpdate.setVisibility(View.GONE);
            llProgress.setVisibility(View.VISIBLE);
            progressBar.setShowArrow(true);
            // 下载apk
            downloadApk();
            // 下载中不允许取消;
            setCancelable(false);
            setCanceledOnTouchOutside(false);
        }
    }

    /**
     * 下载apk
     */
    private void downloadApk() {
        mDownloadManager = DownloadManager.getInstance();
        mDownloadManager.setMultithread(isMultithread);
        final String mExternalAppCachePath = PathUtils.getExternalAppCachePath();
        mDownloadManager.add(url, mExternalAppCachePath, new DownloadListner() {
            @Override
            public void onFinished() {
                dismiss();
                String fileName = FileUtil.getFileName(url);
                InstallUtil installUtil = new InstallUtil(context, mExternalAppCachePath + File.separator + fileName);
                installUtil.install();

            }

            @Override
            public void onProgress(int progress) {
                progressBar.setProgress(progress);
            }

            @Override
            public void onPause() {

            }

            @Override
            public void onCancel() {

            }
        });
        mDownloadManager.download(url);
        //是否开启多线程，（服务是否支持多线程？）
    }

    private void setWindowSize(Context context) {
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenUtil.getWith(context) * 0.7f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
    }

    public interface CloseLisener{
        public void closed();
    }
}
