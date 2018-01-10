package com.wyj.cn.jinkaolademo.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.suke.widget.SwitchButton;
import com.wyj.cn.jinkaolademo.MultiImageSelector;
import com.wyj.cn.jinkaolademo.R;
import com.wyj.cn.jinkaolademo.Utils.BitmapUtils;
import com.wyj.cn.jinkaolademo.Utils.ScreenUtils;
import com.wyj.cn.jinkaolademo.Utils.TimeUtils;
import com.wyj.cn.jinkaolademo.adapter.SketchDataGridAdapter;
import com.wyj.cn.jinkaolademo.bean.SketchData;
import com.wyj.cn.jinkaolademo.bean.StrokeRecord;
import com.wyj.cn.jinkaolademo.ui.drawer.MyWeiKe;
import com.wyj.cn.jinkaolademo.widget.SketchView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.wyj.cn.jinkaolademo.bean.StrokeRecord.STROKE_TYPE_CIRCLE;
import static com.wyj.cn.jinkaolademo.bean.StrokeRecord.STROKE_TYPE_DRAW;
import static com.wyj.cn.jinkaolademo.bean.StrokeRecord.STROKE_TYPE_ERASER;
import static com.wyj.cn.jinkaolademo.bean.StrokeRecord.STROKE_TYPE_LINE;
import static com.wyj.cn.jinkaolademo.bean.StrokeRecord.STROKE_TYPE_RECTANGLE;
import static com.wyj.cn.jinkaolademo.bean.StrokeRecord.STROKE_TYPE_TEXT;

public class WhiteBoardFragment extends Fragment implements SketchView.OnDrawChangedListener, View.OnClickListener {

    final String TAG = getClass().getSimpleName();
    private ImageView btn_drawer;
    private SlidingMenu slidingMenu;
    private ImageView btn_xingzhuang;
    private PopupWindow xingzhuangPopwindow, backgroundPopwindow, xuanzePopwindow, tuozhuaiPopwindow,
            beijingPopwind;
    private SwitchButton xingzhuangSwitchButton;
    //private SwitchButton yanseSwitchButton;用于添加滑动透明度
    private SeekBar xingzhuangSeekbar;
    // private SeekBar yanseSeekbar;开关
    private View drawer_view;
    private View rootView;
    private RadioButton btn_qianbi;
    private RadioButton btn_maobi;
    private RadioButton btn_gangbi;
    private SeekBar mystrokeSeekBar;
    private RadioGroup mystrokeColorRG;


    public interface SendBtnCallback {
        void onSendBtnClick(File filePath);
    }

    static final int COLOR_BLACK = Color.parseColor("#ff000000");
    static final int COLOR_RED = Color.parseColor("#ffff4444");
    static final int COLOR_GREEN = Color.parseColor("#ff99cc00");
    static final int COLOR_ORANGE = Color.parseColor("#ffffbb33");
    static final int COLOR_BLUE = Color.parseColor("#ff33b5e5");
    public static final int REQUEST_IMAGE = 2;
    public static final int REQUEST_BACKGROUND = 3;

    private static final float BTN_ALPHA = 0.4f;

    //文件保存目录
    public static final String TEMP_FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/YingHe/temp/";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/YingHe/sketchPhoto/";
    public static final String TEMP_FILE_NAME = "temp_";

    int keyboardHeight;
    int textOffX;
    int textOffY;

    SketchView mSketchView;//画板

    View controlLayout;//控制布局

    //guaju
    public ImageView luping_img,zanting_img;//屏幕录制按钮
    public Chronometer timer;


    ImageView btn_add;//添加画板
    ImageView btn_stroke;//画笔
    ImageView btn_eraser;//橡皮擦
    ImageView btn_undo;//撤销
    ImageView btn_redo;//取消撤销
    ImageView btn_photo;//加载图片
    ImageView btn_background;//背景图片
    ImageView btn_drag;//拖拽
    ImageView btn_save;//保存
    ImageView btn_empty;//清空
    ImageView btn_send;//推送
    //ImageView btn_send_space;//推送按钮间隔
    ImageView btn_zujian;//组件
    ImageView btn_xuanze;
    ImageView btn_tuozhuai;
    ImageView beijing_img;


    RadioGroup strokeTypeRG, strokeColorRG;

    Activity activity;//上下文

    int strokeMode;//模式
    int strokeType;//模式

    EditText saveET;
    AlertDialog saveDialog;
    GridView sketchGV;
    SketchDataGridAdapter sketchGVAdapter;

    int pupWindowsDPWidth = 300;//弹窗宽度，单位DP
    int strokePupWindowsDPHeight = 275;//画笔弹窗高度，单位DP
    int eraserPupWindowsDPHeight = 90;//橡皮擦弹窗高度，单位DP


    SendBtnCallback sendBtnCallback;
    boolean isTeacher;
    PopupWindow strokePopupWindow, mystrkePopwindow, eraserPopupWindow, textPopupWindow,
            zujianPopupWindow, xuanzePopupWindow;//画笔、橡皮擦,组件参数设置弹窗实例
    private View popupStrokeLayout, popupMyStrokeLayout, popupEraserLayout, popupTextLayout, popZuJianLayout, popXingZhuangLayout,
            popbackground, popupXuanze, popupTuozhuai;//画笔、橡皮擦弹窗布局
    private SeekBar strokeSeekBar, strokeAlphaSeekBar, eraserSeekBar;
    private ImageView strokeImageView, strokeAlphaImage, eraserImageView;//画笔宽度，画笔不透明度，橡皮擦宽度IV
    private EditText strokeET;//绘制文字的内容
    private int size;
    private AlertDialog dialog;
    private ArrayList<String> mSelectPath;

