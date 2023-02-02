package com.gykj.grandphotos.utils.result;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gykj.grandphotos.GrandPhotoPickers;
import com.gykj.grandphotos.callback.SelectCallback;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;
import com.gykj.grandphotos.ui.GrandPhotosActivity;

import java.util.ArrayList;


public class HolderFragment extends Fragment {

    private static final int HOLDER_SELECT_REQUEST_CODE = 0x44;
    private SelectCallback mSelectCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void startGrandPhoto(SelectCallback callback) {
        mSelectCallback = callback;
        GrandPhotosActivity.start(this, HOLDER_SELECT_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            switch (requestCode) {
                case HOLDER_SELECT_REQUEST_CODE:
                    if (mSelectCallback != null) {
                        ArrayList<GrandPhotoBean> resultPhotos = data.getParcelableArrayListExtra(GrandPhotoPickers.RESULT_PHOTOS);
                        boolean selectedOriginal = data.getBooleanExtra(GrandPhotoPickers.RESULT_SELECTED_ORIGINAL, false);
                        mSelectCallback.onResult(resultPhotos,  selectedOriginal);
                    }
                    break;
            }
            return;
        }
        if (Activity.RESULT_CANCELED == resultCode) {
            switch (requestCode) {
                case HOLDER_SELECT_REQUEST_CODE:
                    if (mSelectCallback != null) {
                        mSelectCallback.onCancel();
                    }
                    break;
            }
        }
    }
}
