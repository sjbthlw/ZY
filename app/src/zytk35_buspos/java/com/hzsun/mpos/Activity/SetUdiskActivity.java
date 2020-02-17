package com.hzsun.mpos.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.hzsun.mpos.Adapter.SetMenuListviewAdapter;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.Usb.USBManager;
import com.hzsun.mpos.progressutils.ProgressManage;
import com.hzsun.mpos.thread.WriteDataToUsbThread;
import com.hzsun.mpos.thread.WriteIdentitySoundThread;
import com.hzsun.mpos.thread.WriteUsbAppThread;
import com.hzsun.mpos.thread.WriteUsbFeatureThread;

import java.util.ArrayList;

import static com.hzsun.mpos.Global.Global.APPFILE_NAME;
import static com.hzsun.mpos.Global.Global.IAPAPPFILE_NAME;
import static com.hzsun.mpos.Global.Global.ZYTK35Path;
import static com.hzsun.mpos.Public.Publicfun.InstallApk;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.FAIL;
import static com.hzsun.mpos.Public.ToastUtils.SUCCESS;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

/**
 * 选择设置下的菜单
 */
public class SetUdiskActivity extends BaseActivity
        implements AdapterView.OnItemClickListener {

    private String TAG = getClass().getSimpleName();
    private USBManager usbManager;
    private ListView lv_SetMenu;
    private ImageView ImageBack;
    private SetMenuListviewAdapter setMenuListviewAdapter;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    public Handler UISetMenuHandler;
    private ProgressManage progressManage;

    private WriteUsbFeatureThread ThreadUsbFeatureWork;
    private WriteUsbAppThread ThreadUsbAppWork;
    private WriteUsbAppThread ThreadUsbIAPAppWork;
    private WriteDataToUsbThread ThreadDataToUsbWork;
    private WriteIdentitySoundThread ThreadIdentitySoundWork;

    private int positionId = 0;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_udisk);
        initData();
        initViews();
        initListener();

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
                    case 6:
                        progressManage.setProgress(100);
                        progressManage.dismiss();
                        ToastUtils.showText(SetUdiskActivity.this, "身份语音文件导入完成", SUCCESS, BOTTOM, Toast.LENGTH_LONG);
                        break;
                    case 7:
                        progressManage.dismiss();
                        ToastUtils.showText(SetUdiskActivity.this, "身份语音文件导入失败", FAIL, BOTTOM, Toast.LENGTH_LONG);
                        break;
                    case 8:
                        progressManage.dismiss();
                        ToastUtils.showText(SetUdiskActivity.this, "身份语音文件格式不正确,导入失败", FAIL, BOTTOM, Toast.LENGTH_LONG);
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
                progressManage = new ProgressManage(this, lv_SetMenu, getWindow());
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
                progressManage = new ProgressManage(this, lv_SetMenu, getWindow());
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
                progressManage = new ProgressManage(this, lv_SetMenu, getWindow());
                progressManage.setMessage("正在更新引导APP...");
                progressManage.show();
                ThreadUsbIAPAppWork = new WriteUsbAppThread(this, usbManager, UISetMenuHandler, 1);
                ThreadUsbIAPAppWork.start();
            } else if (iRet == 3) {
                if (ThreadDataToUsbWork != null) {
                    ToastUtils.showText(this, "数据更新中！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
                //U盘导出交易流水记录
                usbManager = new USBManager(this);
                progressManage = new ProgressManage(this, lv_SetMenu, getWindow());
                progressManage.setMessage("正在导出数据文件...");
                progressManage.show();
                ThreadDataToUsbWork = new WriteDataToUsbThread(this, usbManager, UISetMenuHandler);
                ThreadDataToUsbWork.start();
            } else if (iRet == 4) {
                //导入身份语音文件
                if (ThreadDataToUsbWork != null) {
                    ToastUtils.showText(this, "数据更新中！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    return;
                }
                //U盘升级应用程序
                usbManager = new USBManager(this);
                progressManage = new ProgressManage(this, lv_SetMenu, getWindow());
                progressManage.setMessage("正在导入身份语音文件...");
                progressManage.show();
                ThreadIdentitySoundWork = new WriteIdentitySoundThread(usbManager, UISetMenuHandler);
                ThreadIdentitySoundWork.start();
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
    }

    private void initViews() {

        positionId = iPosition;
        lv_SetMenu = ((ListView) findViewById(R.id.lv_set_menu));
        ImageBack = ((ImageView) findViewById(R.id.iv_back));
        setTitle(strTitle);

        setMenuListviewAdapter = new SetMenuListviewAdapter(this, OptionItemList);
        lv_SetMenu.setAdapter(setMenuListviewAdapter);
        lv_SetMenu.setSelection(iPosition);//选择选中的对象
        setMenuListviewAdapter.setCurrentItem(iPosition);//系统级需要自己选择

        ImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PutResult(iType,positionId);
                finish();//此处一定要调用finish()方法
            }
        });
    }

    private void initListener() {
        lv_SetMenu.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        setMenuListviewAdapter.setCurrentItem(position);
        setMenuListviewAdapter.notifyDataSetChanged();
        positionId = position;
        PutResult(iType, positionId);
    }

    //在onResume()方法注册
    @Override
    protected void onResume() {

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
        super.onDestroy();
    }

}
