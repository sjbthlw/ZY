package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.fs.FileSystem;
import com.hzsun.mpos.Adapter.SetMenuListviewAdapter;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.Usb.USBManager;
import com.hzsun.mpos.Adapter.ListInfoBean;
import com.hzsun.mpos.progressutils.ProgressManage;
import com.hzsun.mpos.thread.WriteDataToUsbThread;
import com.hzsun.mpos.thread.WriteUsbAppThread;
import com.hzsun.mpos.thread.WriteUsbFeatureThread;

import java.io.File;
import java.util.ArrayList;

import static com.hzsun.mpos.Global.Global.APPFILE_NAME;
import static com.hzsun.mpos.Global.Global.IAPAPPFILE_NAME;
import static com.hzsun.mpos.Global.Global.ZYTK35Path;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.InstallApk;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

/**
 * 选择设置下的菜单
 */
public class SetUdiskActivity extends BaseActivity {

    private String TAG = getClass().getSimpleName();
    private USBManager usbManager;

    private ImageView ivCheckMenu;
    private TextView textTitle;
    private ListView lvSetMenu;
    private SetMenuListviewAdapter setMenuListviewAdapter;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;
    public Handler UISetMenuHandler;
    private ProgressManage progressManage;
    private FileSystem currentFs;

    private WriteUsbFeatureThread ThreadUsbFeatureWork;
    private WriteUsbAppThread ThreadUsbAppWork;
    private WriteUsbAppThread ThreadUsbIAPAppWork;
    private WriteDataToUsbThread ThreadDataToUsbWork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_udisk);
        isShowPerScreen(true);
        initData();
        initViews();

        usbManager = new USBManager(this);

        UISetMenuHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        int iRate = (int) msg.obj;
                        progressManage.setProgress(iRate);
                        break;

                    case 2:
                        progressManage.setMessage("更新特征码数据完成");
                        progressManage.setProgress(100);

                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                progressManage.dismiss();
                                //PutResult(100,0);//后退不处理返回值
                            }
                        }, 1000);
                        break;

                    case 3:
                        progressManage.setMessage("下载app数据完成");
                        progressManage.setProgress(100);

                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                progressManage.dismiss();
                            }
                        }, 1000);
                        InstallApk(ZYTK35Path + APPFILE_NAME, 1);
                        break;

                    case 4:
                        progressManage.setMessage("下载引导app数据完成");
                        progressManage.setProgress(100);

                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                progressManage.dismiss();
                            }
                        }, 1000);
                        InstallApk(ZYTK35Path + IAPAPPFILE_NAME, 1);
                        break;

                    case 5:
                        progressManage.setMessage("导出数据完成");
                        progressManage.setProgress(100);

                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                progressManage.dismiss();
                            }
                        }, 1000);
                        break;

                    case 10:
                        progressManage.dismiss();
                        ToastUtils.showText(SetUdiskActivity.this, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
                        break;

                    case 20:
                        progressManage.dismiss();
                        ToastUtils.showText(SetUdiskActivity.this, "未找到对应文件！", WARN, BOTTOM, Toast.LENGTH_LONG);
                        break;
                }
            }
        };
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {

        if (iType != 100) {
            if (iRet == 0) {
                //U盘导入
                if (ThreadUsbFeatureWork != null) {
                    ToastUtils.showText(this, "数据更新中！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
                usbManager = new USBManager(this);
                progressManage = new ProgressManage(this, textTitle, getWindow());
                progressManage.setMessage("正在更新数据中！");
                progressManage.show();
                ThreadUsbFeatureWork = new WriteUsbFeatureThread(this, usbManager, UISetMenuHandler);
                ThreadUsbFeatureWork.start();
            } else if (iRet == 1) {
                if (ThreadUsbAppWork != null) {
                    ToastUtils.showText(this, "数据更新中！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
                //U盘升级应用程序
                usbManager = new USBManager(this);
                progressManage = new ProgressManage(this, textTitle, getWindow());
                progressManage.setMessage("正在更新APP...");
                progressManage.show();
                ThreadUsbAppWork = new WriteUsbAppThread(this, usbManager, UISetMenuHandler, 0);
                ThreadUsbAppWork.start();
            } else if (iRet == 2) {
                if (ThreadUsbIAPAppWork != null) {
                    ToastUtils.showText(this, "数据更新中！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
                //U盘升级引导程序
                usbManager = new USBManager(this);
                progressManage = new ProgressManage(this, textTitle, getWindow());
                progressManage.setMessage("正在更新引导APP...");
                progressManage.show();
                ThreadUsbIAPAppWork = new WriteUsbAppThread(this, usbManager, UISetMenuHandler, 1);
                ThreadUsbIAPAppWork.start();
            } else if (iRet == 3) {
                if (ThreadDataToUsbWork != null) {
                    ToastUtils.showText(this, "数据更新中！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
                //U盘导出POS数据
                usbManager = new USBManager(this);
                //判断U盘是否插入
                currentFs = usbManager.GetCurFS();
                if (currentFs == null) {
                    ToastUtils.showText(SetUdiskActivity.this, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
                //保存文件路径名到集合
                String strFoldPath = ZYTK35Path;
                File ZYTKFile = new File(strFoldPath);
                if (!ZYTKFile.exists()) {
                    Log.e(TAG, "copyFolder: cannot create directory.");
                    ToastUtils.showText(SetUdiskActivity.this, "未找到对应文件！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
                ArrayList<ListInfoBean> listInfoBeans = new ArrayList<>();
                for (File sdFile : ZYTKFile.listFiles()) {
                    if ((sdFile.getName().contains("data"))
                            || (sdFile.getName().contains("log"))
                            || (sdFile.getName().contains(".ini"))) {
                        listInfoBeans.add(0, new ListInfoBean(sdFile.getName(), "不导出", "导出", 1));
                    } else {
                        if((sdFile.isDirectory()) && (!(sdFile.getName().contains("sh")))){
                            listInfoBeans.add(new ListInfoBean(sdFile.getName(), "不导出", "导出", 0));
                        }
                    }
                }
                Intent intent = new Intent(this, DataExportActivity.class);
                intent.putExtra("list", listInfoBeans);
                startActivity(intent);
            }
        } else {
            if (ThreadUsbFeatureWork != null) {
                if (ThreadUsbFeatureWork.GetFlag()) {
                    ThreadUsbFeatureWork.StopThread();
                    progressManage.dismiss();
                    ToastUtils.showText(this, "取消更新数据！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
            }
            if (ThreadUsbAppWork != null) {
                if (ThreadUsbAppWork.GetFlag()) {
                    ThreadUsbAppWork.StopThread();
                    progressManage.dismiss();
                    ToastUtils.showText(this, "取消更新数据！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
            }
            if (ThreadUsbIAPAppWork != null) {
                if (ThreadUsbIAPAppWork.GetFlag()) {
                    ThreadUsbIAPAppWork.StopThread();
                    progressManage.dismiss();
                    ToastUtils.showText(this, "取消更新数据！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
            }
            if (ThreadDataToUsbWork != null) {
                if (ThreadDataToUsbWork.GetFlag()) {
                    ThreadDataToUsbWork.StopThread();
                    progressManage.dismiss();
                    ToastUtils.showText(this, "取消导出数据！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
            }
            Intent intent = new Intent();
            Log.i(TAG, "iType:" + iType + " result:" + iRet);
            intent.putExtra("result", iRet);
            setResult(iType, intent);
            finish();//此处一定要调用finish()方法
        }
    }

    private void initData() {
        iType = getIntent().getIntExtra("Type", -1);
        iPosition = getIntent().getIntExtra("Position", -1);
        strTitle = getIntent().getStringExtra("strTitle");
        OptionItemList = getIntent().getStringArrayListExtra("OptionItemList");
        image = getIntent().getIntExtra("Image", -1);
    }

    private void initViews() {
        ivCheckMenu = ((ImageView) findViewById(R.id.iv_check_menu));
        textTitle = ((TextView) findViewById(R.id.text_title));
        lvSetMenu = ((ListView) findViewById(R.id.lv_set_menu));

        textTitle.setText(strTitle);

        if (image != 0)
            ivCheckMenu.setImageResource(image);
        else
            ivCheckMenu.setImageResource(R.mipmap.s_local_senior);

        setMenuListviewAdapter = new SetMenuListviewAdapter(this, OptionItemList);
        lvSetMenu.setAdapter(setMenuListviewAdapter);
        lvSetMenu.setSelection(iPosition);//选择选中的对象
        setMenuListviewAdapter.setCurrentItem(iPosition);//系统级需要自己选择
        lvSetMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setMenuListviewAdapter.setCurrentItem(position);
                setMenuListviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lvSetMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //确定键
                Log.i(TAG, "===Position:" + position);
                PutResult(iType, position);
            }
        });
    }

    //在onResume()方法注册
    @Override
    protected void onResume() {
        g_WorkInfo.cUDiskState = 1;
        Log.i(TAG, "注册");
        super.onResume();
    }

    //onPause()方法注销
    @Override
    protected void onPause() {
        Log.i(TAG, "注销");
        if (usbManager != null)
            usbManager.unRegisterReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "销毁");
        g_WorkInfo.cUDiskState = 0;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.i(TAG, "KeyValue:" + KeyValue);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                PutResult(100, 0);//后退不处理返回值
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

//    //记录用户首次点击返回键的时间
//    private long firstTime = 0;
//    public void onBackPressed()
//    {
//        //不执行回退功能
////        long secondTime = System.currentTimeMillis();
////        if (secondTime - firstTime > 2000) {
////            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
////            firstTime = secondTime;
////        } else {
////            super.onBackPressed();
////        }
//    }
}
