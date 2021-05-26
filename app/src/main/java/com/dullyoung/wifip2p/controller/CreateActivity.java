package com.dullyoung.wifip2p.controller;

import android.graphics.Bitmap;
import android.os.Build;
import android.widget.ImageView;

import com.dullyoung.wifip2p.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;

public class CreateActivity extends BaseActivity {


    @BindView(R.id.iv_pic)
    ImageView mIvPic;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_create;
    }

    @Override
    protected void initViews() {
        String content = Build.DEVICE + "----" + Build.BRAND + "--"
                + new SimpleDateFormat("hh:mm:ss", Locale.CHINA).format(new Date());
        //HashMap设置二维码参数
        Map map = new HashMap();
        //  设置容错率 L>M>Q>H 等级越高扫描时间越长,准确率越高
        map.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        //设置字符集
        map.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //设置外边距
        map.put(EncodeHintType.MARGIN, 1);
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = null;
        try {
            bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 500, 500, map);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        mIvPic.setImageBitmap(bitmap);
    }
}