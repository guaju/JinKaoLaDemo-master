package com.wyj.cn.jinkaolademo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.guaju.screenrecorderlibrary.ScreenRecorderHelper;
import com.wyj.cn.jinkaolademo.app.MyApplication;
import com.wyj.cn.jinkaolademo.ui.WhiteBoardFragment;
import com.wyj.cn.jinkaolademo.widget.UploadRecordFileDialog;

public class MainActivity extends FragmentActivity {


    private WhiteBoardFragment whiteBoardFragment;
    private FragmentManager fm;
    private ScreenRecorderHelper srHelper;
    WhiteBoardFragment wb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        srHelper = MyApplication.getApp().getSRHelper();
        srHelper.initRecordService(this);
        fm = getSupportFragmentManager();
        initPermission();
        initview();
        initListener();


    }

    private void initListener() {

        wb = whiteBoardFragment;
        //等待找到控件
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (wb.luping_img == null) {

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wb.luping_img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                srHelper.startRecord(MainActivity.this);
                            }
                        });

                        wb.zanting_img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                srHelper.stopRecord(new ScreenRecorderHelper.OnRecordStatusChangeListener() {
                                    @Override
                                    public void onChangeSuccess() {
                                        //当停止成功，做界面变化
                                        wb.luping_img.setBackgroundResource(R.drawable.luzhi);
                                        wb.zanting_img.setBackgroundResource(R.drawable.paused);
                                        wb.timer.stop();
                                        //Toast.makeText(MainActivity.this, "录屏成功"+srHelper.getRecordFilePath(), Toast.LENGTH_SHORT).show();
                                        showUploadDialog();

                                    }

                                    @Override
                                    public void onChangeFailed() {
                                        //不作处理

                                    }
                                });
                            }
                        });
                    }
                });
            }
        }).start();




}

    private void showUploadDialog() {
        UploadRecordFileDialog uploadRecordFileDialog = new UploadRecordFileDialog(this);
        wb.timer.setVisibility(View.GONE);
        uploadRecordFileDialog.show();
    }

    private void initPermission() {
        //6.0读写权限
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {

                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE

            };
            ActivityCompat.requestPermissions(this, permissions, 123);

            if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 0);

            }
        }
    }


    private void initview() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //获取WhiteBoardFragment实例
        whiteBoardFragment = WhiteBoardFragment.newInstance();
        transaction.add(R.id.controlLayout, whiteBoardFragment, "wb").commit();

    }
    @Override   //其中123是requestcode，可以根据这个code判断，用户是否同意了授权。
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("tag", "requestCode****" + requestCode);
        srHelper.onActivityResult(this, requestCode, resultCode, data, new ScreenRecorderHelper.OnRecordStatusChangeListener() {
            @Override
            public void onChangeSuccess() {
                //控制开始按钮的文字变化
//                bt_start.setText("正在录制");
//                Toast.makeText(MainActivity.this, "开始录制~~~", Toast.LENGTH_SHORT).show();
                wb.luping_img.setBackgroundResource(R.drawable.recording);
                wb.zanting_img.setBackgroundResource(R.drawable.pause);
                wb.timer.setVisibility(View.VISIBLE);
                wb.timer.setBase(SystemClock.elapsedRealtime());//计时器清零
                wb.timer.setFormat("%s");
                wb.timer.start();
            }

            @Override
            public void onChangeFailed() {
                //如果录制失败，则不作任何变化
//                bt_start.setText("开始录制");
            }
        });
    }

}
