package com.hzsun.mpos.Pos;

import android.util.Log;

import com.hzsun.mpos.CardWork.CardApp;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.data.WasteBooks;
import com.hzsun.mpos.data.WasteBooksRW;

import static com.hzsun.mpos.CardWork.CardApp.SetCurDateTime;
import static com.hzsun.mpos.Global.Global.DATE_TIME_ERROR;
import static com.hzsun.mpos.Global.Global.MAXBOOKSCOUNT;
import static com.hzsun.mpos.Global.Global.MEMORY_FAIL;
import static com.hzsun.mpos.Global.Global.OK;
import static com.hzsun.mpos.Global.Global.PAYMENTRECORD_LEN;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;

public class Subsidy {

    private static final String TAG = "Subsidy";


    //领取补助
    public static int Subsidy_CheckOut(int wSubSID, int cSubBurseID, long lngSubMoney) {
        int cResult;
        byte[] cCurDateTime = new byte[6];

        Log.d(TAG, "补助金额：" + lngSubMoney);

        //检查未上传流水数量
        if (g_RecordInfo.lngPaymentRecordID - g_RecordInfo.lngPaymentSendID >= MAXBOOKSCOUNT - 20) {
            return 99;
        }
        // 时间合法性检查
        Publicfun.GetCurrDateTime(cCurDateTime);
        //设置当前系统时间日期
        SetCurDateTime(cCurDateTime);
        //补助流程
        cResult = CardApp.Burse_SubsidyProcess(wSubSID, cSubBurseID, lngSubMoney, g_CardBasicInfo);
        if (cResult == OK) {
            g_WorkInfo.cSubsidyState = 1;
            Log.d(TAG, "记录补助交易流水");
            cResult = WriteSubsidyRecord();
            if (cResult == OK) {
                Log.d(TAG, "补助结帐成功");
                return OK;
            } else {
                Log.d(TAG, "记录流水失败:" + cResult);
                if (cResult != 0) {
                    return cResult;
                }
            }
        }
        return cResult;
    }

    /*
    费用类型	Tmp06	char(2)	Y
    已写卡费用类型：
    00:商务消费;30：以角为单位 60：元
    01:钱包转出；
    02:钱包转入；
    03:交易冲正；33 角  63元
    05:终端存款；35 角  65元
    06:存款冲正；36 角  66元
    07:余额复位；
    08:追扣消费；38角 68元
    09:终端圈存；
    21:信息圈存；
    22:卡户取款；52角 82元
    23:消费管理费 53角 83元
    24:消费管理费冲正； 54角 84元
    
    未写卡费用类型：
    10:商务消费；40角 70元
    11:断点拔卡；
    12:断点恢复；
    13:交易冲正；43角 73元
    15:终端存款；45角  75元
    18:追扣消费；48角  78元
    99:问题流水
    
    交易类：
    A0：刷卡结束；A1：充满结束；A2：中断结束；A3：超过最大功率结束；A4：低于最小功率结束；A5：到点结束；A6：故障结束；B0~BF：各类故障；
    */
    //记录工作钱包正式补助流水 09:终端圈存；
    public static int WriteSubsidyRecord() {
        int cResult;
        long Index;
        int cBurseID;
        long Temp;
        long cMoneyTemp;
        long lngPayRecordID;
        long lngTotalPayMoney;
        byte[] PayDatetime = new byte[4];
        int Crc;

        WasteBooks stWasteBooks = new WasteBooks();
        cBurseID = g_StationInfo.cWorkBurseID;

        //站点号
        Temp = g_StationInfo.iStationID;
        stWasteBooks.cStationID[0] = (byte) (Temp & 0x00FF);
        stWasteBooks.cStationID[1] = (byte) ((Temp & 0xFF00) >> 8);

        //站点流水号 (设备终端流水号)
        lngPayRecordID = g_WasteBookInfo.WriterIndex + 1;
        stWasteBooks.cPayRecordID[0] = (byte) (lngPayRecordID & 0x000000FF);
        stWasteBooks.cPayRecordID[1] = (byte) ((lngPayRecordID & 0x0000FF00) >> 8);
        stWasteBooks.cPayRecordID[2] = (byte) ((lngPayRecordID & 0x00FF0000) >> 16);
        stWasteBooks.cPayRecordID[3] = (byte) ((lngPayRecordID & 0xFF000000) >> 24);

        //终端机号(以太网设备无终端机号)
        stWasteBooks.bWriteContext = 0;

        //营业时段号
        stWasteBooks.cBusinessID = g_WorkInfo.cBusinessID;

        //商户序号
        Log.d(TAG, "商户序号:" + g_LocalInfo.wShopUserID);
        Temp = g_LocalInfo.wShopUserID;
        stWasteBooks.cShopUserID[0] = (byte) (Temp & 0x00ff);
        stWasteBooks.cShopUserID[1] = (byte) ((Temp & 0xff00) >> 8);

        //卡内编号
        Temp = g_CardBasicInfo.lngCardID;
        stWasteBooks.cCardID[0] = (byte) (Temp & 0x000000ff);
        stWasteBooks.cCardID[1] = (byte) ((Temp & 0x0000ff00) >> 8);
        stWasteBooks.cCardID[2] = (byte) ((Temp & 0x00ff0000) >> 16);

        //交易钱包序号
        stWasteBooks.cBurseID = (byte) cBurseID;

        //钱包流水号
        Temp = g_CardBasicInfo.iWorkBurseSID;
        stWasteBooks.cBurseSID[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cBurseSID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        //补助流水号
        Temp = g_CardBasicInfo.iWorkSubsidySID;
        stWasteBooks.cSubsidySID[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cSubsidySID[1] = (byte) ((Temp & 0x0000FF00) >> 8);

        //终端流水号
        Temp = lngPayRecordID;
        stWasteBooks.cPaymentRecordID[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cPaymentRecordID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        stWasteBooks.cPaymentRecordID[2] = (byte) ((Temp & 0x00FF0000) >> 16);

        //交易时间
        cResult = Publicfun.GetCurrCardDate(PayDatetime);
        if (cResult != OK) {
            return DATE_TIME_ERROR;
        }
        System.arraycopy(PayDatetime, 0, stWasteBooks.bPaymentDate, 0, 4);

        //交易类型09:终端圈存；
        stWasteBooks.cPaymentType = 9;

        //交易金额
        cMoneyTemp = g_CardBasicInfo.lngSubMoney;
        stWasteBooks.cPaymentMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
        stWasteBooks.cPaymentMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);

        //优惠金额
        cMoneyTemp = 0;
        stWasteBooks.cPrivelegeMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
        stWasteBooks.cPrivelegeMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);

        //钱包卡余额
        cMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
        stWasteBooks.cBurseMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
        stWasteBooks.cBurseMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);
        stWasteBooks.cBurseMoney[2] = (byte) ((cMoneyTemp & 0x00FF0000) >> 16);

        //累计交易总金额(消费的实际金额)
        lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney;
        Log.d(TAG,"累计交易总金额:"+lngTotalPayMoney);
        stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
        stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
        stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
        stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);

