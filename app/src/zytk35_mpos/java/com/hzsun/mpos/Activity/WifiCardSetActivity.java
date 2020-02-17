package com.hzsun.mpos.Activity;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.Public.WifiUtil;
import com.hzsun.mpos.R;
import com.hzsun.mpos.Sound.SoundPlay;
import com.hzsun.mpos.data.WifiParaInfo;

import java.util.ArrayList;
import java.util.List;

import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_WifiParaInfo;
import static com.hzsun.mpos.Public.Publicfun.ByteToString;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.SUCCESS;
import static com.hzsun.mpos.Public.ToastUtils.WARN;
import static com.hzsun.mpos.Public.WifiUtil.RemoveWifiBySsid;
import static java.util.Arrays.fill;

public class WifiCardSetActivity extends BaseActivity {

    private String TAG = getClass().getSimpleName();
    private int image;
    private String title, proTitle;
    private ImageView ivSetImage;
    private TextView titleTv;
    private LinearLayout wifiLinearLayout;
    private TextView wifiName;
    private TextView tvConfirm;

    private WifiManager wifiManager;
    private List<ScanResult> wifiList;// wifi列表
    private List<String> listSSID = new ArrayList<>();
    private WifiInfo currentWifiInfo;// 当前所连接的wifi
    private WifiParaInfo sWifiParaInfo;
    private boolean isScanCard = false;
    private boolean isScanWifi = false;
    private boolean isStartScan = false;

