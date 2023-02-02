package com.gykj.commontool.addressselect;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gykj.addressselect.AddressWidgetUtils;
import com.gykj.addressselect.AddressWidget;
import com.gykj.addressselect.bean.AddreBean;
import com.gykj.addressselect.bean.AddressUtils;
import com.gykj.addressselect.interfaces.OnAddressSelectedListener;
import com.gykj.addressselect.interfaces.onAddressConfirmClick;
import com.gykj.commontool.R;

import static com.gykj.addressselect.AddressViewsController.INDEX_TAB_CITY;
import static com.gykj.addressselect.AddressViewsController.INDEX_TAB_COUNTY;
import static com.gykj.addressselect.AddressViewsController.INDEX_TAB_PROVINCE;
import static com.gykj.addressselect.AddressViewsController.INDEX_TAB_TOWN;
import static com.gykj.addressselect.AddressViewsController.INDEX_TAB_VILLAGE;

import java.util.ArrayList;
import java.util.List;

public class AddressSelectActivity extends AppCompatActivity implements OnAddressSelectedListener {

    AddressWidget mAddressWidget;

    AddreBean mSelectBean;// 选中的地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_select);
        /**
         * 显示普通地址选择器
         */
        Button mButton = findViewById(R.id.btn_address);
        mButton.setOnClickListener(v -> {
            mAddressWidget = AddressWidgetUtils.getNewInstance().getWidgetNew(AddressSelectActivity.this);
            mAddressWidget.setOnAddressSelectedListener(AddressSelectActivity.this);
            mAddressWidget.showWidget();
        });