        //出纳员编号
        Temp = 0;
        stWasteBooks.CashierNum[0] = (byte) (Temp & 0x00FF);
        stWasteBooks.CashierNum[1] = (byte) ((Temp & 0xFF00) >> 8);

        //重传标志
        stWasteBooks.cReSendFlag = 0;

        //终端识别号
        System.arraycopy(g_BasicInfo.cTerInCode, 0, stWasteBooks.bEquipmentID, 0, 4);

        //CRC16,计算整个数据结构的校验码
        Crc = Publicfun.CRC_Plus(stWasteBooks, PAYMENTRECORD_LEN);
        //校验码
        stWasteBooks.CRC[0] = (byte) (Crc & 0x000000ff);
        stWasteBooks.CRC[1] = (byte) ((Crc & 0x0000ff00) >> 8);
        //姓名
        System.arraycopy(g_CardBasicInfo.cAccName, 0, stWasteBooks.cAccName, 0, 16);
        //预留
        //流水存储位置
        Index = g_WasteBookInfo.WriterIndex;
        //流水存储指针余数;
        Index = (Index % MAXBOOKSCOUNT);
        //写交易流水文件
        cResult = WasteBooksRW.WriteWasteBooksData(stWasteBooks, (int) Index);
        if (cResult != 0) {
            Log.d(TAG, "写流水数据文件失败:" + cResult);
            return MEMORY_FAIL;
        }

