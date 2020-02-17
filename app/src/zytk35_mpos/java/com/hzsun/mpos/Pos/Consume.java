package com.hzsun.mpos.Pos;

import android.util.Log;

import com.hzsun.mpos.CardWork.CardApp;
import com.hzsun.mpos.CardWork.CardBasicParaInfo;
import com.hzsun.mpos.FaceApp.FaceWorkTask;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.data.RecordInfoRW;
import com.hzsun.mpos.data.WasteBooks;
import com.hzsun.mpos.data.WasteBooksRW;
import com.hzsun.mpos.data.WasteFacePayBooks;
import com.hzsun.mpos.data.WasteFacePayBooksRW;
import com.hzsun.mpos.data.WasteQrCodeBooks;
import com.hzsun.mpos.data.WasteQrCodeBooksRW;

import static com.hzsun.mpos.Activity.CardActivity.CARD_PAYALLOK;
import static com.hzsun.mpos.Activity.CardActivity.CARD_PAYOK;
import static com.hzsun.mpos.CardWork.CardApp.PCheckPassword;
import static com.hzsun.mpos.CardWork.CardApp.SetCurDateTime;
import static com.hzsun.mpos.CardWork.CardPublic.PwdLongtoByte;
import static com.hzsun.mpos.Global.Global.DATE_TIME_ERROR;
import static com.hzsun.mpos.Global.Global.MAXBOOKSCOUNT;
import static com.hzsun.mpos.Global.Global.MEMORY_FAIL;
import static com.hzsun.mpos.Global.Global.NETCOM_ERR;
import static com.hzsun.mpos.Global.Global.NOPASSWORD;
import static com.hzsun.mpos.Global.Global.OK;
import static com.hzsun.mpos.Global.Global.PAYMENTRECORD_LEN;
import static com.hzsun.mpos.Global.Global.PSW_ERROR;
import static com.hzsun.mpos.Global.Global.QRPAYMENTRECORD_LEN;
import static com.hzsun.mpos.Global.Global.g_ThirdCodeResultInfo;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardHQRCodeInfo;
import static com.hzsun.mpos.Global.Global.g_CommInfo;
import static com.hzsun.mpos.Global.Global.g_FacePayInfo;
import static com.hzsun.mpos.Global.Global.g_LastRecordPayInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_OnlinePayInfo;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_StatusInfoArray;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;
import static com.hzsun.mpos.Global.Global.g_WasteFaceBookInfo;
import static com.hzsun.mpos.Global.Global.g_WasteQrBookInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Printer.PrintReceipt;
import static com.hzsun.mpos.Public.Printer.PrintReceiptQR;
import static com.hzsun.mpos.Public.Publicfun.ShowCardPaying;
import static com.hzsun.mpos.Public.Utility.memcpy;
import static java.util.Arrays.fill;


public class Consume {

    private static final String TAG = "Consume";

