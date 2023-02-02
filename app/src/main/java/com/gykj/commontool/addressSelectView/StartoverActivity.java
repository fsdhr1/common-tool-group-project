package com.gykj.commontool.addressSelectView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grantch.addressselectview.data.AddressBean;
import com.gykj.commontool.R;

public class StartoverActivity extends AppCompatActivity {

    private TextView tvAddress;
    private AddressBean addressBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startover);
        RelativeLayout rlAddress =findViewById(R.id.rl_address);
        tvAddress = findViewById(R.id.et_address);
        rlAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               gotoSelectAddress();
            }


        });
    }
    private void gotoSelectAddress() {
        Intent intent = new Intent(this,AddressSelectViewDemoActivity.class);
        if (addressBean!=null){
            intent.putExtra("selected",addressBean);
        }
        startActivityForResult(intent,1003);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1003&&data!=null){
            addressBean = (AddressBean) data.getSerializableExtra("selected");
            if (addressBean!=null){
                tvAddress.setText(addressBean.getQhmc());
            }
        }
    }
}