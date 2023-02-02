package com.gykj.grandphotos.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.gykj.grandphotos.R;
import com.gykj.grandphotos.ui.dialog.LoadingDialog;
import com.gykj.grandphotos.constant.GrandCode;
import com.gykj.grandphotos.constant.Key;
import com.gykj.grandphotos.models.album.AlbumModel;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;
import com.gykj.grandphotos.result.Result;
import com.gykj.grandphotos.setting.Setting;
import com.gykj.grandphotos.ui.adapter.PreviewPhotosAdapter;
import com.gykj.grandphotos.ui.widget.PressedTextView;
import com.gykj.grandphotos.utils.Color.ColorUtils;
import com.gykj.grandphotos.utils.system.SystemUtils;

import java.sql.Time;
import java.util.ArrayList;

import static android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE;

/**
 * 预览页
 */
public class PreviewActivity extends AppCompatActivity implements PreviewPhotosAdapter.OnClickListener, View.OnClickListener, PreviewFragment.OnPreviewFragmentClickListener {

    public static void start(Activity act) {
        if (Result.photos.size() == 0) {
            Toast.makeText(act, "请选择照片", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(act, PreviewActivity.class);
//        intent.putExtra(Key.PREVIEW_ALBUM_ITEM_INDEX, albumItemIndex);
//        intent.putExtra(Key.PREVIEW_PHOTO_INDEX, currIndex);
        act.startActivityForResult(intent, GrandCode.REQUEST_PREVIEW_ACTIVITY);
    }


    /**
     * 一些旧设备在UI小部件更新之间需要一个小延迟
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @Override
        public void run() {
            SystemUtils.getInstance().systemUiHide(PreviewActivity.this, decorView);
        }
    };
    private RelativeLayout mBottomBar;
    private FrameLayout mToolBar;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // 延迟显示UI元素
            mBottomBar.setVisibility(View.VISIBLE);
            mToolBar.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    View decorView;
    private TextView tvOriginal, tvNumber;
    private PressedTextView tvDone;
    private ImageView ivSelector;
    private RecyclerView rvPhotos;
    private PreviewPhotosAdapter topPhotoAdapter;
    private PagerSnapHelper snapHelper;
    private LinearLayoutManager lm;
    private ArrayList<GrandPhotoBean> photos = new ArrayList<>();
    private int resultCode = RESULT_CANCELED;
    private int lastPosition = 0;//记录recyclerView最后一次角标位置，用于判断是否转换了item
    private boolean isSingle = Setting.count == 1;
    private boolean unable = Result.count() == Setting.count;

    private FrameLayout flFragment;
    private PreviewFragment previewFragment;
    private int statusColor;
    private ImageView btRouteLeft, btRouteRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();
        SystemUtils.getInstance().systemUiInit(this, decorView);

        setContentView(R.layout.activity_preview_easy_photos);

        hideActionBar();
        adaptationStatusBar();
        if (null == AlbumModel.instance) {
            finish();
            return;
        }
        initData();
        initView();
    }

    private void adaptationStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            statusColor = ContextCompat.getColor(this, R.color.easy_photos_status_bar);
            if (ColorUtils.isWhiteColor(statusColor)) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }


    private void initData() {
        Intent intent = getIntent();
        photos.clear();
        // 清除一下缓存
        Result.initPhotos();
        photos.addAll(Result.photos);// 选中图片
        mVisible = true;
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        AlphaAnimation hideAnimation = new AlphaAnimation(1.0f, 0.0f);
        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBottomBar.setVisibility(View.GONE);
                mToolBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        hideAnimation.setDuration(UI_ANIMATION_DELAY);
        mBottomBar.startAnimation(hideAnimation);
        mToolBar.startAnimation(hideAnimation);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);

        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);

    }


    private void show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 16) {
            SystemUtils.getInstance().systemUiShow(this, decorView);
        }

        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.post(mShowPart2Runnable);
    }

    @Override
    public void onPhotoClick() {
        toggle();
    }

    @Override
    public void onPhotoScaleChanged() {
        if (mVisible) hide();
    }

    @Override
    public void onBackPressed() {
        doBack();
    }

    private void doBack() {
        Intent intent = new Intent();
        intent.putExtra(Key.PREVIEW_CLICK_DONE, false);
        setResult(resultCode, intent);
        finish();
    }

    private void initView() {
        setClick(R.id.iv_back, R.id.tv_edit, R.id.tv_selector);

        mToolBar = (FrameLayout) findViewById(R.id.m_top_bar_layout);
        if (!SystemUtils.getInstance().hasNavigationBar(this)) {
            FrameLayout mRootView = (FrameLayout) findViewById(R.id.m_root_view);
            mRootView.setFitsSystemWindows(true);
            mToolBar.setPadding(0, SystemUtils.getInstance().getStatusBarHeight(this), 0, 0);
            if (ColorUtils.isWhiteColor(statusColor)) {
                SystemUtils.getInstance().setStatusDark(this, true);
            }
        }
        btRouteLeft = (ImageView) findViewById(R.id.bt_icon_left);
        btRouteRight = (ImageView) findViewById(R.id.bt_icon_right);
        mBottomBar = (RelativeLayout) findViewById(R.id.m_bottom_bar);
        ivSelector = (ImageView) findViewById(R.id.iv_selector);
        tvNumber = (TextView) findViewById(R.id.tv_number);
        tvDone = (PressedTextView) findViewById(R.id.tv_done);
        tvOriginal = (TextView) findViewById(R.id.tv_original);
        flFragment = (FrameLayout) findViewById(R.id.fl_fragment);
        previewFragment =
                (PreviewFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_preview);
        if (Setting.showOriginalMenu) {
            processOriginalMenu();
        } else {
            tvOriginal.setVisibility(View.GONE);
        }

        setClick(tvOriginal, tvDone, ivSelector, btRouteLeft, btRouteRight);

        initRecyclerView();
        shouldShowMenuDone();
        //
        if (photos != null) {
            for (GrandPhotoBean mPhoto : photos) {
                if (mPhoto.isClick) {
                    int mI = mPhoto.selPos - 1;
                    if (mI >= 0) {
                        rvPhotos.scrollToPosition(mI);
                        previewFragment.setSelectedPosition(lastPosition = mI);
                    }
                }
            }
        }
    }

    private void initRecyclerView() {
        rvPhotos = (RecyclerView) findViewById(R.id.rv_photos);
        topPhotoAdapter = new PreviewPhotosAdapter(this, photos, this);
        lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvPhotos.setLayoutManager(lm);
        rvPhotos.setAdapter(topPhotoAdapter);
        toggleSelector();
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvPhotos);
        rvPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    View view = snapHelper.findSnapView(lm);
                    if (view == null) {
                        return;
                    }
                    int position = lm.getPosition(view);
                    lastPosition = position;
                    GrandPhotoBean mPhoto = photos.get(position);
                    mPhoto.setAnimateTime(0);
                    topPhotoAdapter.notifyDataSetChanged();
                    Log.e("onScrollStateChanged: ", " --- findSnapView --- ");
                    tvNumber.setText(getString(R.string.preview_current_number_easy_photos,
                            lastPosition + 1, photos.size()));
                    toggleSelector();
                }
            }
        });
        tvNumber.setText(getString(R.string.preview_current_number_easy_photos, 1,
                photos.size()));
    }

    private boolean clickDone = false;
    LoadingDialog loadingDialog;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.iv_back == id) {
            doBack();
        } else if (R.id.tv_selector == id) {
            updateSelector();
        } else if (R.id.iv_selector == id) {
            updateSelector();
        } else if (R.id.tv_original == id) {
            if (!Setting.originalMenuUsable) {
                Toast.makeText(getApplicationContext(), Setting.originalMenuUnusableHint, Toast.LENGTH_SHORT).show();
                return;
            }
            Setting.selectedOriginal = !Setting.selectedOriginal;
            processOriginalMenu();
        } else if (R.id.tv_done == id) {
            if (clickDone) return;
            clickDone = true;
            loadingDialog = LoadingDialog.get(PreviewActivity.this);
            loadingDialog.show();
            Intent intent = new Intent();
            intent.putExtra(Key.PREVIEW_CLICK_DONE, true);
            setResult(RESULT_OK, intent);
            finish();
        } else if (R.id.bt_icon_left == id) {
            takeImgsRotate(true);
        } else if (R.id.bt_icon_right == id) {
            takeImgsRotate(false);
        }
    }

    @Override
    protected void onStop() {
        if (loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
        super.onStop();
    }


    /**
     * left true、right false
     *
     * @param isLeftRotate
     */
    private void takeImgsRotate(boolean isLeftRotate) {
        topPhotoAdapter.setRoateOnce(lastPosition, isLeftRotate);
        previewFragment.setSelectedPosition(lastPosition);
    }

    private void processOriginalMenu() {
        if (Setting.selectedOriginal) {
            tvOriginal.setTextColor(ContextCompat.getColor(this, R.color.easy_photos_fg_accent));
        } else {
            if (Setting.originalMenuUsable) {
                tvOriginal.setTextColor(ContextCompat.getColor(this,
                        R.color.easy_photos_fg_primary));
            } else {
                tvOriginal.setTextColor(ContextCompat.getColor(this,
                        R.color.easy_photos_fg_primary_dark));
            }
        }
    }

    private void toggleSelector() {
        if (photos.get(lastPosition).selected) {
            ivSelector.setImageResource(R.drawable.ic_selector_true_easy_photos);
            if (!Result.isEmpty()) {
                int count = Result.count();
                for (int i = 0; i < count; i++) {
                    if (photos.get(lastPosition).path.equals(Result.getPhotoPath(i))) {
                        previewFragment.setSelectedPosition(lastPosition);
                        break;
                    }
                }
            }
        } else {
            ivSelector.setImageResource(R.drawable.ic_selector_easy_photos);
        }
        shouldShowMenuDone();
    }

    private void updateSelector() {
        resultCode = RESULT_OK;
        GrandPhotoBean item = photos.get(lastPosition);
        if (isSingle) {
            singleSelector(item);
            return;
        }
        if (unable) {
            if (item.selected) {
                Result.removePhoto(item);
                if (unable) {
                    unable = false;
                }
                toggleSelector();
                return;
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.selector_reach_max_image_hint_easy_photos,
                        Setting.count), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        item.selected = !item.selected;
        if (item.selected) {
            int res = Result.addPhoto(item);
            if (res != 0) {
                item.selected = false;
                return;
            }
            if (Result.count() == Setting.count) {
                unable = true;
            }
        } else {
            Result.removePhoto(item);
            previewFragment.setSelectedPosition(-1);
            if (unable) {
                unable = false;
            }
        }
        toggleSelector();
    }

    private void singleSelector(GrandPhotoBean photo) {
        if (!Result.isEmpty()) {
            if (Result.getPhotoPath(0).equals(photo.path)) {
                Result.removePhoto(photo);
            } else {
                Result.removePhoto(0);
                Result.addPhoto(photo);
            }
        } else {
            Result.addPhoto(photo);
        }
        toggleSelector();
    }

    private void shouldShowMenuDone() {
        if (Result.isEmpty()) {
            if (View.VISIBLE == tvDone.getVisibility()) {
                ScaleAnimation scaleHide = new ScaleAnimation(1f, 0f, 1f, 0f);
                scaleHide.setDuration(200);
                tvDone.startAnimation(scaleHide);
            }
            tvDone.setVisibility(View.GONE);
            flFragment.setVisibility(View.GONE);
        } else {
            if (View.GONE == tvDone.getVisibility()) {
                ScaleAnimation scaleShow = new ScaleAnimation(0f, 1f, 0f, 1f);
                scaleShow.setDuration(200);
                tvDone.startAnimation(scaleShow);
            }
            flFragment.setVisibility(View.VISIBLE);
            tvDone.setVisibility(View.VISIBLE);
            tvDone.setText(getString(R.string.selector_action_done_easy_photos, Result.count(),
                    Setting.count));
        }
    }

    @Override
    public void onPreviewPhotoClick(int position) {
        String path = Result.getPhotoPath(position);
        int size = photos.size();
        for (int i = 0; i < size; i++) {
            GrandPhotoBean mPhoto = photos.get(i);
            if (TextUtils.equals(path, mPhoto.path)) {
                mPhoto.setAnimateTime(0);// 点击的动画时长为0
                rvPhotos.scrollToPosition(i);
                topPhotoAdapter.notifyDataSetChanged();
                lastPosition = i;
                tvNumber.setText(getString(R.string.preview_current_number_easy_photos,
                        lastPosition + 1, photos.size()));
                toggleSelector();
                return;
            }
        }
    }

    private void setClick(@IdRes int... ids) {
        for (int id : ids) {
            findViewById(id).setOnClickListener(this);
        }
    }

    private void setClick(View... views) {
        for (View v : views) {
            v.setOnClickListener(this);
        }
    }
}
