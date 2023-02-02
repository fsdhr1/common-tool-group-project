package com.gykj.base.activity;

import android.content.Intent;

/**
 * Created by zy on 2017/12/28.
 */


public interface IOnActivityResult {
    public void onActivityResult(int requestCode, int resultCode, Intent data);
}
