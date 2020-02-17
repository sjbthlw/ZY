package com.hzsun.mpos.Public;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class WifiUtil {

    private static final String TAG = "WifiUtil";

    //连接WIFI设备
    public static String WifiConnect(WifiManager pWifiManager, String strUserName, String strPassword) {
        if (!pWifiManager.isWifiEnabled()) {
            pWifiManager.setWifiEnabled(true);
        }
        WifiInfo CurWifiInfo = pWifiManager.getConnectionInfo();//获取已经连接WIFI信息
        if ("<unknown ssid>".equals(CurWifiInfo.getSSID())) {
            Log.e("WifiUtil", strUserName + " 未连接");
            // 连接配置好指定ID的网络
            WifiConfiguration config = WifiUtil.createWifiInfo(
                    strUserName, strPassword, 3, pWifiManager);

            int networkId = pWifiManager.addNetwork(config);
            if (null != config) {
                if (pWifiManager.enableNetwork(networkId, true) == true)
                    Log.d("WifiUtil", CurWifiInfo.getSSID() + " 连接成功");
                else
                    Log.e("WifiUtil", CurWifiInfo.getSSID() + " 连接失败");

                return strUserName;
            }
        } else {
            Log.d("WifiUtil", CurWifiInfo.getSSID() + " 已连接");
            return strUserName;
        }
        return "";
    }

    /**
     * 配置wifi
     *
     * @param SSID
     * @param Password
     * @param Type
     * @return
     */
    public static WifiConfiguration createWifiInfo(String SSID,
                                                   String Password, int Type, WifiManager wifiManager) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = isExsits(SSID, wifiManager);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) // WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) // WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 判断wifi是否存在
     *
     * @param SSID
     * @param wifiManager
     * @return
     */
    public static WifiConfiguration isExsits(String SSID,
                                             WifiManager wifiManager) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * 忘记某一个wifi密码
     *
     * @param wifiManager
     * @param targetSsid
     */
    public static void RemoveWifiBySsid(WifiManager wifiManager, String targetSsid) {
        Log.d(TAG, "try to removeWifiBySsid, targetSsid=" + targetSsid);
        List<WifiConfiguration> wifiConfigs = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration wifiConfig : wifiConfigs) {
            String ssid = wifiConfig.SSID;
            Log.d(TAG, "removeWifiBySsid ssid=" + ssid);
            if (ssid.equals("\"" + targetSsid + "\"")) {
                Log.d(TAG, "removeWifiBySsid success, SSID = " + wifiConfig.SSID + " netId = " + String.valueOf(wifiConfig.networkId));
                wifiManager.removeNetwork(wifiConfig.networkId);
                wifiManager.saveConfiguration();
            }
        }
    }

    /**
     * 连接有密码的wifi.
     *
     * @param SSID     ssid
     * @param Password Password
     * @return apConfig
     */
    public static WifiConfiguration setWifiParamsPassword(String SSID, String Password) {
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
    public static WifiConfiguration setWifiParamsNoPassword(String ssid) {
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
     * 转换IP地址
     *
     * @param i
     * @return
     */
    public static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + ((i >> 24) & 0xFF);
    }

    public static String getWifiIp(Context myContext, WifiManager wifiManager) {

        if (myContext == null) {
            throw new NullPointerException("Global context is null");
        }
        WifiManager wifiMgr = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            int ipAsInt = wifiMgr.getConnectionInfo().getIpAddress();
            if (ipAsInt == 0) {
                return null;
            } else {
                return intToIp(ipAsInt);
            }
        } else {
            return null;
        }
    }

    // 获取MAC地址 strNif "wlan0"  "eth0"
    public static String getMacAddr(String strNif) {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase(strNif)) continue;
                byte[] macBytes = nif.getHardwareAddress();
                Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                    String strIP =inetAddress.getHostAddress().toString();
                    Log.d(TAG,"IP:"+strIP);
                }
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
}
