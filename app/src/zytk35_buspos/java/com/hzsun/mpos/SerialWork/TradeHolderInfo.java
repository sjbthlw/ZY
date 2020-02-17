package com.hzsun.mpos.SerialWork;

import android.util.Log;

import java.io.UnsupportedEncodingException;

import static com.hzsun.mpos.Global.Global.g_CardAttr;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardHQRCodeInfo;
import static com.hzsun.mpos.Global.Global.g_FacePayInfo;


public class TradeHolderInfo {
    private String TAG = TradeHolderInfo.class.getSimpleName();
    private long lngAccountID;
    private byte[] cPerCode = new byte[16];
    private byte[] cCardSID = new byte[4];
    private byte[] cAccName = new byte[16];
    private byte[] phonenum = new byte[11];
    private long lngCardID;
    private byte[] cIDNo = new byte[18];

    public long getLngAccountID() {
        return lngAccountID;
    }

    public void setLngAccountID(long lngAccountID) {
        this.lngAccountID = lngAccountID;
    }

    public byte[] getcPerCode() {
        return cPerCode;
    }

    public void setcPerCode(byte[] cPerCode) {
        this.cPerCode = cPerCode;
    }

    public byte[] getcCardSID() {
        return cCardSID;
    }

    public void setcCardSID(byte[] cCardSID) {
        this.cCardSID = cCardSID;
    }

    public byte[] getcAccName() {
        return cAccName;
    }

    public void setcAccName(byte[] cAccName) {
        this.cAccName = cAccName;
    }

    public byte[] getPhonenum() {
        return phonenum;
    }

    public void setPhonenum(byte[] phonenum) {
        this.phonenum = phonenum;
    }

    public long getLngCardID() {
        return lngCardID;
    }

    public void setLngCardID(long lngCardID) {
        this.lngCardID = lngCardID;
    }

    public byte[] getcIDNo() {
        return cIDNo;
    }

    public void setcIDNo(byte[] cIDNo) {
        this.cIDNo = cIDNo;
    }

    public void loadCardHolderInfo(int paymode) {
        switch (paymode) {
            case 0:
            case 1:
                //二维码
                if (g_CardHQRCodeInfo != null) {
                    setLngAccountID(g_CardHQRCodeInfo.lngAccountID);
                    setcPerCode(g_CardHQRCodeInfo.cPerCode);
                    setcAccName(g_CardHQRCodeInfo.cAccName);
                    Log.i(TAG, "二维码信息");
                    Log.i(TAG, "账号:" + getLngAccountID());
                    long cardPerCode = 0;
                    for (int i = 0; i < 4; i++) {
                        cardPerCode += (getcPerCode()[i] & 0xff) * Math.pow(2, i * 8);
                    }
                    Log.i(TAG, "个人编号:" + cardPerCode);
                    try {
                        Log.i(TAG, "姓名:" + new String(getcAccName(), "GB2312"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                if (g_FacePayInfo != null) {
                    Log.i(TAG, "人脸信息");
                    setLngAccountID(g_FacePayInfo.lngAccountID);
                    setcPerCode(g_FacePayInfo.cPerCode);
                    setcAccName(g_FacePayInfo.cAccName);
                    Log.i(TAG, "账号:" + getLngAccountID());
                    long cardPerCode = 0;
                    for (int i = 0; i < 4; i++) {
                        cardPerCode += (getcPerCode()[i] & 0xff) * Math.pow(2, i * 8);
                    }
                    Log.i(TAG, "个人编号:" + cardPerCode);
                    try {
                        Log.i(TAG, "姓名:" + new String(getcAccName(), "GB2312"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 3:
                //卡片支付
                if (g_CardBasicInfo != null) {
                    setLngAccountID(g_CardBasicInfo.lngAccountID);  //账号
                    Log.i(TAG, "账号:" + getLngAccountID());

                    setcAccName(g_CardBasicInfo.cAccName);   //姓名
                    try {
                        Log.i(TAG, "姓名:" + new String(g_CardBasicInfo.cAccName, "GB2312"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    setcCardSID(g_CardAttr.cCardSID);     //物理卡号
                    long cardNo = 0;
                    for (int i = 0; i < 4; i++) {
                        cardNo += (g_CardAttr.cCardSID[i] & 0xFF) * Math.pow(2, 8 * i);
                    }
                    Log.i(TAG, String.format("卡号:%d", cardNo));
                    setLngCardID(g_CardBasicInfo.lngCardID);    //卡内编号
                    Log.i(TAG, "卡内编号:" + g_CardBasicInfo.lngCardID);
                    setcPerCode(g_CardBasicInfo.cCardPerCode);    //个人编号
                    long cardPerCode = 0;
                    for (int i = 0; i < 4; i++) {
                        cardPerCode += (g_CardBasicInfo.cCardPerCode[i] & 0xff) * Math.pow(2, i * 8);
                    }
                    Log.i(TAG, "个人编号:" + cardPerCode);
                }
                break;
        }
    }
}