    //普通消费机器
    public static int Consume_CheckOut() {
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

        g_WorkInfo.ChasePaymentState = 0;
        //判断是否是定额模式
        if (g_WorkInfo.cTestState == 1) {
            g_WorkInfo.lngPaymentMoney = 1;
        } else {
            if (g_LocalInfo.cInputMode == 3) {
                g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
            }
        }
        lngPaymentMoney = g_WorkInfo.lngPaymentMoney;

        Log.d(TAG, "消费扣款流程开始");
        g_CardBasicInfo.cNOWriteCardFlag = 0;
        cResult = CardApp.Burse_WorkConsumeProcess(lngPaymentMoney, g_CardBasicInfo);
        //判断是否存在记录断点拔卡流水
        if (g_CardBasicInfo.cNOWriteCardFlag != 0) {
            Log.d(TAG, "记录断点拔卡流水");
            WriteAllNOCardPayRecord(g_CardBasicInfo.cNOWriteCardFlag);
        }
        if (cResult == OK) {
            Log.d(TAG, "实际交易金额" + g_CardBasicInfo.lngPayMoney);
            g_LastRecordPayInfo.cState = 0;
            Log.d(TAG, "发射卡片交易成功信息");
            Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_PAYOK);//显示

            Log.d(TAG, "记录正式交易流水");
            cResult = WriteWorkRecord((byte) 0);
            if (cResult == OK) {
                //记录管理费流水
                if (g_CardBasicInfo.lngManageMoney != 0) {
                    Log.d(TAG, "记录管理费流水");//0x02-收消费管理费、
                    cResult = WriteManageRecord((byte) 0);
                }
                if (cResult == OK) {
                    Log.d(TAG, "开始日累统计1");
                    cResult = TodayConsumStat(g_CardBasicInfo, (byte) 0);
                    if (cResult == OK) {
                        return OK;
                    }
                } else {
                    Log.d(TAG, "记录管理费流水失败:" + cResult);
                    if (cResult != 0) {
                        return cResult;
                    }
                }
                return OK;
            } else {
                Log.d(TAG, "记录流水失败:4" + cResult);
                if (cResult != 0) {
                    return cResult;
                }
            }
        } else if (cResult == 1) {
            //存在追扣钱包,商户允许追扣(追扣不允许有管理费)
            Log.d(TAG, "允许追扣消费");
            cResult = OK;

            if (g_CardBasicInfo.lngWorkPayMoney != 0) {
                Log.d(TAG, "工作钱包扣款流程");
                //追扣钱包消费扣款流程(1:工作钱包扣款0元)
                cResult = CardApp.Burse_WorkConsumeProcess_Chase(g_CardBasicInfo.lngPayMoney, g_CardBasicInfo.lngWorkPayMoney, g_CardBasicInfo);
                //判断是否存在记录断点拔卡流水
                if (g_CardBasicInfo.cNOWriteCardFlag != 0) {
                    Log.d(TAG, "记录断点拔卡流水");
                    WriteAllNOCardPayRecord(g_CardBasicInfo.cNOWriteCardFlag);
                }
                if (cResult == OK) {
                    Log.d(TAG, "记录工作钱包交易流水");
                    cResult = WriteWorkRecord_Chase(0);
                }
            }
            if (cResult == OK) {
                Log.d(TAG, "追扣钱包扣款流程");
                //追扣钱包消费扣款流程
                cResult = CardApp.Burse_ChaseConsumeProcess(g_CardBasicInfo.lngChasePayMoney, g_CardBasicInfo);
                if (g_CardBasicInfo.cNOWriteCardFlag != 0) {
                    Log.d(TAG, "记录断点拔卡流水");
                    WriteAllNOCardPayRecord(g_CardBasicInfo.cNOWriteCardFlag);
                }
                if (cResult == OK) {
                    Log.d(TAG, "实际交易金额：" + g_CardBasicInfo.lngChasePayMoney);
                    g_LastRecordPayInfo.cState = 0;
                    //发射卡片交易成功信息
                    Log.d(TAG, "发射卡片交易成功信息");
                    Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_PAYALLOK);//显示

                    Log.d(TAG, "记录追扣钱包交易流水");
                    cResult = WriteChaseRecord((byte) 0);
                    if (cResult == OK) {
                        Log.d(TAG, "开始日累统计1");
                        cResult = TodayConsumStat(g_CardBasicInfo, (byte) 0);
                        if (cResult == OK) {
                            return OK;
                        }
                    } else {
                        Log.d(TAG, "记录流水失败:" + cResult);
                        return cResult;
                    }
                    return OK;
                }
            } else {
                Log.d(TAG, "记录流水失败:" + cResult);
                return cResult;
            }
        }
        return cResult;
    }

    //普通消费机器的冲正
    public static int Consume_WorkReverCheckOut() {
        int cResult = 0;
        byte cWorkBurseID;
        byte cChaseBurseID;
        byte[] cCurDateTime = new byte[6];

        cWorkBurseID = g_StationInfo.cWorkBurseID;
        cChaseBurseID = g_StationInfo.cChaseBurseID;

        // 检查未上传流水数量
        if (g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex >= MAXBOOKSCOUNT - 20) {
            return 99;
        }
        // 时间合法性检查
        Publicfun.GetCurrDateTime(cCurDateTime);
        //设置当前系统时间日期
        SetCurDateTime(cCurDateTime);
        //判断需要冲正的流水
        if (g_WorkInfo.lngReChasePayMoney != 0) {
            Log.d(TAG, "追扣钱包流水冲正");
            cResult = CardApp.Burse_ChaseConsumeReverProcess(g_WorkInfo.lngReChasePayMoney, g_CardBasicInfo);
            if (cResult == OK) {
                Log.d(TAG, "追扣实际交易金额：" + g_WorkInfo.lngReChasePayMoney);
                Log.d(TAG, "记录正式交易流水");
                cResult = WriteChaseRecord((byte) 1);
                if (cResult == OK) {
                    Log.d(TAG, "追扣钱包冲正结束");
                } else {
                    Log.d(TAG, "记录流水失败:" + cResult);
                    if (cResult == MEMORY_FAIL) {
                        return cResult;
                    }
                }
            } else {
                return cResult;
            }
        }
        if ((g_WorkInfo.lngReWorkPayMoney != 0) || (g_WorkInfo.lngReManageMoney != 0)) {
            Log.d(TAG, "工作钱包流水或者管理费流水冲正");
            cResult = CardApp.Burse_WorkConsumeReverProcess(g_WorkInfo.lngReWorkPayMoney, g_WorkInfo.lngReManageMoney, g_CardBasicInfo);
            if (cResult != OK) {
                return cResult;
            } else {
                Log.d(TAG, "记录正式冲正交易流水");
                Log.d(TAG, String.format("实际交易金额：%d", (g_WorkInfo.lngReWorkPayMoney + g_WorkInfo.lngReManageMoney)));
                cResult = WriteWorkRecord((byte) 1);
                if (cResult == OK) {
                    //记录管理费流水
                    if (g_CardBasicInfo.lngManageMoney != 0) {
                        Log.d(TAG, "记录管理费流水");//0x02-收消费管理费
                        cResult = WriteManageRecord((byte) 1);
                    }
                    if (cResult != OK) {
                        Log.d(TAG, "记录管理费流水失败:%" + cResult);
                        if (cResult == MEMORY_FAIL) {
                            //ShowErrorInfoA(MEMORY_FAIL);
                        }
                    }
                } else {
                    Log.d(TAG, "记录流水失败:" + cResult);
                    if (cResult == MEMORY_FAIL) {
                        //ShowErrorInfoA(MEMORY_FAIL);
                    }
                }
            }
        }
        if (cResult == OK) {
            Log.d(TAG, "开始日累统计1");
            cResult = TodayConsumStat(g_CardBasicInfo, (byte) 1);
            if (cResult == OK) {
                Log.d(TAG, "开始日累统计1成功");
            }
            g_CardBasicInfo.lngPayMoney = g_WorkInfo.lngReManageMoney + g_WorkInfo.lngReWorkPayMoney + g_WorkInfo.lngReChasePayMoney;
            Log.d(TAG, "冲正交易总金额：" + g_CardBasicInfo.lngPayMoney);
//            Log.d(TAG,"发射卡片交易成功信息");
//            Publicfun.ShowCardInfo_Dispel(g_CardBasicInfo,3);//显示
        }
        return cResult;
    }

    //在线卡交易
    public static int Consume_OnlingCheckOut() {
        int cResult;
        long lngSinglePayPswLim;

        if ((g_WorkInfo.cQrCodestatus != 2) || (g_WorkInfo.cRunState == 0)) {
            return NETCOM_ERR;
        }
        Log.d(TAG, "在线卡交易");
        if (g_StationInfo.cCanStatusLimitFee == 0) {
            lngSinglePayPswLim = g_StatusInfoArray.get(g_CardBasicInfo.cStatusID - 1).StatusInfolist.get(g_StationInfo.cWorkBurseID - 1).lngSinglePayPswLim;
            Log.d(TAG, String.format("g_StationInfo.cCanStatusLimitFee:%d--lngSinglePayPswLim:%d",
                    g_StationInfo.cCanStatusLimitFee, lngSinglePayPswLim));
            if (g_WorkInfo.lngPaymentMoney > lngSinglePayPswLim) {
                byte[] cTradePWD= PwdLongtoByte(g_CardBasicInfo.lngPaymentPsw);
                Log.d(TAG, "交易密码:" + cTradePWD);
                //提示输入交易密码
                Log.d(TAG, "单笔消费密码限额");
                cResult = PCheckPassword(2, cTradePWD);
                if (cResult != 0) {
                    if (cResult == 2) {
                        Log.d(TAG, "单笔消费密码限额(密码无输入)");
                        return NOPASSWORD;
                    } else {
                        Log.d(TAG, "单笔消费密码限额(密码错误)");
                        return PSW_ERROR;
                    }
                }
            }
        }
        //在线交易
        if (g_WorkInfo.cQrCodestatus == 2)//获取过订单号了
        {
            ShowCardPaying();
            g_CardHQRCodeInfo.lngAccountID = g_CardBasicInfo.lngAccountID;
            g_CardHQRCodeInfo.lngOrderNum = g_WorkInfo.lngOrderNum;
            g_WorkInfo.cOtherQRFlag = 3;  //在线卡交易
            g_WorkInfo.cScanQRCodeFlag = 0;
            g_CommInfo.cGetQRCodeInfoStatus = 1;
            g_CommInfo.lngSendComStatus |= 0x00000800;

            g_Nlib.QR_SetDeviceReadEnable(2);//结束识读
            if (g_SystemInfo.cFaceDetectFlag == 1)//是否启用人脸
                FaceWorkTask.StartDetecte(false);
            return 0;
        } else {
            Log.d(TAG, "获取过订单号失败");
            return NETCOM_ERR;
        }
    }

    //在线代扣交易
    public static int Consume_WithholdCheckOut() {
        int cResult;
        long lngSinglePayPswLim;

        if ((g_WorkInfo.cRunState == 0)||(g_CommInfo.cWithholdInfoStatus != 0)) {
            return NETCOM_ERR;
        }
        Log.d(TAG, "在线代扣交易");
        if (g_StationInfo.cCanStatusLimitFee == 0) {
            lngSinglePayPswLim = g_StatusInfoArray.get(g_CardBasicInfo.cStatusID - 1).StatusInfolist.get(g_StationInfo.cWorkBurseID - 1).lngSinglePayPswLim;
            Log.d(TAG, String.format("g_StationInfo.cCanStatusLimitFee:%d--lngSinglePayPswLim:%d",
                    g_StationInfo.cCanStatusLimitFee, lngSinglePayPswLim));
            if (g_WorkInfo.lngPaymentMoney > lngSinglePayPswLim) {
                byte[] cTradePWD= PwdLongtoByte(g_CardBasicInfo.lngPaymentPsw);
                Log.d(TAG, "交易密码:" + cTradePWD);
                Log.d(TAG, "单笔消费密码限额");
                cResult = PCheckPassword(2, cTradePWD);
                if (cResult != 0) {
                    if (cResult == 2) {
                        Log.d(TAG, "单笔消费密码限额(密码无输入)");
                        return NOPASSWORD;
                    } else {
                        Log.d(TAG, "单笔消费密码限额(密码错误)");
                        return PSW_ERROR;
                    }
                }
            }
        }
        ShowCardPaying();
        g_CardHQRCodeInfo.lngAccountID = g_CardBasicInfo.lngAccountID;
        g_WorkInfo.cOtherQRFlag = 4;  //在线代扣交易
        g_WorkInfo.cScanQRCodeFlag = 0;
        g_CommInfo.cWithholdInfoStatus = 1;
        g_CommInfo.lngSendComStatus |= 0x00010000;

        g_Nlib.QR_SetDeviceReadEnable(2);//结束识读
        if (g_SystemInfo.cFaceDetectFlag == 1)//是否启用人脸
            FaceWorkTask.StartDetecte(false);
        return 0;
    }

    //Type  0消费  1消费冲正
    public static int WriteWorkRecord(byte Type) {
        int cResult;
        long Index;
        byte cBurseID;
        long Temp;
        long lngMoneyTemp;
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
        stWasteBooks.cBurseID = cBurseID;

        //钱包流水号
        if (g_CardBasicInfo.lngManageMoney != 0)//存在管理费
        {
            Temp = g_CardBasicInfo.iWorkBurseSID - 1;
        } else {
            Temp = g_CardBasicInfo.iWorkBurseSID;
        }
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
        if (Type == 0)        //消费 0x00
        {
            Log.d(TAG, "交易类型(消费)" );
            //00:商务消费 30角 60元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                stWasteBooks.cPaymentType = 0;
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                stWasteBooks.cPaymentType = 30;
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                stWasteBooks.cPaymentType = 60;
            }

            //交易金额(不含管理费)
            lngMoneyTemp = g_CardBasicInfo.lngPayMoney - g_CardBasicInfo.lngManageMoney;
            Log.d(TAG, "交易金额(不含管理费):" + lngMoneyTemp);
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额
            lngMoneyTemp = g_CardBasicInfo.lngPriMoney;
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额(加上管理费)
            lngMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney + g_CardBasicInfo.lngManageMoney;
            Log.d(TAG,"钱包卡余额:"+lngMoneyTemp);
            stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(消费的实际金额)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney + (g_CardBasicInfo.lngPayMoney - g_CardBasicInfo.lngManageMoney);
            Log.d(TAG,"累计交易总金额:"+lngTotalPayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        } else if (Type == 1)    //冲正 0x03
        {
            Log.d(TAG, "交易类型(冲正)" );
            //03:交易冲正 33角 63元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                stWasteBooks.cPaymentType = 3;
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                stWasteBooks.cPaymentType = 33;
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                stWasteBooks.cPaymentType = 63;
            }
            //交易金额(不含管理费)
            lngMoneyTemp = g_CardBasicInfo.lngPayMoney - g_CardBasicInfo.lngManageMoney;
            Log.d(TAG, "交易金额(不含管理费):" + lngMoneyTemp);
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额
            lngMoneyTemp = 0;
            stWasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额
            lngMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney - g_CardBasicInfo.lngManageMoney;
            Log.d(TAG, "钱包卡余额:" + lngMoneyTemp);
            stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(消费的实际金额)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney - (g_CardBasicInfo.lngPayMoney - g_CardBasicInfo.lngManageMoney);
            Log.d(TAG,"累计交易总金额:"+lngTotalPayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        }

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
        //打印小票
        PrintReceipt(stWasteBooks);
        return OK;
    }

    //Type  0消费  1消费冲正
    public static int WriteWorkRecord_Chase(int Type) {
        int cResult;
        long Index;
        byte cBurseID;
        long Temp;
        long lngMoneyTemp;
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
        stWasteBooks.cBurseID = cBurseID;

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
        if (Type == 0)        //消费 0x00
        {
            //00:商务消费 30角 60元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                //交易类型
                stWasteBooks.cPaymentType = 0;
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                //交易类型
                stWasteBooks.cPaymentType = 30;
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                //交易类型
                stWasteBooks.cPaymentType = 60;
            }

            //交易金额(不含管理费)
            lngMoneyTemp = g_CardBasicInfo.lngWorkPayMoney;
            Log.d(TAG, "交易金额(不含管理费):" + lngMoneyTemp);
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额
            lngMoneyTemp = g_CardBasicInfo.lngPriMoney;
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额
            lngMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
            stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(消费的实际金额)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney + (g_CardBasicInfo.lngPayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        } else if (Type == 1)    //冲正 0x03
        {
            //03:交易冲正 33角 63元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                //交易类型
                stWasteBooks.cPaymentType = 3;
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                //交易类型
                stWasteBooks.cPaymentType = 33;
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                //交易类型
                stWasteBooks.cPaymentType = 63;
            }

            //交易金额(不含管理费)
            lngMoneyTemp = g_CardBasicInfo.lngWorkPayMoney;
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额
            lngMoneyTemp = 0;
            stWasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额
            lngMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
            stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(消费的实际金额)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney - (g_CardBasicInfo.lngPayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        }

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

        //写交易流水文件
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

    //记录追扣钱包正式交易流水 08:追扣消费；38角 68元 0:追扣消费 1:追扣冲正
    public static int WriteChaseRecord(byte Type) {
        int cResult;
        long Index;
        byte cBurseID;
        long Temp;
        long lngMoneyTemp;
        long lngPayRecordID;
        long lngTotalPayMoney;
        byte[] PayDatetime = new byte[4];
        int Crc;

        WasteBooks stWasteBooks = new WasteBooks();
        cBurseID = g_StationInfo.cChaseBurseID;//追扣钱包

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
        stWasteBooks.cBurseID = cBurseID;

        //钱包流水号
        Temp = g_CardBasicInfo.iChaseBurseSID;
        stWasteBooks.cBurseSID[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cBurseSID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        //补助流水号
        Temp = g_CardBasicInfo.iChaseSubsidySID;
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

        //交易类型
        if (Type == 0)        //消费 0x00
        {
            //08:追扣消费；38角 68元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                stWasteBooks.cPaymentType = 8;
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                stWasteBooks.cPaymentType = 38;
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                stWasteBooks.cPaymentType = 68;
            }

            //交易金额(不含管理费)
            lngMoneyTemp = g_CardBasicInfo.lngChasePayMoney;
            Log.d(TAG, "交易金额(不含管理费):" + lngMoneyTemp);
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额
            lngMoneyTemp = g_CardBasicInfo.lngPriMoney;
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额
            lngMoneyTemp = g_CardBasicInfo.lngChaseBurseMoney;
            Log.d(TAG, "钱包卡余额:" + lngMoneyTemp);
            stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(消费的实际金额)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney + (g_CardBasicInfo.lngChasePayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        } else if (Type == 1)    //冲正 0x03
        {
            //03:交易冲正；33 角  63元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                stWasteBooks.cPaymentType = 3;
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                stWasteBooks.cPaymentType = 33;
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                stWasteBooks.cPaymentType = 63;
            }

            //交易金额(不含管理费)
            lngMoneyTemp = g_CardBasicInfo.lngChasePayMoney;
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额
            lngMoneyTemp = 0;
            stWasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额
            lngMoneyTemp = g_CardBasicInfo.lngChaseBurseMoney;
            stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(消费的实际金额)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney - (g_CardBasicInfo.lngChasePayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        }

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
        //打印小票
        PrintReceipt(stWasteBooks);
        return OK;
    }

    //23:消费管理费 53角 83元
    //24:消费管理费冲正； 54角 84元
    //记录管理费正式流水(追扣时不允许有管理费)
    public static int WriteManageRecord(byte Type) {
        int cResult;
        long Index;
        byte cBurseID;
        long Temp;
        long lngMoneyTemp;
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
        Temp = g_LocalInfo.wShopUserID;
        stWasteBooks.cShopUserID[0] = (byte) (Temp & 0x00ff);
        stWasteBooks.cShopUserID[1] = (byte) ((Temp & 0xff00) >> 8);

        //卡内编号
        Temp = g_CardBasicInfo.lngCardID;
        stWasteBooks.cCardID[0] = (byte) (Temp & 0x000000ff);
        stWasteBooks.cCardID[1] = (byte) ((Temp & 0x0000ff00) >> 8);
        stWasteBooks.cCardID[2] = (byte) ((Temp & 0x00ff0000) >> 16);

        //交易钱包序号
        stWasteBooks.cBurseID = cBurseID;

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

        //交易类型
        if (Type == 0)        //消费
        {
            Log.d(TAG,"交易类型(消费)");
            //23:消费管理费 53角 83元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                stWasteBooks.cPaymentType = 23;
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                stWasteBooks.cPaymentType = 53;
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                stWasteBooks.cPaymentType = 83;
            }

            //交易金额(管理费)
            lngMoneyTemp = g_CardBasicInfo.lngManageMoney;
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            Log.d(TAG,"交易金额(管理费):"+lngMoneyTemp);
            stWasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额
            lngMoneyTemp = 0;
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            stWasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额
            lngMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
            Log.d(TAG,"钱包卡余额:"+lngMoneyTemp);
            stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(管理费计入扎帐金额2019.1205)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney+ g_CardBasicInfo.lngPayMoney;
            Log.d(TAG,"累计交易总金额:"+lngTotalPayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        } else if (Type == 1)    //冲正
        {
            Log.d(TAG,"交易类型(冲正)");
            //24:消费管理费冲正； 54角 84元
            if (g_StationInfo.cPaymentUnit == 0)//分
            {
                stWasteBooks.cPaymentType = 24;
            } else if (g_StationInfo.cPaymentUnit == 1)//角
            {
                stWasteBooks.cPaymentType = 54;
            } else if (g_StationInfo.cPaymentUnit == 2)//元
            {
                stWasteBooks.cPaymentType = 84;
            }

            //交易金额
            lngMoneyTemp = g_CardBasicInfo.lngManageMoney;
            if (g_StationInfo.cPaymentUnit == 1) {
                lngMoneyTemp = lngMoneyTemp / 10;
            } else if (g_StationInfo.cPaymentUnit == 2) {
                lngMoneyTemp = lngMoneyTemp / 100;
            }
            Log.d(TAG,"交易金额(管理费):"+lngMoneyTemp);
            stWasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //优惠金额
            lngMoneyTemp = 0;
            stWasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);

            //钱包卡余额
            lngMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
            Log.d(TAG,"钱包卡余额:"+lngMoneyTemp);
            stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
            stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
            stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

            //累计交易总金额(管理费计入扎帐金额2019.1205)
            lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney- g_CardBasicInfo.lngPayMoney;
            Log.d(TAG,"累计交易总金额:"+lngTotalPayMoney);
            stWasteBooks.cTotalPaymentMoney[0] = (byte) (lngTotalPayMoney & 0x000000FF);
            stWasteBooks.cTotalPaymentMoney[1] = (byte) ((lngTotalPayMoney & 0x0000FF00) >> 8);
            stWasteBooks.cTotalPaymentMoney[2] = (byte) ((lngTotalPayMoney & 0x00FF0000) >> 16);
            stWasteBooks.cTotalPaymentMoney[3] = (byte) ((lngTotalPayMoney & 0xFF000000) >> 24);
        }

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
        //写交易流水文件
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

    //记录工作钱包断点流水 11:断点拔卡；12:断点恢复；
    public static int WriteWorkCardPayRecord(byte cType, int iBurseSID, long lngBurseMoney) {
        int cResult;
        long Index;
        byte cBurseID;
        long Temp;
        long lngMoneyTemp;
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
        stWasteBooks.cBurseID = cBurseID;

        //钱包流水号
        Temp = iBurseSID;
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

        //交易类型 11:断点拔卡；12:断点恢复；
        stWasteBooks.cPaymentType = cType;

        //金额
        stWasteBooks.cPaymentMoney[0] = (byte) (lngBurseMoney & 0x000000FF);
        stWasteBooks.cPaymentMoney[1] = (byte) ((lngBurseMoney & 0x0000FF00) >> 8);

        //优惠金额
        Temp = 0;
        stWasteBooks.cPrivelegeMoney[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cPrivelegeMoney[1] = (byte) ((Temp & 0x0000FF00) >> 8);

        //钱包卡余额
        lngMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
        stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

        //累计交易总金额(消费的实际金额)
        lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney;
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
        //写交易流水文件
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

    //记录追扣断点流水 11:断点拔卡；12:断点恢复；
    public static int WriteChaseCardPayRecord(byte cType, int iBurseSID, long lngBurseMoney) {
        int cResult;
        long Index;
        byte cBurseID;
        long Temp;
        long lngMoneyTemp;
        long lngPayRecordID;
        long lngTotalPayMoney;
        byte[] PayDatetime = new byte[4];
        int Crc;

        WasteBooks stWasteBooks = new WasteBooks();
        cBurseID = g_StationInfo.cChaseBurseID;

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
        stWasteBooks.cBurseID = cBurseID;

        //钱包流水号
        Temp = iBurseSID;
        stWasteBooks.cBurseSID[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cBurseSID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        //补助流水号
        Temp = g_CardBasicInfo.iChaseSubsidySID;
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

        //交易类型 11:断点拔卡；12:断点恢复；
        stWasteBooks.cPaymentType = cType;

        //金额
        stWasteBooks.cPaymentMoney[0] = (byte) (lngBurseMoney & 0x000000FF);
        stWasteBooks.cPaymentMoney[1] = (byte) ((lngBurseMoney & 0x0000FF00) >> 8);

        //优惠金额
        Temp = 0;
        stWasteBooks.cPrivelegeMoney[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cPrivelegeMoney[1] = (byte) ((Temp & 0x0000FF00) >> 8);

        //钱包卡余额
        lngMoneyTemp = g_CardBasicInfo.lngChaseBurseMoney;
        stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

        //累计交易总金额(消费的实际金额)
        lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney;
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
        fill(stWasteBooks.cReserve, (byte) 0);

        //写交易流水文件
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

    //记录断点拔卡流水
    public static int WriteAllNOCardPayRecord(byte cNOWriteCardFlag) {
        int cResult = 0;

        //记录工作钱包断点流水 11:断点拔卡；12:断点恢复；
        if ((cNOWriteCardFlag & 0x01) == 0x01) {
            cResult = WriteWorkCardPayRecord((byte) 11, g_CardBasicInfo.iWorkBurseSID, g_CardBasicInfo.lngWorkBurseMoney);
            if (cResult != 0) {
                return cResult;
            }
        }
        if ((cNOWriteCardFlag & 0x10) == 0x10) {
            cResult = WriteChaseCardPayRecord((byte) 11, g_CardBasicInfo.iChaseBurseSID, g_CardBasicInfo.lngChaseBurseMoney);
            if (cResult != 0) {
                return cResult;
            }
        }
        return cResult;
    }

    //记录断点恢复流水
    public static int WriteAllCardPayRecord(byte cReWriteCardFlag) {
        int cResult = 0;

        //记录工作钱包断点流水 11:断点拔卡；12:断点恢复；
        if ((cReWriteCardFlag & 0x01) == 0x01) {
            cResult = WriteWorkCardPayRecord((byte) 12, g_CardBasicInfo.iWorkBurseSID, g_CardBasicInfo.lngWorkBurseMoney);
            if (cResult != 0) {
                return cResult;
            }
        }
        if ((cReWriteCardFlag & 0x10) == 0x10) {
            cResult = WriteChaseCardPayRecord((byte) 12, g_CardBasicInfo.iChaseBurseSID, g_CardBasicInfo.lngChaseBurseMoney);
            if (cResult != 0) {
                return cResult;
            }
        }
        return cResult;
    }

    //记录余额复位流水0x07-余额复位
    public static int WriteCardResetRecord(byte Type, int iBurseSID, long lngBurseMoney) {
        int cResult;
        long Index;
        byte cBurseID;
        long Temp;
        long lngMoneyTemp;
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
        stWasteBooks.cBurseID = cBurseID;

        //钱包流水号
        Temp = iBurseSID;
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

        //交易类型
        stWasteBooks.cPaymentType = Type;

        //金额
        stWasteBooks.cPaymentMoney[0] = (byte) (lngBurseMoney & 0x000000FF);
        stWasteBooks.cPaymentMoney[1] = (byte) ((lngBurseMoney & 0x0000FF00) >> 8);

        //优惠金额
        Temp = 0;
        stWasteBooks.cPrivelegeMoney[0] = (byte) (Temp & 0x000000FF);
        stWasteBooks.cPrivelegeMoney[1] = (byte) ((Temp & 0x0000FF00) >> 8);

        //钱包卡余额
        if (g_CardBasicInfo.iWorkBurseSID != iBurseSID) {
            lngMoneyTemp = g_CardBasicInfo.lngPayMoney - lngBurseMoney;
        } else {
            lngMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
        }
        lngMoneyTemp = g_CardBasicInfo.lngWorkBurseMoney;
        stWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

        //累计交易总金额(消费的实际金额)
        lngTotalPayMoney = g_RecordInfo.lngTotalPaymentMoney;
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
        //写交易流水文件
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

    //记录二维码交易流水 0:正元二维码主扫> 1：正元二维码被扫<   2：支付宝连接正元卡余额二维码> 3：支付宝二维码> 4:卡在线交易 5:代扣
    public static int WriteQrPayRecord(int cType) {
        int cResult;
        long Index;
        long Temp;
        long lngMoneyTemp;
        long lngPayRecordID;
        byte[] PayDatetime = new byte[4];
        int Crc;

        WasteQrCodeBooks stQrCodewasteBooks = new WasteQrCodeBooks();
        //站点号
        Temp = g_StationInfo.iStationID;
        stQrCodewasteBooks.cStationID[0] = (byte) (Temp & 0x00FF);
        stQrCodewasteBooks.cStationID[1] = (byte) ((Temp & 0xFF00) >> 8);
        //商户号
        Temp = g_LocalInfo.wShopUserID;
        stQrCodewasteBooks.cShopUserID[0] = (byte) (Temp & 0x00FF);
        stQrCodewasteBooks.cShopUserID[1] = (byte) ((Temp & 0xFF00) >> 8);
        //二维码单号
        Temp = g_OnlinePayInfo.lngOrderNum;
        stQrCodewasteBooks.cQrCodeID[0] = (byte) (Temp & 0x000000FF);
        stQrCodewasteBooks.cQrCodeID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        stQrCodewasteBooks.cQrCodeID[2] = (byte) ((Temp & 0x00FF0000) >> 16);
        stQrCodewasteBooks.cQrCodeID[3] = (byte) ((Temp & 0xFF000000) >> 24);
        //主卡帐号
        if ((g_WorkInfo.cOtherQRFlag == 1) || (g_WorkInfo.cOtherQRFlag == 4))
            Temp = g_ThirdCodeResultInfo.lngAccountID;
        else
            Temp = g_OnlinePayInfo.lngAccountID;

        stQrCodewasteBooks.cAccountID[0] = (byte) (Temp & 0x000000FF);
        stQrCodewasteBooks.cAccountID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        stQrCodewasteBooks.cAccountID[2] = (byte) ((Temp & 0x00FF0000) >> 16);
        stQrCodewasteBooks.cAccountID[3] = (byte) ((Temp & 0xFF000000) >> 24);
        //卡户姓名
        if ((g_WorkInfo.cOtherQRFlag == 1) || (g_WorkInfo.cOtherQRFlag == 4))
            memcpy(stQrCodewasteBooks.cAccName, g_ThirdCodeResultInfo.cAccName, 16);
        else
            memcpy(stQrCodewasteBooks.cAccName, g_OnlinePayInfo.cAccName, 16);

        //交易时间
        cResult = Publicfun.GetCurrCardDate(PayDatetime);
        if (cResult != OK) {
            return DATE_TIME_ERROR;
        }
        System.arraycopy(PayDatetime, 0, stQrCodewasteBooks.cPaymentTime, 0, 4);
        //终端流水号
        lngPayRecordID = g_WasteQrBookInfo.WriterIndex + 1;
        stQrCodewasteBooks.cPayRecordID[0] = (byte) (lngPayRecordID & 0x000000FF);
        stQrCodewasteBooks.cPayRecordID[1] = (byte) ((lngPayRecordID & 0x0000FF00) >> 8);
        stQrCodewasteBooks.cPayRecordID[2] = (byte) ((lngPayRecordID & 0x00FF0000) >> 16);
        stQrCodewasteBooks.cPayRecordID[3] = (byte) ((lngPayRecordID & 0xFF000000) >> 24);
        //交易类型
        if (g_WorkInfo.cOtherQRFlag == 3)//3:在线卡片支付
            stQrCodewasteBooks.cPayType = 6;
        else if (g_WorkInfo.cOtherQRFlag == 4)//4:代扣
            stQrCodewasteBooks.cPayType = 5;
        else
            stQrCodewasteBooks.cPayType = (byte) cType;
        //交易金额
        if ((g_WorkInfo.cOtherQRFlag == 1) || (g_WorkInfo.cOtherQRFlag == 4))
            lngMoneyTemp = g_ThirdCodeResultInfo.lngPayMoney;
        else
            lngMoneyTemp = g_OnlinePayInfo.lngPayMoney;

        Log.d(TAG, "交易金额:" + lngMoneyTemp);
        stQrCodewasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stQrCodewasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stQrCodewasteBooks.cPaymentMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);
        //优惠金额
        if ((g_WorkInfo.cOtherQRFlag == 1) || (g_WorkInfo.cOtherQRFlag == 4))
            lngMoneyTemp = g_ThirdCodeResultInfo.wPrivelegeMoney;
        else
            lngMoneyTemp = g_OnlinePayInfo.lngPriMoney;

        stQrCodewasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stQrCodewasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stQrCodewasteBooks.cPrivelegeMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);
        //管理费
        if (g_WorkInfo.cOtherQRFlag == 1)
            lngMoneyTemp = g_ThirdCodeResultInfo.wManageMoney;
        else
            lngMoneyTemp = g_OnlinePayInfo.lngManageMoney;

        stQrCodewasteBooks.cManageMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stQrCodewasteBooks.cManageMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stQrCodewasteBooks.cManageMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);
        //钱包卡余额
        if ((g_WorkInfo.cOtherQRFlag == 1) || (g_WorkInfo.cOtherQRFlag == 4))
            lngMoneyTemp = g_ThirdCodeResultInfo.lngBurseMoney;
        else
            lngMoneyTemp = g_OnlinePayInfo.lngBurseMoney;

        stQrCodewasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stQrCodewasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stQrCodewasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

        //CRC16,计算整个数据结构的校验码
        Crc = Publicfun.CRC_PlusA(stQrCodewasteBooks, QRPAYMENTRECORD_LEN);
        //校验码
        stQrCodewasteBooks.CRC[0] = (byte) (Crc & 0x000000ff);
        stQrCodewasteBooks.CRC[1] = (byte) ((Crc & 0x0000ff00) >> 8);
        //预留
        fill(stQrCodewasteBooks.cReserve, (byte) 0);

        //打印小票
        PrintReceiptQR(stQrCodewasteBooks);

        //流水存储位置
        Index = g_WasteQrBookInfo.WriterIndex;
        //流水存储指针余数;
        Index = (Index % MAXBOOKSCOUNT);
        //写交易流水文件
        cResult = WasteQrCodeBooksRW.WriteWasteQrCodeBooksData(stQrCodewasteBooks, (int) Index);
        if (cResult != 0) {
            Log.d(TAG, "写流水数据文件失败:" + cResult);
            return MEMORY_FAIL;
        }

        //写流水文件指针
        g_WasteQrBookInfo.UnTransferCount++;
        g_WasteQrBookInfo.WriterIndex++;
        g_WasteQrBookInfo.MaxStationSID = g_WasteQrBookInfo.WriterIndex;
        cResult = WasteQrCodeBooksRW.WriteWasteQrCodeBookInfo(g_WasteQrBookInfo);
        if (cResult != 0) {
            Log.d(TAG, "写流水指针文件失败:" + cResult);
            return MEMORY_FAIL;
        }
        return OK;
    }

    //记录人脸交易流水
    public static int WriteFacePayRecord(int cType) {
        int cResult;
        long Index;
        long Temp;
        long lngMoneyTemp;
        long lngPayRecordID;
        byte[] PayDatetime = new byte[6];
        int Crc;

        WasteFacePayBooks stFaceWasteBooks = new WasteFacePayBooks();
        //站点号
        Temp = g_StationInfo.iStationID;
        stFaceWasteBooks.cStationID[0] = (byte) (Temp & 0x00FF);
        stFaceWasteBooks.cStationID[1] = (byte) ((Temp & 0xFF00) >> 8);
        //商户号
        Temp = g_LocalInfo.wShopUserID;
        stFaceWasteBooks.cShopUserID[0] = (byte) (Temp & 0x00FF);
        stFaceWasteBooks.cShopUserID[1] = (byte) ((Temp & 0xFF00) >> 8);
        //二维码单号
        Temp = g_FacePayInfo.lngOrderNum;
        stFaceWasteBooks.cOrderID[0] = (byte) (Temp & 0x000000FF);
        stFaceWasteBooks.cOrderID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        stFaceWasteBooks.cOrderID[2] = (byte) ((Temp & 0x00FF0000) >> 16);
        stFaceWasteBooks.cOrderID[3] = (byte) ((Temp & 0xFF000000) >> 24);
        //主卡帐号
        Temp = g_FacePayInfo.lngAccountID;
        stFaceWasteBooks.cAccountID[0] = (byte) (Temp & 0x000000FF);
        stFaceWasteBooks.cAccountID[1] = (byte) ((Temp & 0x0000FF00) >> 8);
        stFaceWasteBooks.cAccountID[2] = (byte) ((Temp & 0x00FF0000) >> 16);
        stFaceWasteBooks.cAccountID[3] = (byte) ((Temp & 0xFF000000) >> 24);
        //卡户姓名
        System.arraycopy(g_OnlinePayInfo.cAccName, 0, stFaceWasteBooks.cAccName, 0, g_OnlinePayInfo.cAccName.length);
        //交易时间
        System.arraycopy(g_FacePayInfo.cPayDataTime, 0, stFaceWasteBooks.cPaymentTime, 0, 6);
        //终端流水号
        lngPayRecordID = g_WasteFaceBookInfo.WriterIndex + 1;
        stFaceWasteBooks.cPayRecordID[0] = (byte) (lngPayRecordID & 0x000000FF);
        stFaceWasteBooks.cPayRecordID[1] = (byte) ((lngPayRecordID & 0x0000FF00) >> 8);
        stFaceWasteBooks.cPayRecordID[2] = (byte) ((lngPayRecordID & 0x00FF0000) >> 16);
        stFaceWasteBooks.cPayRecordID[3] = (byte) ((lngPayRecordID & 0xFF000000) >> 24);
        //交易类型
        stFaceWasteBooks.cPayType = (byte) cType;
        //交易金额
        lngMoneyTemp = g_OnlinePayInfo.lngPayMoney;
        Log.d(TAG, "交易金额:" + lngMoneyTemp);
        stFaceWasteBooks.cPaymentMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stFaceWasteBooks.cPaymentMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stFaceWasteBooks.cPaymentMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);
        //优惠金额
        lngMoneyTemp = g_OnlinePayInfo.lngPriMoney;
        stFaceWasteBooks.cPrivelegeMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stFaceWasteBooks.cPrivelegeMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stFaceWasteBooks.cPrivelegeMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);
        //管理费
        lngMoneyTemp = g_OnlinePayInfo.lngManageMoney;
        stFaceWasteBooks.cManageMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stFaceWasteBooks.cManageMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stFaceWasteBooks.cManageMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);
        //钱包卡余额
        lngMoneyTemp = g_OnlinePayInfo.lngBurseMoney;
        stFaceWasteBooks.cBurseMoney[0] = (byte) (lngMoneyTemp & 0x000000FF);
        stFaceWasteBooks.cBurseMoney[1] = (byte) ((lngMoneyTemp & 0x0000FF00) >> 8);
        stFaceWasteBooks.cBurseMoney[2] = (byte) ((lngMoneyTemp & 0x00FF0000) >> 16);

        //CRC16,计算整个数据结构的校验码
        //Crc = Publicfun.CRC_PlusA(stFaceWasteBooks, FACEPAYMENTRECORD_LEN);
        Crc = 0;
        //校验码
        stFaceWasteBooks.CRC[0] = (byte) (Crc & 0x000000ff);
        stFaceWasteBooks.CRC[1] = (byte) ((Crc & 0x0000ff00) >> 8);
        //人脸识别度
        stFaceWasteBooks.fMatchScore=g_FacePayInfo.fMatchScore;
        //预留
        fill(stFaceWasteBooks.cReserve, (byte) 0);

        //写交易流水文件
        //流水存储位置
        Index = g_WasteFaceBookInfo.WriterIndex;
        //流水存储指针余数;
        Index = (Index % MAXBOOKSCOUNT);
        //写交易流水文件
        cResult = WasteFacePayBooksRW.WriteWasteFacePayBooksData(stFaceWasteBooks, (int) Index);
        if (cResult != 0) {
            Log.d(TAG, "写流水数据文件失败:" + cResult);
            return MEMORY_FAIL;
        }

        //写流水文件指针
        g_WasteFaceBookInfo.UnTransferCount++;
        g_WasteFaceBookInfo.WriterIndex++;
        g_WasteFaceBookInfo.MaxStationSID = g_WasteFaceBookInfo.WriterIndex;
        cResult = WasteFacePayBooksRW.WriteWasteFacePayBookInfo(g_WasteFaceBookInfo);
        if (cResult != 0) {
            Log.d(TAG, "写流水指针文件失败:" + cResult);
            return MEMORY_FAIL;
        }
        return OK;
    }

    //日累统计、餐累统计、写末笔营业时段号(TYPE:0 消费 1 冲正 2 在线消费 3 在线冲正）
    public static int TodayConsumStat(CardBasicParaInfo pCardBasicInfo, byte Type) {
        int cResult = 0;
        long lngPaymentMoney = 0;
        int wTodayPaymentSum = 0;             //当日交易笔数
        long lngTodayPaymentMoney = 0;        //当日交易总额
        int wTotalBusinessSum = 0;                //当餐营业笔数
        long lngTotalBusinessMoney = 0;          //当餐营业总额
        long lngTotalPaymentMoney = 0;          //累计交易总金额
        int cLastBusinessID = 0;              //末笔交易营业号
        byte[] cLastPaymentDate = new byte[6];          //末笔交易日期

        //日累和餐累不含管理费
        if ((Type == 0)||(Type == 2)) {
            lngPaymentMoney = pCardBasicInfo.lngPayMoney - pCardBasicInfo.lngManageMoney;
        }
        if ((Type == 1)||(Type == 3)) {
            lngPaymentMoney = g_WorkInfo.lngReWorkPayMoney + g_WorkInfo.lngReChasePayMoney;
        }
        Log.d(TAG, "日累统计 lngPaymentMoney:" + lngPaymentMoney);

        //判断是否是同一天
        cResult = Publicfun.CompareStatLastDate(g_RecordInfo.cLastPaymentDate);
        if (cResult == 0) {
            //日累统计
            Log.d(TAG, "日累统计同一天");
            if ((Type == 0)||(Type == 2)) {
                lngTodayPaymentMoney = g_RecordInfo.lngTodayPaymentMoney + lngPaymentMoney;
                wTodayPaymentSum = g_RecordInfo.wTodayPaymentSum + 1;
            }
            if ((Type == 1)||(Type == 3)) {
                lngTodayPaymentMoney = g_RecordInfo.lngTodayPaymentMoney - lngPaymentMoney;
                wTodayPaymentSum = g_RecordInfo.wTodayPaymentSum + 1;
            }
            //餐累统计
            if (g_WorkInfo.cBusinessID == g_RecordInfo.cLastBusinessID) {
                if ((Type == 0)||(Type == 2)) {
                    lngTotalBusinessMoney = g_RecordInfo.lngTotalBusinessMoney + lngPaymentMoney;
                    wTotalBusinessSum = g_RecordInfo.wTotalBusinessSum + 1;
                }
                if ((Type == 1)||(Type == 3)) {
                    lngTotalBusinessMoney = g_RecordInfo.lngTotalBusinessMoney - lngPaymentMoney;
                    wTotalBusinessSum = g_RecordInfo.wTotalBusinessSum + 1;
                }
            } else {
                wTotalBusinessSum = 1;
                lngTotalBusinessMoney = pCardBasicInfo.lngPayMoney;
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
            lngTotalPaymentMoney = g_RecordInfo.lngTotalPaymentMoney + pCardBasicInfo.lngPayMoney;
        }
        else if (Type == 1) {
            lngTotalPaymentMoney = g_RecordInfo.lngTotalPaymentMoney - pCardBasicInfo.lngPayMoney;
        }
        else
            lngTotalPaymentMoney = g_RecordInfo.lngTotalPaymentMoney;

        Log.d(TAG, "终端累计交易总金额 lngTotalPaymentMoney:" + lngTotalPaymentMoney);

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