    private List<SketchData> sketchDataList = new ArrayList<>();
//    //    private SketchData curSketchData;
//    private List<String> sketchPathList = new ArrayList<>();
//    private int dataPosition;

    //
    public static int sketchViewHeight;
    public static int sketchViewWidth;
    public static int sketchViewRight;
    public static int sketchViewBottom;
    public static int decorHeight;
    public static int decorWidth;

    /**
     * show
     *
     * @author TangentLu
     */
    public static WhiteBoardFragment newInstance() {
        return new WhiteBoardFragment();
    }

    /**
     * show
     *
     * @param callback 推送按钮监听器，接受返回的图片文件路径可用于显示文件
     * @author TangentLu
     */
    public static WhiteBoardFragment newInstance(SendBtnCallback callback) {
        WhiteBoardFragment fragment = new WhiteBoardFragment();
        fragment.sendBtnCallback = callback;
        fragment.isTeacher = true;
        return fragment;
    }

    /**
     * @param imgPath 添加的背景图片文件路径
     * @author TangentLu
     * show 设置当前白板的背景图片
     */
    public void setCurBackgroundByPath(String imgPath) {
        showSketchView(true);
        mSketchView.setBackgroundByPath(imgPath);
    }

    /**
     * show  新增白板并设置白板的背景图片
     *
     * @param imgPath 添加的背景图片文件路径
     * @author TangentLu
     */
    public void setNewBackgroundByPath(String imgPath) {
        showSketchView(true);
        SketchData newSketchData = new SketchData();
        sketchDataList.add(newSketchData);
        mSketchView.updateSketchData(newSketchData);
        setCurBackgroundByPath(imgPath);
        mSketchView.setEditMode(SketchView.EDIT_STROKE);
    }

    /**
     * show 新增图片到当前白板
     *
     * @param imgPath 新增的图片路径
     * @author TangentLu
     */
    public void addPhotoByPath(String imgPath) {
        showSketchView(true);
        mSketchView.addPhotoByPath(imgPath);
        mSketchView.setEditMode(SketchView.EDIT_PHOTO);//切换图片编辑模式
    }


    /**
     * show 获取当前白板的BitMap
     *
     * @author TangentLu
     */
    public Bitmap getResultBitmap() {
        return mSketchView.getResultBitmap();
    }

