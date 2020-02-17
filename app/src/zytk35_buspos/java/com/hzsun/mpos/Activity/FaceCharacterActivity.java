package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.hzsun.mpos.Adapter.SetMenuListviewAdapter;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.SelectPopWindow;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.thread.LoadFeatureThread;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;
import static com.hzsun.mpos.Global.Global.DATAPath;
import static com.hzsun.mpos.Global.Global.ZYTKFacePath;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.CENTER;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

public class FaceCharacterActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private String TAG = getClass().getSimpleName();
    private ListView lvSetMenu;
    private ImageView ImageBack;
    private SetMenuListviewAdapter setMenuListviewAdapter;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private UISetPayHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_menu);
        initData();
        initViews();
        initListener();
    }


    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        Log.i(TAG, "iType:" + iType + " result:" + iRet);
        intent.putExtra("result", iRet);
        setResult(iType, intent);
        finish();//此处一定要调用finish()方法
    }

    private void initData() {
        iType = getIntent().getIntExtra("Type", -1);
        iPosition = getIntent().getIntExtra("Position", -1);
        strTitle = getIntent().getStringExtra("strTitle");
        OptionItemList = getIntent().getStringArrayListExtra("OptionItemList");
    }

    private void initViews() {
        handler = new UISetPayHandler(this);
        lvSetMenu = ((ListView) findViewById(R.id.lv_set_menu));
        ImageBack = ((ImageView) findViewById(R.id.iv_back));
        setTitle(strTitle);

        setMenuListviewAdapter = new SetMenuListviewAdapter(this, OptionItemList);
        lvSetMenu.setAdapter(setMenuListviewAdapter);
        lvSetMenu.setSelection(iPosition);//选择选中的对象
        setMenuListviewAdapter.setCurrentItem(iPosition);//系统级需要自己选择
    }

    private void initListener() {
        lvSetMenu.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setMenuListviewAdapter.setCurrentItem(position);
        setMenuListviewAdapter.notifyDataSetChanged();
        switch (position) {
            case 0:
                //是否重新加载人脸特征
                if ((g_WorkInfo.cFaceInitState == 1) || (g_WorkInfo.cFaceInitState == 2)) {
                    LoadFeatureThread ThreadLoadFeature = new LoadFeatureThread(handler);
                    ThreadLoadFeature.start();
                }
                break;
            case 1:
                //重新下载人脸特征码
                ToastUtils.showText(FaceCharacterActivity.this, "重新下载人脸特征码！", WARN, BOTTOM, LENGTH_LONG);
                RunShellCmd("rm -r " + ZYTKFacePath + "*");
                RunShellCmd("rm -r " + DATAPath + "FaceCodeInfo.dat");
                ResetDevice();
                break;
        }

    }

    //重启设备
    private void ResetDevice() {
        List<Object> viewList = SelectPopWindow.showSelectPopWindow(this, lvSetMenu, "设备重启");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(FaceCharacterActivity.this, "重启设备", WARN, CENTER, LENGTH_LONG);
                Publicfun.RunShellCmd("reboot");
            }
        });
    }

    static class UISetPayHandler extends Handler {
        WeakReference<FaceCharacterActivity> reference;
        FaceCharacterActivity activity;

        public UISetPayHandler(FaceCharacterActivity activity) {
            reference = new WeakReference<>(activity);
        }

        public void handleMessage(Message message) {
            activity = reference.get();
            if (activity != null) {
                activity.onHandleMessage(message);
            }
        }

    }

    private void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                g_WorkInfo.cFaceInitState = 2;
                ToastUtils.showText(this, "加载人脸特征数据完成！", WARN, BOTTOM, LENGTH_LONG);
                break;
            case 100:
                ToastUtils.showText(this, "加载人脸特征数据失败！", WARN, BOTTOM, LENGTH_LONG);
                break;
        }
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
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "销毁");
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }
}
