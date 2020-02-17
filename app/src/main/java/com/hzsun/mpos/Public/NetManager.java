package com.hzsun.mpos.Public;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
//import android.net.ethernet.EthernetManager;

public class NetManager {


    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    /**
     * 通过网络接口取
     *
     * @return
     */
    public static String GetLocalMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                //if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                if (!nif.getName().equalsIgnoreCase("eth0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X", b));
                }
//                if (res1.length() > 0) {
//                    res1.deleteCharAt(res1.length()-1);
//                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

//    //配置网络参数
//    public static int ConfigNetWorkPara()
//    {
//        String dev_name;
//        String ipaddr; // IP地址
//        String netmask; // 子网掩码
//        String gw; // 网关设置
//        String dns; // DNS设置
//        int mode; // 模式设置：静态或者动态
//        String hwaddr; // MAC地址
//
//        //一、获取EthernetManager实例
//        EthernetManager mEthManager;
//        EthernetDevInfo mDevInfo;
//
//        mEthManager = EthernetManager.getInstance();
//
//        //二、填充mDevInfo
//        mDevInfo = mEthManager.getSavedConfig();
//        mDevInfo.setConnectMode(EthernetDevInfo.ETHERNET_CONN_MODE_MANUAL);
//        mDevInfo.setIpAddress("XXX");
//        mDevInfo.setNetMask("XXX");
//        mDevInfo.setDnsAddr("XXX");
//        mDevInfo.setGateWay("XXX");
//
//        mDevInfo.setHwaddr("XXX"); // getSavedConfig获取的mac地址为null，需要主动填充地址，否则进入系统设置会因为地址null崩溃
//        return 0;
//    }

}