    /**
     * show 手动保存当前画板到文件，耗时操作
     *
     * @param filePath 保存的文件路径
     * @param imgName  保存的文件名
     * @return 返回保存后的文件路径
     * @author TangentLu
     */
    public File saveInOI(String filePath, String imgName) {
        return saveInOI(filePath, imgName, 80);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();//初始化上下文
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //取消状态栏
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        rootView = inflater.inflate(R.layout.fragment_white_board, container, false);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //下面的代码主要是为了解决软键盘弹出后遮挡住文字录入PopWindow的问题
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);//获取rootView的可视区域
                int screenHeight = rootView.getHeight();//获取rootView的高度
                keyboardHeight = screenHeight - (r.bottom - r.top);//用rootView的高度减去rootView的可视区域高度得到软键盘高度
                if (textOffY > (sketchViewHeight - keyboardHeight)) {//如果输入焦点出现在软键盘显示的范围内则进行布局上移操作
                    rootView.setTop(-keyboardHeight);//rootView整体上移软键盘高度
                    //更新PopupWindow的位置
                    int x = textOffX;
                    int y = textOffY - mSketchView.getHeight();
                    textPopupWindow.update(mSketchView, x, y,
                            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                }
            }
        });
        findView(rootView);//载入所有的按钮实例
        initDrawParams();//初始化绘画参数
        initPopupWindows();//初始化弹框
        initSaveDialog();
        initData();
        initSketchGV();


        return rootView;
    }

    private void initData() {
        SketchData newSketchData = new SketchData();
        sketchDataList.add(newSketchData);
        mSketchView.setSketchData(newSketchData);
    }

    private void initSketchGV() {
        sketchGVAdapter = new SketchDataGridAdapter(activity, sketchDataList, new SketchDataGridAdapter.OnActionCallback() {
            @Override
            public void onDeleteCallback(int position) {
                sketchDataList.remove(position);
                sketchGVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAddCallback() {
                SketchData newSketchData = new SketchData();
                sketchDataList.add(newSketchData);
                mSketchView.updateSketchData(newSketchData);
                mSketchView.setEditMode(SketchView.EDIT_STROKE);//切换笔画编辑模式
                showSketchView(true);
            }

            @Override
            public void onSelectCallback(SketchData sketchData) {
                mSketchView.updateSketchData(sketchData);
                mSketchView.setEditMode(SketchView.EDIT_PHOTO);//切换图片编辑模式
                showSketchView(true);
            }
        });
        sketchGV.setAdapter(sketchGVAdapter);
    }

    private void showSketchView(boolean b) {
        mSketchView.setVisibility(b ? View.VISIBLE : View.GONE);
        sketchGV.setVisibility(!b ? View.VISIBLE : View.GONE);
    }

    private void initSaveDialog() {
        saveET = new EditText(activity);
        saveET.setHint("新文件名");
        saveET.setGravity(Gravity.CENTER);
        saveET.setSingleLine();
        saveET.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        saveET.setImeOptions(EditorInfo.IME_ACTION_DONE);
        saveET.setSelectAllOnFocus(true);
        saveET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ScreenUtils.hideInput(saveDialog.getCurrentFocus());
                    saveDialog.dismiss();
                    String input = saveET.getText().toString();
                    saveInUI(input + ".png");
                }
                return true;
            }
        });
        saveDialog = new AlertDialog.Builder(getActivity())
                .setTitle("请输入保存文件名")
                .setMessage("")
                .setView(saveET)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ScreenUtils.hideInput(saveDialog.getCurrentFocus());
                        String input = saveET.getText().toString();
                        saveInUI(input + ".png");
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ScreenUtils.hideInput(saveDialog.getCurrentFocus());
                    }
                })
                .setCancelable(false)
                .create();
    }


    private void initDrawParams() {
        //默认为画笔模式
        strokeMode = STROKE_TYPE_DRAW;

        //画笔宽度缩放基准参数
        Drawable circleDrawable = getResources().getDrawable(R.drawable.circle);
        assert circleDrawable != null;
        size = circleDrawable.getIntrinsicWidth();
    }

    //初始化
    private void initPopupWindows() {
        initStrokePop();
        initMyStrokePop();
        initEraserPop();
        initTextPop();
        initZuJianPop();
        initXingZhuangPop();
        inityansePop();
        initxuanzePop();
        inittuozhuaiPop();


    }

    private void inittuozhuaiPop() {
        tuozhuaiPopwindow = new PopupWindow(activity);
        tuozhuaiPopwindow.setContentView(popupTuozhuai);
        tuozhuaiPopwindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        tuozhuaiPopwindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        tuozhuaiPopwindow.setFocusable(true);

    }

    private void initxuanzePop() {
        xuanzePopwindow = new PopupWindow(activity);
        xuanzePopwindow.setContentView(popupXuanze);
        xuanzePopwindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        xuanzePopwindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        xuanzePopwindow.setFocusable(true);
    }

    //十二颜色选定
    private void inityansePop() {
        backgroundPopwindow = new PopupWindow(activity);
        backgroundPopwindow.setContentView(popbackground);
        backgroundPopwindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        backgroundPopwindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        backgroundPopwindow.setFocusable(true);
    }


    private void initTextPop() {
        textPopupWindow = new PopupWindow(activity);
        textPopupWindow.setContentView(popupTextLayout);
        textPopupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);//宽度200dp
        textPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);//高度自适应
        textPopupWindow.setFocusable(true);
        textPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        textPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        textPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!strokeET.getText().toString().equals("")) {
                    StrokeRecord record = new StrokeRecord(strokeType);
                    record.text = strokeET.getText().toString();
                }
            }
        });
    }

    private void initZuJianPop() {

        zujianPopupWindow = new PopupWindow(activity);
        zujianPopupWindow.setContentView(popZuJianLayout);
        zujianPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        zujianPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        zujianPopupWindow.setFocusable(true);


    }

    private void initEraserPop() {
        //橡皮擦弹窗
        eraserPopupWindow = new PopupWindow(activity);
        eraserPopupWindow.setContentView(popupEraserLayout);//设置主体布局
        eraserPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        eraserPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        eraserPopupWindow.setFocusable(true);
        eraserPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);//动画
        //橡皮擦宽度拖动条
        eraserSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                setSeekBarProgress(progress, STROKE_TYPE_ERASER);
            }

        });
        eraserSeekBar.setProgress(SketchView.DEFAULT_ERASER_SIZE);
    }

    private void initXingZhuangPop() {
        xingzhuangPopwindow = new PopupWindow(activity);
        xingzhuangPopwindow.setContentView(popXingZhuangLayout);
        xingzhuangPopwindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        xingzhuangPopwindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        xingzhuangPopwindow.setFocusable(true);
        xingzhuangSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //默认大小
//        xingzhuangSeekbar.setProgress(50);
    }

    //自己的画笔popwindow
    private void initMyStrokePop() {

        mystrkePopwindow = new PopupWindow(activity);
        mystrkePopwindow.setContentView(popupMyStrokeLayout);
        mystrkePopwindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mystrkePopwindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mystrkePopwindow.setFocusable(true);
        mystrkePopwindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        //点击popwindow就设置为画笔模式
        strokeType = STROKE_TYPE_DRAW;

        mystrokeColorRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                mSketchView.setStrokeType(strokeType);

                switch (checkedId) {
                    case R.id.qianbi:
                        mSketchView.setStrokeAlpha(255);
                        mSketchView.setBackground();
                        mSketchView.setSize(SketchView.MY_DEFAULT_STROKE_SIZE, STROKE_TYPE_DRAW);//默认尺寸是铅笔
                        Toast.makeText(activity, "你选中了铅笔", Toast.LENGTH_LONG).show();
                        strokePopupWindow.dismiss();//切换画笔后隐藏

                        break;

                    case R.id.gangbi:
//                      mystrokeSeekBar.setProgress(SketchView.MY_DEFAULT_STROKE_SIZE);//默认尺寸是铅笔
                        mSketchView.setStrokeAlpha(255);
                        mSketchView.setSize(SketchView.MY_DEFAULT_STROKE_gangbi, STROKE_TYPE_DRAW);
                        Toast.makeText(activity, "你选中了钢笔", Toast.LENGTH_LONG).show();
                        strokePopupWindow.dismiss();//切换画笔后隐藏

                        break;
                    case R.id.maobi:
                        mSketchView.setSize(SketchView.MY_DEFAULT_STROKE_maobi, STROKE_TYPE_DRAW);
                        mSketchView.setStrokeAlpha(125);
                        Toast.makeText(activity, "你选中了毛笔", Toast.LENGTH_LONG).show();
                        break;
                }

            }
        });
        mystrokeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSeekBarProgress(progress, STROKE_TYPE_DRAW);
                //mSketchView.setBackgroundByPath("fs2.png");

                mSketchView.setStrokeColor(Color.parseColor("#2900ea"));//蓝色


               /* mSketchView.setStrokeColor(Color.parseColor("#0083e7"));//淡蓝色
                mSketchView.setStrokeColor(Color.parseColor("#00e5b6"));//图层
                mSketchView.setStrokeColor(Color.parseColor("#00e621"));//绿色
                mSketchView.setStrokeColor(Color.parseColor("#010101"));//黑色
                mSketchView.setStrokeColor(Color.parseColor("#f30000"));//红色
                mSketchView.setStrokeColor(Color.parseColor("#ff6100"));//橙色
                mSketchView.setStrokeColor(Color.parseColor("#cc00ff"));//紫色
                mSketchView.setStrokeColor(Color.parseColor("#f5ff00"));//黄色
                mSketchView.setStrokeColor(Color.parseColor("#5000e9"));//蓝色*/

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    private void initStrokePop() {
        //画笔弹窗
        strokePopupWindow = new PopupWindow(activity);
        strokePopupWindow.setContentView(popupStrokeLayout);//设置主体布局
        strokePopupWindow.setWidth(ScreenUtils.dip2px(getActivity(), pupWindowsDPWidth));//宽度
//        strokePopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);//高度自适应
        strokePopupWindow.setHeight(ScreenUtils.dip2px(getActivity(), strokePupWindowsDPHeight));//高度
        strokePopupWindow.setFocusable(true);
        strokePopupWindow.setBackgroundDrawable(new BitmapDrawable());//设置空白背景
        strokePopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);//动画
        strokeTypeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int resId = R.drawable.stroke_type_rbtn_draw_checked;
                if (checkedId == R.id.stroke_type_rbtn_draw) {
                    strokeType = STROKE_TYPE_DRAW;
                } else if (checkedId == R.id.stroke_type_rbtn_line) {
                    strokeType = STROKE_TYPE_LINE;
                    resId = R.drawable.stroke_type_rbtn_line_checked;
                } else if (checkedId == R.id.stroke_type_rbtn_circle) {
                    strokeType = STROKE_TYPE_CIRCLE;
                    resId = R.drawable.stroke_type_rbtn_circle_checked;
                } else if (checkedId == R.id.stroke_type_rbtn_rectangle) {
                    strokeType = STROKE_TYPE_RECTANGLE;
                    resId = R.drawable.stroke_type_rbtn_rectangle_checked;
                } else if (checkedId == R.id.stroke_type_rbtn_text) {
                    strokeType = STROKE_TYPE_TEXT;
                    resId = R.drawable.stroke_type_rbtn_text_checked;
                }
                btn_stroke.setImageResource(resId);
                mSketchView.setStrokeType(strokeType);
                strokePopupWindow.dismiss();//切换画笔后隐藏
            }
        });
        //设置画笔颜色
        strokeColorRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int color = COLOR_BLACK;
                if (checkedId == R.id.stroke_color_black) {
                    color = COLOR_BLACK;
                } else if (checkedId == R.id.stroke_color_red) {
                    color = COLOR_RED;
                } else if (checkedId == R.id.stroke_color_green) {
                    color = COLOR_GREEN;
                } else if (checkedId == R.id.stroke_color_orange) {
                    color = COLOR_ORANGE;
                } else if (checkedId == R.id.stroke_color_blue) {
                    color = COLOR_BLUE;
                }
                mSketchView.setStrokeColor(color);
            }
        });
        //画笔宽度拖动条
        strokeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                setSeekBarProgress(progress, STROKE_TYPE_DRAW);
            }
        });
        //设置默认画笔默认
        strokeSeekBar.setProgress(SketchView.DEFAULT_STROKE_SIZE);
