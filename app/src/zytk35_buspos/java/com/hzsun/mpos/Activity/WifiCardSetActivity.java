package com.hzsun.mpos.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.Public.WifiUtil;
import com.hzsun.mpos.R;
import com.hzsun.mpos.Sound.SoundPlay;
import com.hzsun.mpos.camera.CameraProxy;
import com.hzsun.mpos.camera.CameraTextureView;
import com.hzsun.mpos.data.WifiParaInfo;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_WifiParaInfo;
import static com.hzsun.mpos.Public.Publicfun.ByteToString;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.SUCCESS;
import static com.hzsun.mpos.Public.ToastUtils.WARN;
import static com.hzsun.mpos.Public.WifiUtil.RemoveWifiBySsid;
import static com.hzsun.mpos.Public.WifiUtil.getMacAddr;
import static com.hzsun.mpos.Public.WifiUtil.getWifiIp;
import static java.util.Arrays.fill;


public class WifiCardSetActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = getClass().getSimpleName();
    private View wifiLinearLayout;
    private TextView wifiName;
    private TextView tvConfirm;
    private Button btn_scancard;
    private Button btn_scanqr;
    private CameraTextureView preview;
    private Allocation in, out;

    private WifiManager wifiManager;
    private WifiInfo currentWifiInfo;// 当前所连接的wifi
    private WifiParaInfo sWifiParaInfo;
    private boolean isScanCard = false;
    private boolean isStartScan = false;
    private MyHandler handler;
    private ScanWifiThread scanWifiThread;
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Bitmap bitmap;
    private CameraProxy mCameraProxy;
    private boolean isCameraScan;      //是否开始通过摄像头扫描二维码

    private static class MyHandler extends Handler {
        WifiCardSetActivity activity;
        WeakReference<WifiCardSetActivity> reference;

        public MyHandler(WifiCardSetActivity activity) {
            reference = new WeakReference<>(activity);
        }

        public void handleMessage(Message msg) {
            activity = reference.get();
            if (activity != null) {
                activity.onHandleMessage(msg);
            }
        }
    }

    private void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 0: //正在获取ip地址...
                new RefreshSsidThread().start();
                break;
            case 1:
                ToastUtils.showText(WifiCardSetActivity.this, "WIFI连接失败！", WARN, BOTTOM, Toast.LENGTH_LONG);
                break;
            case 4:
                ToastUtils.showText(WifiCardSetActivity.this, "WIFI连接成功！", SUCCESS, BOTTOM, Toast.LENGTH_LONG);
                wifiLinearLayout.setVisibility(View.VISIBLE);
                //wifiName.setText(currentWifiInfo.getSSID());

                String strIP=getWifiIp(this, wifiManager);
                Log.d(TAG,"WIFI IP:"+strIP);

                String strMac=getMacAddr("wlan0");
                Log.d(TAG,"WIFI Mac:"+strMac);
                String strTemp=currentWifiInfo.getSSID() + " 连接成功"+"\n"
                        +"IP:"+strIP+"\n"
                        +"Mac:"+strMac+"\n";
                wifiName.setText(strTemp);

                break;

            case 10: //wifi设置卡成功
                int iRet = msg.arg1;
                if (iRet == 0) {
                    sWifiParaInfo = (WifiParaInfo) msg.obj;
                    if (sWifiParaInfo.cInfoState != 0) {
                        SoundPlay.VoicePlay("setting_ok");
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
            case 11:
                preview.setVisibility(View.GONE);
                if (mCameraProxy != null) {
                    mCameraProxy.stopPreview();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_card_set);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        handler = new MyHandler(this);
        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        initViews();
    }

    private void initViews() {
        setTitle("WIFI卡设置");
        wifiLinearLayout = findViewById(R.id.ll_wifi_info);
        wifiName = (TextView) findViewById(R.id.tv_wifiName);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        tvConfirm.setText("请刷WIFI卡或二维码");
        tvConfirm.setTextColor(Color.RED);
        preview = (CameraTextureView) findViewById(R.id.preview);
        btn_scanqr = (Button) findViewById(R.id.btn_scanqr);
        btn_scancard = (Button) findViewById(R.id.btn_scancard);
        btn_scancard.setOnClickListener(this);
        btn_scanqr.setOnClickListener(this);
    }

    private void startPreview() {
        mCameraProxy = preview.getCameraProxy();
        preview.setPreviewCallback(new Camera.PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (yuvType == null) {
                    yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
                    in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
                    rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(THFI_Param.IMG_WIDTH).setY(THFI_Param.IMG_HEIGHT);
                    out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
                }
                in.copyFrom(data);
                yuvToRgbIntrinsic.setInput(in);
                yuvToRgbIntrinsic.forEach(out);
                bitmap = Bitmap.createBitmap(THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, Bitmap.Config.ARGB_8888);
                out.copyTo(bitmap);
            }
        });
    }

    private void decodeQRCode() {
        if (bitmap == null) return;
        int picWidth = bitmap.getWidth();
        int picHeight = bitmap.getHeight();
        int[] pix = new int[picWidth * picHeight];
        Log.e(TAG, "decodeFromPicture:图片大小： " + bitmap.getByteCount() / 1024 / 1024 + "M");
        bitmap.getPixels(pix, 0, picWidth, 0, 0, picWidth, picHeight);
        //构造LuminanceSource对象
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(picWidth
                , picHeight, pix);
        BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        //因为解析的条码类型是二维码，所以这边用QRCodeReader最合适。
        QRCodeReader qrCodeReader = new QRCodeReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, true);
        Result result;
        try {
            result = qrCodeReader.decode(bb, hints);
            String strResult = result.toString();
            Log.i("scan", "扫描结果:" + result);

            WifiParaInfo pWifiParaInfo = new WifiParaInfo();

            //WIFI:T:WPA;S:zyzh-808PD;P:1357924681;;
            if (strResult.contains("P:") && strResult.contains("T:")) {
                if (handler != null) {
                    handler.sendEmptyMessage(11);
                }
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
                if (pWifiParaInfo != null) {
                    Message message = Message.obtain();
                    message.obj = pWifiParaInfo;
                    message.what = 10;
                    message.arg1 = 0;
                    handler.sendMessage(message);
                }
            } else {
                if (isCameraScan) {
                    SoundPlay.VoicePlay("invalid_qrcode");
                }
            }
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scancard:
                isCameraScan = false;
                Log.i(TAG, "按了确定键,重新刷卡配置wifi");
                SoundPlay.VoicePlay("setcard");
                if (isStartScan == false) {
                    isStartScan = true;
                    g_Nlib.QR_ClearRecvData(10);
                    g_Nlib.QR_SetDeviceReadEnable(1);
                }
                break;
            case R.id.btn_scanqr:
                preview.setVisibility(View.VISIBLE);
                isStartScan = true;
                isCameraScan = true;
                startPreview();
                break;
        }
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
            //wifiName.setText(currentWifiInfo.getSSID() + " 已连接");

            String strIP=getWifiIp(this, wifiManager);
            Log.d(TAG,"WIFI IP:"+strIP);

            String strMac=getMacAddr("wlan0");
            Log.d(TAG,"WIFI Mac:"+strMac);
            String strTemp=currentWifiInfo.getSSID() + " 已连接"+"\n"
                    +"IP:"+strIP+"\n"
                    +"Mac:"+strMac+"\n";
            wifiName.setText(strTemp);
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
                    decodeQRCode();
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
        }
        return 0;
    }

    //扫描wifi二维码
    private int ScanWifiQRCode() {
        byte[] RecvData = new byte[1024];
        byte[] RecvQRCode = new byte[1024];
        int iRecvLen = 0;
        int iResult = 1;
        g_Nlib.QR_ClearRecvData(10);
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

            //WIFI:T:WPA;S:zyzh-808PD;P:1357924681;
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
                if (!isCameraScan && isScanCard) {
                    SoundPlay.VoicePlay("invalid_qrcode");
                }
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
//                    setWifiParamsPassword( params[0],  params[1])), true);
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

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

        if (mCameraProxy != null) {
            mCameraProxy.stopPreview();
        }
    }


}
