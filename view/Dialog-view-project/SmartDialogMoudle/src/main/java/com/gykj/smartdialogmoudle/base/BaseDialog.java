package com.gykj.smartdialogmoudle.base;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.gykj.smartdialogmoudle.utils.StatusBarUtils;


public abstract class BaseDialog<T extends BaseDialog<T>> extends Dialog {

    private Context context;

    private DisplayMetrics dm;

    private boolean cancel;

    private float widthScale = 1;

    private float heightScale;

    private BaseAnimatorSet showAnim;

    private BaseAnimatorSet dismissAnim;

    private LinearLayout ll_top;

    protected LinearLayout ll_control_height;

    private boolean isShowAnim;

    private boolean isDismissAnim;

    private float maxHeight;

    public BaseDialog(Context context) {
        super(context);
        this.context = context;
    }

    public BaseDialog(Context context,int mStyle) {
        super(context,mStyle);
        this.context = context;
    }

    public abstract View onCreateView();

    public abstract void setUiBeforShow();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dm = context.getResources().getDisplayMetrics();
        ll_top = new LinearLayout(context);
        ll_top.setGravity(Gravity.CENTER);

        ll_control_height = new LinearLayout(context);
        ll_control_height.setOrientation(LinearLayout.VERTICAL);

        ll_control_height.addView(onCreateView());
        ll_top.addView(ll_control_height);

        maxHeight = dm.heightPixels - StatusBarUtils.getHeight(context);
        setContentView(ll_top, new ViewGroup.LayoutParams(dm.widthPixels, (int) maxHeight));
        setCanceledOnTouchOutside(true);

        ll_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancel) {
                    dismiss();
                }
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        setUiBeforShow();

        int width;
        if (widthScale == 0) {
            width = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            width = (int) (dm.widthPixels * widthScale);
        }

        int height;
        if (heightScale == 0) {
            height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else if (heightScale == 1) {
            height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            height = (int) (maxHeight * heightScale);
        }

        ll_control_height.setLayoutParams(new LinearLayout.LayoutParams(width, height));

        if (showAnim != null) {
            showAnim.listener(new BaseAnimatorSet.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    isShowAnim = true;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isShowAnim = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isShowAnim = false;
                }
            }).playOn(ll_control_height);
        } else {
            BaseAnimatorSet.reset(ll_control_height);
        }
    }


    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        this.cancel = cancel;
        super.setCanceledOnTouchOutside(cancel);
    }

    @Override
    public void dismiss() {
        if (dismissAnim != null) {
            dismissAnim.listener(new BaseAnimatorSet.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    isDismissAnim = true;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isDismissAnim = false;
                    superDismiss();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isDismissAnim = false;
                    superDismiss();
                }
            }).playOn(ll_control_height);
        } else {
            superDismiss();
        }
    }


    public void superDismiss() {
        super.dismiss();
    }


    public void show(int animStyle) {
        Window window = getWindow();
        window.setWindowAnimations(animStyle);
        show();
    }




    public T widthScale(float widthScale) {
        this.widthScale = widthScale;
        return (T) this;
    }


    public T heightScale(float heightScale) {
        this.heightScale = heightScale;
        return (T) this;
    }

    public T showAnim(BaseAnimatorSet showAnim) {
        this.showAnim = showAnim;
        return (T) this;
    }


    public T dismissAnim(BaseAnimatorSet dismissAnim) {
        this.dismissAnim = dismissAnim;
        return (T) this;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isDismissAnim || isShowAnim) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (isDismissAnim || isShowAnim) {
            return;
        }
        super.onBackPressed();
    }

}