//        strokeColorRG.check(R.id.stroke_color_black);

        //画笔不透明度拖动条
        strokeAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                int alpha = (progress * 255) / 100;//百分比转换成256级透明度
                mSketchView.setStrokeAlpha(alpha);
                strokeAlphaImage.setAlpha(alpha);
            }
        });
        strokeAlphaSeekBar.setProgress(SketchView.DEFAULT_STROKE_ALPHA);
    }


    private void findView(View view) {


        //初始化抽屉
        slidingMenu = new SlidingMenu(getActivity());
        //左滑菜单
        slidingMenu.setMode(SlidingMenu.LEFT);
        //触摸屏幕(禁止触摸 )
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        //设置阴影宽度
//        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        //设置阴影效果
//        slidingMenu.setShadowDrawable(R.mipmap.ic_launcher_round);
        //设置滑动菜单的宽度
        slidingMenu.setBehindWidth(470);
        //菜单距离主页面的距离
//        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);

        //设置渐入渐出效果
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(getActivity(), SlidingMenu.SLIDING_CONTENT);

        drawer_view = View.inflate(getActivity(), R.layout.left_menu, null);

        //为侧滑菜单设置布局
        // slidingMenu.setMenu(R.layout.left_menu);
        slidingMenu.setMenu(drawer_view);
        //抽屉里的控件
        LinearLayout wodelayout = (LinearLayout) drawer_view.findViewById(R.id.wode_layout);
        LinearLayout wodeweike_layout = (LinearLayout) drawer_view.findViewById(R.id.wodeweike_layout);
        LinearLayout shangchuanliebiao_layout = (LinearLayout) drawer_view.findViewById(R.id.shangchuanliebiao_layout);
        LinearLayout shezhi_layout = (LinearLayout) drawer_view.findViewById(R.id.shezhi_layout);
        LinearLayout bangzhu_layout = (LinearLayout) drawer_view.findViewById(R.id.bangzhu_layout);
        TextView tuichudenglu = (TextView) drawer_view.findViewById(R.id.tuichudenglu);

        wodelayout.setOnClickListener(this);
        wodeweike_layout.setOnClickListener(this);
        shangchuanliebiao_layout.setOnClickListener(this);
        shezhi_layout.setOnClickListener(this);
        bangzhu_layout.setOnClickListener(this);
        tuichudenglu.setOnClickListener(this);


        sketchGV = (GridView) view.findViewById(R.id.sketch_data_gv);
        //画板整体布局
        mSketchView = (SketchView) view.findViewById(R.id.sketch_view);

        controlLayout = view.findViewById(R.id.controlLayout);

        //guaju屏幕录制
        luping_img = (ImageView) view.findViewById(R.id.luping_img);
        zanting_img = (ImageView) view.findViewById(R.id.zanting_img);
        timer = (Chronometer) view.findViewById(R.id.timer);


        btn_add = (ImageView) view.findViewById(R.id.btn_add);
        btn_stroke = (ImageView) view.findViewById(R.id.btn_stroke);
        btn_eraser = (ImageView) view.findViewById(R.id.btn_eraser);
        btn_undo = (ImageView) view.findViewById(R.id.btn_undo);
        btn_redo = (ImageView) view.findViewById(R.id.btn_redo);
        btn_photo = (ImageView) view.findViewById(R.id.btn_photo);
        btn_background = (ImageView) view.findViewById(R.id.btn_background);
        btn_drag = (ImageView) view.findViewById(R.id.btn_drag);
        btn_save = (ImageView) view.findViewById(R.id.btn_save);
        btn_empty = (ImageView) view.findViewById(R.id.btn_empty);
        btn_send = (ImageView) view.findViewById(R.id.btn_send);
        btn_drawer = (ImageView) view.findViewById(R.id.btn_drawer);
        btn_xingzhuang = (ImageView) view.findViewById(R.id.btn_xinghzunag);
        btn_zujian = (ImageView) view.findViewById(R.id.btn_zujian);

        btn_xuanze = (ImageView) view.findViewById(R.id.btn_xuanze);
        btn_tuozhuai = (ImageView) view.findViewById(R.id.btn_tuozhuai);

        // btn_send_space = (ImageView) view.findViewById(R.id.btn_send_space);


        if (isTeacher) {
            btn_send.setVisibility(View.VISIBLE);
            //  btn_send_space.setVisibility(View.VISIBLE);
        }

        //设置点击监听
        mSketchView.setOnDrawChangedListener(this);//设置撤销动作监听器
        btn_add.setOnClickListener(this);
        btn_stroke.setOnClickListener(this);
        btn_eraser.setOnClickListener(this);
        btn_undo.setOnClickListener(this);
        btn_redo.setOnClickListener(this);
        btn_empty.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btn_photo.setOnClickListener(this);
        btn_background.setOnClickListener(this);
        btn_drag.setOnClickListener(this);
        btn_drawer.setOnClickListener(this);
        btn_send.setOnClickListener(this);

        btn_xingzhuang.setOnClickListener(this);
        btn_zujian.setOnClickListener(this);
        btn_xuanze.setOnClickListener(this);
        btn_tuozhuai.setOnClickListener(this);

        mSketchView.setTextWindowCallback(new SketchView.TextWindowCallback() {
            @Override
            public void onText(View anchor, StrokeRecord record) {
                textOffX = record.textOffX;
                textOffY = record.textOffY;
                showTextPopupWindow(anchor, record);
            }
        });

        // popupWindow布局
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity
                .LAYOUT_INFLATER_SERVICE);


        //画笔弹窗布局
        popupStrokeLayout = inflater.inflate(R.layout.popup_sketch_stroke, null);
        strokeImageView = (ImageView) popupStrokeLayout.findViewById(R.id.stroke_circle);
        strokeAlphaImage = (ImageView) popupStrokeLayout.findViewById(R.id.stroke_alpha_circle);
        strokeSeekBar = (SeekBar) (popupStrokeLayout.findViewById(R.id.stroke_seekbar));
        strokeAlphaSeekBar = (SeekBar) (popupStrokeLayout.findViewById(R.id.stroke_alpha_seekbar));
        //画笔颜色
        strokeTypeRG = (RadioGroup) popupStrokeLayout.findViewById(R.id.stroke_type_radio_group);
        strokeColorRG = (RadioGroup) popupStrokeLayout.findViewById(R.id.stroke_color_radio_group);


        //我的画笔弹窗布局
        popupMyStrokeLayout = inflater.inflate(R.layout.popup_sketch_bi, null);
        mystrokeColorRG = (RadioGroup) popupMyStrokeLayout.findViewById(R.id.mystroke_type_radio_group);
        btn_qianbi = (RadioButton) popupMyStrokeLayout.findViewById(R.id.qianbi);
        btn_maobi = (RadioButton) popupMyStrokeLayout.findViewById(R.id.maobi);
        btn_gangbi = (RadioButton) popupMyStrokeLayout.findViewById(R.id.gangbi);
        mystrokeSeekBar = (SeekBar) popupMyStrokeLayout.findViewById(R.id.stroke_huabichicui);


        //橡皮擦弹窗布局
        popupEraserLayout = inflater.inflate(R.layout.popup_sketch_eraser, null);
        eraserImageView = (ImageView) popupEraserLayout.findViewById(R.id.stroke_circle);
        eraserSeekBar = (SeekBar) (popupEraserLayout.findViewById(R.id.stroke_seekbar));

        //形状弹框布局
        popXingZhuangLayout = inflater.inflate(R.layout.popup_sketch_xingzhuang, null);
        xingzhuangSwitchButton = (SwitchButton) popXingZhuangLayout.findViewById(R.id.xingzhuang_switch);
        xingzhuangSeekbar = (SeekBar) popXingZhuangLayout.findViewById(R.id.stroke_xingzhuangchicui);
        //文本录入弹窗布局
        popupTextLayout = inflater.inflate(R.layout.popup_sketch_text, null);
        strokeET = (EditText) popupTextLayout.findViewById(R.id.text_pupwindow_et);

        popZuJianLayout = inflater.inflate(R.layout.popup_sketch_zujian, null);
        popbackground = inflater.inflate(R.layout.popup_sketch_yanshebeijing, null);
        popupXuanze = inflater.inflate(R.layout.popup_sketch_jietu, null);
        popupTuozhuai = inflater.inflate(R.layout.popup_sketch_tudong, null);

        getSketchSize();//计算选择图片弹窗的高宽
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        getSketchSize();
    }

    private void getSketchSize() {
        ViewTreeObserver vto = mSketchView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (sketchViewHeight == 0 && sketchViewWidth == 0) {
                    int height = mSketchView.getMeasuredHeight();
                    int width = mSketchView.getMeasuredWidth();
                    sketchViewHeight = height;
                    sketchViewWidth = width;
                    sketchViewRight = mSketchView.getRight();
                    sketchViewBottom = mSketchView.getBottom();
                    Log.i("onPreDraw", sketchViewHeight + "  " + sketchViewWidth);
                    decorHeight = getActivity().getWindow().getDecorView().getMeasuredHeight();
                    decorWidth = getActivity().getWindow().getDecorView().getMeasuredWidth();
                    Log.i("onPreDraw", "decor height:" + decorHeight + "   width:" + decorHeight);
                    int height3 = controlLayout.getMeasuredHeight();
                    int width3 = controlLayout.getMeasuredWidth();
                    Log.i("onPreDraw", "controlLayout  height:" + height3 + "   width:" + width3);
                }
                return true;
            }
        });
        Log.i("getSketchSize", sketchViewHeight + "  " + sketchViewWidth);
    }

    protected void setSeekBarProgress(int progress, int drawMode) {
        int calcProgress = progress > 1 ? progress : 1;
        int newSize = Math.round((size / 100f) * calcProgress);
        int offset = Math.round((size - newSize) / 2);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(newSize, newSize);
        lp.setMargins(offset, offset, offset, offset);
        if (drawMode == STROKE_TYPE_DRAW) {
            strokeImageView.setLayoutParams(lp);
        } else {
            eraserImageView.setLayoutParams(lp);
        }
        mSketchView.setSize(newSize, drawMode);
    }


    @Override
    public void onDrawChanged() {
        // Undo
        if (mSketchView.getStrokeRecordCount() > 0)
            btn_undo.setAlpha(1f);
        else
            btn_undo.setAlpha(0.4f);
        // Redo
        if (mSketchView.getRedoCount() > 0)
            btn_redo.setAlpha(1f);
        else
            btn_redo.setAlpha(0.4f);
    }

    private void updateGV() {
        sketchGVAdapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_add) {
            if (mSketchView.getVisibility() == View.VISIBLE) {
                mSketchView.createCurThumbnailBM();
                showSketchView(false);
            } else {
                showSketchView(true);
            }
            updateGV();
        } else if (id == R.id.btn_stroke) {
//            if (mSketchView.getEditMode() == SketchView.EDIT_STROKE && mSketchView.getStrokeType() != STROKE_TYPE_ERASER) {
////                showParamsPopupWindow(v, STROKE_TYPE_DRAW);
//
//            } else {
//                int checkedId = strokeTypeRG.getCheckedRadioButtonId();
//                if (checkedId == R.id.stroke_type_rbtn_draw) {
//                    strokeType = STROKE_TYPE_DRAW;
//                } else if (checkedId == R.id.stroke_type_rbtn_line) {
//                    strokeType = STROKE_TYPE_LINE;
//                } else if (checkedId == R.id.stroke_type_rbtn_circle) {
//                    strokeType = STROKE_TYPE_CIRCLE;
//                } else if (checkedId == R.id.stroke_type_rbtn_rectangle) {
//                    strokeType = STROKE_TYPE_RECTANGLE;
//                } else if (checkedId == R.id.stroke_type_rbtn_text) {
//                    strokeType = STROKE_TYPE_TEXT;
//                }
//                mSketchView.setStrokeType(strokeType);
//            }
            myshowParamsPopupWindow(v, STROKE_TYPE_DRAW);
//            mSketchView.setEditMode(SketchView.EDIT_STROKE);
            showBtn(btn_stroke);
        } else if (id == R.id.btn_eraser) {
            if (mSketchView.getEditMode() == SketchView.EDIT_STROKE && mSketchView.getStrokeType() == STROKE_TYPE_ERASER) {
                eraserPopupWindow.showAsDropDown(v, v.getWidth() / 2, -v.getHeight(), Gravity.RIGHT);
            } else {
                mSketchView.setStrokeType(STROKE_TYPE_ERASER);
            }

            mSketchView.setEditMode(SketchView.EDIT_STROKE);
            showBtn(btn_eraser);
        } else if (id == R.id.btn_undo) {
            mSketchView.undo();
        } else if (id == R.id.btn_redo) {
            mSketchView.redo();
        } else if (id == R.id.btn_empty) {
            askForErase();
        } else if (id == R.id.btn_save) {
            if (mSketchView.getRecordCount() == 0) {
                Toast.makeText(getActivity(), "您还没有绘图", Toast.LENGTH_SHORT).show();
            } else {
                showSaveDialog();
            }
        } else if (id == R.id.btn_drawer) {
            slidingMenu.showMenu();
        } else if (id == R.id.btn_photo) {
            startMultiImageSelector(REQUEST_IMAGE);

        } else if (id == R.id.btn_xuanze) {
            xuanzePopwindow.showAsDropDown(v, v.getWidth() / 2, -v.getHeight() * 1, Gravity.RIGHT);

        } else if (id == R.id.btn_background) {
            backgroundPopwindow.showAsDropDown(v, v.getWidth() / 2, -v.getHeight() * 2, Gravity.RIGHT);
//            startMultiImageSelector(REQUEST_BACKGROUND);

        } else if (id == R.id.btn_drag) {
            mSketchView.setEditMode(SketchView.EDIT_PHOTO);
            showBtn(btn_drag);
        } else if (id == R.id.btn_tuozhuai) {
            tuozhuaiPopwindow.showAsDropDown(v, v.getWidth() / 2, -v.getHeight() * 1, Gravity.RIGHT);
        } else if (id == R.id.btn_xinghzunag) {
            xingzhuangPopwindow.showAsDropDown(v, v.getWidth() / 2, -v.getHeight() * 2, Gravity.RIGHT);

        } else if (id == R.id.btn_zujian) {
            zujianPopupWindow.showAsDropDown(v, v.getWidth() / 2, -v.getHeight(), Gravity.RIGHT);

        } else if (id == R.id.wode_layout) {
            Log.e("tag", "****************************************");
            Toast.makeText(getActivity(), "1111111", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.wodeweike_layout) {
            startActivity(new Intent(activity, MyWeiKe.class));
        } else if (id == R.id.shangchuanliebiao_layout) {
            Toast.makeText(getActivity(), "1111111", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.shezhi) {
            Toast.makeText(getActivity(), "1111111", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.bangzhu_layout) {
            Toast.makeText(getActivity(), "1111111", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.tuichudenglu) {
            Log.e("tag", "****************************************");
            Toast.makeText(getActivity(), "1111111", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btn_send) {
            if (sendBtnCallback != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String photoName = TEMP_FILE_NAME + TimeUtils.getNowTimeString();
                        sendBtnCallback.onSendBtnClick(saveInOI(TEMP_FILE_PATH, photoName, 50));
                    }
                }).start();
            }
        }

    }

    private void startMultiImageSelector(int request) {
        MultiImageSelector selector = MultiImageSelector.create(getActivity());
        selector.showCamera(true);
        selector.count(9);
        selector.single();
        selector.origin(mSelectPath);
        Bundle boundsBundle = new Bundle();
        Rect rect = new Rect();
        mSketchView.getLocalVisibleRect(rect);
        int[] boundsInts = new int[4];
        //noinspection Range
        mSketchView.getLocationInWindow(boundsInts);
        boundsInts[1] -= ScreenUtils.getStatusBarHeight(activity);
        boundsInts[2] = mSketchView.getWidth();
        boundsInts[3] = mSketchView.getHeight();
        selector.start(this, boundsInts, request);
    }

    private void showSaveDialog() {
        saveDialog.show();
        saveET.setText(TimeUtils.getNowTimeString());
        saveET.selectAll();
        ScreenUtils.showInput(mSketchView);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == getActivity().RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                String path = "";
                if (mSelectPath.size() == 1) {
                    path = mSelectPath.get(0);
                } else if (mSelectPath == null || mSelectPath.size() == 0) {
                    Toast.makeText(getActivity(), "图片加载失败,请重试!", Toast.LENGTH_LONG).show();
                }
                //加载图片
                mSketchView.addPhotoByPath(path);
                mSketchView.setEditMode(SketchView.EDIT_PHOTO);
                showBtn(btn_drag);
            }
        } else if (requestCode == REQUEST_BACKGROUND) {//设置背景成功
            if (resultCode == getActivity().RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                String path = "";
                if (mSelectPath.size() == 1) {
                    path = mSelectPath.get(0);
                } else if (mSelectPath == null || mSelectPath.size() == 0) {
                    Toast.makeText(getActivity(), "图片加载失败,请重试!", Toast.LENGTH_LONG).show();
                }
                mSketchView.setBackgroundByPath(path);
                Log.i("imgPath", path);
                //加载图片设置画板背景
            }
        }
    }

    private void showParamsPopupWindow(View anchor, int drawMode) {
        if (BitmapUtils.isLandScreen(activity)) {
            if (drawMode == STROKE_TYPE_DRAW) {
                strokePopupWindow.showAsDropDown(anchor, ScreenUtils.dip2px(activity, -pupWindowsDPWidth), -anchor.getHeight());
            } else {
                eraserPopupWindow.showAsDropDown(anchor, ScreenUtils.dip2px(activity, -pupWindowsDPWidth), -anchor.getHeight());
            }
        } else {
            if (drawMode == STROKE_TYPE_DRAW) {
//                strokePopupWindow.showAsDropDown(anchor, 0, ScreenUtils.dip2px(activity, -strokePupWindowsDPHeight) - anchor.getHeight());
                strokePopupWindow.showAsDropDown(anchor, 0, 0);
            } else {
//                eraserPopupWindow.showAsDropDown(anchor, 0, ScreenUtils.dip2px(activity, -eraserPupWindowsDPHeight) - anchor.getHeight());
                eraserPopupWindow.showAsDropDown(anchor, 0, 0);
            }
        }
    }

    private void myshowParamsPopupWindow(View v, int drawMode) {
        if (BitmapUtils.isLandScreen(activity)) {
            if (drawMode == STROKE_TYPE_DRAW) {
                mystrkePopwindow.showAsDropDown(v, v.getWidth() / 2, -v.getHeight(), Gravity.RIGHT);
            }
        } else {
            if (drawMode == STROKE_TYPE_DRAW) {
//                strokePopupWindow.showAsDropDown(anchor, 0, ScreenUtils.dip2px(activity, -strokePupWindowsDPHeight) - anchor.getHeight());
                strokePopupWindow.showAsDropDown(v, 0, 0);
            }
        }
    }

    private void showTextPopupWindow(View anchor, final StrokeRecord record) {
        strokeET.requestFocus();
        textPopupWindow.showAsDropDown(anchor, record.textOffX, record.textOffY - mSketchView.getHeight());
        textPopupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        textPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!strokeET.getText().toString().equals("")) {
                    record.text = strokeET.getText().toString();
                    record.textPaint.setTextSize(strokeET.getTextSize());
                    record.textWidth = strokeET.getMaxWidth();
                    mSketchView.addStrokeRecord(record);
                }
            }
        });
    }


    private void saveInUI(final String imgName) {
        new saveToFileTask().execute(imgName);
    }

    /**
     * show 保存图片到本地文件，耗时操作
     *
     * @param filePath 文件保存路径
     * @param imgName  文件名
     * @param compress 压缩百分比1-100
     * @return 返回保存的图片文件
     * @author TangentLu
     */
    public File saveInOI(String filePath, String imgName, int compress) {
        if (!imgName.contains(".png")) {
            imgName += ".png";
        }
        Log.e(TAG, "saveInOI: " + System.currentTimeMillis());
        Bitmap newBM = mSketchView.getResultBitmap();
        Log.e(TAG, "saveInOI: " + System.currentTimeMillis());

        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File f = new File(filePath, imgName);
            if (!f.exists()) {
                f.createNewFile();
            } else {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            Log.e(TAG, "saveInOI: " + System.currentTimeMillis());

            if (compress >= 1 && compress <= 100)
                newBM.compress(Bitmap.CompressFormat.PNG, compress, out);
            else {
                newBM.compress(Bitmap.CompressFormat.PNG, 80, out);
            }
            Log.e(TAG, "saveInOI: " + System.currentTimeMillis());

            out.close();
            newBM.recycle();
            newBM = null;
            return f;
        } catch (Exception e) {
            return null;
        }
    }


    private void askForErase() {
        new AlertDialog.Builder(getActivity())
                .setMessage("擦除手绘?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSketchView.erase();
                    }
                })
                .create()
                .show();
    }

    //设置透明度
    private void showBtn(ImageView iv) {
        btn_eraser.setAlpha(BTN_ALPHA);
        btn_stroke.setAlpha(BTN_ALPHA);
        btn_drag.setAlpha(BTN_ALPHA);
        iv.setAlpha(1f);
    }

    class saveToFileTask extends AsyncTask<String, Void, File> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new AlertDialog.Builder(activity)
                    .setTitle("保存画板")
                    .setMessage("保存中...")
                    .show();
        }

        @Override
        protected File doInBackground(String... photoName) {
            return saveInOI(FILE_PATH, photoName[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (file.exists()) {
                Toast.makeText(getActivity(), file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "保存失败！", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        }
    }

}