        /**
         * 输入父级区划显示子集区划列表
         */
        Button mBtqhdmsss = findViewById(R.id.btn_input_address);
        EditText mEditText2sss = new EditText(this);
        AlertDialog mDialog22 = createAlertDialog(mEditText2sss);
        mBtqhdmsss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                if (mDialog22 != null) {
                    mDialog22.show();
                }
            }
        });

        /**
         * 输入父级qhdm、qhmc；且显示在第一项
         */
        Button mBtqhdm = findViewById(R.id.btn_input_head_address);
        EditText mEditText = new EditText(this);
        AlertDialog mDialog = createAlertDialog22(mEditText);
        mBtqhdm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                if (mDialog != null) {
                    mDialog.show();
                }
            }
        });

        /**
         * 自定义 - 手动输入区划代码，可选择到任一级别
         */
        Button mBtqhdm2 = findViewById(R.id.btn_input_custom_address);
        EditText mEditText2 = new EditText(this);
        AlertDialog mDialog2 = createCustomAddressAlertDialog(mEditText2);
        mBtqhdm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                if (mDialog2 != null) {
                    mDialog2.show();
                }
            }
        });

        /**
         * 自定义 - 输入区划代码列表--选择其子集
         */
        Button mBtqhdm3 = findViewById(R.id.btn_list_custom_address);
        EditText mEditText3 = new EditText(this);
        AlertDialog mDialog3 = createCustomAddressListAlertDialog(mEditText3);
        mBtqhdm3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                if (mDialog3 != null) {
                    mDialog3.show();
                }
            }
        });

        /**
         * 自定义 - 输入区划代码列表--选择其子集
         * 可以选择到任意位置
         */
        Button mBtqhdm4 = findViewById(R.id.btn_list_custom_address_any_position);
        EditText mEditText4 = new EditText(this);
        AlertDialog mDialog4 = createCustomAddressListAlertDialogAnyPosition(mEditText4);
        mBtqhdm4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                if (mDialog4 != null) {
                    mDialog4.show();
                }
            }
        });

    }

    private AlertDialog createAlertDialog(EditText mEditText) {

        AlertDialog mDialog = new AlertDialog.Builder(this).setTitle("请输入区划代码")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(mEditText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mInterface, int i) {
                        String input = mEditText.getText().toString().trim();
                        if (TextUtils.isEmpty(input)) return;
//                        mBottomAddressDialog.showDialog(input);
                        mAddressWidget = AddressWidgetUtils.getInstance().getWidgetNew(AddressSelectActivity.this);
                        mAddressWidget
                                .setOnAddressSelectedListener(AddressSelectActivity.this::onAddressSelected)
                                .showWidget(input);
                        mInterface.dismiss();
                    }
                }).setNegativeButton("取消", null).create();
        return mDialog;
    }

    private AlertDialog createAlertDialog22(EditText mEditText) {

        AlertDialog mDialog = new AlertDialog.Builder(this).setTitle("请输入区划代码")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(mEditText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mInterface, int i) {
                        String input = mEditText.getText().toString().trim();
                        if (TextUtils.isEmpty(input)) return;
//                        mBottomAddressDialog.showDialog(input);
                        mAddressWidget = AddressWidgetUtils.getInstance().getWidgetNew(AddressSelectActivity.this);
                        mAddressWidget
                                .setOnAddressSelectedListener(AddressSelectActivity.this::onAddressSelected)
                                .showWidget(input, "第一项为区划名称");
                        mInterface.dismiss();
                    }
                }).setNegativeButton("取消", null).create();
        return mDialog;
    }

    private AlertDialog createCustomAddressAlertDialog(EditText mEditText) {

        AlertDialog mDialog = new AlertDialog.Builder(this).setTitle("请输入区划代码")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(mEditText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mInterface, int i) {
                        String input = mEditText.getText().toString().trim();
                        if (TextUtils.isEmpty(input)) return;
//                        mBottomAddressDialog.showDialog(input);
                        mAddressWidget = AddressWidgetUtils.getInstance().getWidgetNew(AddressSelectActivity.this);
                        mAddressWidget
                                .setOnAddressSelectedListener(AddressSelectActivity.this::onAddressSelected)
                                .showWidget(input, "名称", new onAddressConfirmClick() {
                                    @Override
                                    public void onAddressConfirmResp() {
                                        if (mSelectBean != null) {
                                            Toast.makeText(AddressSelectActivity.this, mSelectBean.getQhmc(), Toast.LENGTH_SHORT).show();
                                            mAddressWidget.onDissWidget();
                                        }
                                    }
                                });
                        mInterface.dismiss();
                    }
                }).setNegativeButton("取消", null).create();
        return mDialog;
    }

    private AlertDialog createCustomAddressListAlertDialog(EditText mEditText) {

        AlertDialog mDialog = new AlertDialog.Builder(this).setTitle("请选择区划")
                .setIcon(android.R.drawable.sym_def_app_icon)
//                .setView(mEditText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mInterface, int index) {

                        List<AddreBean> mList = new ArrayList<>();

                        for (int i = 0; i < 2; i++) {
                            AddreBean mBean = new AddreBean();
                            if (i == 0) {
                                mBean.setQhdm("1301");
                                mBean.setQhmc("石家庄");
                            } else if (i == 1) {
                                mBean.setQhdm("2101");
                                mBean.setQhmc("沈阳市");
                            }
                            mList.add(mBean);
                        }

                        mAddressWidget = AddressWidgetUtils.getInstance().getWidgetNew(AddressSelectActivity.this);
                        mAddressWidget
                                .setOnAddressSelectedListener(AddressSelectActivity.this::onAddressSelected)
                                .showWidgetList(mList);
                        mInterface.dismiss();
                    }
                }).setNegativeButton("取消", null).create();
        return mDialog;
    }

    private AlertDialog createCustomAddressListAlertDialogAnyPosition(EditText mEditText) {

        AlertDialog mDialog = new AlertDialog.Builder(this).setTitle("请选择区划")
                .setIcon(android.R.drawable.sym_def_app_icon)
//                .setView(mEditText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mInterface, int index) {

                        List<AddreBean> mList = new ArrayList<>();

                        for (int i = 0; i < 2; i++) {
                            AddreBean mBean = new AddreBean();
                            if (i == 0) {
                                mBean.setQhdm("13");
                                mBean.setQhmc("河北");
                            } else if (i == 1) {
                                mBean.setQhdm("21");
                                mBean.setQhmc("辽宁");
                            }
                            mList.add(mBean);
                        }

                        mAddressWidget = AddressWidgetUtils.getInstance().getWidgetNew(AddressSelectActivity.this);
                        mAddressWidget
                                .setOnAddressSelectedListener(AddressSelectActivity.this::onAddressSelected)
                                .showWidgetList(mList, new onAddressConfirmClick() {
                                    @Override
                                    public void onAddressConfirmResp() {
                                        if (mSelectBean != null) {
                                            String mQhmc = mSelectBean.getQhmc();
                                            Toast.makeText(AddressSelectActivity.this, mQhmc, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        mInterface.dismiss();
                    }
                }).setNegativeButton("取消", null).create();
        return mDialog;
    }


    /**
     * 地址选择器选中的回调
     *
     * @param mIndex
     * @param province
     * @param city
     * @param county
     * @param town
     * @param village
     */
    @Override
    public void onAddressSelected(int mIndex, AddreBean province, AddreBean city, AddreBean county, AddreBean town, AddreBean village) {
        switch (mIndex) {
            case INDEX_TAB_PROVINCE: //
                // 根据省份 查询 城市
                String mQhdm = province.getQhdm();
                String mQhmc = province.getQhmc();
                mSelectBean = province;
                if (AddressUtils.isParentAddress(province)) {
                    Toast.makeText(this, mSelectBean.getQhmc(), Toast.LENGTH_SHORT).show();
                } else {
                    mAddressWidget.getController().retrieveCitiesWith(province);
                }

                break;
            case INDEX_TAB_CITY://
                // 根据城市 查询 县
                String mCityQhdm = city.getQhdm();
                String mCityQhmc = city.getQhmc();
                mSelectBean = city;
                if (AddressUtils.isParentAddress(city)) {
                    Toast.makeText(this, mSelectBean.getQhmc(), Toast.LENGTH_SHORT).show();
                } else {
                    mAddressWidget.getController().retrieveCountiesWith(city);
                }
                break;
            case INDEX_TAB_COUNTY://
                // 根据县找到乡
                String mCountyQhdm = county.getQhdm();
                String mCountyQhmc = county.getQhmc();
                mSelectBean = county;
                if (AddressUtils.isParentAddress(county)) {
                    Toast.makeText(this, mSelectBean.getQhmc(), Toast.LENGTH_SHORT).show();
                } else {
                    mAddressWidget.getController().retrieveTownWith(county);
                }
                break;
            case INDEX_TAB_TOWN:
                // 根据乡找到村
                String mTownQhdm = town.getQhdm();
                String mTownQhmc = town.getQhmc();
                mSelectBean = town;
                if (AddressUtils.isParentAddress(town)) {
                    Toast.makeText(this, mSelectBean.getQhmc(), Toast.LENGTH_SHORT).show();
                } else {
                    mAddressWidget.getController().retrieveVillage(town);
                }
                break;
            case INDEX_TAB_VILLAGE://
                String liveAddress = "";
                if (province != null)
                    liveAddress += province.getQhmc();
                if (city != null)
                    liveAddress += city.getQhmc();
                if (county != null)
                    liveAddress += county.getQhmc();
                if (town != null)
                    liveAddress += town.getQhmc();
                if (village != null)
                    liveAddress += village.getQhmc();

                //
                mSelectBean = village;
                if (AddressUtils.isParentAddress(village)) {
                    Toast.makeText(this, mSelectBean.getQhmc(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, mSelectBean.getQhmc(), Toast.LENGTH_SHORT).show();
                }
                //  mBottomAddressDialog.dissmissDialog();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //
        mAddressWidget.onWidgetDestory();
//        Log.e("mAddressWidget", "----- mAddressWidgetonDestroy -----");
    }
}