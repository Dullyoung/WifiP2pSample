package com.dullyoung.wifip2p.controller;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.dullyoung.wifip2p.PermissionHelper;
import com.jakewharton.rxbinding4.view.RxView;


import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity   {

    protected PermissionHelper mPermissionHelper;


    public PermissionHelper getPermissionHelper() {
        return mPermissionHelper;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(getLayoutId());
        setTranslucentStatus();
        ButterKnife.bind(this);

        mPermissionHelper = new PermissionHelper();
        initVars();
        initViews();
        bindClick();
    }


    protected void initVars() {
    }

    /**
     * 设置view的点击事件
     * 最好结合{@link #setClick(int, Runnable)} 使用
     */
    protected void bindClick() {

    }

    /**
     * @param id       view id
     * @param runnable when click to do sth.
     */
    protected void setClick(@IdRes int id, @NonNull Runnable runnable) {
        RxView.clicks(findViewById(id)).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(view -> {
            runnable.run();
        });
    }

    /**
     * @param view     view
     * @param runnable when click to do sth.
     */
    protected void setClick(@NonNull View view, @NonNull Runnable runnable) {
        RxView.clicks(view).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(view1 -> {
            runnable.run();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionHelper.onRequestPermissionsResult(this, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPermissionHelper.onRequestPermissionsResult(this, requestCode);
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public Context getContext() {
        return this;
    }

    protected abstract int getLayoutId();

    protected abstract void initViews();

    protected void setTranslucentStatus() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.addFlags(Integer.MIN_VALUE);
            window.setStatusBarColor(0x00000000);
        } else if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public void setFullScreen() {
        getWindow().getDecorView().setSystemUiVisibility(1280 | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    /**
     * 刘海屏全屏
     */
    public void setFullScreenWithCutOutScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
    }




}
