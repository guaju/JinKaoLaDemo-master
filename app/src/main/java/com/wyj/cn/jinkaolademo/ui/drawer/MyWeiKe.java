package com.wyj.cn.jinkaolademo.ui.drawer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.wyj.cn.jinkaolademo.R;

/**
 * Created by acer-pc on 2018/1/2.
 */

public class MyWeiKe extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.activity_myweike);

    }
}
