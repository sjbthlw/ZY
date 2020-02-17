package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.hzsun.mpos.Adapter.MenuListviewAdapter;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.SelectPopWindow;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.LocalInfoRW;
import com.hzsun.mpos.thread.LoadFeatureThread;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;
import static com.hzsun.mpos.Global.Global.DATAPath;
import static com.hzsun.mpos.Global.Global.ZYTKFacePath;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.CENTER;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

public class FaceInfoActivity extends
        BaseActivity implements AdapterView.OnItemClickListener {

    private String TAG = getClass().getSimpleName();
    private ListView lv_FaceInfo;
    private ArrayList<String> stringList;
    private MenuListviewAdapter ListviewAdapter;
    private List<Object> viewList;
    public Handler UISetPayHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_info);
        initViews();
        initListView();
        UISetPayHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        g_WorkInfo.cFaceInitState = 2;
                        ToastUtils.showText(FaceInfoActivity.this, "加载人脸特征数据完成！", WARN, BOTTOM, Toast.LENGTH_LONG);
                        break;
                    case 100:
                        ToastUtils.showText(FaceInfoActivity.this, "加载人脸特征数据失败！", WARN, BOTTOM, Toast.LENGTH_LONG);
                        break;
                }
            }
        };
    }

    private void initViews() {
        lv_FaceInfo = ((ListView) findViewById(R.id.lv_faceInfo));
        setTitle("人脸参数设置");
    }


    private void initListView() {

        stringList = new ArrayList<>();
        stringList.add("设置人脸识别模式");
        stringList.add("设置人脸下发特征");
        stringList.add("设置人脸参数");
        ListviewAdapter = new MenuListviewAdapter(this, stringList);
        lv_FaceInfo.setAdapter(ListviewAdapter);
        lv_FaceInfo.setOnItemClickListener(this);
    }

    //设置人脸识别模式
    private void SetFaceRecognitionMode(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置人脸识别模式";

        // 0:手动模式  1:自动模式
        OptionItemList.add("手动模式");
        OptionItemList.add("自动模式");

        iSelectItem = g_LocalInfo.cFaceModeFlag;// 0:手动模式  1:自动模式

        startActivityForResult(new Intent(this, SetMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }


    //设置人脸下发特征
    private void SetFaceCode(int iPosition) {
        int iType = iPosition;
        int iSelectItem;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();

        String strTitle = "设置人脸特征";
        OptionItemList.add("重新加载人脸特征码");
        OptionItemList.add("重新下载人脸特征码");

        if (g_SystemInfo.cFaceDetectFlag != 1) {
            ToastUtils.showText(this, "不允许操作！", WARN, BOTTOM, Toast.LENGTH_LONG);
            return;
        }

        iSelectItem = 0;
        startActivity(new Intent(this, FaceCharacterActivity.class)
                .putExtra("Type", iType)
                .putExtra("Position", iSelectItem)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0: //设置人脸识别模式
                SetFaceRecognitionMode(position);
                break;
            case 1: //设置人脸下发特征
                SetFaceCode(position);
                break;
            case 2: //设置人脸参数
                startActivity(new Intent(FaceInfoActivity.this, FaceInfoSetActivity.class));
                break;
        }
    }

    //重启设备
    private void ResetDevice(int iPosition) {
        viewList = SelectPopWindow.showSelectPopWindow(this, lv_FaceInfo, "设备重启");
        final PopupWindow popupWindow = (PopupWindow) viewList.get(0);
        Button bt_confirm = (Button) viewList.get(1);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ToastUtils.showText(FaceInfoActivity.this, "重启设备", WARN, CENTER, LENGTH_LONG);
                Publicfun.RunShellCmd("reboot");
            }
        });
    }

    /**
     * 为了得到传回的数据，必须在前面的Activity中（指MainActivity类）重写onActivityResult方法
     * <p>
     * requestCode 请求码，即调用startActivityForResult()传递过去的值
     * resultCode 结果码，结果码用于标识返回数据来自哪个新Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int iRet;
        switch (resultCode) {

            case 0:  //人脸识别模式
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                g_LocalInfo.cFaceModeFlag = (short) iRet;
                LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                break;


            case 1:  //设置人脸特征
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                Log.i(TAG, "返回的结果:" + iRet);
                if (iRet == 0) {
                    //是否重新加载人脸特征
                    if ((g_WorkInfo.cFaceInitState == 1) || (g_WorkInfo.cFaceInitState == 2)) {
                        LoadFeatureThread ThreadLoadFeature = new LoadFeatureThread(UISetPayHandler);
                        ThreadLoadFeature.start();
                    }
                } else {
                    //重新下载人脸特征码
                    ToastUtils.showText(FaceInfoActivity.this, "重新下载人脸特征码！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    RunShellCmd("rm -r " + ZYTKFacePath + "*");
                    RunShellCmd("rm -r " + DATAPath + "FaceCodeInfo.dat");
                    ResetDevice(300);
                }
                break;
        }
    }
}