        //写流水文件指针
        g_WasteBookInfo.UnTransferCount++;
        g_WasteBookInfo.WriterIndex++;
        g_WasteBookInfo.MaxStationSID = g_WasteBookInfo.WriterIndex;
        cResult = WasteBooksRW.WriteWasteBookInfo(g_WasteBookInfo);
        if (cResult != 0) {
            Log.d(TAG, "写流水指针文件失败:" + cResult);
            return MEMORY_FAIL;
        }
        return OK;
    }

    //记录卡片信息同步记录21:信息圈存；
    public static int WriteCardInStepRecord() {
        int cResult;
        long Index;
        int cBurseID;
        long Temp;
        long cMoneyTemp;
        long lngPayRecordID;
        long lngTotalPayMoney;
        byte[] PayDatetime = new byte[4];
        int Crc;

        WasteBooks stWasteBooks = new WasteBooks();
        cBurseID = g_StationInfo.cWorkBurseID;

        //站点号
        Temp = g_StationInfo.iStationID;
        stWasteBooks.cStationID[0] = (byte) (Temp & 0x00FF);
        stWasteBooks.cStationID[1] = (byte) ((Temp & 0xFF00) >> 8);

        //站点流水号 (设备终端流水号)
        lngPayRecordID = g_WasteBookInfo.WriterIndex + 1;
        stWasteBooks.cPayRecordID[0] = (byte) (lngPayRecordID & 0x000000FF);
        stWasteBooks.cPayRecordID[1] = (byte) ((lngPayRecordID & 0x0000FF00) >> 8);
        stWasteBooks.cPayRecordID[2] = (byte) ((lngPayRecordID & 0x00FF0000) >> 16);
        stWasteBooks.cPayRecordID[3] = (byte) ((lngPayRecordID & 0xFF000000) >> 24);

        //终端机号(以太网设备无终端机号)
        stWasteBooks.bWriteContext = 0;

        //营业时段号
        stWasteBooks.cBusinessID = g_WorkInfo.cBusinessID;

        //商户序号
        Log.d(TAG, "商户序号:" + g_LocalInfo.wShopUserID);
        Temp = g_LocalInfo.wShopUserID;
        stWasteBooks.cShopUserID[0] = (byte) (Temp & 0x00ff);
        stWasteBooks.cShopUserID[1] = (byte) ((Temp & 0xff00) >> 8);

        //卡内编号
        Temp = g_CardBasicInfo.lngCardID;
        stWasteBooks.cCardID[0] = (byte) (Temp & 0x000000ff);
        stWasteBooks.cCardID[1] = (byte) ((Temp & 0x0000ff00) >> 8);
        stWasteBooks.cCardID[2] = (byte) ((Temp & 0x00ff0000) >> 16);

        //交易钱包序号
        stWasteBooks.cBurseID = (byte) cBurseID;

        //钱包流水号
        Temp = g_CardBasicInfo.iWorkBurseSID;
        stWasteBooks.cBurseSID[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cBurseSID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        //补助流水号
        Temp = g_CardBasicInfo.iWorkSubsidySID;
        stWasteBooks.cSubsidySID[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cSubsidySID[1] = (byte) ((Temp & 0x0000FF00) >> 8);

        //终端流水号
        Temp = lngPayRecordID;
        stWasteBooks.cPaymentRecordID[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cPaymentRecordID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        stWasteBooks.cPaymentRecordID[2] = (byte) ((Temp & 0x00FF0000) >> 16);

        //交易时间
        cResult = Publicfun.GetCurrCardDate(PayDatetime);
        if (cResult != OK) {
            return DATE_TIME_ERROR;
        }
        System.arraycopy(PayDatetime, 0, stWasteBooks.bPaymentDate, 0, 4);

        //交易类型21:信息圈存；
        stWasteBooks.cPaymentType = 21;

        //交易金额(补助金额)
        cMoneyTemp = g_CardBasicInfo.lngSubMoney;
        stWasteBooks.cPaymentMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
        stWasteBooks.cPaymentMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);

        //优惠金额
        cMoneyTemp = 0;
        stWasteBooks.cPrivelegeMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
        stWasteBooks.cPrivelegeMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);

        //钱包卡余额
        cMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
        stWasteBooks.cBurseMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
        stWasteBooks.cBurseMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);
        stWasteBooks.cBurseMoney[2] = (byte) ((cMoneyTemp & 0x00FF0000) >> 16);

        //累计交易总金额(消费的实际金额)
        lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney;
        Log.d(TAG,"累计交易总金额:"+lngTotalPayMoney);
        stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
        stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
        stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
        stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);

        //出纳员编号
        Temp = 0;
        stWasteBooks.CashierNum[0] = (byte) (Temp & 0x00FF);
        stWasteBooks.CashierNum[1] = (byte) ((Temp & 0xFF00) >> 8);

        //重传标志
        stWasteBooks.cReSendFlag = 0;

        //终端识别号
        System.arraycopy(g_BasicInfo.cTerInCode, 0, stWasteBooks.bEquipmentID, 0, 4);

        //CRC16,计算整个数据结构的校验码
        Crc = Publicfun.CRC_Plus(stWasteBooks, PAYMENTRECORD_LEN);
        //校验码
        stWasteBooks.CRC[0] = (byte) (Crc & 0x000000ff);
        stWasteBooks.CRC[1] = (byte) ((Crc & 0x0000ff00) >> 8);
        //姓名
        System.arraycopy(g_CardBasicInfo.cAccName, 0, stWasteBooks.cAccName, 0, 16);
        //预留
        //流水存储位置
        Index = g_WasteBookInfo.WriterIndex;
        //流水存储指针余数;
        Index = (Index % MAXBOOKSCOUNT);
        //写交易流水文件
        cResult = WasteBooksRW.WriteWasteBooksData(stWasteBooks, (int) Index);
        if (cResult != 0) {
            Log.d(TAG, "写流水数据文件失败:" + cResult);
            return MEMORY_FAIL;
        }

        //写流水文件指针
        g_WasteBookInfo.UnTransferCount++;
        g_WasteBookInfo.WriterIndex++;
        g_WasteBookInfo.MaxStationSID = g_WasteBookInfo.WriterIndex;
        cResult = WasteBooksRW.WriteWasteBookInfo(g_WasteBookInfo);
        if (cResult != 0) {
            Log.d(TAG, "写流水指针文件失败:" + cResult);
            return MEMORY_FAIL;
        }
        return OK;
    }

}
