package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class LocalInfo {

    @StructField(order = 0)
    public int wShopUserID;                //本机商户号
    @StructField(order = 1)
    public short cInputMode;                    //输入方式 1:普通 2:单键 3:定额 4:商品编码
    @StructField(order = 2)
    public int[] wBookMoney = new int[6];        //定义的定额值
    @StructField(order = 3)
    public short cBookSureMode;                //定额方式是否需要确认 0:不需要 1:需要
    @StructField(order = 4)
    public short cKeyLockState;                //键盘锁定      0:不锁定 1:锁定
    @StructField(order = 5)
    public short cTradFailCall;                //交易失败提示模式
    @StructField(order = 6)
    public short cConServerMode;                //连接服务器方式
    @StructField(order = 7)
    public short cUpPamentSum;                //上传交易记录打包笔数
    @StructField(order = 8)
    public short cMoneyDispMode;             //余额显示方式0:默认显示工作钱包1:显示工作钱包和追扣钱包2:显示工作和追扣钱包之和
    @StructField(order = 9)
    public short cPrinterMode;                //打印小票模式      0:不打印 1:直接打印 2:打印提示
    @StructField(order = 10)
    public short cLCDBacklightLevel;        //LCD背光级数 (调节亮度 1-99 1:最亮)
    @StructField(order = 11)
    public short cVolumeLevel;               //语言音量级数 Playback 0 - 127
    @StructField(order = 12)
    public short cPowerSaveTime;             //启用省电模式时长 5-30(分钟)
    @StructField(order = 13)
    public short cLowPowerPercent;           //电量阀值       5-20
    @StructField(order = 14)
    public short cFaceModeFlag;                // 人脸模式 0:手动模式  1:自动模式
    @StructField(order = 15)
    public short cPlayVoiceMoneyFlag;                //是否播放具体金额 0: 不启用 1:启用
    @StructField(order = 16)
    public short cQRCodeShowFlag;                //是否显示二维码 0: 显示 1:不显示
    @StructField(order = 17)
    public short cFaceDetectFlag;                //是否启用人脸识别 0: 不启用 1:启用
    @StructField(order = 18)
    public int iMaxFaceNum;                     //最大支持的检测人脸数量
    @StructField(order = 19)
    public short cFaceLiveFlag;                //人脸活体设置 0:打开活体 1:关闭活体
    @StructField(order = 20)
    public short cLogFlag;                      //日志设置 0:关闭 1:打开
    @StructField(order = 21)
    public short cAccNameShowMode;                //账户显示姓名模式 0:* 号模式  1:正常模式

    @StructField(order = 22)
    public int iPupilDistance;  // 瞳距
    @StructField(order = 23)
    public float fLiveThrehold; //人脸活体检测率
    @StructField(order = 24)
    public float fFraction;      //人脸相识率

    @StructField(order = 25)
    public int iWithholdState;  // 是否启用代扣 0:不启用  1:启用
    @StructField(order = 26)
    public int iBusinessQRState;  // 是否显示商户二维码 0:不启用  1:启用

    @StructField(order = 27)
    public int iPayShowTime;  //在线支付成功显示时长（秒s）

    @StructField(order = 28)
    public int iDisplayInfr;  //显示红外

    @StructField(order = 29)
    public int iDeviceType;  //设备类型

    @StructField(order = 30)
    public short cPowerSaveTimeA;             //启用省电模式时长 5-30(分钟)

    @StructField(order = 31)
    public int iRelayState;     // 继电器状态 0:关 1:开

    @StructField(order = 32)      // 继电器模式 1:常开 0:常闭
    public int iRelayMode;

    @StructField(order = 33)
    public int iRelayOperTime;    // 继电器动作时长(ms)

    @StructField(order = 34)      // 动作脉冲数
    public int iRelayOperCnt;

    @StructField(order = 35)
    public int cDockposFlag;     //是否启用对接机 0-不启用,1-启用

    @StructField(order = 36)
    public byte[] Reserve = new byte[512];          //预留512
    @StructField(order = 37)
    public byte[] CRC16 = new byte[2];            //校验码       2字节

    public int getwShopUserID() {
        return wShopUserID;
    }

    public void setwShopUserID(int wShopUserID) {
        this.wShopUserID = wShopUserID;
    }

    public short getcInputMode() {
        return cInputMode;
    }

    public void setcInputMode(short cInputMode) {
        this.cInputMode = cInputMode;
    }

    public int[] getwBookMoney() {
        return wBookMoney;
    }

    public void setwBookMoney(int[] wBookMoney) {
        this.wBookMoney = wBookMoney;
    }

    public short getcBookSureMode() {
        return cBookSureMode;
    }

    public void setcBookSureMode(short cBookSureMode) {
        this.cBookSureMode = cBookSureMode;
    }

    public short getcKeyLockState() {
        return cKeyLockState;
    }

    public void setcKeyLockState(short cKeyLockState) {
        this.cKeyLockState = cKeyLockState;
    }

    public short getcTradFailCall() {
        return cTradFailCall;
    }

    public void setcTradFailCall(short cTradFailCall) {
        this.cTradFailCall = cTradFailCall;
    }

    public short getcConServerMode() {
        return cConServerMode;
    }

    public void setcConServerMode(short cConServerMode) {
        this.cConServerMode = cConServerMode;
    }

    public short getcUpPamentSum() {
        return cUpPamentSum;
    }

    public void setcUpPamentSum(short cUpPamentSum) {
        this.cUpPamentSum = cUpPamentSum;
    }

    public short getcMoneyDispMode() {
        return cMoneyDispMode;
    }

    public void setcMoneyDispMode(short cMoneyDispMode) {
        this.cMoneyDispMode = cMoneyDispMode;
    }

    public short getcPrinterMode() {
        return cPrinterMode;
    }

    public void setcPrinterMode(short cPrinterMode) {
        this.cPrinterMode = cPrinterMode;
    }

    public short getcLCDBacklightLevel() {
        return cLCDBacklightLevel;
    }

    public void setcLCDBacklightLevel(short cLCDBacklightLevel) {
        this.cLCDBacklightLevel = cLCDBacklightLevel;
    }

    public short getcVolumeLevel() {
        return cVolumeLevel;
    }

    public void setcVolumeLevel(short cVolumeLevel) {
        this.cVolumeLevel = cVolumeLevel;
    }

    public short getcPowerSaveTimeA() {
        return cPowerSaveTimeA;
    }

    public void setcPowerSaveTimeA(short cPowerSaveTimeA) {
        this.cPowerSaveTimeA = cPowerSaveTimeA;
    }

    public short getcLowPowerPercent() {
        return cLowPowerPercent;
    }

    public void setcLowPowerPercent(short cLowPowerPercent) {
        this.cLowPowerPercent = cLowPowerPercent;
    }

    public short getcFaceModeFlag() {
        return cFaceModeFlag;
    }

    public void setcFaceModeFlag(short cFaceModeFlag) {
        this.cFaceModeFlag = cFaceModeFlag;
    }

    public short getcPlayVoiceMoneyFlag() {
        return cPlayVoiceMoneyFlag;
    }

    public void setcPlayVoiceMoneyFlag(short cPlayVoiceMoneyFlag) {
        this.cPlayVoiceMoneyFlag = cPlayVoiceMoneyFlag;
    }

    public short getcQRCodeShowFlag() {
        return cQRCodeShowFlag;
    }

    public void setcQRCodeShowFlag(short cQRCodeShowFlag) {
        this.cQRCodeShowFlag = cQRCodeShowFlag;
    }

    public short getcFaceDetectFlag() {
        return cFaceDetectFlag;
    }

    public void setcFaceDetectFlag(short cFaceDetectFlag) {
        this.cFaceDetectFlag = cFaceDetectFlag;
    }

    public int getiMaxFaceNum() {
        return iMaxFaceNum;
    }

    public void setiMaxFaceNum(int iMaxFaceNum) {
        this.iMaxFaceNum = iMaxFaceNum;
    }

    public short getcFaceLiveFlag() {
        return cFaceLiveFlag;
    }

    public void setcFaceLiveFlag(short cFaceLiveFlag) {
        this.cFaceLiveFlag = cFaceLiveFlag;
    }

    public short getcLogFlag() {
        return cLogFlag;
    }

    public void setcLogFlag(short cLogFlag) {
        this.cLogFlag = cLogFlag;
    }

    public short getcAccNameShowMode() {
        return cAccNameShowMode;
    }

    public void setcAccNameShowMode(short cAccNameShowMode) {
        this.cAccNameShowMode = cAccNameShowMode;
    }

    public int getiPupilDistance() {
        return iPupilDistance;
    }

    public void setiPupilDistance(int iPupilDistance) {
        this.iPupilDistance = iPupilDistance;
    }

    public float getfLiveThrehold() {
        return fLiveThrehold;
    }

    public void setfLiveThrehold(float fLiveThrehold) {
        this.fLiveThrehold = fLiveThrehold;
    }

    public float getfFraction() {
        return fFraction;
    }

    public void setfFraction(float fFraction) {
        this.fFraction = fFraction;
    }

    public int getiWithholdState() {
        return iWithholdState;
    }

    public void setiWithholdState(int iWithholdState) {
        this.iWithholdState = iWithholdState;
    }

    public int getiBusinessQRState() {
        return iBusinessQRState;
    }

    public void setiBusinessQRState(int iBusinessQRState) {
        this.iBusinessQRState = iBusinessQRState;
    }

    public byte[] getReserve() {
        return Reserve;
    }

    public void setReserve(byte[] reserve) {
        Reserve = reserve;
    }

    public byte[] getCRC16() {
        return CRC16;
    }

    public void setCRC16(byte[] CRC16) {
        this.CRC16 = CRC16;
    }

    @Override
    public String toString() {
        return "LocalInfo{" +
                "wShopUserID=" + wShopUserID +
                ", cInputMode=" + cInputMode +
                ", wBookMoney=" + Arrays.toString(wBookMoney) +
                ", cBookSureMode=" + cBookSureMode +
                ", cKeyLockState=" + cKeyLockState +
                ", cTradFailCall=" + cTradFailCall +
                ", cConServerMode=" + cConServerMode +
                ", cUpPamentSum=" + cUpPamentSum +
                ", cMoneyDispMode=" + cMoneyDispMode +
                ", cPrinterMode=" + cPrinterMode +
                ", cLCDBacklightLevel=" + cLCDBacklightLevel +
                ", cVolumeLevel=" + cVolumeLevel +
                ", cPowerSaveTimeA=" + cPowerSaveTimeA +
                ", cLowPowerPercent=" + cLowPowerPercent +
                ", cFaceModeFlag=" + cFaceModeFlag +
                ", cPlayVoiceMoneyFlag=" + cPlayVoiceMoneyFlag +
                ", cQRCodeShowFlag=" + cQRCodeShowFlag +
                ", cFaceDetectFlag=" + cFaceDetectFlag +
                ", iMaxFaceNum=" + iMaxFaceNum +
                ", cFaceLiveFlag=" + cFaceLiveFlag +
                ", cLogFlag=" + cLogFlag +
                ", cAccNameShowMode=" + cAccNameShowMode +
                ", iPupilDistance=" + iPupilDistance +
                ", fLiveThrehold=" + fLiveThrehold +
                ", fFraction=" + fFraction +
                ", iWithholdState=" + iWithholdState +
                ", iBusinessQRState=" + iBusinessQRState +
                //", Reserve=" + Arrays.toString(Reserve) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}
