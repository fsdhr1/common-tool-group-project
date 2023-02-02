package com.gykj.commontool.sampledialogtest;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.blankj.utilcode.util.ToastUtils;
import com.gykj.commontool.R;
import com.gykj.smartdialogmoudle.base.DialogAnimType;
import com.gykj.smartdialogmoudle.bean.SelectDialogListBean;
import com.gykj.smartdialogmoudle.dialogs.SimpleDialog;

import java.util.ArrayList;
import java.util.List;

public class SmartDialogModuleTestActivity extends AppCompatActivity implements View.OnClickListener {

    private Button normal,warnning,input,loading,select,selectMore,progress,changeData;
    private int count =0;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x0001:
                    simpleDialog.setmProgress(msg.arg1);
                    //如果进度小于100,则延迟100毫秒之后重复执行runnable
                    if(msg.arg1<100){
                        mHandler.postDelayed(r, 100);
                    }else{
                        //否则，都置零，线程重新执行
                        simpleDialog.dismiss();

                    }
                    break;
            }
        }
    };
    Runnable r = new Runnable() {
        int currentProgress = 0;
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 0x0001;
            currentProgress = simpleDialog.getmProgress()+1;
            simpleDialog.setmProgress(currentProgress);
            msg.arg1 = currentProgress;
            mHandler.sendMessage(msg);
        }
    };
    private SimpleDialog simpleDialog;
    private SimpleDialog selectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_dialog_test);
        normal = findViewById(R.id.bt1);
        warnning=findViewById(R.id.bt2);
        input =findViewById(R.id.bt3);
        loading =findViewById(R.id.bt4);
        select =findViewById(R.id.bt5);
        selectMore =findViewById(R.id.bt6);
        progress =findViewById(R.id.bt7);
        changeData =findViewById(R.id.bt8);

        normal.setOnClickListener(this);
        warnning.setOnClickListener(this);
        input.setOnClickListener(this);
        loading.setOnClickListener(this);
        select.setOnClickListener(this);
        selectMore.setOnClickListener(this);
        progress.setOnClickListener(this);
        changeData.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt1:
                showNormalDialog();
                break;
            case R.id.bt2:
                showWarnningDialog();
                break;
            case R.id.bt3:
                showInputDialog();
                break;
            case R.id.bt4:
                showLoadingDialog();
                break;
            case R.id.bt5:
                showSelectDialog();
                break;
            case R.id.bt6:
                showSelectMoreDialog();
                break;
            case R.id.bt7:
                showProgressDialog();
                break;
            case R.id.bt8:
                setNewData();
                break;
        }
    }

    private void setNewData() {
        List<SelectDialogListBean> list =new ArrayList<>();


        for (int i=0;i<5;i++){
            SelectDialogListBean<OtherBean> selectDialogListBean = new SelectDialogListBean();
            selectDialogListBean.setData(new OtherBean());
            selectDialogListBean.setValue("企业"+i);
            selectDialogListBean.setKey(i+"");
            list.add(selectDialogListBean);
        }
        if (selectDialog!=null){
            selectDialog.setSelectListData(list);
            selectDialog.show();
        }

    }

    private void showProgressDialog() {

        simpleDialog = new SimpleDialog(this, SimpleDialog.PROGRESS_TYPE);
        simpleDialog.setTitle("当前进度")
                .setmMax(100)
                //.setmPbDrawable(R.drawable.progressbar_line)
        .show();
        if(simpleDialog.getmProgress() < simpleDialog.getmMax()){
            // 把r加入到线程队列，然后线程队列里就开始执行runnable对象中的run()
            mHandler.post(r);
        }else{//不用的时候，就把r从线程队列移除，这是一个小的性能优化
            mHandler.removeCallbacks(r);
        }
    }

    private void showSelectMoreDialog() {
        List<SelectDialogListBean> list =new ArrayList<>();

        for (int i=0;i<10;i++){
            SelectDialogListBean selectDialogListBean = new SelectDialogListBean();
            selectDialogListBean.setValue("公司"+i);
            selectDialogListBean.setKey(i+"");
            list.add(selectDialogListBean);
        }

        SimpleDialog simpleDialog = new SimpleDialog(this, SimpleDialog.SELECT_MORE_TYPE);
        simpleDialog.setSelectListData(list)
                .setCancelOnOutside(true)
                .setMoreSelectListener(new SimpleDialog.ListSelectMoreListener() {
                    @Override
                    public void onResultFormDialog(Dialog dialog,List<SelectDialogListBean> t) {
                        ToastUtils.showShort("已选择"+t.size()+"个公司");
                    }
                });
        simpleDialog.show();
    }

    private void showSelectDialog() {
        List<SelectDialogListBean> list =new ArrayList<>();


        for (int i=0;i<10;i++){
            SelectDialogListBean<OtherBean> selectDialogListBean = new SelectDialogListBean();
            selectDialogListBean.setData(new OtherBean());
            selectDialogListBean.setValue("公司"+i);
            selectDialogListBean.setKey(i+"");
            list.add(selectDialogListBean);
        }

        selectDialog = new SimpleDialog(this, SimpleDialog.SELECT_SINGE_TYPE);
        selectDialog.setSelectListData(list)
                .setTitle("请选择")
                .setAnmiation(DialogAnimType.SCALE_V)
                .setThemeColor(R.color.blue,R.color.white)
        .setSignSelectListener(new SimpleDialog.ListSelectListener() {
            @Override
            public void onResultFormDialog(Dialog dialog,SelectDialogListBean t) {
                ToastUtils.showShort("已选择第"+t.getKey()+"个公司");

            }
        });

        selectDialog.show();

    }

    private void showLoadingDialog() {
        SimpleDialog simpleDialog = new SimpleDialog(this, SimpleDialog.LOADING_TYPE);
        simpleDialog.setLoadingPic(R.mipmap.dialog_pic)
                .setLoadingText("努力加载中...")
                .show();
    }

    private void showInputDialog() {
        SimpleDialog simpleDialog = new SimpleDialog(this, SimpleDialog.INPUT_TYPE);
        simpleDialog.setTitle("输入")
                .setNegativeButton("拒绝消息")
                .setPositiveButton("好的")
                .setThemeColor(R.color.addddddddddddd,R.color.white)
                .setCellName("元")
                .setConfirmClickListener(new SimpleDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm, String returnMessage) {

                    }
                }).show();
    }

    private void showWarnningDialog() {
        SimpleDialog simpleDialog = new SimpleDialog(this, SimpleDialog.WARNNING_TYPE);
        simpleDialog.setTitle("温馨提示")
                .setMakeSureMessage("我知道了！！！")
                .setContent("提示信息内容")
                .setThemeColor(R.color.addddddddddddd,R.color.white)
                .setWarnningBottomColor(R.color.input_stroke_color,R.color.white)
                .setConfirmClickListener(new SimpleDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm, String returnMessage) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showNormalDialog() {
        SimpleDialog simpleDialog = new SimpleDialog(this, SimpleDialog.NORMAL_TYPE);
        simpleDialog.setTitle("普通")
                .setNegativeButton("拒绝消息")
                .setPositiveButton("好的")
                .setNegativeColor(R.color.white,R.color.fc_4d4d4d)
                .setThemeColor(R.color.addddddddddddd,R.color.white)
                .setContent("提示信息")
                .setCancelClickListener(new SimpleDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm, String returnMessage) {
                        dialog.dismiss();
                    }
                })
                .setConfirmClickListener(new SimpleDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm, String returnMessage) {
                        dialog.dismiss();
                    }
                }).show();
    }


}