    private ScanWifiThread scanWifiThread;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: //正在获取ip地址...
                    new RefreshSsidThread().start();
                    break;
                case 1:
                    //Toast.makeText(WifiCardSetActivity.this, "WIFI连接失败！", Toast.LENGTH_SHORT).show();
                    ToastUtils.showText(WifiCardSetActivity.this, "WIFI连接失败！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    break;
                case 4:
                    //Toast.makeText(WifiCardSetActivity.this, "WIFI连接成功！", Toast.LENGTH_SHORT).show();
                    ToastUtils.showText(WifiCardSetActivity.this, "WIFI连接成功！", SUCCESS, BOTTOM, Toast.LENGTH_LONG);

                    wifiLinearLayout.setVisibility(View.VISIBLE);
                    wifiName.setText(currentWifiInfo.getSSID());
                    break;

                case 10: //wifi设置卡成功
                    int iRet = msg.arg1;
                    if (iRet == 0) {
                        sWifiParaInfo = (WifiParaInfo) msg.obj;
                        if (sWifiParaInfo.cInfoState != 0) {
                            SoundPlay.VoicePlay("setting_ok");
                            //scanWifiThread.s_exit();
                            Log.i(TAG, "wifi用户名:" + sWifiParaInfo.strUserName);
                            Log.i(TAG, "wifi密码:" + sWifiParaInfo.strPassword);
                            tvConfirm.setText("用户名:" + sWifiParaInfo.strUserName);
                            g_Nlib.QR_SetDeviceReadEnable(2);//1:$108001-9E81 开始识读2
                            new ConnectWifiThread().execute(sWifiParaInfo.strUserName, sWifiParaInfo.strPassword);
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    PutResult(sWifiParaInfo, 10, 1);
                                }
                            }, 2000);
                        }
                    } else if (iRet == 1)
                        tvConfirm.setText("请刷WIFI设置卡或二维码!");
                    else if (iRet == 2) {
                        tvConfirm.setText("错误，请刷WIFI设置卡或二维码!");
                        SoundPlay.VoicePlay("setcard");
                    }
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_card_set);
        isShowPerScreen(true);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        initData();
        initViews();
    }

    private void initData() {
        image = getIntent().getIntExtra("Image", -1);
        title = getIntent().getStringExtra("Title");
        proTitle = getIntent().getStringExtra("proTitle");
    }

    private void initViews() {
        wifiLinearLayout = ((LinearLayout) findViewById(R.id.ll_wifi_info));
        wifiName = ((TextView) findViewById(R.id.tv_wifiName));
        tvConfirm = ((TextView) findViewById(R.id.tv_confirm));
        ivSetImage = ((ImageView) findViewById(R.id.iv_set_image));
        titleTv = ((TextView) findViewById(R.id.title_tv));
        ivSetImage.setImageResource(image);
        titleTv.setText(proTitle + " > " + title);
        tvConfirm.setText("按确定键后,刷WIFI卡或二维码");
        tvConfirm.setTextColor(Color.RED);
    }

    @Override
    protected void onResume() {
        openWifi(); //打开wifi
        g_WifiParaInfo = Publicfun.ReadWifiInfoStrCfg();
        currentWifiInfo = wifiManager.getConnectionInfo();//获取已经连接WIFI信息

        if ("<unknown ssid>".equals(currentWifiInfo.getSSID())) {
            //wifiLinearLayout.setVisibility(View.GONE);
            wifiName.setText(g_WifiParaInfo.strUserName + " 未连接");
        } else {
            wifiName.setText(currentWifiInfo.getSSID() + " 已连接");
        }
        scanWifiThread = new ScanWifiThread();
        scanWifiThread.start();

        super.onResume();
    }

    /**
     * 打开wifi
     */
    public void openWifi() {

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }
//
//    /**
//     * 扫描wifi线程
//     *
//     * @author passing
//     */
//    class ScanWifiThread extends Thread {
//
//        public void s_exit(){
//            isScanWifi = false;
//            try{
//                this.join();
//            }catch (InterruptedException e){
//            }
//        }
//        @Override
//        public void run() {
//            isScanWifi = true;
//
//            while (isScanWifi) {
//                startWifiScan();
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    break;
//                }
//            }
//        }
//    }
//
//    /**
//     * 扫描wifi
//     */
//    public void startWifiScan() {
//        wifiManager.startScan();
//        // 获取扫描结果
//        wifiList = wifiManager.getScanResults();
//    }

    /**
     * 扫描wifi卡和二维码线程
     *
     * @author passing
     */
    class ScanWifiThread extends Thread {

        public void exit() {
            isScanCard = false;
            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }

        @Override
        public void run() {
            isScanCard = true;

            while (isScanCard) {

                if (isStartScan == true) {
                    ScanWifiCard();
                    ScanWifiQRCode();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    //扫描wifi卡
    private int ScanWifiCard() {
        int cResult;
        byte[] bCardUID = new byte[8];
        byte[] bContextTemp = new byte[16];
        byte[][] bCardContext = new byte[3][16];

        cResult = g_Nlib.ReaderCardUID(bCardUID);
        if (cResult == 0) {
            WifiParaInfo pWifiParaInfo = new WifiParaInfo();
            cResult = g_Nlib.ReadMifareSector((byte) 10, bCardUID, bCardContext);
            if (cResult == 0) {
                if (bCardContext[0][0] == 1) {
                    byte[] bTemp = new byte[16];
                    pWifiParaInfo.cIPMode = bCardContext[0][1];        //WIFI获取IP的方式
                    if ((bCardContext[1][0] != 0)
                            && (bCardContext[2][0] != 0)) {
                        //wifi用户名
                        System.arraycopy(bCardContext[1], 0, bContextTemp, 0, bContextTemp.length);
                        pWifiParaInfo.strUserName = ByteToString(bContextTemp);
                        //wifi密码
                        System.arraycopy(bCardContext[2], 0, bContextTemp, 0, bContextTemp.length);
                        pWifiParaInfo.strPassword = ByteToString(bContextTemp);
                        pWifiParaInfo.cInfoState = 1;//WIFI是否刷了设置卡
                    } else {
                        pWifiParaInfo.cInfoState = 0;
                    }
                } else {
                    Log.i(TAG, "不是 wifi 卡");
                    cResult = 2;
                }
                isScanCard = false;
            } else {
                cResult = 2;
            }
            Message message = Message.obtain();
            message.obj = pWifiParaInfo;
            message.what = 10;
            message.arg1 = cResult;
            handler.sendMessage(message);
        } else {
            cResult = 1;
        }
        return 0;
    }

    //扫描wifi二维码
    private int ScanWifiQRCode() {
        byte[] RecvData = new byte[1024];
        byte[] RecvQRCode = new byte[1024];
        int iRecvLen = 0;
        int iResult = 1;

        iRecvLen = g_Nlib.QR_ScanQRCode(RecvData);
        if (iRecvLen > 0) {
            SoundPlay.VoicePlay("beep");
            if (iRecvLen > 512) {
                iRecvLen = 0;
                fill(RecvQRCode, (byte) 0);
                fill(RecvData, (byte) 0);
                return 1;
            }
            byte[] RecvQRCodeTemp = new byte[iRecvLen];
            System.arraycopy(RecvData, 0, RecvQRCodeTemp, 0, iRecvLen);
            String strResult = new String(RecvQRCodeTemp);

            Log.i(TAG, "接收到QR数据:" + strResult);
            WifiParaInfo pWifiParaInfo = new WifiParaInfo();

            //WIFI:T:WPA;S:zyzh-808PD;P:1357924681;;
            if (strResult.contains("P:") && strResult.contains("T:")) {
                Log.e("扫描返回的结果----->", strResult);// 还是要判断
                String passwordTemp = strResult.substring(strResult
                        .indexOf("P:"));
                String password = passwordTemp.substring(2,
                        passwordTemp.indexOf(";"));
                String netWorkTypeTemp = strResult.substring(strResult
                        .indexOf("T:"));
                String netWorkType = netWorkTypeTemp.substring(2,
                        netWorkTypeTemp.indexOf(";"));
                String netWorkNameTemp = strResult.substring(strResult
                        .indexOf("S:"));
                String netWorkName = netWorkNameTemp.substring(2,
                        netWorkNameTemp.indexOf(";"));

                if ((passwordTemp != null) && (netWorkNameTemp != null) && (passwordTemp.indexOf(";") > 2) && (netWorkNameTemp.indexOf(";") > 2)) {
                    pWifiParaInfo.strUserName = netWorkName;//wifi用户名
                    pWifiParaInfo.strPassword = password;//wifi密码
                    pWifiParaInfo.cInfoState = 1;//WIFI是否刷了设置卡
                    pWifiParaInfo.cIPMode = 1;
                    isStartScan = false;
                    if (!wifiManager.isWifiEnabled()) {
                        Toast.makeText(this, "开启wifi设置", Toast.LENGTH_LONG)
                                .show();
                        openWifi();
                    }
                    iResult = 0;
                } else {
                    SoundPlay.VoicePlay("invalid_qrcode");
                    iResult = 1;
                }
            } else {
                SoundPlay.VoicePlay("invalid_qrcode");
                iResult = 1;
            }
            Message message = Message.obtain();
            message.obj = pWifiParaInfo;
            message.what = 10;
            message.arg1 = iResult;
            handler.sendMessage(message);
        }
        return 0;
    }

    /**
     * 连接有密码的wifi.
     *
     * @param SSID     ssid
     * @param Password Password
     * @return apConfig
     */
    private WifiConfiguration setWifiParamsPassword(String SSID, String Password) {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = "\"" + SSID + "\"";
        apConfig.preSharedKey = "\"" + Password + "\"";
        //不广播其SSID的网络
        apConfig.hiddenSSID = true;
        apConfig.status = WifiConfiguration.Status.ENABLED;
        //公认的IEEE 802.11验证算法。
        apConfig.allowedAuthAlgorithms.clear();
        apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        //公认的的公共组密码
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        //公认的密钥管理方案
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        //密码为WPA。
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        //公认的安全协议。
        apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return apConfig;
    }

    /**
     * 连接没有密码wifi.
     *
     * @param ssid ssid
     * @return configuration
     */
    private WifiConfiguration setWifiParamsNoPassword(String ssid) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + ssid + "\"";
        configuration.status = WifiConfiguration.Status.ENABLED;
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        configuration.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        configuration.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return configuration;
    }

    /**
     * 忘记某一个wifi密码
     *
     * @param wifiManager
     * @param targetSsid
     */
    public void removeWifiBySsid(WifiManager wifiManager, String targetSsid) {
        Log.d(TAG, "try to removeWifiBySsid, targetSsid=" + targetSsid);
        List<WifiConfiguration> wifiConfigs = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration wifiConfig : wifiConfigs) {
            String ssid = wifiConfig.SSID;
            Log.d(TAG, "removeWifiBySsid ssid=" + ssid);
            if (ssid.equals(targetSsid)) {
                Log.d(TAG, "removeWifiBySsid success, SSID = " + wifiConfig.SSID + " netId = " + String.valueOf(wifiConfig.networkId));
                wifiManager.removeNetwork(wifiConfig.networkId);
                wifiManager.saveConfiguration();
            }
        }
    }

    /**
     * 连接wifi
     *
     * @author passing
     */
    public class ConnectWifiThread extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
//            for (int i = 0; i < wifiList.size(); i++) {
//                listSSID.add(wifiList.get(i).SSID);
//            }
//            if (!listSSID.contains(params[0])) {
//                return null;
//            }

//            //需要密码
//            wifiManager.enableNetwork(wifiManager.addNetwork(
//                    setWifiParamsPassword(name, password)), true);
//            //不需要密码
//            wifiManager.enableNetwork(wifiManager.addNetwork(setWifiParamsNoPassword(name)),
//                    true);

            // 连接配置好指定ID的网络
            WifiConfiguration config = WifiUtil.createWifiInfo(
                    params[0], params[1], 3, wifiManager);

            int networkId = wifiManager.addNetwork(config);
            if (null != config) {
                wifiManager.enableNetwork(networkId, true);
                return params[0];
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (null != result) {
                handler.sendEmptyMessage(0);
            } else {
                handler.sendEmptyMessage(1);
            }
            super.onPostExecute(result);
        }
    }

    /**
     * 获取网络ip地址
     *
     * @author passing
     */
    class RefreshSsidThread extends Thread {
        private boolean flag = true;

        public void exit() {
            flag = false;
        }

        @Override
        public void run() {

            while (flag) {
                currentWifiInfo = wifiManager.getConnectionInfo();
                if (null != currentWifiInfo.getSSID()
                        && 0 != currentWifiInfo.getIpAddress()) {
                    flag = false;
                }
            }
            handler.sendEmptyMessage(4);
            super.run();
        }
    }

    //Activity返回参数
    private void PutResult(WifiParaInfo pWifiParaInfo, int iType, int iRet) {

        Publicfun.SetWifiParaFile(pWifiParaInfo);
        if ((pWifiParaInfo.strUserName.equals(g_WifiParaInfo.strUserName))
                && (pWifiParaInfo.strPassword.equals(g_WifiParaInfo.strPassword))) {
            Log.d(TAG, "WIFI用户名和密码相同");
        } else {
            Log.d(TAG, "删除原有的wifi：" + g_WifiParaInfo.strUserName);

//            if (wifiManager.isWifiEnabled()) {
//                Log.d(TAG,"WIFI已经打开");
//            }
//            WifiConfiguration tempConfig = isExsits(g_WifiParaInfo.strUserName, wifiManager);
//            if (tempConfig != null) {
//                wifiManager.removeNetwork(tempConfig.networkId);
//                wifiManager.saveConfiguration();
//                //RemoveWifiBySsid(wifiManager, g_WifiParaInfo.strUserName);
//            }
            RemoveWifiBySsid(wifiManager, g_WifiParaInfo.strUserName);
        }
        try {
            g_WifiParaInfo = (WifiParaInfo) pWifiParaInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        wifiName.setText(g_WifiParaInfo.strUserName + " 连接中...");

//        Intent intent = new Intent();
//        Log.i(TAG,"iType:"+iType+" result:"+iRet);
//        intent.putExtra("result",iRet);
//        if((iType==10)&&(iRet==1))
//        {
//            Publicfun.SetWifiParaFile(g_WifiParaInfo);
//            intent.putExtra("strUserName",g_WifiParaInfo.strUserName);
//            intent.putExtra("strPassword",g_WifiParaInfo.strPassword);
//        }
//        setResult(iType,intent);
        //finish();//此处一定要调用finish()方法
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        g_Nlib.QR_SetDeviceReadEnable(2);//1:$108001-9E81 开始识读2
        scanWifiThread.exit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.i(TAG,"keyCode:"+keyCode);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_ENTER:
                Log.i(TAG, "按了确定键,重新刷卡配置wifi");
                SoundPlay.VoicePlay("setcard");
                if (isStartScan == false) {
                    isStartScan = true;
                    g_Nlib.QR_ClearRecvData(5);
                    g_Nlib.QR_SetDeviceReadEnable(1);//1:$108001-9E81 开始识读2
                }
                break;

            case KeyEvent.KEYCODE_BACK:
                Log.i(TAG, "按了取消键");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}
