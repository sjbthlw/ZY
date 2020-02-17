package com.hzsun.mpos.Pos;

import android.util.Log;

import com.hzsun.mpos.CardWork.CardApp;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.data.RecordInfoRW;
import com.hzsun.mpos.data.WasteBooks;
import com.hzsun.mpos.data.WasteBooksRW;

import static com.hzsun.mpos.Activity.CardActivity.CARD_PAYOK;
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
import static com.hzsun.mpos.Public.Printer.PrintReceipt;

public class MoneyPoint {

    private static final String TAG = "MoneyPoint";

    //现金充值机结帐
    public static int MoneyPoint_CheckOut() {
        int cResult;
        long lngPaymentMoney;
        int cWorkBurseID;
        byte[] cCurDateTime = new byte[6];

        cWorkBurseID = g_StationInfo.cWorkBurseID;

        // 检查未上传流水数量
        if (g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex >= MAXBOOKSCOUNT - 20) {
            return 99;
        }
        // 时间合法性检查
        Publicfun.GetCurrDateTime(cCurDateTime);
        //设置当前系统时间日期
        SetCurDateTime(cCurDateTime);

        //判断是否是定额模式
        if (g_LocalInfo.cInputMode == 3) {
            g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
        }
        lngPaymentMoney = g_WorkInfo.lngPaymentMoney;

        //现金充值流程
        Log.d(TAG, "现金充值流程开始");
        g_CardBasicInfo.cNOWriteCardFlag = 0;
        cResult = CardApp.Burse_MoneyPointProcess(lngPaymentMoney, g_CardBasicInfo);
        //判断是否存在记录断点拔卡流水
        if (g_CardBasicInfo.cNOWriteCardFlag != 0) {
            Log.d(TAG, "记录断点拔卡流水");
            Consume.WriteAllNOCardPayRecord(g_CardBasicInfo.cNOWriteCardFlag);
        }
        if (cResult == OK) {
            Log.d(TAG, "实际充值金额：" + g_CardBasicInfo.lngPayMoney);
            Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_PAYOK);//显示

            Log.d(TAG, "记录正式交易流水");
            cResult = WriteMoneyPointRecord(0);
            if (cResult == OK) {
                Log.d(TAG, "开始日累统计1");
                cResult = TodayMoneyPointStat(0);
                if (cResult == OK) {
                    return OK;
                }
            } else {
                Log.d(TAG, "记录流水失败:" + cResult);
                if (cResult != 0) {
                    return cResult;
                }
            }
        }
        return cResult;
    }

    //现金充值机冲正
    public static int MoneyPoint_ReverCheckOut() {
        int cResult = 0;
        int cWorkBurseID;
        byte[] cCurDateTime = new byte[6];

        cWorkBurseID = g_StationInfo.cWorkBurseID;

        // 检查未上传流水数量
        if (g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex >= MAXBOOKSCOUNT - 20) {
            return 99;
        }
        // 时间合法性检查
        Publicfun.GetCurrDateTime(cCurDateTime);
        //设置当前系统时间日期
        SetCurDateTime(cCurDateTime);

        //判断需要冲正的流水
        if (g_WorkInfo.lngReWorkPayMoney != 0) {
            Log.d(TAG, "工作钱包流水冲正");
            cResult = CardApp.Burse_MoneyPointReverProcess(g_WorkInfo.lngReWorkPayMoney, g_CardBasicInfo);
            if (cResult != OK) {
                return cResult;
            } else {
                Log.d(TAG, "实际交易金额：" + (g_WorkInfo.lngReWorkPayMoney));
                if (g_WorkInfo.cTestState == 0) {
                    //打印卡片基础信息
                    //Publicfun.PrintfAllCardInfo();
                }
                Log.d(TAG, "记录正式交易流水");
                cResult = WriteMoneyPointRecord(1);
                if (cResult != OK) {
                    Log.d(TAG, "记录流水失败:" + cResult);
                    if (cResult == MEMORY_FAIL) {
                        //ShowErrorInfoA(MEMORY_FAIL);
                    }
                }
            }
        }
        //冲正0元消费流水
        if (g_WorkInfo.lngReWorkPayMoney == 0) {
            Log.d(TAG, "工作钱包冲正0元消费流水");
            cResult = CardApp.Burse_WorkConsumeReverProcess(g_WorkInfo.lngReWorkPayMoney, 0, g_CardBasicInfo);
            if (cResult != OK) {
                return cResult;
            } else {
                Log.d(TAG, "实际交易金额：" + g_CardBasicInfo.lngPayMoney);
                if (g_WorkInfo.cTestState == 0) {
                    //打印卡片基础信息
                    //Publicfun.PrintfAllCardInfo();
                }

                Log.d(TAG, "记录正式交易流水");
                cResult = WriteMoneyPointRecord(1);
                if (cResult != OK) {
                    Log.d(TAG, "记录流水失败:" + cResult);
                    if (cResult == MEMORY_FAIL) {
                        //ShowErrorInfoA(MEMORY_FAIL);
                    }
                }
            }
        }
        if (cResult == OK) {
            Log.d(TAG, "开始日累统计1");
            cResult = TodayMoneyPointStat(1);
            if (cResult == OK) {
                Log.d(TAG, "开始日累统计1成功");
            }
            g_CardBasicInfo.lngPayMoney = g_WorkInfo.lngReWorkPayMoney;
            Log.d(TAG, "充值冲正交易总金额：" + g_CardBasicInfo.lngPayMoney);
//            Log.d(TAG,"发射卡片交易成功信息");
//            Publicfun.ShowCardInfo_Dispel(g_CardBasicInfo,3);//显示
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
    //记录工作钱包正式交易流水 00:商务消费;30：以角为单位 60：元
    //Type  0充值  1充值冲正
    public static int WriteMoneyPointRecord(int Type) {
        int cResult;
        long Index;
        long FilePos, FileLen;
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
        stWasteBooks.cBurseSID[0] = (byte) (Temp & 0x00FF);
        stWasteBooks.cBurseSID[1] = (byte) ((Temp & 0xFF00) >> 8);
        //补助流水号
        Temp = g_CardBasicInfo.iWorkSubsidySID;
        stWasteBooks.cSubsidySID[0] = (byte) (Temp & 0x00FF);
        stWasteBooks.cSubsidySID[1] = (byte) ((Temp & 0xFF00) >> 8);

        //终端流水号
        Temp = lngPayRecordID;
        stWasteBooks.cPaymentRecordID[0] = (byte) (Temp & 0x0000FF);
        stWasteBooks.cPaymentRecordID[1] = (byte) ((Temp & 0x00FF00) >> 8);
        stWasteBooks.cPaymentRecordID[2] = (byte) ((Temp & 0xFF0000) >> 16);

        //交易时间
        cResult = Publicfun.GetCurrCardDate(PayDatetime);
        if (cResult != OK) {
            return DATE_TIME_ERROR;
        }
        System.arraycopy(PayDatetime, 0, stWasteBooks.bPaymentDate, 0, 4);

        //交易类型
        if (Type == 0)        //05:终端存款；35 角  65元
        {
            //05:终端存款；35 角  65元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                stWasteBooks.cPaymentType = 5;//交易类型
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                stWasteBooks.cPaymentType = 35;//交易类型
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                stWasteBooks.cPaymentType = 65;//交易类型
            }

            //充值交易金额
            cMoneyTemp = g_CardBasicInfo.lngPayMoney;
            Log.d(TAG, "充值交易金额:" + cMoneyTemp);
            if (g_StationInfo.cPaymentUnit == 1) {
                cMoneyTemp = cMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                cMoneyTemp = cMoneyTemp / 100;
            }
            stWasteBooks.cPaymentMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额 //充值机优惠金额字段 定义为手续费 2017.830
            //cMoneyTemp=g_CardBasicInfo.lngPriMoney;
            cMoneyTemp = g_CardBasicInfo.lngManageMoney;
            Log.d(TAG, "充值手续费金额:" + cMoneyTemp);
            if (g_StationInfo.cPaymentUnit == 1) {
                cMoneyTemp = cMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                cMoneyTemp = cMoneyTemp / 100;
            }
            stWasteBooks.cPrivelegeMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额
            cMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
            stWasteBooks.cBurseMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((cMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(消费的实际金额)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney + (g_CardBasicInfo.lngPayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        } else if (Type == 1)    //06:存款冲正；36 角  66元
        {
            //06:存款冲正；36 角  66元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                //交易类型
                stWasteBooks.cPaymentType = 6;
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                //交易类型
                stWasteBooks.cPaymentType = 36;
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                //交易类型
                stWasteBooks.cPaymentType = 66;
            }

            //冲正交易金额
            cMoneyTemp = g_CardBasicInfo.lngPayMoney;
            if (g_StationInfo.cPaymentUnit == 1) {
                cMoneyTemp = cMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                cMoneyTemp = cMoneyTemp / 100;
            }
            stWasteBooks.cPaymentMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额//2017.830优惠金额字段修改为手续费
            cMoneyTemp = g_CardBasicInfo.lngManageMoney;
            stWasteBooks.cPrivelegeMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额
            cMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
            stWasteBooks.cBurseMoney[0] = (byte) (cMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((cMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((cMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(消费的实际金额)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney - (g_CardBasicInfo.lngPayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        }
        //出纳员编号
        System.arraycopy(g_WorkInfo.cCasherID, 0, stWasteBooks.CashierNum, 0, stWasteBooks.CashierNum.length);

        //重传标志
        stWasteBooks.cReSendFlag = 0;

        //终端识别号
        System.arraycopy(g_BasicInfo.cTerInCode, 0, stWasteBooks.bEquipmentID, 0, g_BasicInfo.cTerInCode.length);

        //CRC16,计算整个数据结构的校验码
        Crc = Publicfun.CRC_Plus(stWasteBooks, PAYMENTRECORD_LEN);
        //校验码
        stWasteBooks.CRC[0] = (byte) (Crc & 0x000000ff);
        stWasteBooks.CRC[1] = (byte) ((Crc & 0x0000ff00) >> 8);
        //姓名
        System.arraycopy(g_CardBasicInfo.cAccName, 0, stWasteBooks.cAccName, 0, g_CardBasicInfo.cAccName.length);
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
        //打印小票
        PrintReceipt(stWasteBooks);
        return OK;
    }

    //日累统计、餐累统计、写末笔营业时段号(TYPE:0 充值 1 冲正）
    public static int TodayMoneyPointStat(int Type) {
        int cResult;
        long lngPaymentMoney = 0;
        int wTodayPaymentSum = 0;             //当日交易笔数
        long lngTodayPaymentMoney = 0;        //当日交易总额
        int wTotalBusinessSum = 0;                //当餐营业笔数
        long lngTotalBusinessMoney = 0;          //当餐营业总额
        long lngTotalPaymentMoney = 0;          //累计交易总金额
        byte[] cLastPaymentDate = new byte[6];          //末笔交易日期
        int cLastBusinessID;              //末笔交易营业号

        //日累和餐累不含管理费
        if (Type == 0) {
            lngPaymentMoney = g_CardBasicInfo.lngPayMoney;
        }
        if (Type == 1) {
            lngPaymentMoney = g_WorkInfo.lngReWorkPayMoney;
        }

        Log.d(TAG, "日累统计 lngPaymentMoney:" + lngPaymentMoney);

        //判断是否是同一天
        cResult = Publicfun.CompareStatLastDate(g_RecordInfo.cLastPaymentDate);
        if (cResult == 0) {
            //日累统计
            Log.d(TAG, "日累统计同一天");
            if (Type == 0) {
                lngTodayPaymentMoney = g_RecordInfo.lngTodayPaymentMoney + lngPaymentMoney;
                wTodayPaymentSum = g_RecordInfo.wTodayPaymentSum + 1;
            }
            if (Type == 1) {
                lngTodayPaymentMoney = g_RecordInfo.lngTodayPaymentMoney - lngPaymentMoney;
                wTodayPaymentSum = g_RecordInfo.wTodayPaymentSum + 1;
            }

            //餐累统计
            if (g_WorkInfo.cBusinessID == g_RecordInfo.cLastBusinessID) {
                if (Type == 0) {
                    lngTotalBusinessMoney = g_RecordInfo.lngTotalBusinessMoney + lngPaymentMoney;
                    wTotalBusinessSum = g_RecordInfo.wTotalBusinessSum + 1;
                }
                if (Type == 1) {
                    lngTotalBusinessMoney = g_RecordInfo.lngTotalBusinessMoney - lngPaymentMoney;
                    wTotalBusinessSum = g_RecordInfo.wTotalBusinessSum + 1;
                }
            } else {
                wTotalBusinessSum = 1;
                lngTotalBusinessMoney = g_CardBasicInfo.lngPayMoney;
            }
        } else {
            Log.d(TAG, "日累统计不是同一天");
            wTodayPaymentSum = 1;
            lngTodayPaymentMoney = lngPaymentMoney;
            wTotalBusinessSum = 1;
            lngTotalBusinessMoney = lngPaymentMoney;
        }

        //累计交易总金额
        if (Type == 0) {
            lngTotalPaymentMoney = g_RecordInfo.lngTotalPaymentMoney + g_CardBasicInfo.lngPayMoney;
        }
        if (Type == 1) {
            lngTotalPaymentMoney = g_RecordInfo.lngTotalPaymentMoney - g_CardBasicInfo.lngPayMoney;
        }
        //末笔交易日期
        Publicfun.GetCurrDateTime(cLastPaymentDate);
        //记录末笔营业号
        cLastBusinessID = g_WorkInfo.cBusinessID;

        //当日交易笔数
        g_RecordInfo.wTodayPaymentSum = wTodayPaymentSum;
        //当日交易总额
        g_RecordInfo.lngTodayPaymentMoney = lngTodayPaymentMoney;
        //当餐营业笔数
        g_RecordInfo.wTotalBusinessSum = wTotalBusinessSum;
        //当餐营业总额
        g_RecordInfo.lngTotalBusinessMoney = lngTotalBusinessMoney;
        //累计交易总金额
        g_RecordInfo.lngTotalPaymentMoney = lngTotalPaymentMoney;
        //末笔交易日期
        System.arraycopy(cLastPaymentDate, 0, g_RecordInfo.cLastPaymentDate, 0, 6);
        //末笔交易营业号
        g_RecordInfo.cLastBusinessID = (byte) cLastBusinessID;

        //记录交易完成后日累，餐累，末笔营业，累计总金额参数
        cResult = RecordInfoRW.WriteAllRecordInfo(g_RecordInfo);
        if (cResult != OK) {
            return MEMORY_FAIL;
        }
        return OK;
    }


}
