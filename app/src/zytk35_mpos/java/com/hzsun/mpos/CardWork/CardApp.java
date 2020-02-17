package com.hzsun.mpos.CardWork;

import android.os.SystemClock;
import android.util.Log;

import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.data.BurseInfo;
import com.hzsun.mpos.data.StatusBurInfo;
import com.hzsun.mpos.data.StatusInfo;
import com.hzsun.mpos.data.StatusPriInfo;

import java.util.Arrays;

import static com.hzsun.mpos.Activity.CardActivity.CARD_PWDKBCLOSE;
import static com.hzsun.mpos.Activity.CardActivity.CARD_PWDKBOPEN;
import static com.hzsun.mpos.Activity.CardActivity.CARD_RWEND;
import static com.hzsun.mpos.Activity.CardActivity.CARD_RWSTART;
import static com.hzsun.mpos.CardWork.CardPublic.PwdLongtoByte;
import static com.hzsun.mpos.Global.Global.BEYOND_LIMIT_TIM;
import static com.hzsun.mpos.Global.Global.OK;
import static com.hzsun.mpos.Global.Global.OUT_OF_CONSUMERANGE;
import static com.hzsun.mpos.Global.Global.g_UICardHandler;
import static com.hzsun.mpos.Global.Global.g_CardAttr;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_EP_BurseInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_StatusInfoArray;
import static com.hzsun.mpos.Global.Global.g_StatusPriInfoArray;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.PrintArray;
import static com.hzsun.mpos.Public.Utility.memcmp;
import static com.hzsun.mpos.Public.Utility.memcpy;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePlay;

public class CardApp {

    private static final String TAG = "CardApp";

//    private static final int OK=0 ;     //0
//    private static final int OUT_OF_CONSUMERANGE=2;//超出消费范围    2
//    private static final int BEYOND_LIMIT_TIM=5;           //超出有效期    5

    //身份参数
    static StatusInfo s_StatusInfo;
    //工作钱包
    static StatusBurInfo s_StatusWorkBurInfo;
    //追扣钱包
    static StatusBurInfo s_StatusChaseBurInfo;
    //身份优惠参数
    static StatusPriInfo s_StatusPriInfo;

    //====================卡片参数数据=====================
    //基础应用信息区
    private static CardBaseInfo s_CardBaseData;
    //扩展应用信息区
    private static CardExtentInfo s_CardExtentData;
    //钱包应用信息区
    private static CardBurseInfo s_CardBurseInfo, s_CardChaseBurseInfo;

    private static byte[] s_cCurDateTime = new byte[6];
    private static byte s_ReWriteCardFlag;
    private static byte s_NOWriteCardFlag;

    private static int s_cUsePrivLimit;          //是否启动优惠
    private static long s_lngPayMoney;           //交易金额
    private static long s_lngWorkPayMoney;        //追扣时工作钱包交易金额
    private static long s_lngChasePayMoney;        //追扣时追扣钱包交易金额

    private static long s_lngPriMoney;        //优惠金额
    private static long s_lngManageMoney;    //管理费金额

    //设置当前日期时间
    public static void SetCurDateTime(byte[] cCurDateTime) {
        memcpy(s_cCurDateTime, cCurDateTime, 6);
    }

    //读卡片属性
    public static int ReadCardAttrInfo(CardAttrInfo CardAttr) {
        int cResult;
        byte cCardType;
        byte[] cCardTempSID = new byte[16];
        byte[] SAK = new byte[2];
        byte[] resp = new byte[256];
        int rlen = 0;

        cResult = g_Nlib.ReadCardAttrib(cCardTempSID, SAK);
        if (cResult != 0) {
            return -1;
        }
        if (g_SystemInfo.cCardMode == 0) {
            if ((SAK[0] & 0x20) == 0x20 && (SAK[0] & 0x08) == 0x08) {
                //启用卡种 StationInfo.cUseCardType
                //0-复旦微(cpu部分)、NFC和SIMPASS卡都没有启用，
                //1-启用复旦微卡、NFC卡
                //2-只启用SIMPASS卡、
                //3-复旦微、NFC和SIMPASS卡都启用
                //cUseCardClass:1 M1cpu混合卡 cUseCardClass:2 M1 cUseCardClass:3 CPU
                //cSIMPassType:0  M1 cSIMPassType:1 SIM
                if (g_StationInfo.cUseCardType == 0) {
                    //Log.i(TAG,"未启用 卡片类型1");
                    cCardType = 1;
                } else if (g_StationInfo.cUseCardType == 2) {
                    if (g_StationInfo.cSIMPassType == 0) {
                        cCardType = 1;
                    } else {
                        cCardType = 4;
                    }
                } else {
                    resp[6] = 0x0;
                    rlen = g_Nlib.CardSelectProCard(resp);
                    //Log.i(TAG,String.format("resp[6]=%02x--rlen:",resp[6],rlen));
                    //3：混合卡使用cpu部分
                    if ((resp[6] & 0xff) == 0x90)//判断返回的第6字节为0X90 为复旦微的卡片
                    {
                        //Log.i(TAG,"为复旦微的卡片");
                        cCardType = 2;
                    } else if ((resp[5] == 0x86) && (resp[6] == 0x75)) //判断柯斯NFC卡COS厂商标识：8675（上海柯斯注册标识）
                    {
                        //0d.78.80.a0.02.86.75.14.10.06.
                        //Log.i(TAG,"柯斯NFC卡片");
                        cCardType = 5;
                    } else if ((((resp[5] == 0x00) && (resp[6] == 0x00)) ||
                            ((resp[5] == 0x00) && (resp[6] == 0x73)) ||
                            ((resp[5] == 0x5a) && (resp[6] == 0x4a))) && (rlen > 7)) {
                        cCardType = 6;
                        if ((resp[2] != 0xb3) && (resp[3] != 0xb4)) {
                            //g_WorkInfo.cHNcbcState = 1;//表示是恒宝的COS卡
                        }
                    } else {   //其他卡都当做simpass卡
                        if (g_StationInfo.cSIMPassType == 0) {
                            cCardType = 1;
                        } else {
                            cCardType = 4;
                        }
                    }
                    //2：混合卡使用M1
                    if (g_StationInfo.cUseCardClass == 2) {   //重新判断这几种混合卡使用模式
                        if ((cCardType == 2) || (cCardType == 5) || (cCardType == 6)) {
                            cCardType = 1;
                        }
                    }
                }
            } else {
                if ((SAK[0] & 0x20) == 0x20) {
                    resp[6] = 0;
                    cCardType = 2;
                    rlen = g_Nlib.CardSelectProCard(resp);
                    if (rlen == 0) {
                        return 2;
                    }
                    if ((resp[5] == 0x86) && (resp[6] == 0x75)) //判断柯斯NFC卡COS厂商标识：8675（上海柯斯注册标识）
                    {
                        //Log.i(TAG,"柯斯NFC卡片");
                        cCardType = 5;//柯斯NFC卡
                    } else if ((((resp[5] == 0x00) && (resp[6] == 0x00)) ||//2016版新卡14.78.b3.b4.02.00.00.86
                            ((resp[5] == 0x00) && (resp[6] == 0x73)) ||//湖南建行卡 0d.78.b3.b4.02.00.73.c8
                            ((resp[5] == 0x5a) && (resp[6] == 0x4a))) && (rlen > 7)) {
                        //湖南建行卡
                        //Log.i(TAG,"湖南建行卡");
                        cCardType = 6;
                        if ((resp[2] != 0xb3) && (resp[3] != 0xb4)) {
                            //g_WorkInfo.cHNcbcState = 1;//表示是恒宝的COS卡
                        }
                    }
                    if (resp[6] == 0x90)//判断返回的第6字节为0X90 为复旦微的卡片
                    {
                        //Log.i(TAG,"为复旦微的卡片");
                        cCardType = 2;
                    } else {
                        //判断电信NFC卡 2015.0811
                        //Log.i(TAG,"选择翼机通环境");
//                        cResult=SelectTelAllPSE();
//                        if(cResult==0)
//                        {
//                            Log.i(TAG,"电信NFC卡--卡片类型4:0x20");
//                            cCardType=4;
//                        }
                    }
                } else if ((SAK[0] & 0x29) == 0x29) {
                    cCardType = 4;
                    //Log.i(TAG,"电信NFC卡--卡片类型4:0x29");
                } else {
                    if ((SAK[0] & 0x18) == 0x18) {
                        cCardType = 1;
                        CardAttr.cSAK = 0x18;
                    } else if ((SAK[0] & 0x08) == 0x08) {
                        cCardType = 1;
                    } else {
                        cCardType = 1;
                    }
                }
            }
        } else {
            //Log.i(TAG,"cCardMode:"+g_SystemInfo.cCardMode);
//            Log.i(TAG,String.format("cCardSID:%02x.%02x.%02x.%02x",
//                    CardAttr.cCardSID[0],CardAttr.cCardSID[1],CardAttr.cCardSID[2],CardAttr.cCardSID[3]));
            cCardType = 1;
        }
        //获取卡片属性
        System.arraycopy(cCardTempSID, 0, CardAttr.cCardSID, 0, 4);
        CardAttr.cCardType = cCardType;

        return 0;
    }

    //判断M1卡是S50卡还是S70卡
    public static int CheckM1CardS50orS70() {
        int cResult;
        byte[] cCardContext = new byte[64];
        //读S70卡基本扇区第1数据块(S70卡SystemInfo.cBasicSectorID+16)
        if ((g_EP_BurseInfo.size() <= (g_CardAttr.cWorkBurseID))
                || (g_EP_BurseInfo.size() <= (g_CardAttr.cChaseBurseID))) {
            Log.i(TAG, "钱包参数出错");
            return 2;
        }
        if (g_CardAttr.cWorkBurseID < 1) {
            Log.i(TAG, "钱包参数出错");
            return 2;
        }

        Log.i(TAG, String.format("基本扇区%d.扩展扇区%d.工作钱包%d.追扣%d.工作块号%d.备份%d.", g_CardAttr.cBasicSectorID, g_CardAttr.cExtendSectorID, g_CardAttr.cWorkBurseID
                , g_CardAttr.cChaseBurseID, g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID - 1).cBlockID, g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID - 1).cBakBlockID));
        if ((g_CardAttr.cBasicSectorID >= 16) || (g_CardAttr.cExtendSectorID >= 16)) {
            //基本扇区号
            g_CardAttr.cBasicSectorID = (byte) (g_CardAttr.cBasicSectorID - 16);
            // 扩展扇区号
            g_CardAttr.cExtendSectorID = (byte) (g_CardAttr.cExtendSectorID - 16);
        }
        if ((g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID - 1).cBlockID >= 64)
                || (g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID - 1).cBakBlockID >= 64)) {
            // 工作钱包号
            BurseInfo pBurseInfo = g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID - 1);
            pBurseInfo.cBlockID = (byte) (pBurseInfo.cBlockID - 64);
            pBurseInfo.cBakBlockID = (byte) (pBurseInfo.cBakBlockID - 64);
            g_EP_BurseInfo.set((g_CardAttr.cWorkBurseID - 1), pBurseInfo);

//            g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID-1).cBlockID= (byte) (g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID-1).cBlockID-64);
//            g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID-1).cBakBlockID= (byte) (g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID-1).cBakBlockID-64);
            //被追扣钱包号
            if (((g_CardAttr.cChaseBurseID) != (g_CardAttr.cWorkBurseID))
                    && (g_CardAttr.cChaseBurseID > 0)) {
                BurseInfo pChaseBurseInfo = g_EP_BurseInfo.get(g_CardAttr.cChaseBurseID - 1);
                pChaseBurseInfo.cBlockID = (byte) (pChaseBurseInfo.cBlockID - 64);
                pChaseBurseInfo.cBakBlockID = (byte) (pChaseBurseInfo.cBakBlockID - 64);
                g_EP_BurseInfo.set((g_CardAttr.cChaseBurseID - 1), pChaseBurseInfo);

            }
        }
        //2013.1209	 //混合卡M1部分读取失败2014.1010修改
        if (g_CardAttr.cSAK != 0x18) {
            return 0;
        }
        if (g_CardAttr.cSAK == 0x18) {
            //读卡片数据
            cResult = g_Nlib.ReadMifareBlock((byte) ((g_CardAttr.cBasicSectorID + 16) * 4 + 1), g_CardAttr.cCardSID, cCardContext);
            if (cResult == 0) {
                //基本扇区号
                g_CardAttr.cBasicSectorID = (byte) (g_CardAttr.cBasicSectorID + 16);
                // 扩展扇区号
                g_CardAttr.cExtendSectorID = (byte) (g_CardAttr.cExtendSectorID + 16);
                // 工作钱包号
                BurseInfo pBurseInfo = g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID - 1);
                pBurseInfo.cBlockID = (byte) (pBurseInfo.cBlockID + 64);
                pBurseInfo.cBakBlockID = (byte) (pBurseInfo.cBakBlockID + 64);
                g_EP_BurseInfo.set((g_CardAttr.cWorkBurseID - 1), pBurseInfo);

//            g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID-1).cBlockID= (byte) (g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID-1).cBlockID+64);
//            g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID-1).cBakBlockID= (byte) (g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID-1).cBakBlockID+64);

                //被追扣钱包号
                if (((g_CardAttr.cChaseBurseID) != (g_CardAttr.cWorkBurseID))
                        && (g_CardAttr.cChaseBurseID > 0)) {
                    BurseInfo pChaseBurseInfo = g_EP_BurseInfo.get(g_CardAttr.cChaseBurseID - 1);
                    pChaseBurseInfo.cBlockID = (byte) (pChaseBurseInfo.cBlockID + 64);
                    pChaseBurseInfo.cBakBlockID = (byte) (pChaseBurseInfo.cBakBlockID + 64);
                    g_EP_BurseInfo.set((g_CardAttr.cChaseBurseID - 1), pChaseBurseInfo);
                }
            }
            Log.i(TAG, "修改S70贴片卡块号操作流程成功");
            Log.i(TAG, String.format("基本扇区%d.扩展扇区%d.工作钱包%d.追扣%d.工作块号%d.备份%d.", g_CardAttr.cBasicSectorID, g_CardAttr.cExtendSectorID, g_CardAttr.cWorkBurseID
                    , g_CardAttr.cChaseBurseID, g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID - 1).cBlockID, g_EP_BurseInfo.get(g_CardAttr.cWorkBurseID - 1).cBakBlockID));
            return 0;
        } else {
            return 61;
        }
    }

    //获取卡片所有的信息
    public static int GetCardAllInfoData(CardBasicParaInfo CardBasicInfo) {
        int cResult;
        Log.i(TAG, "获取卡片所有的信息");

        //初始化卡片参数数据
        s_CardBaseData = new CardBaseInfo();
        s_CardExtentData = new CardExtentInfo();
        s_CardBurseInfo = new CardBurseInfo();
        s_CardChaseBurseInfo = new CardBurseInfo();

        //获取当前时间日期
        Publicfun.GetCurrDateTime(s_cCurDateTime);

        Log.i(TAG, "基础扇区:" + g_SystemInfo.cBasicSectorID);
        Log.i(TAG, "扩展扇区:" + g_SystemInfo.cExtendSectorID);
        Log.i(TAG, String.format("工作钱包号:%d,追扣钱包号:", g_StationInfo.cWorkBurseID, g_StationInfo.cChaseBurseID));

        //CardBasicParaInfo CardBasicInfo = new CardBasicParaInfo();
        g_CardAttr.cBasicSectorID = g_SystemInfo.cBasicSectorID;
        g_CardAttr.cExtendSectorID = g_SystemInfo.cExtendSectorID;
        g_CardAttr.cWorkBurseID = g_StationInfo.cWorkBurseID;
        g_CardAttr.cChaseBurseID = g_StationInfo.cChaseBurseID;

        if ((g_CardAttr.cCardType == 2)) {
            Log.i(TAG, "CPU卡片校验");
            cResult = CheckCPUCardProcess(CardBasicInfo, g_CardAttr);
            if (cResult != 0) {
                return cResult;
            }
        } else {
            Log.i(TAG, "M1卡片校验");
            if ((g_CardAttr.cCardType == 1)) {
                Log.i(TAG, "判断S50卡S70卡");
                cResult = CheckM1CardS50orS70();
                if (cResult != 0) {
                    Log.i(TAG, "判断S50卡S70卡失败");
                }
            }
            cResult = CheckMifareCardProcess(CardBasicInfo, g_CardAttr);
            if (cResult != 0) {
                return cResult;
            }
        }
        return cResult;
    }

    //CPU卡认证过程
    private static int CheckCPUCardProcess(CardBasicParaInfo CardBasicInfo, CardAttrInfo CardAttr) {
        int cResult;

//        //校验基础应用信息区数据
//        cResult=CheckBasicInfo(s_CardBaseData, CardAttr);
//        if(cResult!=0)
//        {
//            Log.i(TAG,"校验基础应用信息区数据失败");
//            return 61;
//        }
//
//        //校验扩展应用信息区数据
//        cResult=CheckExtentInfo(s_CardExtentData, CardAttr);
//        if(cResult!=0)
//        {
//            Log.i(TAG,"校验扩展应用信息区数据失败");
//            return 61;
//        }

        //校验基础扩展应用信息区数据
        cResult = CheckBasicExtentInfo(s_CardBaseData, s_CardExtentData, CardAttr);
        if (cResult != 0) {
            Log.i(TAG, "校验基础扩展应用信息区数据失败");
            return 61;
        }

//        Log.i(TAG,String.format("工作钱包块号:%d,",g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID-1).cBlockID,
//                g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID-1).cBakBlockID));
//
//        Log.i(TAG,String.format("工作钱包号:%d,",g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID-1).cBurseID,
//                g_StationInfo.cWorkBurseID));
        if (g_StationInfo.cWorkBurseID < 1) {
            Log.i(TAG, "工作钱包号出错");
            return 1;
        }

        if (g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID - 1).cBurseID != g_StationInfo.cWorkBurseID) {
            Log.i(TAG, "工作钱包号不一致");
            return 1;
        }

        s_ReWriteCardFlag = 0;
        //校验钱包信息区数据
        cResult = CheckCardBurseInfo(s_CardBurseInfo, CardAttr);
        if (cResult != 0) {
            Log.i(TAG, "校验钱包信息区数据失败");
            return 61;
        }
        //判断是否需要读出追扣钱包
        if ((g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID - 1).cCanPermitChase == 1) && (g_StationInfo.cChaseBurseID != 0)) {
            Log.i(TAG, "读取追扣钱包数据");
            cResult = CheckCardChaseBurseInfo(s_CardChaseBurseInfo, CardAttr);
            if (cResult != 0) {
                Log.i(TAG, "校验钱包信息区数据失败");
                return 61;
            }
        }
        //组织卡片数据
        //基本扇区
        CardBasicInfo.cAgentID = s_CardBaseData.cAgentID;                //代理号
        CardBasicInfo.iGuestID = s_CardBaseData.iGuestID;               //客户号
        CardBasicInfo.cAuthenVer = s_CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
        //卡认证码	4
        System.arraycopy(s_CardBaseData.cCardAuthenCode, 0, CardBasicInfo.cCardAuthenCode, 0, 4);
        CardBasicInfo.cCardState = s_CardBaseData.cCardState;         //卡片状态	1
        CardBasicInfo.lngCardID = s_CardBaseData.lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
        CardBasicInfo.cCampusID = s_CardBaseData.cCampusID;         //园区号	1	范围为1~250
        CardBasicInfo.cStatusID = s_CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64
        CardBasicInfo.iValidTime = s_CardBaseData.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
        //基本扇区第1块内容
        System.arraycopy(s_CardBaseData.bBasic1Context, 0, CardBasicInfo.bBasic1Context, 0, 16);

        CardBasicInfo.lngAccountID = s_CardBaseData.lngAccountID;       //帐号	4	1～4294967296
        Log.d(TAG, String.format("帐号:%d", CardBasicInfo.lngAccountID));
        CardBasicInfo.lngPaymentPsw = s_CardBaseData.lngPaymentPsw;       //交易密码	3	六位数字密码
        CardBasicInfo.cCardStructVer = s_CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
        //基本扇区第2块内容
        System.arraycopy(s_CardBaseData.bBasic2Context, 0, CardBasicInfo.bBasic2Context, 0, 16);

        //扩展扇区
        //卡户姓名
        System.arraycopy(s_CardExtentData.cAccName, 0, CardBasicInfo.cAccName, 0, 16);
        //性别
        System.arraycopy(s_CardExtentData.cSexState, 0, CardBasicInfo.cSexState, 0, 2);
        //开户日期
        System.arraycopy(s_CardExtentData.cCreateCardDate, 0, CardBasicInfo.cCreateCardDate, 0, 3);
        CardBasicInfo.iDepartID = s_CardExtentData.iDepartID;            //部门编号
        //第三方对接关键字
        System.arraycopy(s_CardExtentData.cOtherLinkID, 0, CardBasicInfo.cOtherLinkID, 0, 10);
        //个人编号
        System.arraycopy(s_CardExtentData.cCardPerCode, 0, CardBasicInfo.cCardPerCode, 0, 16);

        CardBasicInfo.cReWriteCardFlag = s_ReWriteCardFlag;       //断点恢复标记     前4位是工作钱包    后4位是追扣钱包
        CardBasicInfo.cNOWriteCardFlag = s_NOWriteCardFlag;       //断点拔卡标记     前4位是工作钱包    后4位是追扣钱包

        //钱包信息
        //工作钱包信息
        CardBasicInfo.lngWorkBurseMoney = s_CardBurseInfo.lngBurseMoney;             //工作钱包余额
        CardBasicInfo.iWorkSubsidySID = s_CardBurseInfo.iSubsidySID;               //补助流水号
        CardBasicInfo.iWorkBurseSID = s_CardBurseInfo.iBurseSID;                    //工作钱包流水号
        CardBasicInfo.iWorkLastPayDate = s_CardBurseInfo.iLastPayDate;              //工作钱包末笔交易日期
        CardBasicInfo.lngWorkDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额
        CardBasicInfo.iWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;           //末笔交易营业号
        CardBasicInfo.iWorkDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardBurseInfo.cBurseAuthen, 0, CardBasicInfo.cWorkBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardBurseInfo.bBurseContext, 0, CardBasicInfo.bWorkBurseContext, 0, 16);  //当前钱包块的内容

        //追扣钱包信息
        CardBasicInfo.lngChaseBurseMoney = s_CardChaseBurseInfo.lngBurseMoney;            //主钱包余额
        CardBasicInfo.iChaseSubsidySID = s_CardChaseBurseInfo.iSubsidySID;               //补助流水号
        CardBasicInfo.iChaseBurseSID = s_CardChaseBurseInfo.iBurseSID;                 //主钱包流水号
        CardBasicInfo.iChaseLastPayDate = s_CardChaseBurseInfo.iLastPayDate;             //主钱包末笔交易日期
        CardBasicInfo.lngChaseDayPaymentTotal = s_CardChaseBurseInfo.lngDayPaymentTotal;        //当日消费累计额
        CardBasicInfo.iChaseLastBusinessID = s_CardChaseBurseInfo.iLastBusinessID;          //末笔交易营业号
        CardBasicInfo.iChaseDayPaymentCount = s_CardChaseBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardChaseBurseInfo.cBurseAuthen, 0, CardBasicInfo.cChaseBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardChaseBurseInfo.bBurseContext, 0, CardBasicInfo.bChaseBurseContext, 0, 16);  //当前钱包块的内容
        return 0;
    }

    //Mifare卡认证过程
    private static int CheckMifareCardProcess(CardBasicParaInfo CardBasicInfo, CardAttrInfo CardAttr) {
        int cResult;

//        //校验基础应用信息区数据
//        cResult=CheckBasicInfo(s_CardBaseData, CardAttr);
//        if(cResult!=0)
//        {
//            Log.i(TAG,"校验基础应用信息区数据失败");
//            return cResult;
//        }
//
//        //校验扩展应用信息区数据
//        cResult=CheckExtentInfo(s_CardExtentData, CardAttr);
//        if(cResult!=0)
//        {
//            Log.i(TAG,"校验扩展应用信息区数据失败");
//            return cResult;
//        }

        //校验基础扩展应用信息区数据
        cResult = CheckBasicExtentInfo(s_CardBaseData, s_CardExtentData, CardAttr);
        if (cResult != 0) {
            Log.i(TAG, "校验基础扩展应用信息区数据失败");
            return 61;
        }
        if (g_StationInfo.cWorkBurseID < 1) {
            Log.i(TAG, "工作钱包号出错");
            return 1;
        }

//        Log.i(TAG,String.format("工作钱包块号:%d,",g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID-1).cBlockID,
//                g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID-1).cBakBlockID));
//
//        Log.i(TAG,String.format("工作钱包号:%d,",g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID-1).cBurseID,
//                g_StationInfo.cWorkBurseID));
        if (g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID - 1).cBurseID != g_StationInfo.cWorkBurseID) {
            Log.i(TAG, "工作钱包号不一致");
            return 1;
        }

        s_ReWriteCardFlag = 0;
        //校验钱包信息区数据
        cResult = CheckCardBurseInfo(s_CardBurseInfo, CardAttr);
        if (cResult != 0) {
            Log.i(TAG, "校验钱包信息区数据失败");
            return cResult;
        }
        //判断是否需要读出追扣钱包
        if ((g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID - 1).cCanPermitChase == 1) && (g_StationInfo.cChaseBurseID != 0)) {
            Log.i(TAG, "读取追扣钱包数据");
            cResult = CheckCardChaseBurseInfo(s_CardChaseBurseInfo, CardAttr);
            if (cResult != 0) {
                Log.i(TAG, "校验钱包信息区数据失败");
                return cResult;
            }
        }
        //组织卡片数据
        //基本扇区
        CardBasicInfo.cAgentID = s_CardBaseData.cAgentID;                //代理号
        CardBasicInfo.iGuestID = s_CardBaseData.iGuestID;                 //客户号
        CardBasicInfo.cAuthenVer = s_CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
        //卡认证码	4
        System.arraycopy(s_CardBaseData.cCardAuthenCode, 0, CardBasicInfo.cCardAuthenCode, 0, 4);
        CardBasicInfo.cCardState = s_CardBaseData.cCardState;         //卡片状态	1
        CardBasicInfo.lngCardID = s_CardBaseData.lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
        CardBasicInfo.cCampusID = s_CardBaseData.cCampusID;         //园区号	1	范围为1~250
        CardBasicInfo.cStatusID = s_CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64
        CardBasicInfo.iValidTime = s_CardBaseData.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
        //基本扇区第1块内容
        System.arraycopy(s_CardBaseData.bBasic1Context, 0, CardBasicInfo.bBasic1Context, 0, 16);

        CardBasicInfo.lngAccountID = s_CardBaseData.lngAccountID;       //帐号	4	1～4294967296
        Log.d(TAG, String.format("帐号:%d", CardBasicInfo.lngAccountID));
        CardBasicInfo.lngPaymentPsw = s_CardBaseData.lngPaymentPsw;       //交易密码	3	六位数字密码
        CardBasicInfo.cCardStructVer = s_CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
        //基本扇区第2块内容
        System.arraycopy(s_CardBaseData.bBasic2Context, 0, CardBasicInfo.bBasic2Context, 0, 16);

        //扩展扇区
        //卡户姓名
        System.arraycopy(s_CardExtentData.cAccName, 0, CardBasicInfo.cAccName, 0, 16);
        //性别
        System.arraycopy(s_CardExtentData.cSexState, 0, CardBasicInfo.cSexState, 0, 2);
        //开户日期
        System.arraycopy(s_CardExtentData.cCreateCardDate, 0, CardBasicInfo.cCreateCardDate, 0, 3);
        CardBasicInfo.iDepartID = s_CardExtentData.iDepartID;            //部门编号
        //第三方对接关键字
        System.arraycopy(s_CardExtentData.cOtherLinkID, 0, CardBasicInfo.cOtherLinkID, 0, 10);
        //个人编号
        System.arraycopy(s_CardExtentData.cCardPerCode, 0, CardBasicInfo.cCardPerCode, 0, 16);

        CardBasicInfo.cReWriteCardFlag = s_ReWriteCardFlag;       //断点恢复标记     前4位是工作钱包    后4位是追扣钱包
        CardBasicInfo.cNOWriteCardFlag = s_NOWriteCardFlag;       //断点拔卡标记     前4位是工作钱包    后4位是追扣钱包

        //钱包信息
        //工作钱包信息
        CardBasicInfo.lngWorkBurseMoney = s_CardBurseInfo.lngBurseMoney;             //工作钱包余额
        CardBasicInfo.iWorkSubsidySID = s_CardBurseInfo.iSubsidySID;               //补助流水号
        CardBasicInfo.iWorkBurseSID = s_CardBurseInfo.iBurseSID;                    //工作钱包流水号
        CardBasicInfo.iWorkLastPayDate = s_CardBurseInfo.iLastPayDate;              //工作钱包末笔交易日期
        CardBasicInfo.lngWorkDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额
        CardBasicInfo.iWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;           //末笔交易营业号
        CardBasicInfo.iWorkDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardBurseInfo.cBurseAuthen, 0, CardBasicInfo.cWorkBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardBurseInfo.bBurseContext, 0, CardBasicInfo.bWorkBurseContext, 0, 16);  //当前钱包块的内容

        //追扣钱包信息
        CardBasicInfo.lngChaseBurseMoney = s_CardChaseBurseInfo.lngBurseMoney;            //主钱包余额
        CardBasicInfo.iChaseSubsidySID = s_CardChaseBurseInfo.iSubsidySID;               //补助流水号
        CardBasicInfo.iChaseBurseSID = s_CardChaseBurseInfo.iBurseSID;                 //主钱包流水号
        CardBasicInfo.iChaseLastPayDate = s_CardChaseBurseInfo.iLastPayDate;             //主钱包末笔交易日期
        CardBasicInfo.lngChaseDayPaymentTotal = s_CardChaseBurseInfo.lngDayPaymentTotal;        //当日消费累计额
        CardBasicInfo.iChaseLastBusinessID = s_CardChaseBurseInfo.iLastBusinessID;          //末笔交易营业号
        CardBasicInfo.iChaseDayPaymentCount = s_CardChaseBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardChaseBurseInfo.cBurseAuthen, 0, CardBasicInfo.cChaseBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardChaseBurseInfo.bBurseContext, 0, CardBasicInfo.bChaseBurseContext, 0, 16);  //当前钱包块的内容

        return 0;
    }

    //校验基础扩展应用信息区数据
    private static int CheckBasicExtentInfo(CardBaseInfo CardBaseData, CardExtentInfo CardExtentData, CardAttrInfo CardAttr) {
        int i;
        int cResult;

        Log.i(TAG, "读取基础扩展应用信息区数据");
        //读取基础应用信息区数据
        //long lngStart=System.currentTimeMillis();
        cResult = ReadBaicExtentInfoData(CardBaseData, CardExtentData, CardAttr);
        //Log.e(TAG,String.format("=========读取基础扩展应用信息区数据时长:%d==========",(System.currentTimeMillis()-lngStart)));
        if (cResult != 0) {
            Log.i(TAG, "读取基础扩展应用信息区数据失败");
            return 61;
        }
        return 0;
    }

    //校验基础应用信息区数据
    private static int CheckBasicInfo(CardBaseInfo CardBaseData, CardAttrInfo CardAttr) {
        int i;
        int cResult;

        Log.i(TAG, "读取基础应用信息区数据");
        //读取基础应用信息区数据
        //long lngStart=System.currentTimeMillis();
        cResult = ReadBaseInfoData(CardBaseData, CardAttr);
        //Log.e(TAG,String.format("=========读取基础应用信息区数据时长:%d==========",(System.currentTimeMillis()-lngStart)));
        if (cResult != 0) {
            Log.i(TAG, "读取基础应用信息区数据失败");
            return 61;
        }
        return 0;
    }

    //校验持卡人应用信息区
    private static int CheckExtentInfo(CardExtentInfo CardExtentData, CardAttrInfo CardAttr) {
        int cResult;

        //读取持卡人应用信息区数据
        //long lngStart=System.currentTimeMillis();
        cResult = ReadExtentInfoData(CardExtentData, CardAttr);
        //Log.e(TAG,String.format("=========校验持卡人应用信息区数据时长:%d==========",(System.currentTimeMillis()-lngStart)));

        if (cResult != 0) {
            Log.i(TAG, "校验持卡人应用信息区数据失败");
            return 61;
        }
        return 0;
    }

    //校验钱包应用信息区
    private static int CheckCardBurseInfo(CardBurseInfo CardBurseData, CardAttrInfo CardAttr) {
        int cResult;

        //读取钱包应用信息区数据
        //long lngStart=System.currentTimeMillis();
        cResult = ReadBurseInfoData(CardBurseData, CardAttr);
        //Log.e(TAG,String.format("=========校验钱包应用信息区数据时长:%d==========",(System.currentTimeMillis()-lngStart)));
        if (cResult != 0) {
            Log.i(TAG, "校验钱包应用信息区数据失败");
            return 61;
        }

        //判断是否与当前时间是同一天
        cResult = CardPublic.CompareDateSame(CardBurseData.iLastPayDate, s_cCurDateTime);
        if (cResult == 0) {
            Log.i(TAG, String.format("日累次数:%d, 日累金额:%d, 末笔交易时段号:" +
                    CardBurseData.iDayPaymentCount, CardBurseData.lngDayPaymentTotal, CardBurseData.iLastBusinessID));
        } else {
            Log.i(TAG, "不是同一天");
            //末笔交易营业号
            CardBurseData.iLastBusinessID = 0;
            //当日消费累计额
            CardBurseData.lngDayPaymentTotal = 0;
            //消费次数写0
            CardBurseData.iDayPaymentCount = 0;
        }
        Log.i(TAG, "读钱包应用信息区数据成功");
        return 0;
    }

    //校验追扣钱包应用信息区
    private static int CheckCardChaseBurseInfo(CardBurseInfo CardBurseData, CardAttrInfo CardAttr) {
        int cResult;

        //读取钱包应用信息区数据
        //long lngStart=System.currentTimeMillis();
        cResult = ReadChaseBurseInfoData(CardBurseData, CardAttr);
        //Log.e(TAG,String.format("=========校验追扣钱包应用信息区时长:%d==========",(System.currentTimeMillis()-lngStart)));
        if (cResult != 0) {
            Log.i(TAG, "校验钱包应用信息区数据失败");
            return 61;
        }
        //判断是否与当前时间是同一天
        cResult = CardPublic.CompareDateSame(CardBurseData.iLastPayDate, s_cCurDateTime);
        if (cResult == 0) {
            Log.i(TAG, String.format("追扣-->日累次数:%d, 日累金额:%d, 末笔交易时段号:" +
                    CardBurseData.iDayPaymentCount, CardBurseData.lngDayPaymentTotal, CardBurseData.iLastBusinessID));
        } else {
            //末笔交易营业号
            CardBurseData.iLastBusinessID = 0;
            //当日消费累计额
            CardBurseData.lngDayPaymentTotal = 0;
            //消费次数写0
            CardBurseData.iDayPaymentCount = 0;
        }
        return 0;
    }

    //读取基础扩展应用信息区数据
    private static int ReadBaicExtentInfoData(CardBaseInfo CardBaseData, CardExtentInfo CardExtentData, CardAttrInfo CardAttr) {
        int i;
        int cResult;
        byte cCardType;
        byte cBSectorID, cESectorID;
        byte[] cCardSID = new byte[8];
        byte cLen;
        long wTemp;
        int iCreateCardDate;                //开户日期
        byte[] bBasic1Context = new byte[16];            //基本扇区第1块内容
        byte[] bBasic2Context = new byte[16];            //基本扇区第2块内容

        byte[] bExtent1Context = new byte[16];            //扩展扇区第1块内容
        byte[] bExtent2Context = new byte[16];            //扩展扇区第2块内容
        byte[] bExtent3Context = new byte[16];            //扩展扇区第3块内容

        byte[][] cCardContext = new byte[3][16];
        byte[] cCardContextDate = new byte[64];
        byte[] cCardEContextDate = new byte[64];

        System.arraycopy(CardAttr.cCardSID, 0, cCardSID, 0, 4);
        cCardType = CardAttr.cCardType;
        cBSectorID = CardAttr.cBasicSectorID;
        cESectorID = CardAttr.cExtendSectorID;
        if (cCardType == 1)    //M1
        {
            //基本扇区
            cResult = g_Nlib.ReadMifareSector(cBSectorID, cCardSID, cCardContext);
            if (cResult != 0) {
                Log.i(TAG, "读失败");
                return 2;
            }
            System.arraycopy(cCardContext[1], 0, bBasic1Context, 0, 16);
            System.arraycopy(cCardContext[2], 0, bBasic2Context, 0, 16);

            //扩展扇区
            cResult = g_Nlib.ReadMifareSector(cESectorID, cCardSID, cCardContext);
            if (cResult != 0) {
                Log.i(TAG, "读失败");
                return 2;
            }
            System.arraycopy(cCardContext[0], 0, bExtent1Context, 0, 16);
            System.arraycopy(cCardContext[1], 0, bExtent2Context, 0, 16);
            System.arraycopy(cCardContext[2], 0, bExtent3Context, 0, 16);
        }
        else if (cCardType == 2)    //CPU
        {
            cResult = g_Nlib.CPU_BasicExternInfo_Get(cCardContextDate, cCardEContextDate, cCardSID);
            if (cResult != 0) {
                Log.i(TAG, "读失败："+cResult);
                return 66;
            }
            System.arraycopy(cCardContextDate, 0, bBasic1Context, 0, 16);
            System.arraycopy(cCardContextDate, 16, bBasic2Context, 0, 16);

            System.arraycopy(cCardEContextDate, 0, bExtent1Context, 0, 16);
            System.arraycopy(cCardEContextDate, 16, bExtent2Context, 0, 16);
            System.arraycopy(cCardEContextDate, 32, bExtent3Context, 0, 16);
        }
        //数据解析
        cLen = 0;
        if ((cCardType == 1) || (cCardType == 2)) {
            CardBaseData.cAgentID = (bBasic1Context[cLen++] & 0xff);                //代理号   1

            wTemp = (bBasic1Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic1Context[cLen++] & 0xff) * 256;
            CardBaseData.iGuestID = (int) wTemp;               //客户号     2

            CardBaseData.cAuthenVer = (bBasic1Context[cLen++] & 0xff);             //认证版本号	1	范围为0~250，缺省为0
            System.arraycopy(bBasic1Context, cLen, CardBaseData.cCardAuthenCode, 0, 4);    //卡认证码	4
            cLen = (byte) (cLen + 4);

            CardBaseData.cCardState = (byte) (bBasic1Context[cLen++] & 0xff);         //卡片状态	1

            wTemp = (bBasic1Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic1Context[cLen++] & 0xff) * 256;
            wTemp = wTemp + (bBasic1Context[cLen++] & 0xff) * 256 * 256;
            CardBaseData.lngCardID = wTemp;          //卡内编号	3

            CardBaseData.cCampusID = (bBasic1Context[cLen++] & 0xff);         //园区号	1

            CardBaseData.cStatusID = (bBasic1Context[cLen++] & 0xff);          //身份编号	1	最大64种，1~64

            wTemp = (bBasic1Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic1Context[cLen++] & 0xff) * 256;
            CardBaseData.iValidTime = (int) wTemp;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)

            System.arraycopy(bBasic1Context, 0, CardBaseData.bBasic1Context, 0, 16); //基本扇区第1块内容

            cLen = 0;
            wTemp = (bBasic2Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256;
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256 * 256;
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256 * 256 * 256;
            CardBaseData.lngAccountID = wTemp;       //帐号	4

            cLen = (byte) (cLen + 4);    //保留位

            wTemp = (bBasic2Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256;
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256 * 256;
            CardBaseData.lngPaymentPsw = wTemp;       //交易密码	3	六位数字密码

            CardBaseData.cCardStructVer = (byte) (bBasic2Context[cLen++] & 0xff);      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
            cLen = (byte) (cLen + 4);    //保留位
            System.arraycopy(bBasic2Context, 0, CardBaseData.bBasic2Context, 0, 16);            //基本扇区第2块内容

            //--------------扩展扇区1----------------
            cLen = 0;
            System.arraycopy(bExtent1Context, 0, CardExtentData.cAccName, 0, 16); //卡户姓名

            //扩展扇区2
            cLen = 0;
            CardExtentData.cSexState[0] = (byte) (bExtent2Context[cLen++] & 0xff);
            CardExtentData.cSexState[1] = (byte) (bExtent2Context[cLen++] & 0xff);//性别 2

            iCreateCardDate = (bExtent2Context[cLen++] & 0xff);
            iCreateCardDate = iCreateCardDate + (bExtent2Context[cLen++] & 0xff) * 256;//开户日期 2

            //年
            CardExtentData.cCreateCardDate[0] = (byte) ((iCreateCardDate & 0x7E00) >> 9);
            //月
            CardExtentData.cCreateCardDate[1] = (byte) ((iCreateCardDate & 0x01E0) >> 5);
            //日
            CardExtentData.cCreateCardDate[2] = (byte) (iCreateCardDate & 0x001F);

            wTemp = (bExtent2Context[cLen++] & 0xff);
            wTemp = wTemp + (bExtent2Context[cLen++] & 0xff) * 256;
            CardExtentData.iDepartID = (int) wTemp;            //部门编号 2

            System.arraycopy(bExtent2Context, cLen, CardExtentData.cOtherLinkID, 0, 10); //第三方对接关键字 10
            System.arraycopy(bExtent3Context, 0, CardExtentData.cCardPerCode, 0, 16);    //个人编号
        } else {
        }
        return 0;
    }

    //读取基础应用信息区数据
    private static int ReadBaseInfoData(CardBaseInfo CardBaseData, CardAttrInfo CardAttr) {
        int i;
        int cResult;
        byte cCardType;
        byte cSectorID;
        byte[] cCardSID = new byte[8];
        byte cLen;
        long wTemp;
        byte[] bBasic1Context = new byte[16];            //基本扇区第1块内容
        byte[] bBasic2Context = new byte[16];            //基本扇区第2块内容
        byte[][] cCardContext = new byte[3][16];
        byte[] cCardContextDate = new byte[64];

        System.arraycopy(CardAttr.cCardSID, 0, cCardSID, 0, 4);
        cCardType = CardAttr.cCardType;
        cSectorID = CardAttr.cBasicSectorID;

        if (cCardType == 1)    //M1
        {
            cResult = g_Nlib.ReadMifareSector(cSectorID, cCardSID, cCardContext);
            if (cResult != 0) {
                Log.i(TAG, "读失败");
                return 2;
            }
            System.arraycopy(cCardContext[1], 0, bBasic1Context, 0, 16);
            System.arraycopy(cCardContext[2], 0, bBasic2Context, 0, 16);
        } else if (cCardType == 2)    //CPU
        {
            cResult = g_Nlib.CPU_BasicInfo_Get(cCardContextDate, cCardSID);
            if (cResult != 0) {
                return 66;
            }
            System.arraycopy(cCardContextDate, 0, bBasic1Context, 0, 16);
            System.arraycopy(cCardContextDate, 16, bBasic2Context, 0, 16);
        }

        //数据解析
        cLen = 0;
        if ((cCardType == 1) || (cCardType == 2)) {
            CardBaseData.cAgentID = (bBasic1Context[cLen++] & 0xff);                //代理号   1

            wTemp = (bBasic1Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic1Context[cLen++] & 0xff) * 256;
            CardBaseData.iGuestID = (int) wTemp;               //客户号     2

            CardBaseData.cAuthenVer = (bBasic1Context[cLen++] & 0xff);             //认证版本号	1	范围为0~250，缺省为0
            System.arraycopy(bBasic1Context, cLen, CardBaseData.cCardAuthenCode, 0, 4);    //卡认证码	4
            cLen = (byte) (cLen + 4);

            CardBaseData.cCardState = (byte) (bBasic1Context[cLen++] & 0xff);         //卡片状态	1

            wTemp = (bBasic1Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic1Context[cLen++] & 0xff) * 256;
            wTemp = wTemp + (bBasic1Context[cLen++] & 0xff) * 256 * 256;
            CardBaseData.lngCardID = wTemp;          //卡内编号	3

            CardBaseData.cCampusID = (bBasic1Context[cLen++] & 0xff);         //园区号	1

            CardBaseData.cStatusID = (bBasic1Context[cLen++] & 0xff);          //身份编号	1	最大64种，1~64

            wTemp = (bBasic1Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic1Context[cLen++] & 0xff) * 256;
            CardBaseData.iValidTime = (int) wTemp;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)

            System.arraycopy(bBasic1Context, 0, CardBaseData.bBasic1Context, 0, 16); //基本扇区第1块内容

            cLen = 0;
            wTemp = (bBasic2Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256;
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256 * 256;
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256 * 256 * 256;
            CardBaseData.lngAccountID = wTemp;       //帐号	4

            cLen = (byte) (cLen + 4);    //保留位

            wTemp = (bBasic2Context[cLen++] & 0xff);
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256;
            wTemp = wTemp + (bBasic2Context[cLen++] & 0xff) * 256 * 256;
            CardBaseData.lngPaymentPsw = wTemp;       //交易密码	3	六位数字密码

            CardBaseData.cCardStructVer = (byte) (bBasic2Context[cLen++] & 0xff);      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0

            cLen = (byte) (cLen + 4);    //保留位

            System.arraycopy(bBasic2Context, 0, CardBaseData.bBasic2Context, 0, 16);            //基本扇区第2块内容
        } else {
        }
        return 0;
    }

    //写基础应用信息区数据
    private static int WriteBaseInfoData(CardBaseInfo CardBaseData, CardAttrInfo CardAttr) {
        int i;
        int cResult;
        byte cCardType;
        byte cSectorID;
        byte[] cCardSID = new byte[8];
        byte cLen;

        byte[] cCardDataTemp = new byte[64];
        byte[][] cCardContext = new byte[3][16];

        System.arraycopy(CardAttr.cCardSID, 0, cCardSID, 0, 4);
        cCardType = CardAttr.cCardType;
        cSectorID = CardAttr.cBasicSectorID;

        if (cCardType == 1)    //M1
        {
            //第二块
            cLen = 0;

            cCardDataTemp[cLen++] = (byte) CardBaseData.cAgentID;                //代理号   1

            cCardDataTemp[cLen++] = (byte) (CardBaseData.iGuestID & 0x00ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.iGuestID & 0xff00) >> 8);             //客户号     2

            cCardDataTemp[cLen++] = (byte) CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0

            System.arraycopy(CardBaseData.cCardAuthenCode, 0, cCardDataTemp, cLen, 4);    //卡认证码	4
            cLen = (byte) (cLen + 4);

            cCardDataTemp[cLen++] = CardBaseData.cCardState;         //卡片状态	1

            cCardDataTemp[cLen++] = (byte) (CardBaseData.lngCardID & 0x0000ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngCardID & 0x00ff00) >> 8);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngCardID & 0xff0000) >> 16); //卡内编号	3

            cCardDataTemp[cLen++] = (byte) CardBaseData.cCampusID;         //园区号	1

            cCardDataTemp[cLen++] = (byte) CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64

            cCardDataTemp[cLen++] = (byte) (CardBaseData.iValidTime & 0x0000ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.iValidTime & 0x00ff00) >> 8);//有效时限	2	范围00~63年(7位)月(4位)日(5位)

            System.arraycopy(cCardDataTemp, 0, cCardContext[1], 0, 16); //基本扇区第1块内容

            //第三块
            cLen = 0;
            System.arraycopy(CardBaseData.bBasic2Context, 0, cCardDataTemp, 0, 16);
            cLen = 4;
            cCardDataTemp[cLen++] = (byte) (CardBaseData.lngAccountID & 0x000000ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngAccountID & 0x0000ff00) >> 8);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngAccountID & 0x00ff0000) >> 16);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngAccountID & 0xff000000) >> 24);    //主卡帐号	4

            cLen = (byte) (cLen + 4);    //保留位

            cCardDataTemp[cLen++] = (byte) (CardBaseData.lngPaymentPsw & 0x000000ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngPaymentPsw & 0x0000ff00) >> 8);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngPaymentPsw & 0x00ff0000) >> 16);   //交易密码	3	六位数字密码

            cCardDataTemp[cLen++] = CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0

            cLen = (byte) (cLen + 4);    //保留位

            System.arraycopy(cCardDataTemp, 0, cCardContext[2], 0, 16); //基本扇区第1块内容

            //写Mifare卡片扇区数据
            cResult = g_Nlib.WriteMifareSector(cSectorID, cCardSID, cCardContext);
            if (cResult != 0) {
                Log.i(TAG, "写失败");
                return 2;
            }
        } else if (cCardType == 2)    //CPU
        {
            //第二块
            cLen = 0;

            cCardDataTemp[cLen++] = (byte) CardBaseData.cAgentID;                //代理号   1

            cCardDataTemp[cLen++] = (byte) (CardBaseData.iGuestID & 0x00ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.iGuestID & 0xff00) >> 8);             //客户号     2

            cCardDataTemp[cLen++] = (byte) CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0

            System.arraycopy(CardBaseData.cCardAuthenCode, 0, cCardDataTemp, cLen, 4);    //卡认证码	4
            cLen = (byte) (cLen + 4);

            cCardDataTemp[cLen++] = CardBaseData.cCardState;         //卡片状态	1

            cCardDataTemp[cLen++] = (byte) (CardBaseData.lngCardID & 0x0000ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngCardID & 0x00ff00) >> 8);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngCardID & 0xff0000) >> 16); //卡内编号	3

            cCardDataTemp[cLen++] = (byte) CardBaseData.cCampusID;         //园区号	1

            cCardDataTemp[cLen++] = (byte) CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64

            cCardDataTemp[cLen++] = (byte) (CardBaseData.iValidTime & 0x0000ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.iValidTime & 0x00ff00) >> 8);//有效时限	2	范围00~63年(7位)月(4位)日(5位)

            //第三块
            System.arraycopy(CardBaseData.bBasic2Context, 0, cCardDataTemp, cLen, 16);
            cLen += 4;//2016.1107
            cCardDataTemp[cLen++] = (byte) (CardBaseData.lngAccountID & 0x000000ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngAccountID & 0x0000ff00) >> 8);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngAccountID & 0x00ff0000) >> 16);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngAccountID & 0xff000000) >> 24);    //主卡帐号	4

            //cLen=cLen+4;    //保留位

            cCardDataTemp[cLen++] = (byte) (CardBaseData.lngPaymentPsw & 0x000000ff);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngPaymentPsw & 0x0000ff00) >> 8);
            cCardDataTemp[cLen++] = (byte) ((CardBaseData.lngPaymentPsw & 0x00ff0000) >> 16);   //交易密码	3	六位数字密码

            cResult = g_Nlib.CPU_BasicAllInfo_Set(cCardDataTemp, cCardSID);
            if (cResult != 0) {
                Log.i(TAG, "CPU--写失败");
                return 66;
            }
        } else        //其他卡类型
        {
        }
        return 0;
    }

    //读取扩展信息区数据
    private static int ReadExtentInfoData(CardExtentInfo CardExtentData, CardAttrInfo CardAttr) {
        int i;
        int cResult;
        byte cCardType;
        byte cSectorID;
        byte[] cCardSID = new byte[8];
        byte cLen;
        long lngTemp;
        int iCreateCardDate;                //开户日期
        byte[] bExtent1Context = new byte[16];            //扩展扇区第1块内容
        byte[] bExtent2Context = new byte[16];            //扩展扇区第2块内容
        byte[] bExtent3Context = new byte[16];            //扩展扇区第3块内容
        byte[][] cCardContext = new byte[3][16];
        byte[] cCardContextDate = new byte[64];

        System.arraycopy(CardAttr.cCardSID, 0, cCardSID, 0, 4);
        cCardType = CardAttr.cCardType;
        cSectorID = CardAttr.cExtendSectorID;

        if (cCardType == 1)    //M1
        {
            cResult = g_Nlib.ReadMifareSector(cSectorID, cCardSID, cCardContext);
            if (cResult != 0) {
                Log.i(TAG, "读失败");
                return 2;
            }
            System.arraycopy(cCardContext[0], 0, bExtent1Context, 0, 16);
            System.arraycopy(cCardContext[1], 0, bExtent2Context, 0, 16);
            System.arraycopy(cCardContext[2], 0, bExtent3Context, 0, 16);
        } else if (cCardType == 2)    //CPU
        {
            cResult = g_Nlib.CPU_ExternInfo_Get(cCardContextDate, cCardSID);
            if (cResult != 0) {
                return 66;
            }
            System.arraycopy(cCardContextDate, 0, bExtent1Context, 0, 16);
            System.arraycopy(cCardContextDate, 16, bExtent2Context, 0, 16);
            System.arraycopy(cCardContextDate, 32, bExtent3Context, 0, 16);
        }
        if ((cCardType == 1) || (cCardType == 2)) {
            //扩展扇区1
            cLen = 0;
            System.arraycopy(bExtent1Context, 0, CardExtentData.cAccName, 0, 16); //卡户姓名

            //扩展扇区2
            cLen = 0;
            CardExtentData.cSexState[0] = (byte) (bExtent2Context[cLen++] & 0xff);
            CardExtentData.cSexState[1] = (byte) (bExtent2Context[cLen++] & 0xff);//性别 2

            iCreateCardDate = (bExtent2Context[cLen++] & 0xff);
            iCreateCardDate = iCreateCardDate + (bExtent2Context[cLen++] & 0xff) * 256;//开户日期 2

            //年
            CardExtentData.cCreateCardDate[0] = (byte) ((iCreateCardDate & 0x7E00) >> 9);
            //月
            CardExtentData.cCreateCardDate[1] = (byte) ((iCreateCardDate & 0x01E0) >> 5);
            //日
            CardExtentData.cCreateCardDate[2] = (byte) (iCreateCardDate & 0x001F);

            lngTemp = (bExtent2Context[cLen++] & 0xff);
            lngTemp = lngTemp + (bExtent2Context[cLen++] & 0xff) * 256;
            CardExtentData.iDepartID = (int) lngTemp;            //部门编号 2

            System.arraycopy(bExtent2Context, cLen, CardExtentData.cOtherLinkID, 0, 10); //第三方对接关键字 10
            System.arraycopy(bExtent3Context, 0, CardExtentData.cCardPerCode, 0, 16);    //个人编号
        } else {
        }
        return 0;
    }

    //写扩展信息区数据
    private static int WriteExtentInfoData(CardExtentInfo CardExtentData, CardAttrInfo CardAttr) {
        int i;
        int cResult;
        byte cCardType;
        byte cSectorID;
        byte[] cCardSID = new byte[8];
        byte cLen;
        byte[] bTempContext = new byte[16];
        int iYear, iMonth, iDay;
        byte[][] cCardContext = new byte[3][16];
        byte[] cCardContextDate = new byte[64];

        System.arraycopy(CardAttr.cCardSID, 0, cCardSID, 0, 8);
        cCardType = CardAttr.cCardType;
        cSectorID = CardAttr.cExtendSectorID;

        if (cCardType == 1)    //M1
        {
            cLen = 0;
            //第一块
            cLen = 0;
            System.arraycopy(CardExtentData.cAccName, 0, cCardContext[0], 0, 16);    //卡户姓名

            //第二块
            cLen = 0;
            cCardContext[1][cLen++] = CardExtentData.cSexState[0];
            cCardContext[1][cLen++] = CardExtentData.cSexState[1];//性别 2

            iYear = CardExtentData.cCreateCardDate[0];
            iMonth = CardExtentData.cCreateCardDate[1];
            iDay = CardExtentData.cCreateCardDate[2];
            bTempContext[1] = (byte) (iYear << 1);
            if (iMonth >= 8) {
                bTempContext[1] = (byte) (bTempContext[1] + 1);
            }
            bTempContext[0] = (byte) (iMonth << 5);
            bTempContext[0] = (byte) (bTempContext[0] + iDay);

            cCardContext[1][cLen++] = bTempContext[0];
            cCardContext[1][cLen++] = bTempContext[1];//开户日期 2

            cCardContext[1][cLen++] = (byte) (CardExtentData.iDepartID & 0x00ff);
            cCardContext[1][cLen++] = (byte) ((CardExtentData.iDepartID & 0xff00) >> 8);//部门编号 2

            System.arraycopy(CardExtentData.cOtherLinkID, 0, cCardContext[1], cLen, 10); //第三方对接关键字 10

            //第三块
            System.arraycopy(CardExtentData.cCardPerCode, 0, cCardContext[2], 0, 16);    //个人编号

            //写Mifare卡片扇区数据
            cResult = g_Nlib.WriteMifareSector(cSectorID, cCardSID, cCardContext);
            if (cResult != 0) {
                Log.i(TAG, "写失败");
                return 2;
            }
        } else if (cCardType == 2)    //CPU
        {
            //第一块
            cLen = 0;
            System.arraycopy(CardExtentData.cAccName, 0, cCardContextDate, 0, 16);    //卡户姓名

            //第二块
            cLen = 16;
            cCardContextDate[cLen++] = CardExtentData.cSexState[0];
            cCardContextDate[cLen++] = CardExtentData.cSexState[1];//性别 2

            iYear = CardExtentData.cCreateCardDate[0];
            iMonth = CardExtentData.cCreateCardDate[1];
            iDay = CardExtentData.cCreateCardDate[2];
            bTempContext[1] = (byte) (iYear << 1);
            if (iMonth >= 8) {
                bTempContext[1] = (byte) (bTempContext[1] + 1);
            }
            bTempContext[0] = (byte) (iMonth << 5);
            bTempContext[0] = (byte) (bTempContext[0] + iDay);

            cCardContextDate[cLen++] = bTempContext[0];
            cCardContextDate[cLen++] = bTempContext[1];//开户日期 2

            cCardContextDate[cLen++] = (byte) (CardExtentData.iDepartID & 0x00ff);
            cCardContextDate[cLen++] = (byte) ((CardExtentData.iDepartID & 0xff00) >> 8);//部门编号 2

            System.arraycopy(CardExtentData.cOtherLinkID, 0, cCardContextDate, cLen, 10); //第三方对接关键字 10

            //第三块
            cLen = 32;
            System.arraycopy(CardExtentData.cCardPerCode, 0, cCardContextDate, cLen, 16);    //个人编号

            //写CPU卡片扩展数据
            cResult = g_Nlib.CPU_ExternAllInfo_Set(cCardContextDate, cCardSID);
            if (cResult != 0) {
                Log.i(TAG, "CPU--写失败");
                return 66;
            }
        } else        //其他卡类型
        {
        }

        return 0;
    }

    //读取钱包交易信息区数据
    private static int ReadBurseInfoData(CardBurseInfo CardBurseData, CardAttrInfo CardAttr) {
        int i;
        byte cLen;
        int cResult;
        byte cBurseID;
        byte cCardType;
        byte[] cCardSID = new byte[8];
        long lngMoneyTemp;
        int iBurseNoteID;
        int iSubsidySID;
        long lngTemp;
        byte cBlockID;
        byte cIsBlockID;      //判断是正本还是副本 0:正 1:副
        byte[] cBurseAuthen = new byte[3];
        byte[] cBurseData = new byte[16];
        byte[] cBurseOneData = new byte[16];//正本
        byte[] cBurseBakData = new byte[16];//副本
        byte[][] cCardContext = new byte[3][16];
        byte[] cCardContextDate = new byte[64];
        byte[] cTempContext = new byte[16];

        cBurseID = CardAttr.cWorkBurseID;
        if (cBurseID < 1) {
            Log.i(TAG, "工作钱包号出错");
            return 1;
        }
        Log.i(TAG, String.format("工作钱包块号:%d,%d", g_EP_BurseInfo.get(cBurseID - 1).cBlockID,
                g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID));

        if (cBurseID > 8) {
            Log.i(TAG, "钱包号超出范围");
            return 1;
        }
        System.arraycopy(CardAttr.cCardSID, 0, cCardSID, 0, 8);
        cCardType = CardAttr.cCardType;

        if (cCardType == 1)    //M1
        {
            //判断是否钱包块号在同一扇区
            if ((g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4) == (g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID / 4)) {
                cResult = (byte) g_Nlib.ReadMifareSector((byte) (g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4), cCardSID, cCardContext);
                if (cResult != 0) {
                    Log.i(TAG, "读失败");
                    return 2;
                }

                cBlockID = (byte) (g_EP_BurseInfo.get(cBurseID - 1).cBlockID % 4);
                System.arraycopy(cCardContext[cBlockID], 0, cBurseOneData, 0, 16);//正本

                cBlockID = (byte) (g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID % 4);
                System.arraycopy(cCardContext[cBlockID], 0, cBurseBakData, 0, 16);//备本
            } else {
                Log.i(TAG, "钱包块号不在同一扇区");
                cResult = (byte) g_Nlib.ReadMifareBlock((g_EP_BurseInfo.get(cBurseID - 1).cBlockID), cCardSID, cBurseOneData);//正本
                if (cResult != 0) {
                    Log.i(TAG, "读失败");
                    return 2;
                }

                cResult = (byte) g_Nlib.ReadMifareBlock((g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID), cCardSID, cBurseBakData);//备本
                if (cResult != 0) {
                    Log.i(TAG, "读失败");
                    return 2;
                }
            }
        } else if (cCardType == 2)    //CPU
        {
            cResult = g_Nlib.CPU_BurseInfo_Get((char) cBurseID, cCardContextDate, cCardSID);
            if (cResult != 0) {
                Log.i(TAG, "CPU_BurseInfo_Get 失败:"+cResult);
                return 66;
            }
            System.arraycopy(cCardContextDate, 0, cBurseData, 0, 16);
        } else {
            return 1;
        }
        //数据解析
        if (cCardType == 1) {
        /*
        钱包余额	3	-8000000~8000000,以分为单位
        补助流水号	2	0~65535
        钱包流水号	2	0~65535
        末笔交易日期	2	范围00~63年(6位)月(4位)日(5位)
        当日消费累计额	2	0~65535，以分为单位
        末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
        当日消费累计次	1	0～255（当超过255不允许交易）
        钱包认证码	3	卡号+代理序号+客户序号+钱包余额+扇区号(1~4)+钱包流水号计算得出
        */
            //判断正备本数据
            cIsBlockID = 0;
            if (Arrays.equals(cBurseOneData, cBurseBakData)) {
                Log.i(TAG, "正备本数据相同");
                System.arraycopy(cBurseOneData, 0, cBurseData, 0, 16);
                cIsBlockID = 0;
            } else {
                Log.i(TAG, "正备本数据不相同,以副本为准");
                if (Arrays.equals(cBurseBakData, cTempContext)) {
                    Log.i(TAG, "副本数据为%02x,以正本为准" + cBurseBakData[0]);
                    System.arraycopy(cBurseOneData, 0, cBurseData, 0, 16);
                    cIsBlockID = 2;
                } else {
                    System.arraycopy(cBurseBakData, 0, cBurseData, 0, 16);
                    cIsBlockID = 1;
                }
            }

            cLen = 0;
            // 主钱包的余额(在这里正负的处理)
            byte[] bytesTemp = new byte[4];
            System.arraycopy(cBurseData, cLen, bytesTemp, 0, 4);
            lngMoneyTemp = CardPublic.TransMoney(bytesTemp);
            cLen = (byte) (cLen + 3);
            CardBurseData.lngBurseMoney = (int) lngMoneyTemp;        //钱包余额	3		单位分，考虑正负数
            Log.i(TAG, "钱包余额:" + CardBurseData.lngBurseMoney);

            iSubsidySID = (cBurseData[cLen++] & 0xff);
            iSubsidySID = iSubsidySID + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iSubsidySID = iSubsidySID;                //补助版本号	2

            iBurseNoteID = (cBurseData[cLen++] & 0xff);
            iBurseNoteID = iBurseNoteID + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iBurseSID = iBurseNoteID;            //钱包流水号	2

            lngTemp = (cBurseData[cLen++] & 0xff);
            lngTemp = lngTemp + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iLastPayDate = (int) lngTemp;    //末笔交易日期	2		6位年4位月5位日

            lngTemp = (cBurseData[cLen++] & 0xff);
            lngTemp = lngTemp + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.lngDayPaymentTotal = lngTemp;        //日累金额	2		0~65535，以分为单位

            CardBurseData.iLastBusinessID = (cBurseData[cLen++] & 0xff); //末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)

            CardBurseData.iDayPaymentCount = (cBurseData[cLen++] & 0xff);        //日累次数	1

            System.arraycopy(cBurseData, cLen, CardBurseData.cBurseAuthen, 0, 3);//钱包认证码

            //获取钱包认证码
            g_Nlib.UCardBurAuthen(cCardSID,
                    g_SystemInfo.cAgentID,
                    g_SystemInfo.iGuestID,
                    g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4,
                    lngMoneyTemp,
                    iBurseNoteID,
                    cBurseAuthen);
            //验证钱包论证码
            if (!Arrays.equals(CardBurseData.cBurseAuthen, cBurseAuthen)) {
                Log.i(TAG, String.format("钱包认证码错误:%02x.%02x.%02x.----%02x.%02x.%02x.",
                        CardBurseData.cBurseAuthen[0], CardBurseData.cBurseAuthen[1], CardBurseData.cBurseAuthen[2],
                        cBurseAuthen[0], cBurseAuthen[1], cBurseAuthen[2]));
                return 64;
            }

            //判断是正本还是副本
            if (cIsBlockID == 1) {
                Log.i(TAG, "副本覆盖正本");
                // 1:写卡状态1 ->失败退出,成功进入2
                cResult = (byte) g_Nlib.WriteMifareBlock(g_EP_BurseInfo.get(cBurseID - 1).cBlockID, cCardSID, cBurseData);
                if (cResult != 0) {
                    Log.i(TAG, "1:写卡状态1 ->失败退出");
                    return 2;
                } else {
                    Log.i(TAG, "需要记录断点恢复流水");
                    s_ReWriteCardFlag |= 0x01;
                }
            } else if (cIsBlockID == 2) {
                Log.i(TAG, "正本覆盖副本");
                // 1:写卡状态1 ->失败退出,成功进入2
                cResult = (byte) g_Nlib.WriteMifareBlock(g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID, cCardSID, cBurseData);
                if (cResult != 0) {
                    Log.i(TAG, "1:写卡状态1 ->失败退出");
                    return 2;
                } else {
                    Log.i(TAG, "需要记录断点恢复流水");
                    s_ReWriteCardFlag |= 0x01;
                }
            }
            Log.i(TAG, String.format("读钱包%d 数据成功", cBurseID));
            return 0;
        } else if (cCardType == 2)   //CPU
        {
        /*
        钱包余额	3	-8000000~8000000,以分为单位
        补助流水号	2	0~65535
        钱包流水号	2	0~65535
        末笔交易日期	2	范围00~63年(6位)月(4位)日(5位)
        当日消费累计额	2	0~65535，以分为单位
        末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
        当日消费累计次	1	0～255（当超过255不允许交易）
        钱包认证码	3	卡号+代理序号+客户序号+钱包余额+扇区号(1~4)+钱包流水号计算得出
         */

            cLen = 0;
            // 主钱包的余额(在这里正负的处理)
            byte[] bytesTemp = new byte[4];
            System.arraycopy(cBurseData, cLen, bytesTemp, 0, 4);
            lngMoneyTemp = CardPublic.TransMoney(bytesTemp);
            cLen = (byte) (cLen + 3);
            CardBurseData.lngBurseMoney = (int) lngMoneyTemp;        //钱包余额	3		单位分，考虑正负数
            Log.i(TAG, "钱包余额: " + CardBurseData.lngBurseMoney);

            iSubsidySID = (cBurseData[cLen++] & 0xff);
            iSubsidySID = iSubsidySID + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iSubsidySID = iSubsidySID;                //补助版本号	2

            iBurseNoteID = (cBurseData[cLen++] & 0xff);
            iBurseNoteID = iBurseNoteID + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iBurseSID = iBurseNoteID;            //钱包流水号	2

            lngTemp = (cBurseData[cLen++] & 0xff);
            lngTemp = lngTemp + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iLastPayDate = (int) lngTemp;    //末笔交易日期	2		6位年4位月5位日

            lngTemp = (cBurseData[cLen++] & 0xff);
            lngTemp = lngTemp + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.lngDayPaymentTotal = lngTemp;        //日累金额	2		0~65535，以分为单位

            CardBurseData.iLastBusinessID = (cBurseData[cLen++] & 0xff); //末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)

            CardBurseData.iDayPaymentCount = (cBurseData[cLen++] & 0xff);        //日累次数	1

            System.arraycopy(cBurseData, cLen, CardBurseData.cBurseAuthen, 0, 3);//钱包认证码

            //获取钱包认证码
            g_Nlib.UCardBurAuthen(cCardSID,
                    g_SystemInfo.cAgentID,
                    g_SystemInfo.iGuestID,
                    0,              //扇区号特殊处理(CPU)
                    lngMoneyTemp,
                    iBurseNoteID,
                    cBurseAuthen);
            //验证钱包论证码
            if (!Arrays.equals(CardBurseData.cBurseAuthen, cBurseAuthen)) {
                Log.i(TAG, String.format("钱包认证码错误:%02x.%02x.%02x.----%02x.%02x.%02x.",
                        CardBurseData.cBurseAuthen[0], CardBurseData.cBurseAuthen[1], CardBurseData.cBurseAuthen[2],
                        cBurseAuthen[0], cBurseAuthen[1], cBurseAuthen[2]));
                return 64;
            }
            Log.i(TAG, "读钱包:" + cBurseID + "数据成功");
            return 0;
        } else {
            return 1;
        }
    }

    //读取钱包交易信息区数据
    private static int ReadChaseBurseInfoData(CardBurseInfo CardBurseData, CardAttrInfo CardAttr) {
        int i;
        byte cLen;
        int cResult;
        int cBurseID;
        byte cCardType;
        byte[] cCardSID = new byte[8];
        long lngMoneyTemp;
        int iBurseNoteID;
        int iSubsidySID;
        long lngTemp;
        byte cBlockID;
        byte cIsBlockID;      //判断是正本还是副本 0:正 1:副
        byte[] cBurseAuthen = new byte[3];
        byte[] cBurseData = new byte[16];
        byte[] cBurseOneData = new byte[16];//正本
        byte[] cBurseBakData = new byte[16];//副本
        byte[][] cCardContext = new byte[3][16];
        byte[] cCardContextDate = new byte[64];
        byte[] cTempContext = new byte[16];

        cBurseID = CardAttr.cChaseBurseID;
        if (cBurseID < 1) {
            Log.i(TAG, "工作钱包号出错");
            return 1;
        }
        Log.i(TAG, String.format("追扣钱包块号:%d,%d", g_EP_BurseInfo.get(cBurseID - 1).cBlockID,
                g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID));

        if (cBurseID > 8) {
            Log.i(TAG, "钱包号超出范围");
            return 1;
        }
        System.arraycopy(CardAttr.cCardSID, 0, cCardSID, 0, 8);
        cCardType = CardAttr.cCardType;

        if (cCardType == 1)    //M1
        {
            //判断是否钱包块号在同一扇区
            if ((g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4) == (g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID / 4)) {
                cResult = g_Nlib.ReadMifareSector((byte) (g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4), cCardSID, cCardContext);
                if (cResult != 0) {
                    Log.i(TAG, "读失败");
                    return 2;
                }

                cBlockID = (byte) (g_EP_BurseInfo.get(cBurseID - 1).cBlockID % 4);
                System.arraycopy(cCardContext[cBlockID], 0, cBurseOneData, 0, 16);//正本

                cBlockID = (byte) (g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID % 4);
                System.arraycopy(cCardContext[cBlockID], 0, cBurseBakData, 0, 16);//备本
            } else {
                Log.i(TAG, "钱包块号不在同一扇区");
                cResult = (byte) g_Nlib.ReadMifareBlock((g_EP_BurseInfo.get(cBurseID - 1).cBlockID), cCardSID, cBurseOneData);//正本
                if (cResult != 0) {
                    Log.i(TAG, "读失败");
                    return 2;
                }

                cResult = g_Nlib.ReadMifareBlock((g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID), cCardSID, cBurseBakData);//备本
                if (cResult != 0) {
                    Log.i(TAG, "读失败");
                    return 2;
                }
            }
        } else if (cCardType == 2)    //CPU
        {
            cResult = g_Nlib.CPU_BurseInfo_Get((char) cBurseID, cCardContextDate, cCardSID);
            if (cResult != 0) {
                return 66;
            }
            System.arraycopy(cCardContextDate, 0, cBurseData, 0, 16);
        } else {
            return 1;
        }
        //数据解析
        if (cCardType == 1) {
            /*
            钱包余额	3	-8000000~8000000,以分为单位
            补助流水号	2	0~65535
            钱包流水号	2	0~65535
            末笔交易日期	2	范围00~63年(6位)月(4位)日(5位)
            当日消费累计额	2	0~65535，以分为单位
            末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
            当日消费累计次	1	0～255（当超过255不允许交易）
            钱包认证码	3	卡号+代理序号+客户序号+钱包余额+扇区号(1~4)+钱包流水号计算得出
             */
            //判断正备本数据
            cIsBlockID = 0;
            if ((Arrays.equals(cBurseOneData, cBurseBakData))) {
                Log.i(TAG, "正备本数据相同");
                System.arraycopy(cBurseOneData, 0, cBurseData, 0, 16);
                cIsBlockID = 0;
            } else {
                Log.i(TAG, "正备本数据不相同,以副本为准");
                if (Arrays.equals(cBurseBakData, cTempContext)) {
                    Log.i(TAG, "副本数据为0,以正本为准");
                    System.arraycopy(cBurseOneData, 0, cBurseData, 0, 16);
                    cIsBlockID = 2;
                } else {
                    System.arraycopy(cBurseBakData, 0, cBurseData, 0, 16);
                    cIsBlockID = 1;
                }
            }
            cLen = 0;
            // 主钱包的余额(在这里正负的处理)
            byte[] bytesTemp = new byte[4];
            System.arraycopy(cBurseData, cLen, bytesTemp, 0, 4);
            lngMoneyTemp = CardPublic.TransMoney(bytesTemp);
            cLen = (byte) (cLen + 3);
            CardBurseData.lngBurseMoney = (int) lngMoneyTemp;        //钱包余额	3		单位分，考虑正负数

            iSubsidySID = (cBurseData[cLen++] & 0xff);
            iSubsidySID = iSubsidySID + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iSubsidySID = iSubsidySID;                //补助版本号	2

            iBurseNoteID = (cBurseData[cLen++] & 0xff);
            iBurseNoteID = iBurseNoteID + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iBurseSID = iBurseNoteID;            //钱包流水号	2

            lngTemp = (cBurseData[cLen++] & 0xff);
            lngTemp = lngTemp + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iLastPayDate = (int) lngTemp;    //末笔交易日期	2		6位年4位月5位日

            lngTemp = (cBurseData[cLen++] & 0xff);
            lngTemp = lngTemp + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.lngDayPaymentTotal = lngTemp;        //日累金额	2		0~65535，以分为单位

            CardBurseData.iLastBusinessID = (cBurseData[cLen++] & 0xff); //末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)

            CardBurseData.iDayPaymentCount = (cBurseData[cLen++] & 0xff);        //日累次数	1

            System.arraycopy(cBurseData, cLen, CardBurseData.cBurseAuthen, 0, 3);//钱包认证码

            //获取钱包认证码
            g_Nlib.UCardBurAuthen(cCardSID,
                    g_SystemInfo.cAgentID,
                    g_SystemInfo.iGuestID,
                    g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4,
                    lngMoneyTemp,
                    iBurseNoteID,
                    cBurseAuthen);
            //验证钱包论证码
            if (!Arrays.equals(CardBurseData.cBurseAuthen, cBurseAuthen)) {
                Log.i(TAG, String.format("钱包认证码错误:%02x.%02x.%02x.----%02x.%02x.%02x.",
                        CardBurseData.cBurseAuthen[0], CardBurseData.cBurseAuthen[1], CardBurseData.cBurseAuthen[2],
                        cBurseAuthen[0], cBurseAuthen[1], cBurseAuthen[2]));

                return 64;
            }

            //判断是正本还是副本
            if (cIsBlockID == 1) {
                Log.i(TAG, "副本覆盖正本");
                // 1:写卡状态1 ->失败退出,成功进入2
                cResult = g_Nlib.WriteMifareBlock(g_EP_BurseInfo.get(cBurseID - 1).cBlockID, cCardSID, cBurseData);
                if (cResult != 0) {
                    Log.i(TAG, "1:写卡状态1 ->失败退出");
                    return 2;
                } else {
                    Log.i(TAG, "追扣钱包需要记录断点恢复流水");
                    s_ReWriteCardFlag |= 0x10;
                }
            } else if (cIsBlockID == 2) {
                Log.i(TAG, "正本覆盖副本");
                // 1:写卡状态1 ->失败退出,成功进入2
                cResult = g_Nlib.WriteMifareBlock(g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID, cCardSID, cBurseData);
                if (cResult != 0) {
                    Log.i(TAG, "1:写卡状态1 ->失败退出");
                    return 2;
                } else {
                    Log.i(TAG, "追扣钱包需要记录断点恢复流水");
                    s_ReWriteCardFlag |= 0x10;
                }
            }
            return 0;
        } else if (cCardType == 2) {
            /*
            钱包余额	3	-8000000~8000000,以分为单位
            补助流水号	2	0~65535
            钱包流水号	2	0~65535
            末笔交易日期	2	范围00~63年(6位)月(4位)日(5位)
            当日消费累计额	2	0~65535，以分为单位
            末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
            当日消费累计次	1	0～255（当超过255不允许交易）
            钱包认证码	3	卡号+代理序号+客户序号+钱包余额+扇区号(1~4)+钱包流水号计算得出
             */
            //判断正备本数据
            cLen = 0;
            // 主钱包的余额(在这里正负的处理)
            byte[] bytesTemp = new byte[4];
            System.arraycopy(cBurseData, cLen, bytesTemp, 0, 4);
            lngMoneyTemp = CardPublic.TransMoney(bytesTemp);
            cLen = (byte) (cLen + 3);
            CardBurseData.lngBurseMoney = (int) lngMoneyTemp;        //钱包余额	3		单位分，考虑正负数

            iSubsidySID = (cBurseData[cLen++] & 0xff);
            iSubsidySID = iSubsidySID + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iSubsidySID = iSubsidySID;                //补助版本号	2

            iBurseNoteID = (cBurseData[cLen++] & 0xff);
            iBurseNoteID = iBurseNoteID + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iBurseSID = iBurseNoteID;            //钱包流水号	2

            lngTemp = (cBurseData[cLen++] & 0xff);
            lngTemp = lngTemp + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.iLastPayDate = (int) lngTemp;    //末笔交易日期	2		6位年4位月5位日

            lngTemp = (cBurseData[cLen++] & 0xff);
            lngTemp = lngTemp + (cBurseData[cLen++] & 0xff) * 256;
            CardBurseData.lngDayPaymentTotal = lngTemp;        //日累金额	2		0~65535，以分为单位

            CardBurseData.iLastBusinessID = (cBurseData[cLen++] & 0xff); //末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)

            CardBurseData.iDayPaymentCount = (cBurseData[cLen++] & 0xff);        //日累次数	1

            System.arraycopy(cBurseData, cLen, CardBurseData.cBurseAuthen, 0, 3);//钱包认证码

            //获取钱包认证码
            g_Nlib.UCardBurAuthen(cCardSID,
                    g_SystemInfo.cAgentID,
                    g_SystemInfo.iGuestID,
                    0,                      //扇区号特殊处理(CPU)
                    lngMoneyTemp,
                    iBurseNoteID,
                    cBurseAuthen);
            //验证钱包论证码
            if (!Arrays.equals(CardBurseData.cBurseAuthen, cBurseAuthen)) {
                Log.i(TAG, String.format("钱包认证码错误:%02x.%02x.%02x.----%02x.%02x.%02x.",
                        CardBurseData.cBurseAuthen[0], CardBurseData.cBurseAuthen[1], CardBurseData.cBurseAuthen[2],
                        cBurseAuthen[0], cBurseAuthen[1], cBurseAuthen[2]));

                return 64;
            }
            return 0;
        } else {
            return 1;
        }
    }

    //写钱包交易信息区数据
    private static int WriteBurseInfoData(CardBurseInfo CardBurseData, CardAttrInfo CardAttr) {
        int i;
        int cResult = 0;
        byte cCardType;
        byte[] cCardSID = new byte[8];
        byte cLen;
        byte cBurseID;
        byte[] cBurseAuthen = new byte[3];
        byte[] cBurseData = new byte[16];
        byte[] cInitBurseData = new byte[16];
        //byte cCardContext[3][16];

        cBurseID = CardAttr.cWorkBurseID;
        if (cBurseID < 1) {
            Log.i(TAG, "工作钱包号出错");
            return 1;
        }
        Log.i(TAG, String.format("工作钱包块号:%d,%d", g_EP_BurseInfo.get(cBurseID - 1).cBlockID,
                g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID));

        if (cBurseID > 8) {
            Log.i(TAG, "钱包号超出范围");
            return 1;
        }
        System.arraycopy(CardAttr.cCardSID, 0, cCardSID, 0, 8);
        cCardType = CardAttr.cCardType;
        Log.i(TAG, "卡片类型:" + cCardType);

        if (cCardType == 1)    //M1
        {
            //钱包数据
            cLen = 0;
            (cBurseData[cLen++]) = (byte) (CardBurseData.lngBurseMoney & 0x000000ff);            //钱包余额	3		单位分，考虑正负数
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x0000ff00) >> 8);
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x00ff0000) >> 16);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iSubsidySID & 0x000000ff);            //补助版本号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iSubsidySID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iBurseSID & 0x000000ff);            //交易流水号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iBurseSID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iLastPayDate & 0x000000ff);            //末笔交易日期	4		6位年4位月5位日5位时6位分6位秒
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iLastPayDate & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.lngDayPaymentTotal & 0x000000ff);            //日累金额	2		以角为单位
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngDayPaymentTotal & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) CardBurseData.iLastBusinessID;        //末笔营业号	1

            (cBurseData[cLen++]) = (byte) CardBurseData.iDayPaymentCount;        //日累次数	1

            //计算钱包认证码
            g_Nlib.UCardBurAuthen(cCardSID,
                    g_SystemInfo.cAgentID,
                    g_SystemInfo.iGuestID,
                    g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4,
                    CardBurseData.lngBurseMoney,
                    CardBurseData.iBurseSID,
                    cBurseAuthen);

            System.arraycopy(cBurseAuthen, 0, cBurseData, cLen, 3);

            //判断是否钱包块号在同一扇区
            if ((g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4) == (g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID / 4)) {
                // 写钱包扇区数据
                for (i = 0; i < 1; i++) {
                    cResult = g_Nlib.WriteMifareBurseSector(cCardSID, (g_EP_BurseInfo.get(cBurseID - 1).cBlockID), cBurseData, (g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID), cBurseData);
                    if (cResult == 0) {
                        break;
                    } else {
                        Log.i(TAG, "写失败:" + cResult);
                    }
                }
                if (cResult != 0) {
                    //获取原始数据 注意工作钱包和追扣钱包
                    if ((g_StationInfo.cWorkBurseID) == cBurseID) {
                        Log.i(TAG, "====工作钱包:" + CardAttr.cWorkBurseID);
                        GetBurseInfoData(s_CardBurseInfo, cInitBurseData);
                    }
                    byte cIsResult = 0;
                    if (cResult == 71) {
                        cIsResult = 71;
                    }
                    //重新写卡
                    cResult = ReWriteBurseDate(cBurseID, CardAttr, cBurseData, cInitBurseData);
                    if (cResult != 0) {
                        Log.i(TAG, "M1重写卡失败:" + cResult);
                        if (cIsResult == 71) {
                            return 71;
                        }
                        return cResult;
                    }
                }
            } else {
                Log.i(TAG, "钱包块号不在同一扇区");
                for (i = 0; i < 3; i++) {
                    cResult = g_Nlib.WriteMifareBlock((g_EP_BurseInfo.get(cBurseID - 1).cBlockID), cCardSID, cBurseData);//正本
                    if (cResult == 0) {
                        break;
                    } else {
                        continue;
                    }
                }

                if (cResult == 0) {
                    for (i = 0; i < 3; i++) {
                        cResult = g_Nlib.WriteMifareBlock((g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID), cCardSID, cBurseData);//备本
                        if (cResult == 0) {
                            break;
                        } else {
                            continue;
                        }
                    }
                    if (cResult != 0) {
                        Log.i(TAG, "写钱包数据正本成功,副本失败 ");
                        return 71;
                    }
                } else {
                    Log.i(TAG, "写失败");
                    return 2;
                }
            }
            return 0;
        }
        else if (cCardType == 2)    //CPU
        {
            /*
            钱包余额	3	-8000000~8000000,以分为单位
            补助流水号	2	0~65535
            钱包流水号	2	0~65535
            末笔交易日期	2	范围00~63年(6位)月(4位)日(5位)
            当日消费累计额	2	0~65535，以分为单位
            末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
            当日消费累计次	1	0～255（当超过255不允许交易）
            钱包认证码	3	卡号+代理序号+客户序号+钱包余额+扇区号(1~4)+钱包流水号计算得出
             */
            //钱包数据
            cLen = 0;
            (cBurseData[cLen++]) = (byte) (CardBurseData.lngBurseMoney & 0x000000ff);            //钱包余额	3		单位分，考虑正负数
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x0000ff00) >> 8);
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x00ff0000) >> 16);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iSubsidySID & 0x000000ff);            //补助版本号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iSubsidySID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iBurseSID & 0x000000ff);            //交易流水号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iBurseSID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iLastPayDate & 0x000000ff);            //末笔交易日期	4		6位年4位月5位日5位时6位分6位秒
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iLastPayDate & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.lngDayPaymentTotal & 0x000000ff);            //日累金额	2		以角为单位
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngDayPaymentTotal & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) CardBurseData.iLastBusinessID;        //末笔营业号	1

            (cBurseData[cLen++]) = (byte) CardBurseData.iDayPaymentCount;        //日累次数	1

            //计算钱包认证码
            g_Nlib.UCardBurAuthen(cCardSID,
                    g_SystemInfo.cAgentID,
                    g_SystemInfo.iGuestID,
                    0,                  //扇区号特殊处理(CPU)
                    CardBurseData.lngBurseMoney,
                    CardBurseData.iBurseSID,
                    cBurseAuthen);

            System.arraycopy(cBurseAuthen, 0, cBurseData, cLen, 3);

            //写CPU卡片钱包数据
            cResult = g_Nlib.CPU_BurseInfo_Set((char) cBurseID, cBurseData, cCardSID);
            if (cResult != 0) {
                Log.i(TAG, "CPU--写失败,重写卡：" + cResult);
                //获取原始数据 注意工作钱包和追扣钱包
                if ((g_StationInfo.cWorkBurseID) == cBurseID) {
                    Log.i(TAG, "====工作钱包:" + CardAttr.cWorkBurseID);
                    GetBurseInfoData(s_CardBurseInfo, cInitBurseData);
                }
                //重新写卡
                cResult = CPU_ReWriteAppFileBinary(cBurseID, cCardSID, cBurseData, cInitBurseData);
                if (cResult != 0) {
                    Log.i(TAG, "CPU--重写卡失败:" + cResult);
                    return cResult;
                }
                return cResult;
            }
            return 0;
        } else if ((cCardType == 4) || (cCardType == 5) || (cCardType == 6))//湖南大众建行卡//NFC卡
        {
           /*
            钱包余额	3	-8000000~8000000,以分为单位
            补助流水号	2	0~65535
            钱包流水号	2	0~65535
            末笔交易日期	2	范围00~63年(6位)月(4位)日(5位)
            当日消费累计额	2	0~65535，以分为单位
            末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
            当日消费累计次	1	0～255（当超过255不允许交易）
            钱包认证码	3	卡号+代理序号+客户序号+钱包余额+扇区号(1~4)+钱包流水号计算得出
             */

            //钱包数据
            cLen = 0;
            (cBurseData[cLen++]) = (byte) (CardBurseData.lngBurseMoney & 0x000000ff);            //钱包余额	3		单位分，考虑正负数
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x0000ff00) >> 8);
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x00ff0000) >> 16);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iBurseSID & 0x000000ff);            //钱包流水号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iBurseSID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iSubsidySID & 0x000000ff);            //补助版本号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iSubsidySID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iLastPayDate & 0x000000ff);            //末笔交易日期	2		6位年4位月5位日5位时6位分6位秒
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iLastPayDate & 0x0000ff00) >> 8);

            //计算钱包认证码
            g_Nlib.UCardBurAuthen(cCardSID,
                    g_SystemInfo.cAgentID,
                    g_SystemInfo.iGuestID,
                    1, //扇区号特殊处理(CPU)
                    CardBurseData.lngBurseMoney,
                    CardBurseData.iBurseSID,
                    cBurseAuthen);
            (cBurseData[cLen++]) = cBurseAuthen[0];

            (cBurseData[cLen++]) = (byte) (CardBurseData.lngDayPaymentTotal & 0x000000ff);            //日累金额	2		以角为单位
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngDayPaymentTotal & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) CardBurseData.iLastBusinessID;        //末笔营业号	1
            (cBurseData[cLen++]) = (byte) CardBurseData.iDayPaymentCount;        //日累次数	1

//            if(cCardType==4)
//            {
//                Log.i(TAG,"SIMPASS卡开始写卡");
//                //写自定义数据流程
//                cResult=SIMP_WriteData(50,cLen,cBurseData);
//                if(cResult!=0)
//                {
//                    //Log.i(TAG,"写钱包数据失败");
//                    return 66;
//                }
//            }
//            else if(cCardType==5)
//            {
//                //写自定义数据流程
//                cResult=NFC_WriteData(50,cLen,cBurseData);
//                if(cResult!=0)
//                {
//                    //Log.i(TAG,"写钱包数据失败");
//                    return 66;
//                }
//            }
//            else if(cCardType==6)
//            {
//                //写自定义数据流程
//                cResult=HN_CPU_WriteData(50,cLen,cBurseData);
//                if(cResult!=0)
//                {
//                    //Log.i(TAG,"写钱包数据失败");
//                    return 66;
//                }
//            }
//            return 0;
            return 1;
        } else {
            return 1;
        }
    }

    //写追扣钱包交易信息区数据
    private static int WriteChaseBurseInfoData(CardBurseInfo CardBurseData, CardAttrInfo CardAttr) {
        int i;
        int cResult = 0;
        byte cCardType;
        byte[] cCardSID = new byte[8];
        byte cLen;
        byte cBurseID;
        byte[] cBurseAuthen = new byte[3];
        byte[] cBurseData = new byte[16];
        byte[] cInitBurseData = new byte[16];
        byte[][] cCardContext = new byte[3][16];

        cBurseID = CardAttr.cChaseBurseID;
        Log.i(TAG, String.format("追扣钱包块号:%d,%d", g_EP_BurseInfo.get(cBurseID - 1).cBlockID,
                g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID));

        if (cBurseID > 8) {
            Log.i(TAG, "钱包号超出范围");
            return 1;
        }
        System.arraycopy(CardAttr.cCardSID, 0, cCardSID, 0, 8);
        cCardType = CardAttr.cCardType;
        //Log.i(TAG,"卡片类型:"+cCardType);

        if (cCardType == 1)    //M1
        {
            /*
            钱包余额	3	-8000000~8000000,以分为单位
            补助流水号	2	0~65535
            钱包流水号	2	0~65535
            末笔交易日期	2	范围00~63年(6位)月(4位)日(5位)
            当日消费累计额	2	0~65535，以分为单位
            末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
            当日消费累计次	1	0～255（当超过255不允许交易）
            钱包认证码	3	卡号+代理序号+客户序号+钱包余额+扇区号(1~4)+钱包流水号计算得出
             **/
            //钱包数据
            cLen = 0;
            (cBurseData[cLen++]) = (byte) (CardBurseData.lngBurseMoney & 0x000000ff);            //钱包余额	3		单位分，考虑正负数
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x0000ff00) >> 8);
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x00ff0000) >> 16);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iSubsidySID & 0x000000ff);            //补助版本号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iSubsidySID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iBurseSID & 0x000000ff);            //交易流水号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iBurseSID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iLastPayDate & 0x000000ff);            //末笔交易日期	4		6位年4位月5位日5位时6位分6位秒
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iLastPayDate & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.lngDayPaymentTotal & 0x000000ff);            //日累金额	2		以角为单位
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngDayPaymentTotal & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) CardBurseData.iLastBusinessID;        //末笔营业号	1

            (cBurseData[cLen++]) = (byte) CardBurseData.iDayPaymentCount;        //日累次数	1

            //计算钱包认证码
            g_Nlib.UCardBurAuthen(cCardSID,
                    g_SystemInfo.cAgentID,
                    g_SystemInfo.iGuestID,
                    g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4,
                    CardBurseData.lngBurseMoney,
                    CardBurseData.iBurseSID,
                    cBurseAuthen);

            System.arraycopy(cBurseAuthen, 0, cBurseData, cLen, 3);

            //判断是否钱包块号在同一扇区
            if ((g_EP_BurseInfo.get(cBurseID - 1).cBlockID / 4) == (g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID / 4)) {
                // 写钱包扇区数据
                for (i = 0; i < 1; i++) {
                    cResult = g_Nlib.WriteMifareBurseSector(
                            cCardSID, (g_EP_BurseInfo.get(cBurseID - 1).cBlockID), cBurseData, (g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID), cBurseData);
                    if (cResult == 0) {
                        break;
                    } else {
                        Log.i(TAG, "写失败:" + cResult);
                    }
                }
                if (cResult != 0) {
                    //获取原始数据 注意工作钱包和追扣钱包
                    if ((g_StationInfo.cChaseBurseID) == cBurseID) {
                        Log.i(TAG, "====追扣钱包:" + CardAttr.cChaseBurseID);
                        GetBurseInfoData(s_CardChaseBurseInfo, cInitBurseData);
                    }
                    byte cIsResult = 0;
                    if (cResult == 71) {
                        cIsResult = 71;
                    }
                    //重新写卡
                    cResult = ReWriteBurseDate(cBurseID, CardAttr, cBurseData, cInitBurseData);
                    if (cResult != 0) {
                        Log.i(TAG, "M1重写卡失败:%d" + cResult);
                        if (cResult == 71) {
                            return 71;
                        }
                        return cResult;
                    }
                }
            } else {
                Log.i(TAG, "钱包块号不在同一扇区");
                for (i = 0; i < 3; i++) {
                    cResult = g_Nlib.WriteMifareBlock((g_EP_BurseInfo.get(cBurseID - 1).cBlockID), cCardSID, cBurseData);//正本
                    if (cResult == 0) {
                        break;
                    } else {
                        continue;
                    }
                }

                if (cResult == 0) {
                    for (i = 0; i < 3; i++) {
                        cResult = g_Nlib.WriteMifareBlock((g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID), cCardSID, cBurseData);//备本
                        if (cResult == 0) {
                            break;
                        } else {
                            continue;
                        }
                    }
                    if (cResult != 0) {
                        Log.i(TAG, "写钱包数据正本成功,副本失败 ");
                        return 71;
                    }
                } else {
                    Log.i(TAG, "写失败");
                    return 2;
                }
            }
            return 0;
        } else if (cCardType == 2)    //CPU
        {
            /*
            钱包余额	3	-8000000~8000000,以分为单位
            补助流水号	2	0~65535
            钱包流水号	2	0~65535
            末笔交易日期	2	范围00~63年(6位)月(4位)日(5位)
            当日消费累计额	2	0~65535，以分为单位
            末笔营业号	1	最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
            当日消费累计次	1	0～255（当超过255不允许交易）
            钱包认证码	3	卡号+代理序号+客户序号+钱包余额+扇区号(1~4)+钱包流水号计算得出
             */
            cLen = 0;

            (cBurseData[cLen++]) = (byte) (CardBurseData.lngBurseMoney & 0x000000ff);            //钱包余额	3		单位分，考虑正负数
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x0000ff00) >> 8);
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngBurseMoney & 0x00ff0000) >> 16);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iSubsidySID & 0x000000ff);            //补助版本号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iSubsidySID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iBurseSID & 0x000000ff);            //交易流水号	2
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iBurseSID & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.iLastPayDate & 0x000000ff);            //末笔交易日期	4		6位年4位月5位日5位时6位分6位秒
            (cBurseData[cLen++]) = (byte) ((CardBurseData.iLastPayDate & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) (CardBurseData.lngDayPaymentTotal & 0x000000ff);            //日累金额	2		以角为单位
            (cBurseData[cLen++]) = (byte) ((CardBurseData.lngDayPaymentTotal & 0x0000ff00) >> 8);

            (cBurseData[cLen++]) = (byte) CardBurseData.iLastBusinessID;        //末笔营业号	1

            (cBurseData[cLen++]) = (byte) CardBurseData.iDayPaymentCount;        //日累次数	1

            //计算钱包认证码
            g_Nlib.UCardBurAuthen(cCardSID,
                    g_SystemInfo.cAgentID,
                    g_SystemInfo.iGuestID,
                    0,                  //扇区号特殊处理(CPU)
                    CardBurseData.lngBurseMoney,
                    CardBurseData.iBurseSID,
                    cBurseAuthen);

            System.arraycopy(cBurseAuthen, 0, cBurseData, cLen, 3);

            //写CPU卡片钱包数据
            cResult = g_Nlib.CPU_BurseInfo_Set(cBurseID, cBurseData, cCardSID);
            if (cResult != 0) {
                Log.i(TAG, "CPU--写失败,重写卡");
                //获取原始数据 注意工作钱包和追扣钱包
                if ((g_StationInfo.cChaseBurseID) == cBurseID) {
                    Log.i(TAG, "====追扣钱包:%d====" + CardAttr.cChaseBurseID);
                    GetBurseInfoData(s_CardChaseBurseInfo, cInitBurseData);
                }
                //重新写卡
                cResult = CPU_ReWriteAppFileBinary(cBurseID, cCardSID, cBurseData, cInitBurseData);
                if (cResult != 0) {
                    Log.i(TAG, "CPU--重写卡失败:" + cResult);
                    return cResult;
                }
                return cResult;
            }
            return 0;
        } else {
            return 1;
        }
    }

    //解析获取钱包数据
    private static void GetBurseInfoData(CardBurseInfo CardBurseData, byte[] cBurseData) {
        int cLen;
        byte[] cData = new byte[32];

        cLen = 0;
        cData[cLen++] = (byte) (CardBurseData.lngBurseMoney & 0x000000ff);            //钱包余额	3		单位分，考虑正负数
        cData[cLen++] = (byte) ((CardBurseData.lngBurseMoney & 0x0000ff00) >> 8);
        cData[cLen++] = (byte) ((CardBurseData.lngBurseMoney & 0x00ff0000) >> 16);

        cData[cLen++] = (byte) (CardBurseData.iSubsidySID & 0x000000ff);            //补助版本号	2
        cData[cLen++] = (byte) ((CardBurseData.iSubsidySID & 0x0000ff00) >> 8);

        cData[cLen++] = (byte) (CardBurseData.iBurseSID & 0x000000ff);            //交易流水号	2
        cData[cLen++] = (byte) ((CardBurseData.iBurseSID & 0x0000ff00) >> 8);

        cData[cLen++] = (byte) (CardBurseData.iLastPayDate & 0x000000ff);            //末笔交易日期	4		6位年4位月5位日5位时6位分6位秒
        cData[cLen++] = (byte) ((CardBurseData.iLastPayDate & 0x0000ff00) >> 8);

        cData[cLen++] = (byte) (CardBurseData.lngDayPaymentTotal & 0x000000ff);            //日累金额	2		以角为单位
        cData[cLen++] = (byte) ((CardBurseData.lngDayPaymentTotal & 0x0000ff00) >> 8);

        cData[cLen++] = (byte) CardBurseData.iLastBusinessID;        //末笔营业号	1

        cData[cLen++] = (byte) CardBurseData.iDayPaymentCount;        //日累次数	1

        memcpy(cData, cLen, CardBurseData.cBurseAuthen, 0, 3);//钱包认证码
        memcpy(cBurseData, cData, 16);
    }

    //重新写卡钱包数据(M1)
    public static int M1_ReWriteMifareBlockData(byte cBlockID, byte[] cInCardSID, byte[] cWriteDataInfo, byte[] cInitDataInfo) {
        int iCount = 0;
        int cResult = 0;
        int iTimeOutCnt = 0;
        int cReWriteResult = 1;
        byte[] cCardSID = new byte[8];
        byte[] cReadData = new byte[96];

        //发送打开重写界面
        if ((g_WorkInfo.cCardEnableFlag == 1) && (g_UICardHandler != null))
            g_UICardHandler.sendEmptyMessage(CARD_RWSTART);

        g_WorkInfo.cStartReWDialogFlag = 1;
        while (true) {
            SystemClock.sleep(200);
            iTimeOutCnt++;
            if (iTimeOutCnt > 5 * 10) {
                cResult = 3;
                break;
            }
            //判断是否按了取消键
            if (g_WorkInfo.cStartReWDialogFlag == 0){
                cResult = 4;
                Log.i(TAG, "按取消键退出");
                break;
            }
            //提示重新写卡界面
            cResult = g_Nlib.ReaderCardUID(cCardSID);
            if (cResult == 0) {
                //判断卡号是否一致
                if (memcmp(cCardSID, cInCardSID, 4) != 0) {
                    iCount++;
                    if (iCount % 6 == 0)
                        VoicePlay("card_invalid");
                    continue;
                }
                Log.d(TAG, "重新读卡开始");
                cReWriteResult = g_Nlib.ReadMifareBlock(cBlockID, cCardSID, cReadData);
                if (cReWriteResult == 0) {
                    //校验钱包认证码
                    //判断读出的数据 1:卡片读出的数据和卡片原始数据相同    2:卡片读出的数据和卡片写入数据相同
                    if (memcmp(cReadData, cWriteDataInfo, 16) == 0) {
                        Log.d(TAG, "卡片读出的数据和将要写的数据一致");
                        cResult = 0;
                        break;
                    } else {
                        Log.d(TAG, "卡片读出的数据和将要写的数据不一致,则判断读出的数据和原始数据是否一致");
                        if (memcmp(cReadData, cInitDataInfo, 16) == 0) {
                            Log.d(TAG, "卡片读出的数据和原始数据一致");
                        } else {
                            Log.d(TAG, "卡片读出的数据和原始数据不一致,卡片被其他设备写过->失败退出");
                            PrintArray("读出数据", cReadData);
                            PrintArray("原始数据", cInitDataInfo);
                            cResult = 2;
                            break;
                        }
                    }
                } else {
                    Log.d(TAG, "重新读卡失败");
                    continue;
                }
                Log.d(TAG, "重新写卡开始");
                cReWriteResult = 1;
                cReWriteResult = g_Nlib.WriteMifareBlock(cBlockID, cCardSID, cWriteDataInfo);
                if (cReWriteResult == 0) {
                    cResult = 0;
                    break;
                } else {
                    Log.d(TAG, "重新写卡失败");
                    continue;
                }
            } else {
                iCount++;
                if (iCount % 8 == 0)
                    VoicePlay("card_reswipe");
                continue;
            }
        }
        //发送关闭重写界面
        if ((g_WorkInfo.cCardEnableFlag == 1) && (g_UICardHandler != null))
            g_UICardHandler.sendEmptyMessage(CARD_RWEND);

        g_WorkInfo.cStartReWDialogFlag = 0;
        return cResult;
    }

    //重新写卡钱包数据
    private static int ReWriteBurseDate(int cBurseID, CardAttrInfo CardAttr, byte[] cWriteDataInfo, byte[] cInitDataInfo) {
        int i;
        int iCount = 0;
        int cResult = 0;
        int cReWriteResult = 0;
        int iTimeOutCnt = 0;
        byte[] cCardSID = new byte[8];
        byte[] cReadData = new byte[48];

        PrintArray("写入数据", cWriteDataInfo);
        PrintArray("原始数据", cInitDataInfo);
        //发送打开重写界面
        if ((g_WorkInfo.cCardEnableFlag == 1) && (g_UICardHandler != null))
            g_UICardHandler.sendEmptyMessage(CARD_RWSTART);

        g_WorkInfo.cStartReWDialogFlag = 1;
        while (true) {
            //超时退出10S退出
            SystemClock.sleep(200);
            iTimeOutCnt++;
            if (iTimeOutCnt > 5 * 10) {
                cResult = 3;
                break;
            }
            //判断是否按了取消键
            if (g_WorkInfo.cStartReWDialogFlag == 0){
                cResult = 4;
                Log.i(TAG, "按取消键退出");
                break;
            }

            //提示重新写卡界面
            cResult = g_Nlib.ReaderCardUID(cCardSID);
            if (cResult == 0) {
                //判断卡号是否一致
                if (memcmp(cCardSID, CardAttr.cCardSID, 4) != 0) {
                    iCount++;
                    if (iCount % 6 == 0)
                        VoicePlay("card_invalid");
                    continue;
                }
                Log.d(TAG, "重新读卡开始");
                cReWriteResult = g_Nlib.ReadMifareBlock(g_EP_BurseInfo.get(cBurseID - 1).cBlockID, cCardSID, cReadData);//正本
                if (cReWriteResult == 0) {
                    //判断读出的数据 1:卡片读出的数据和卡片原始数据相同    2:卡片读出的数据和卡片写入数据相同
                    if (memcmp(cReadData, cWriteDataInfo, 16) == 0) {
                        Log.i(TAG, "卡片读出的数据和将要写的数据一致");
                    } else {
                        Log.i(TAG, "卡片读出的数据和将要写的数据不一致,则判断读出的数据和原始数据是否一致");
                        if (memcmp(cReadData, cInitDataInfo, 16) == 0) {
                            Log.i(TAG, "卡片读出的数据和原始数据一致");
                        } else {
                            Log.i(TAG, "卡片读出的数据和原始数据不一致,卡片被其他设备写过->失败退出");
                            PrintArray("读出数据", cReadData);
                            PrintArray("原始数据", cInitDataInfo);
                            cResult = 2;
                            break;
                        }
                    }
                } else {
                    Log.i(TAG, "重新读卡失败");
                    continue;
                }

                Log.i(TAG, "重新写卡开始");
                cReWriteResult = 1;
                cReWriteResult = g_Nlib.WriteMifareBurseSector(cCardSID, (g_EP_BurseInfo.get(cBurseID - 1).cBlockID), cWriteDataInfo, (g_EP_BurseInfo.get(cBurseID - 1).cBakBlockID), cWriteDataInfo);
                if (cReWriteResult == 0) {
                    cResult = cReWriteResult;
                    break;
                } else {
                    Log.i(TAG, "重新写卡失败");
                    continue;
                }
            } else {
                iCount++;
                if (iCount % 8 == 0)
                    VoicePlay("card_reswipe");
                continue;
            }
        }
        //发送关闭重写界面
        if ((g_WorkInfo.cCardEnableFlag == 1) && (g_UICardHandler != null))
            g_UICardHandler.sendEmptyMessage(CARD_RWEND);

        g_WorkInfo.cStartReWDialogFlag = 0;
        return cResult;
    }

    //重新写卡钱包数据
    public static int CPU_ReWriteAppFileBinary(byte cBurseID, byte[] cInCardSID, byte[] cWriteDataInfo, byte[] cInitDataInfo) {
        int iCount = 0;
        int cResult = 0;
        int iTimeOutCnt = 0;
        byte cReWriteResult = 1;
        byte[] cReadData = new byte[96];
        byte[] cCardSID = new byte[8];

        //发送打开重写界面
        if ((g_WorkInfo.cCardEnableFlag == 1) && (g_UICardHandler != null))
            g_UICardHandler.sendEmptyMessage(CARD_RWSTART);

        g_WorkInfo.cStartReWDialogFlag = 1;
        while (true) {
            //超时退出10S退出
            SystemClock.sleep(200);
            iTimeOutCnt++;
            if (iTimeOutCnt > 5 * 10) {
                cResult = 3;
                Log.i(TAG, "超时退出");
                break;
            }
            //判断是否按了取消键
            if (g_WorkInfo.cStartReWDialogFlag == 0){
                cResult = 4;
                Log.i(TAG, "按取消键退出");
                break;
            }

            //提示重新写卡界面
            cResult = g_Nlib.ReadCPUCardSID(cCardSID);
            if (cResult == 0) {
                //判断卡号是否一致
                if (memcmp(cCardSID, cInCardSID, 4) != 0) {
                    iCount++;
                    if (iCount % 6 == 0)
                        VoicePlay("card_invalid");
                    continue;
                }
                Log.i(TAG, "CPU重新读卡开始");
                cReWriteResult = (byte) g_Nlib.CPU_BurseInfo_Get(cBurseID, cReadData, cInCardSID);
                if (cReWriteResult == 0) {
                    //判断读出的数据 1:卡片读出的数据和卡片原始数据相同    2:卡片读出的数据和卡片写入数据相同
                    if (memcmp(cReadData, cWriteDataInfo, 16) == 0) {
                        Log.i(TAG, "卡片读出的数据和将要写的数据一致");
                    } else {
                        Log.i(TAG, "卡片读出的数据和将要写的数据不一致,则判断读出的数据和原始数据是否一致");
                        if (memcmp(cReadData, cInitDataInfo, 16) == 0) {
                            Log.i(TAG, "卡片读出的数据和原始数据一致");
                        } else {
                            Log.i(TAG, "卡片读出的数据和原始数据不一致,卡片被其他设备写过->失败退出");
                            PrintArray("读出数据", cReadData);
                            PrintArray("原始数据", cInitDataInfo);
                            cResult = 2;
                            break;
                        }
                    }
                } else {
                    Log.d(TAG, "重新读卡失败");
                    continue;
                }
                Log.i(TAG, "CPU重新写卡开始");
                cReWriteResult = 1;
                cReWriteResult = (byte) g_Nlib.CPU_BurseInfo_Set(cBurseID, cWriteDataInfo, cInCardSID);
                if (cReWriteResult == 0) {
                    cResult = 0;
                    break;
                } else {
                    Log.d(TAG, "重新写卡失败");
                    continue;
                }
            } else {
                iCount++;
                if (iCount % 8 == 0)
                    VoicePlay("card_reswipe");
                continue;
            }
        }

        //发送关闭重写界面
        if ((g_WorkInfo.cCardEnableFlag == 1) && (g_UICardHandler != null))
            g_UICardHandler.sendEmptyMessage(CARD_RWEND);

        g_WorkInfo.cStartReWDialogFlag = 0;
        return cResult;
    }

//    //重新写卡钱包数据(CPU卡)
//    private static int ReWriteBurseDate_CPU(int cBurseID,CardAttrInfo CardAttr,byte[]cWriteDataInfo,byte[] cInitDataInfo)
//    {
//        int i;
//        int cResult;
//        int cReWriteResult;
//        byte[] cReadData=new byte[32];
//
//        cReWriteResult=1;
//        PrintArray("写入数据", cWriteDataInfo);
//        PrintArray("原始数据", cInitDataInfo);
//
//        while(true)
//        {
//            //提示重新写卡界面
//            cResult=PReWriteCard(0,CardAttr->cCardSID);
//            if(cResult==0)
//            {
//                Log.i(TAG,"CPU重新读卡开始");
//                cReWriteResult=g_Nlib.CPU_BurseInfo_Get(cBurseID,cReadData,CardAttr->cCardSID);
//                if(cReWriteResult==0)
//                {
//                    //判断读出的数据 1:卡片读出的数据和卡片原始数据相同    2:卡片读出的数据和卡片写入数据相同
//                    if(memcmp(cReadData,cWriteDataInfo,16)==0)
//                    {
//                        Log.i(TAG,"卡片读出的数据和将要写的数据一致");
//                        //return 0;
//                    }
//                    else
//                    {
//                        Log.i(TAG,"卡片读出的数据和将要写的数据不一致,则判断读出的数据和原始数据是否一致");
//                        if(memcmp(cReadData,cInitDataInfo,16)==0)
//                        {
//                            Log.i(TAG,"卡片读出的数据和原始数据一致");
//                        }
//                        else
//                        {
//                            Log.i(TAG,"卡片读出的数据和原始数据不一致,卡片被其他设备写过->失败退出");
//                            PrintArray("读出数据", cReadData);
//                            PrintArray("原始数据", cInitDataInfo);
//                            return 2;
//                        }
//                    }
//                }
//                else
//                {
//                    Log.i(TAG,"CPU重新读卡失败");
//                    continue;
//                }
//
//                Log.i(TAG,"CPU重新写卡开始");
//                cReWriteResult=1;
//                cReWriteResult=g_Nlib.CPU_BurseInfo_Set(cBurseID,cWriteDataInfo,CardAttr->cCardSID);
//                if(cReWriteResult==0)
//                {
//                    return 0;
//                }
//                else
//                {
//                    Log.i(TAG,"CPU重新写卡失败");
//                    continue;
//                }
//            }
//            else
//            {
//                if(cReWriteResult!=0)
//                {
//                    Log.i(TAG,"CPU--写钱包文件数据 ->失败退出");
//                    return cReWriteResult;
//                }
//            }
//        }
//    }

    //--------------------------工作钱包消费功能集合----------------------------//
    //工作钱包消费流程
    public static int Burse_WorkConsumeProcess(long lngPayMoney, CardBasicParaInfo pCardBasicInfo) {
        int cResult;

        //钱包余额校验
        cResult = Burse_WorkConsumeCheck(lngPayMoney);
        if (cResult != 0) {
            if (cResult == 1) {
                //追扣钱包信息
                pCardBasicInfo.lngPayMoney = s_lngPayMoney;
                pCardBasicInfo.lngWorkPayMoney = s_lngWorkPayMoney;
                pCardBasicInfo.lngChasePayMoney = s_lngChasePayMoney;
                pCardBasicInfo.lngManageMoney = s_lngManageMoney;
                pCardBasicInfo.lngPriMoney = s_lngPriMoney;
            }
            return cResult;
        }

        //钱包消费
        cResult = Burse_WorkConsumeOperate(s_lngPayMoney, s_lngManageMoney);
        if ((cResult != 0) && (cResult != 71)) {
            return cResult;
        }

        if (cResult == 71) {
            Log.i(TAG, "记录工作钱包断点拔卡标识");
            pCardBasicInfo.cNOWriteCardFlag |= 0x01;
        }

        //组织需要传出的参数数据
        //基本扇区
        pCardBasicInfo.cAgentID = s_CardBaseData.cAgentID;                //代理号
        pCardBasicInfo.iGuestID = s_CardBaseData.iGuestID;               //客户号
        pCardBasicInfo.cAuthenVer = s_CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
        //卡认证码	4
        System.arraycopy(s_CardBaseData.cCardAuthenCode, 0, pCardBasicInfo.cCardAuthenCode, 0, 4);
        pCardBasicInfo.cCardState = s_CardBaseData.cCardState;         //卡片状态	1
        pCardBasicInfo.lngCardID = s_CardBaseData.lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
        pCardBasicInfo.cCampusID = s_CardBaseData.cCampusID;         //园区号	1	范围为1~250
        pCardBasicInfo.cStatusID = s_CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64
        pCardBasicInfo.iValidTime = s_CardBaseData.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
        //基本扇区第1块内容
        System.arraycopy(s_CardBaseData.bBasic1Context, 0, pCardBasicInfo.bBasic1Context, 0, 16);

        pCardBasicInfo.lngAccountID = s_CardBaseData.lngAccountID;       //帐号	4	1～4294967296
        pCardBasicInfo.lngPaymentPsw = s_CardBaseData.lngPaymentPsw;       //交易密码	3	六位数字密码
        pCardBasicInfo.cCardStructVer = s_CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
        //基本扇区第2块内容
        System.arraycopy(s_CardBaseData.bBasic2Context, 0, pCardBasicInfo.bBasic2Context, 0, 16);

        //扩展扇区
        //卡户姓名
        System.arraycopy(s_CardExtentData.cAccName, 0, pCardBasicInfo.cAccName, 0, 16);
        //性别
        System.arraycopy(s_CardExtentData.cSexState, 0, pCardBasicInfo.cSexState, 0, 2);
        //开户日期
        System.arraycopy(s_CardExtentData.cCreateCardDate, 0, pCardBasicInfo.cCreateCardDate, 0, 3);
        pCardBasicInfo.iDepartID = s_CardExtentData.iDepartID;            //部门编号
        //第三方对接关键字
        System.arraycopy(s_CardExtentData.cOtherLinkID, 0, pCardBasicInfo.cOtherLinkID, 0, 10);
        //个人编号
        System.arraycopy(s_CardExtentData.cCardPerCode, 0, pCardBasicInfo.cCardPerCode, 0, 16);

        //钱包信息
        //工作钱包信息
        pCardBasicInfo.lngWorkBurseMoney = s_CardBurseInfo.lngBurseMoney;             //工作钱包余额
        pCardBasicInfo.iWorkSubsidySID = s_CardBurseInfo.iSubsidySID;               //补助流水号
        pCardBasicInfo.iWorkBurseSID = s_CardBurseInfo.iBurseSID;                    //工作钱包流水号
        pCardBasicInfo.iWorkLastPayDate = s_CardBurseInfo.iLastPayDate;              //工作钱包末笔交易日期
        pCardBasicInfo.lngWorkDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额
        pCardBasicInfo.iWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;           //末笔交易营业号
        pCardBasicInfo.iWorkDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardBurseInfo.cBurseAuthen, 0, pCardBasicInfo.cWorkBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardBurseInfo.bBurseContext, 0, pCardBasicInfo.bWorkBurseContext, 0, 3);  //当前钱包块的内容

        pCardBasicInfo.lngPayMoney = s_lngPayMoney;
        pCardBasicInfo.lngWorkPayMoney = s_lngPayMoney;       //工作钱包交易金额
        pCardBasicInfo.lngChasePayMoney = 0;                  //追扣钱包交易金额
        pCardBasicInfo.lngManageMoney = s_lngManageMoney;
        pCardBasicInfo.lngPriMoney = s_lngPriMoney;

        return 0;
    }

    //工作钱包余额校验
    private static int Burse_WorkConsumeCheck(long lngPayMoney) {
        int cResult = 0;
        byte cBytePos, cBitPos;
        long lngTemp;
        long lngOutPayMoney = 0;
        long lngOnPermitLimit = 0;        //允许在线交易限额
        long lngOffPermitLimit = 0;       //脱机单笔交易限额
        int cOutLimitFlag = 0;        //是否有消费限次后追扣

        s_cUsePrivLimit = 0;
        s_lngWorkPayMoney = 0;        //追扣时工作钱包交易金额
        s_lngChasePayMoney = 0;        //追扣时追扣钱包交易金额
        s_lngPriMoney = 0;
        s_lngManageMoney = 0;
        s_lngPayMoney = lngPayMoney;

        //是否允许优惠
        if (g_WorkInfo.cBusinessID > 0) {
            if (s_StatusWorkBurInfo.cPrivilegeMode > 0) {
                //不启用优惠限次
                cResult = BusinessCanPriv();
                Log.i(TAG, "是否启用优惠限次判断:" + cResult);
                if (cResult == 1) {
                    cBytePos = (byte) ((g_WorkInfo.cBusinessID - 1) / 8);
                    cBitPos = (byte) ((g_WorkInfo.cBusinessID - 1) % 8);
                    Log.i(TAG, String.format("优惠时段号:%d,字节位置:%d,位位置:", g_WorkInfo.cBusinessID, cBytePos, cBitPos));

                    lngOutPayMoney = s_lngPayMoney;

                    if (((s_StatusPriInfo.bPrivilegeTime[cBytePos] >> cBitPos) & 0x01) == 0x01) {
                        s_cUsePrivLimit = 1;
                        //折扣优惠
                        if (s_StatusWorkBurInfo.cPrivilegeMode == 1) {
                            Log.i(TAG, "折扣优惠");
                            lngTemp = lngOutPayMoney * s_StatusWorkBurInfo.cPrivilegeDis;

                            s_lngPriMoney = lngOutPayMoney - (lngTemp / 100);
                            s_lngPayMoney = lngOutPayMoney - s_lngPriMoney;
                        }
                        //定额优惠
                        if (s_StatusWorkBurInfo.cPrivilegeMode == 2) {
                            Log.i(TAG, "定额优惠");
                            if (lngOutPayMoney > s_StatusWorkBurInfo.cBookPrivelege) {
                                s_lngPayMoney = lngOutPayMoney - s_StatusWorkBurInfo.cBookPrivelege;
                                s_lngPriMoney = s_StatusWorkBurInfo.cBookPrivelege;
                            } else {
                                s_lngPayMoney = 0;
                                s_lngPriMoney = lngOutPayMoney;
                            }
                        }
                        //定额消费
                        if (s_StatusWorkBurInfo.cPrivilegeMode == 3) {
                            Log.i(TAG, "定额消费");
                            if (lngOutPayMoney > s_StatusWorkBurInfo.cBookMoney) {
                                s_lngPriMoney = lngOutPayMoney - s_StatusWorkBurInfo.cBookMoney;
                            } else {
                                s_lngPriMoney = 0;
                            }
                            s_lngPayMoney = s_StatusWorkBurInfo.cBookMoney;
                        }
                    } else {
                        Log.i(TAG, "时段不允许优惠");
                    }
                } else {
                    if (cResult == 2) {
                        Log.i(TAG, "消费管理费限次");
                    } else {
                        Log.i(TAG, "优惠限次");
                    }
                }
            } else {
                Log.i(TAG, "优惠模式=0");
            }
        }
        //是否消费限次
        if (g_WorkInfo.cBusinessID > 0) {
            //判断是否启用限次消费
            if (g_StationInfo.cPaymentLimit == 1) {
                Log.i(TAG, String.format("启用限次消费：%d,", g_WorkInfo.cBusinessID, (s_CardBurseInfo.iLastBusinessID & 0x7F)));
                //判断当前营业时段号
                if (g_WorkInfo.cBusinessID == (s_CardBurseInfo.iLastBusinessID & 0x7F)) {
                    cBytePos = (byte) ((g_WorkInfo.cBusinessID - 1) / 8);
                    cBitPos = (byte) ((g_WorkInfo.cBusinessID - 1) % 8);
                    Log.i(TAG, String.format("限次时段号:%d,字节位置:%d,位位置:", g_WorkInfo.cBusinessID, cBytePos, cBitPos));

                    //判断当前时段是否限次消费
                    if (((s_StatusInfo.bPaymentLimitTime[cBytePos] >> cBitPos) & 0x01) == 0x01) {
                        Log.i(TAG, "当前时段是限次消费时段");
                        //判断启用消费限次后是否允许追扣
                        if (g_StationInfo.cCanChasePayment == 0) {
                            Log.i(TAG, "消费限次后不允许追扣");
                            return 10;
                        } else {
                            Log.i(TAG, "消费限次后允许追扣");
                            cOutLimitFlag = 1;
                            //return 9;
                        }
                    }
                }
            } else {
                Log.i(TAG, "不启用限次消费");
            }
        }

        if (g_WorkInfo.cTestState == 1) {
            //钱包余额小于交易金额
            if (s_lngPayMoney > s_CardBurseInfo.lngBurseMoney) {
                return 3;
            } else {
                Log.i(TAG, "测试模式不进行钱包校验");
                return OK;
            }
        }
        if (g_StationInfo.cPaymentUnit == 0)    //分
        {
            lngOnPermitLimit = g_StationInfo.lngOnPermitLimit;
            lngOffPermitLimit = g_StationInfo.lngOffPermitLimit;
        } else if (g_StationInfo.cPaymentUnit == 1)    //角
        {
            lngOnPermitLimit = g_StationInfo.lngOnPermitLimit * 10;
            lngOffPermitLimit = g_StationInfo.lngOffPermitLimit * 10;
        } else if (g_StationInfo.cPaymentUnit == 2)    //元
        {
            lngOnPermitLimit = g_StationInfo.lngOnPermitLimit * 100;
            lngOffPermitLimit = g_StationInfo.lngOffPermitLimit * 100;
        }
        //商户单笔限额
        if (g_WorkInfo.cRunState == 1)    //联网
        {
            if (s_lngPayMoney > lngOnPermitLimit) {
                Log.i(TAG, "超出联网单笔限额:" + lngOnPermitLimit);
                return 07;
            }
        }
        if (g_WorkInfo.cRunState == 2)    //脱机
        {
            if (s_lngPayMoney > lngOffPermitLimit) {
                Log.i(TAG, "超出脱网单笔限额:" + lngOffPermitLimit);
                return 07;
            }
        }

        //是否启用解除身份参数(日累超限、超额)限制 0:启用 1:不启用
        if (g_StationInfo.cCanStatusLimitFee == 0) {
            //日累消费金额限制
            Log.i(TAG, "日累消费金额限制:" + s_StatusWorkBurInfo.lngDayTotalMoneyLim);
            if (s_lngPayMoney + s_CardBurseInfo.lngDayPaymentTotal > s_StatusWorkBurInfo.lngDayTotalMoneyLim) {
                Log.i(TAG, "日累消费金额限制");
                if (g_StationInfo.cCanStatusLimitFee == 0) {
                    return 07;
                }
            }
            //日累消费次数限制
            Log.i(TAG, "日累消费次数限制:" + s_StatusWorkBurInfo.cDayTotalCountLim);
            if ((s_CardBurseInfo.iDayPaymentCount + 1) > s_StatusWorkBurInfo.cDayTotalCountLim) {
                Log.i(TAG, "日累消费次数限制");
                if (g_StationInfo.cCanStatusLimitFee == 0) {
                    return 07;
                }
            }
            byte[] cTradePWD= PwdLongtoByte(s_CardBaseData.lngPaymentPsw);
            Log.i(TAG, "交易密码:" + cTradePWD);
            Log.i(TAG, "单笔消费密码限额:" + s_StatusWorkBurInfo.lngSinglePayPswLim);
            if (s_lngPayMoney >= s_StatusWorkBurInfo.lngSinglePayPswLim) {
                //提示输入交易密码
                Log.i(TAG, "单笔消费密码限额");
                cResult = PCheckPassword(2, cTradePWD);
                if (cResult != 0) {
                    if (cResult == 2) {
                        Log.i(TAG, "单笔消费密码限额(密码无输入)");
                        return 40;
                    } else {
                        Log.i(TAG, "单笔消费密码限额(密码错误)");
                        return 8;
                    }
                }
            } else {
                //日累消费密码限额
                Log.i(TAG, "日累消费密码限额:" + s_StatusWorkBurInfo.lngDayPayPswLim);

                if (s_lngPayMoney + s_CardBurseInfo.lngDayPaymentTotal >= s_StatusWorkBurInfo.lngDayPayPswLim) {
                    //提示输入交易密码
                    cResult = PCheckPassword(2, cTradePWD);
                    if (cResult != 0) {
                        if (cResult == 2) {
                            Log.i(TAG, "日累消费密码限额(密码无输入)");
                            return 40;
                        } else {
                            Log.i(TAG, "日累消费密码限额(密码错误)");
                            return 8;
                        }
                    }
                }
            }
        }
        //收取管理费
        if (g_StationInfo.cCanManagementFee == 1) {
            Log.i(TAG, "收取消费管理费 ");
            if (s_StatusWorkBurInfo.cManagementMode == 1) {
                Log.i(TAG, "按比率管理费模式");
                lngTemp = s_lngPayMoney * s_StatusWorkBurInfo.cManageMoneyRate;
                s_lngManageMoney = lngTemp / 100;
                s_lngPayMoney = s_lngPayMoney + s_lngManageMoney;

            } else if (s_StatusWorkBurInfo.cManagementMode == 2) {
                Log.i(TAG, "按金额管理费模式");
                s_lngPayMoney = s_lngPayMoney + s_StatusWorkBurInfo.iManageMoney;
                s_lngManageMoney = s_StatusWorkBurInfo.iManageMoney;
            } else {
                Log.i(TAG, "不启用管理费模式");
            }
        } else {
            Log.i(TAG, "不收取消费管理费 ");
        }
        Log.i(TAG, "实际交易金额(包含管理费):" + s_lngPayMoney);

        if (1 == cOutLimitFlag) {
            if (g_StationInfo.cWorkBurseID == 1) {
                Log.i(TAG, "工作钱包为主钱包：" + g_StationInfo.cWorkBurseID);
                return 10;
            }
            //钱包余额小于交易金额
            if (s_lngPayMoney <= s_CardChaseBurseInfo.lngBurseMoney) {
                //判断是否允许追扣
                if ((g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID - 1).cCanPermitChase == 1) && (g_StationInfo.cChaseBurseID != 0)
                        && (s_lngManageMoney == 0) && (s_lngPriMoney == 0) && (s_cUsePrivLimit == 0)) {
                    Log.i(TAG, "允许追扣 追扣钱包:" + s_CardChaseBurseInfo.lngBurseMoney);
                    s_lngWorkPayMoney = 0;
                    //追扣钱包交易金额(追扣时)
                    s_lngChasePayMoney = s_lngPayMoney;

                    Log.i(TAG, "交易金额:" + s_lngPayMoney);
                    Log.i(TAG, "工作钱包交易金额:" + s_lngWorkPayMoney);
                    Log.i(TAG, "追扣钱包交易金额:" + s_lngChasePayMoney);
                    Log.i(TAG, "优惠金额：" + s_lngPriMoney);
                    Log.i(TAG, "管理费金额：" + s_lngManageMoney);
                    return 1;
                } else {
                    Log.i(TAG, "不允许追扣 ");
                    return 9;
                }
            } else {
                Log.i(TAG, "不允许追扣，余额不足 ");
                return 3;
            }
        } else {
            //钱包余额小于交易金额
            if (s_lngPayMoney > s_CardBurseInfo.lngBurseMoney) {
                //判断是否允许追扣
                if ((g_EP_BurseInfo.get(g_StationInfo.cWorkBurseID - 1).cCanPermitChase == 1)
                        && (g_StationInfo.cChaseBurseID != 0)
                        && (g_StationInfo.cWorkBurseID != 1)
                        && (s_lngManageMoney == 0) && (s_lngPriMoney == 0) && (s_cUsePrivLimit == 0)) {
                    Log.i(TAG, String.format("允许追扣 工作钱包：%d 追扣钱包:%d", s_CardBurseInfo.lngBurseMoney, s_CardChaseBurseInfo.lngBurseMoney));
                    if (s_lngPayMoney > (s_CardBurseInfo.lngBurseMoney + s_CardChaseBurseInfo.lngBurseMoney)) {
                        Log.i(TAG, "2个钱包余额之和不足 ");
                        return 3;
                    } else {
                        if (s_CardBurseInfo.lngBurseMoney < 0) {
                            s_lngWorkPayMoney = 0;
                        } else {
                            s_lngWorkPayMoney = s_CardBurseInfo.lngBurseMoney;
                        }
                        //追扣钱包交易金额(追扣时)
                        s_lngChasePayMoney = s_lngPayMoney - s_lngWorkPayMoney;
                    }
                } else {
                    Log.i(TAG, "不允许追扣");
                    return 3;
                }
                Log.i(TAG, "交易金额:" + s_lngPayMoney);
                Log.i(TAG, "工作钱包交易金额:" + s_lngWorkPayMoney);
                Log.i(TAG, "追扣钱包交易金额:" + s_lngChasePayMoney);
                Log.i(TAG, "优惠金额：" + s_lngPriMoney);
                Log.i(TAG, "管理费金额：" + s_lngManageMoney);
                return 1;
            }
        }
        Log.i(TAG, "优惠金额：" + s_lngPriMoney);
        Log.i(TAG, "管理费金额：" + s_lngManageMoney);
        Log.i(TAG, "交易金额:" + s_lngPayMoney);
        return 0;
    }

    //设置密码
    public static byte PCheckPassword(int iMode, byte[] cTradePWD) {

        int i;
        g_WorkInfo.cInUserPWDFlag = 1;
        g_WorkInfo.strUserPwd = "";

        for (i = 0; i < cTradePWD.length; i++) {
            if (cTradePWD[i] == 0x00)
                break;
        }
        byte[] bTemp = new byte[i];
        memcpy(bTemp, cTradePWD, bTemp.length);
        String strTemp = new String(bTemp);
        Log.i(TAG, "用户交易密钥：" + strTemp);

        //发送打开密码键盘
        if ((g_WorkInfo.cCardEnableFlag == 1) && (g_UICardHandler != null))
            g_UICardHandler.sendEmptyMessage(CARD_PWDKBOPEN);

        while (true) {
            SystemClock.sleep(100);
            //判断密码是否输入完成
            if (g_WorkInfo.cInUserPWDFlag == 0)
                break;
        }
        //发送关闭密码键盘
        if ((g_WorkInfo.cCardEnableFlag == 1) && (g_UICardHandler != null))
            g_UICardHandler.sendEmptyMessage(CARD_PWDKBCLOSE);

        g_WorkInfo.cInUserPWDFlag = 0;
        //判断密码是否正确
        if (strTemp.equals(g_WorkInfo.strUserPwd)) {   //密码正确-->
            Log.i(TAG, "密码正确：" + strTemp);
            return 0;
        } else {  //密码错误
            Log.e(TAG, "密码错误：" + strTemp + "-" + g_WorkInfo.strUserPwd);
            return 1;
        }
    }

    //工作钱包消费
    private static int Burse_WorkConsumeOperate(long lngPayMoney, long lngManageMoney) {
        int i;
        int cResult;
        long lngDayTotalMon;         //日累计金额
        int iDayPaymentCount;           //日累次数
        int cWorkLastBusinessID;

        //组织钱包信息
        /*
        long lngBurseMoney;             //钱包余额   3
        int iSubsidySID;               //补助流水号  2
        int iBurseSID;		        	//钱包流水号 2
        int iLastPayDate;              //钱包末笔交易日期   2
        long lngDayPaymentTotal;         //当日消费累计额   2
        int  iLastBusinessID;            //最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
        int  iDayPaymentCount;			//当日消费累计次   1
        int  cBurseAuthen[3];            //钱包认证码     3
        int bBurseContext[16];           //当前钱包块的内容
         */
        CardBurseInfo pCardBurseInfo = new CardBurseInfo();
        pCardBurseInfo.lngBurseMoney = s_CardBurseInfo.lngBurseMoney - (int) lngPayMoney;        //钱包余额	3		单位分，考虑正负数

        //判断是否有管理费
        if (lngManageMoney != 0) {
            Log.i(TAG, "有管理费");
            pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 2;            //交易流水号
        } else {
            pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 1;            //交易流水号
        }
        pCardBurseInfo.iSubsidySID = s_CardBurseInfo.iSubsidySID;            //补助版本号

        //末笔交易日期6位年4位月5位日5位时6位分6位秒
        pCardBurseInfo.iLastPayDate = CardPublic.GetCurrentCardDate(s_cCurDateTime);

        //判断是否是当日的
        //日累次数、日累金额
        cResult = CardPublic.CompareDateSame(s_CardBurseInfo.iLastPayDate, s_cCurDateTime);
        if (cResult == 0) {
            Log.i(TAG, "同一天");
            iDayPaymentCount = s_CardBurseInfo.iDayPaymentCount + 1;
            if ((iDayPaymentCount > s_StatusWorkBurInfo.cDayTotalCountLim) && (g_WorkInfo.cTestState == 0)) {
                if (g_StationInfo.cCanStatusLimitFee == 0) {
                    Log.e(TAG, "写卡日累限次");
                    return 07;
                } else {
                    iDayPaymentCount = s_StatusWorkBurInfo.cDayTotalCountLim;
                }
            }
            pCardBurseInfo.iDayPaymentCount = (iDayPaymentCount & 0xff);        //日累次数	1

            if ((g_StationInfo.cCanManagementFee == 1) && (s_StatusWorkBurInfo.cManagementMode != 0)) {
                //当日消费累计额
                lngDayTotalMon = s_CardBurseInfo.lngDayPaymentTotal + (lngPayMoney - lngManageMoney);
            } else {
                //当日消费累计额
                lngDayTotalMon = (s_CardBurseInfo.lngDayPaymentTotal + lngPayMoney);
            }
            Log.i(TAG, String.format("同一天：%d %d ", lngDayTotalMon, s_CardBurseInfo.lngDayPaymentTotal));
            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0xffff);//日累金额	2		以角为单位
            Log.i(TAG, "CardBurseInfo.wDayTotalMon: " + pCardBurseInfo.lngDayPaymentTotal);
        } else {
            Log.i(TAG, "不是同一天");
            pCardBurseInfo.iDayPaymentCount = 1;                        //日累次数	1
            if ((g_StationInfo.cCanManagementFee == 1) && (s_StatusWorkBurInfo.cManagementMode != 0)) {
                //当日消费累计额
                lngDayTotalMon = (lngPayMoney - lngManageMoney);
            } else {
                //当日消费累计额
                lngDayTotalMon = lngPayMoney;
            }
            lngDayTotalMon = (lngPayMoney);
            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0xffff);//日累金额	2		以角为单位
        }

        Log.i(TAG, "当前营业号:" + g_WorkInfo.cBusinessID);
        if (g_WorkInfo.cBusinessID == 1)//全天营业 无优惠无限次 不修改卡内末笔营业号
        {
            if (s_cUsePrivLimit == 0) {
                Log.i(TAG, "全天营业优惠不考虑");
                if (g_StationInfo.cPaymentLimit == 1) {
                    Log.i(TAG, "全天营业优惠不考虑时有限次");
                    cWorkLastBusinessID = (g_WorkInfo.cBusinessID | (s_CardBurseInfo.iLastBusinessID & 0x80));
                } else {
                    Log.i(TAG, "全天营业无优惠无限次");
                    cWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;
                }
            } else {
                Log.i(TAG, "全天营业优惠考虑");
                cWorkLastBusinessID = g_WorkInfo.cBusinessID | 0x80;
            }
        } else //其他营业时段正常处理
        {
            if (s_cUsePrivLimit == 0) {
                Log.i(TAG, "优惠不考虑");
                cWorkLastBusinessID = (g_WorkInfo.cBusinessID | (s_CardBurseInfo.iLastBusinessID & 0x80));
            } else {
                Log.i(TAG, "优惠考虑");
                cWorkLastBusinessID = g_WorkInfo.cBusinessID | 0x80;
            }
        }
        Log.i(TAG, "末笔营业时段:" + cWorkLastBusinessID);
        pCardBurseInfo.iLastBusinessID = cWorkLastBusinessID;

        Log.i(TAG, String.format("日累次数:%d,日累金额:%d", pCardBurseInfo.iDayPaymentCount, pCardBurseInfo.lngDayPaymentTotal));
        //写钱包交易信息区数据
        cResult = WriteBurseInfoData(pCardBurseInfo, g_CardAttr);
        if (cResult == 0) {
            //写钱包交易信息成功
            try {
                s_CardBurseInfo = (CardBurseInfo) pCardBurseInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return 0;
        } else {
            if (cResult == 71) {
                Log.i(TAG, "写钱包数据正本成功,副本失败 ");
                return 71;
            } else {
                Log.i(TAG, "写钱包交易信息区数据失败:" + cResult);
                return 62;
            }
        }
    }

    //--------------------------追扣工作钱包消费功能集合----------------------------//
    //工作钱包消费流程(追扣时)
    public static int Burse_WorkConsumeProcess_Chase(long lngPayMoney, long lngWorkPayMoney, CardBasicParaInfo CardBasicInfo) {
        int cResult;

        //钱包消费
        cResult = Burse_WorkConsumeOperate_Chase(lngPayMoney, lngWorkPayMoney);
        if ((cResult != 0) && (cResult != 71)) {
            return cResult;
        }
        if (cResult == 71) {
            Log.i(TAG, "记录工作钱包断点拔卡标识");
            CardBasicInfo.cNOWriteCardFlag |= 0x01;
        }
        //组织需要传出的参数数据
        //组织卡片数据
        //基本扇区
        CardBasicInfo.cAgentID = s_CardBaseData.cAgentID;                //代理号
        CardBasicInfo.iGuestID = s_CardBaseData.iGuestID;               //客户号
        CardBasicInfo.cAuthenVer = s_CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
        //卡认证码	4
        System.arraycopy(s_CardBaseData.cCardAuthenCode, 0, CardBasicInfo.cCardAuthenCode, 0, 4);
        CardBasicInfo.cCardState = s_CardBaseData.cCardState;         //卡片状态	1
        CardBasicInfo.lngCardID = s_CardBaseData.lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
        CardBasicInfo.cCampusID = s_CardBaseData.cCampusID;         //园区号	1	范围为1~250
        CardBasicInfo.cStatusID = s_CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64
        CardBasicInfo.iValidTime = s_CardBaseData.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
        //基本扇区第1块内容
        System.arraycopy(s_CardBaseData.bBasic1Context, 0, CardBasicInfo.bBasic1Context, 0, 16);

        CardBasicInfo.lngAccountID = s_CardBaseData.lngAccountID;       //帐号	4	1～4294967296
        CardBasicInfo.lngPaymentPsw = s_CardBaseData.lngPaymentPsw;       //交易密码	3	六位数字密码
        CardBasicInfo.cCardStructVer = s_CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
        //基本扇区第2块内容
        System.arraycopy(s_CardBaseData.bBasic2Context, 0, CardBasicInfo.bBasic2Context, 0, 16);

        //扩展扇区
        //卡户姓名
        System.arraycopy(s_CardExtentData.cAccName, 0, CardBasicInfo.cAccName, 0, 16);
        //性别
        System.arraycopy(s_CardExtentData.cSexState, 0, CardBasicInfo.cSexState, 0, 2);
        //开户日期
        System.arraycopy(s_CardExtentData.cCreateCardDate, 0, CardBasicInfo.cCreateCardDate, 0, 3);
        CardBasicInfo.iDepartID = s_CardExtentData.iDepartID;            //部门编号
        //第三方对接关键字
        System.arraycopy(s_CardExtentData.cOtherLinkID, 0, CardBasicInfo.cOtherLinkID, 0, 10);
        //个人编号
        System.arraycopy(s_CardExtentData.cCardPerCode, 0, CardBasicInfo.cCardPerCode, 0, 16);

        //钱包信息
        //工作钱包信息
        CardBasicInfo.lngWorkBurseMoney = s_CardBurseInfo.lngBurseMoney;             //工作钱包余额
        CardBasicInfo.iWorkSubsidySID = s_CardBurseInfo.iSubsidySID;               //补助流水号
        CardBasicInfo.iWorkBurseSID = s_CardBurseInfo.iBurseSID;                    //工作钱包流水号
        CardBasicInfo.iWorkLastPayDate = s_CardBurseInfo.iLastPayDate;              //工作钱包末笔交易日期
        CardBasicInfo.lngWorkDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额
        CardBasicInfo.iWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;           //末笔交易营业号
        CardBasicInfo.iWorkDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardBurseInfo.cBurseAuthen, 0, CardBasicInfo.cWorkBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardBurseInfo.bBurseContext, 0, CardBasicInfo.bWorkBurseContext, 0, 3);  //当前钱包块的内容

        CardBasicInfo.lngPayMoney = s_lngPayMoney;
        CardBasicInfo.lngWorkPayMoney = lngWorkPayMoney;       //工作钱包交易金额
        CardBasicInfo.lngManageMoney = s_lngManageMoney;
        CardBasicInfo.lngPriMoney = s_lngPriMoney;
        return 0;
    }

    //工作钱包消费操作(追扣时)
    public static int Burse_WorkConsumeOperate_Chase(long lngPayMoney, long lngWorkPayMoney) {
        int i;
        int cResult;
        long lngDayTotalMon;         //日累计金额
        int iDayPaymentCount;           //日累次数
        int cWorkLastBusinessID;

        //组织钱包信息
        /*
        long lngBurseMoney;             //钱包余额   3
        int iSubsidySID;               //补助流水号  2
        int iBurseSID;		        	//钱包流水号 2
        int iLastPayDate;              //钱包末笔交易日期   2
        long lngDayPaymentTotal;         //当日消费累计额   2
        int  iLastBusinessID;            //最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
        int  iDayPaymentCount;			//当日消费累计次   1
        int  cBurseAuthen[3];            //钱包认证码     3
        int bBurseContext[16];           //当前钱包块的内容
        */
        CardBurseInfo pCardBurseInfo = new CardBurseInfo();
        pCardBurseInfo.lngBurseMoney = s_CardBurseInfo.lngBurseMoney - ((int) lngWorkPayMoney);        //钱包余额	3		单位分，考虑正负数
        pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 1;            //交易流水号
        pCardBurseInfo.iSubsidySID = s_CardBurseInfo.iSubsidySID;            //补助版本号
        //末笔交易日期6位年4位月5位日5位时6位分6位秒
        pCardBurseInfo.iLastPayDate = CardPublic.GetCurrentCardDate(s_cCurDateTime);

        //判断是否是当日的
        //日累次数、日累金额
        cResult = CardPublic.CompareDateSame(s_CardBurseInfo.iLastPayDate, s_cCurDateTime);
        if (cResult == 0) {
            Log.i(TAG, "同一天");
            iDayPaymentCount = s_CardBurseInfo.iDayPaymentCount + 1;
            pCardBurseInfo.iDayPaymentCount = (iDayPaymentCount & 0xff);        //日累次数	1

            lngDayTotalMon = (s_CardBurseInfo.lngDayPaymentTotal + lngPayMoney);
            Log.i(TAG, String.format("同一天：%d %d", lngDayTotalMon, s_CardBurseInfo.lngDayPaymentTotal));

            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0x00ffff);//日累金额	2		以角为单位
            Log.i(TAG, "CardBurseInfo.wDayTotalMon: " + pCardBurseInfo.lngDayPaymentTotal);
        } else {
            Log.i(TAG, "不是同一天");
            pCardBurseInfo.iDayPaymentCount = 1;                        //日累次数	1

            lngDayTotalMon = (lngPayMoney);
            if (lngDayTotalMon > 0xffff) {
                lngDayTotalMon = 0xffff;
            }
            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0xffff);//日累金额	2		以角为单位
        }
        pCardBurseInfo.iLastBusinessID = g_WorkInfo.cBusinessID;        //末笔交易时段号	1		时段号：营业时段号

        if (g_WorkInfo.cBusinessID == 1)//全天营业 无优惠无限次 不修改卡内末笔营业号
        {
            if (s_cUsePrivLimit == 0) {
                Log.i(TAG, "全天营业优惠不考虑");
                if (g_StationInfo.cPaymentLimit == 1) {
                    Log.i(TAG, "全天营业优惠不考虑时有限次");
                    cWorkLastBusinessID = (g_WorkInfo.cBusinessID | (s_CardBurseInfo.iLastBusinessID & 0x80));
                } else {
                    Log.i(TAG, "全天营业无优惠无限次");
                    cWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;
                }
            } else {
                Log.i(TAG, "全天营业优惠考虑");
                cWorkLastBusinessID = g_WorkInfo.cBusinessID | 0x80;
            }
        } else //其他营业时段正常处理
        {
            if (s_cUsePrivLimit == 0) {
                Log.i(TAG, String.format("优惠不考虑:02x", s_CardBurseInfo.iLastBusinessID));
                cWorkLastBusinessID = (g_WorkInfo.cBusinessID | (s_CardBurseInfo.iLastBusinessID & 0x80));
            } else {
                Log.i(TAG, "优惠考虑");
                cWorkLastBusinessID = g_WorkInfo.cBusinessID | 0x80;
            }
        }
        Log.i(TAG, "末笔营业时段:" + cWorkLastBusinessID);
        pCardBurseInfo.iLastBusinessID = cWorkLastBusinessID;

        Log.i(TAG, String.format("日累次数:%d,日累金额:%d", pCardBurseInfo.iDayPaymentCount, pCardBurseInfo.lngDayPaymentTotal));

        //写钱包交易信息区数据
        for (i = 0; i < 1; i++) {
            cResult = WriteBurseInfoData(pCardBurseInfo, g_CardAttr);
            if (cResult == 0) {
                //写钱包交易信息成功
                try {
                    s_CardBurseInfo = (CardBurseInfo) pCardBurseInfo.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        if (cResult == 0) {
            return 0;
        } else {
            if (cResult == 71) {
                Log.i(TAG, "写钱包数据正本成功,副本失败 ");
                return 71;
            } else {
                Log.i(TAG, "写钱包交易信息区数据失败:" + cResult);
                return 62;
            }
        }
    }

    //--------------------------追扣钱包消费功能集合----------------------------//
    //追扣钱包消费流程
    public static int Burse_ChaseConsumeProcess(long lngPayMoney, CardBasicParaInfo CardBasicInfo) {
        int cResult;

        //钱包余额校验
        cResult = Burse_ChaseConsumeCheck(lngPayMoney);
        if (cResult != 0) {
            return cResult;
        }
        //钱包消费
        cResult = Burse_ChaseConsumeOperate(lngPayMoney);
        if ((cResult != 0) && (cResult != 71)) {
            return cResult;
        }
        if (cResult == 71) {
            Log.i(TAG, "记录工作钱包断点拔卡标识");
            CardBasicInfo.cNOWriteCardFlag |= 0x10;
        }
        //组织需要传出的参数数据
        //钱包交易信息区
        CardBasicInfo.lngChaseBurseMoney = s_CardChaseBurseInfo.lngBurseMoney;        //钱包余额	3		单位分，考虑正负数

        CardBasicInfo.iChaseBurseSID = s_CardChaseBurseInfo.iBurseSID;            //交易流水号	2
        CardBasicInfo.iChaseLastPayDate = s_CardChaseBurseInfo.iLastPayDate;    //末笔交易日期	4		6位年4位月5位日5位时6位分6位秒

        CardBasicInfo.lngChasePayMoney = lngPayMoney;

        Log.i(TAG, String.format("追扣钱包消费流程 -->交易金额:%d,钱包余额:%d,钱包流水号:%d", lngPayMoney, s_CardChaseBurseInfo.lngBurseMoney, s_CardChaseBurseInfo.iBurseSID));

        return 0;
    }

    //追扣钱包消费校验检测
    public static int Burse_ChaseConsumeCheck(long lngPayMoney) {
        return 0;
    }

    //追扣钱包消费
    public static int Burse_ChaseConsumeOperate(long lngPayMoney) {
        int i;
        int cResult;
        long lngDayTotalMon;
        long iDayPaymentCount;
        CardBurseInfo pCardBurseInfo = new CardBurseInfo();

        //pCardBurseInfo=s_CardChaseBurseInfo;
        try {
            pCardBurseInfo = (CardBurseInfo) s_CardChaseBurseInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "追扣钱包扣款金额:" + lngPayMoney);

        //组织钱包信息
        pCardBurseInfo.lngBurseMoney = (int) (s_CardChaseBurseInfo.lngBurseMoney - lngPayMoney);        //钱包余额	3		单位分，考虑正负数

        pCardBurseInfo.iBurseSID = s_CardChaseBurseInfo.iBurseSID + 1;            //交易流水号	2

        pCardBurseInfo.iSubsidySID = s_CardChaseBurseInfo.iSubsidySID;            //补助版本号

        //末笔交易日期6位年4位月5位日5位时6位分6位秒
        pCardBurseInfo.iLastPayDate = CardPublic.GetCurrentCardDate(s_cCurDateTime);

        //判断是否是当日的
        //日累次数、日累金额
        cResult = CardPublic.CompareDateSame(s_CardChaseBurseInfo.iLastPayDate, s_cCurDateTime);
        if (cResult == 0) {
            Log.i(TAG, "同一天");
            iDayPaymentCount = s_CardChaseBurseInfo.iDayPaymentCount + 1;
            if (iDayPaymentCount > 0xff) {
                iDayPaymentCount = 0xff;
            }
            pCardBurseInfo.iDayPaymentCount = (int) (iDayPaymentCount & 0xff);        //日累次数	1

            lngDayTotalMon = (s_CardChaseBurseInfo.lngDayPaymentTotal + lngPayMoney);
            if (lngDayTotalMon > 0xffff) {
                lngDayTotalMon = 0xffff;
            }
            Log.i(TAG, String.format("同一天：%d", lngDayTotalMon));

            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0x00ffff);//日累金额	2		以角为单位
            Log.i(TAG, "CardBurseInfo.wDayTotalMon: " + pCardBurseInfo.lngDayPaymentTotal);
        } else {
            Log.i(TAG, "不是同一天");
            pCardBurseInfo.iDayPaymentCount = 1;                        //日累次数	1
            lngDayTotalMon = (lngPayMoney);
            if (lngDayTotalMon > 0xffff) {
                lngDayTotalMon = 0xffff;
            }
            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0xffff);//日累金额	2		以角为单位
        }
        //末笔营业号
        pCardBurseInfo.iLastBusinessID = s_CardChaseBurseInfo.iLastBusinessID;

        //写钱包交易信息区数据
        cResult = WriteChaseBurseInfoData(pCardBurseInfo, g_CardAttr);
        if (cResult == 0) {
            //写钱包交易信息成功
            try {
                s_CardChaseBurseInfo = (CardBurseInfo) pCardBurseInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return 0;
        } else {
            if (cResult == 71) {
                Log.i(TAG, "写钱包数据正本成功,副本失败 ");
                return 71;
            } else {
                Log.i(TAG, "写钱包交易信息区数据失败:" + cResult);
                return 62;
            }
        }
    }

    //--------------------------钱包余额复位功能集合----------------------------//
    //卡片钱包余额复位
    public static int Burse_ResetEWalletProcess(CardBasicParaInfo CardBasicInfo) {
        int cResult;
        int cBurseID;
        long lngBurseResetMoney;

        cBurseID = g_StationInfo.cWorkBurseID;
        lngBurseResetMoney = s_CardBurseInfo.lngBurseMoney;
        Log.i(TAG, "复位金额:" + lngBurseResetMoney);

        //钱包消费
        cResult = Burse_WorkResetOperate(lngBurseResetMoney);
        if (cResult != 0) {
            return cResult;
        }

        //组织需要传出的参数数据
        //组织卡片数据
        //基本扇区
        CardBasicInfo.cAgentID = s_CardBaseData.cAgentID;                //代理号
        CardBasicInfo.iGuestID = s_CardBaseData.iGuestID;               //客户号
        CardBasicInfo.cAuthenVer = s_CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
        //卡认证码	4
        System.arraycopy(s_CardBaseData.cCardAuthenCode, 0, CardBasicInfo.cCardAuthenCode, 0, 4);
        CardBasicInfo.cCardState = s_CardBaseData.cCardState;         //卡片状态	1
        CardBasicInfo.lngCardID = s_CardBaseData.lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
        CardBasicInfo.cCampusID = s_CardBaseData.cCampusID;         //园区号	1	范围为1~250
        CardBasicInfo.cStatusID = s_CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64
        CardBasicInfo.iValidTime = s_CardBaseData.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
        //基本扇区第1块内容
        System.arraycopy(s_CardBaseData.bBasic1Context, 0, CardBasicInfo.bBasic1Context, 0, 16);

        CardBasicInfo.lngAccountID = s_CardBaseData.lngAccountID;       //帐号	4	1～4294967296
        CardBasicInfo.lngPaymentPsw = s_CardBaseData.lngPaymentPsw;       //交易密码	3	六位数字密码
        CardBasicInfo.cCardStructVer = s_CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
        //基本扇区第2块内容
        System.arraycopy(s_CardBaseData.bBasic2Context, 0, CardBasicInfo.bBasic2Context, 0, 16);

        //扩展扇区
        //卡户姓名
        System.arraycopy(s_CardExtentData.cAccName, 0, CardBasicInfo.cAccName, 0, 16);
        //性别
        System.arraycopy(s_CardExtentData.cSexState, 0, CardBasicInfo.cSexState, 0, 2);
        //开户日期
        System.arraycopy(s_CardExtentData.cCreateCardDate, 0, CardBasicInfo.cCreateCardDate, 0, 3);
        CardBasicInfo.iDepartID = s_CardExtentData.iDepartID;            //部门编号
        //第三方对接关键字
        System.arraycopy(s_CardExtentData.cOtherLinkID, 0, CardBasicInfo.cOtherLinkID, 0, 10);
        //个人编号
        System.arraycopy(s_CardExtentData.cCardPerCode, 0, CardBasicInfo.cCardPerCode, 0, 16);

        //钱包信息
        //工作钱包信息
        CardBasicInfo.lngWorkBurseMoney = s_CardBurseInfo.lngBurseMoney;             //工作钱包余额
        CardBasicInfo.iWorkSubsidySID = s_CardBurseInfo.iSubsidySID;               //补助流水号
        CardBasicInfo.iWorkBurseSID = s_CardBurseInfo.iBurseSID;                    //工作钱包流水号
        CardBasicInfo.iWorkLastPayDate = s_CardBurseInfo.iLastPayDate;              //工作钱包末笔交易日期
        CardBasicInfo.lngWorkDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额
        CardBasicInfo.iWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;           //末笔交易营业号
        CardBasicInfo.iWorkDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardBurseInfo.cBurseAuthen, 0, CardBasicInfo.cWorkBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardBurseInfo.bBurseContext, 0, CardBasicInfo.bWorkBurseContext, 0, 16);
        CardBasicInfo.lngPayMoney = lngBurseResetMoney;

        return 0;
    }

    //钱包余额复位操作
    public static int Burse_WorkResetOperate(long lngResetMoney) {
        int cResult;

        CardBurseInfo pCardBurseInfo = new CardBurseInfo();
        try {
            pCardBurseInfo = (CardBurseInfo) s_CardBurseInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        //组织钱包信息
        /*
        long lngBurseMoney;             //钱包余额   3
        int iSubsidySID;               //补助流水号  2
        int iBurseSID;		        	//钱包流水号 2
        int iLastPayDate;              //钱包末笔交易日期   2
        long lngDayPaymentTotal;         //当日消费累计额   2
        int  iLastBusinessID;            //最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
        int  iDayPaymentCount;			//当日消费累计次   1
        int  cBurseAuthen[3];            //钱包认证码
        */

        //钱包余额	3
        pCardBurseInfo.lngBurseMoney = (int) (s_CardBurseInfo.lngBurseMoney - lngResetMoney);

        //2017.2.9余额复位超过两字节流水记两笔
        if ((lngResetMoney > 65535) && ((lngResetMoney % 100) != 0)) {
            //交易流水号	2
            pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 2;
        } else {
            //交易流水号	2
            pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 1;
        }
        //末笔交易日期6位年4位月5位日5位时6位分6位秒
        pCardBurseInfo.iLastPayDate = CardPublic.GetCurrentCardDate(s_cCurDateTime);

        //当日消费累计额   2
        pCardBurseInfo.lngDayPaymentTotal = 0;
        //末笔交易时段号	1
        pCardBurseInfo.iLastBusinessID = s_CardBurseInfo.iLastBusinessID;        //最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
        //当日消费累计次   1
        pCardBurseInfo.iDayPaymentCount = 0;        //日累金额	2		以角为单位

        //写钱包交易信息区数据
        cResult = WriteBurseInfoData(pCardBurseInfo, g_CardAttr);
        if (cResult == 0) {
            //写钱包交易信息成功
            try {
                s_CardBurseInfo = (CardBurseInfo) pCardBurseInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            return 62;
        }
        return 0;
    }

    //--------------------------补助功能集合----------------------------//
    //钱包补助流程
    public static int Burse_SubsidyProcess(int wSubSID, int cSubBurseID, long lngSubMoney, CardBasicParaInfo pCardBasicInfo) {
        int cResult;
        int wSubsidySum;

        //钱包余额校验
        cResult = Burse_SubsidyCheck(lngSubMoney);
        if (cResult != 0) {
            return cResult;
        }
        wSubsidySum = wSubSID - pCardBasicInfo.iWorkSubsidySID;

        //钱包操作
        cResult = Burse_SubsidyOperate(wSubSID, lngSubMoney);
        if (cResult != 0) {
            return cResult;
        }
        //组织需要传出的参数数据
        //基本扇区
        pCardBasicInfo.cAgentID = s_CardBaseData.cAgentID;                //代理号
        pCardBasicInfo.iGuestID = s_CardBaseData.iGuestID;               //客户号
        pCardBasicInfo.cAuthenVer = s_CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
        //卡认证码	4
        System.arraycopy(s_CardBaseData.cCardAuthenCode, 0, pCardBasicInfo.cCardAuthenCode, 0, 4);
        pCardBasicInfo.cCardState = s_CardBaseData.cCardState;         //卡片状态	1
        pCardBasicInfo.lngCardID = s_CardBaseData.lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
        pCardBasicInfo.cCampusID = s_CardBaseData.cCampusID;         //园区号	1	范围为1~250
        pCardBasicInfo.cStatusID = s_CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64
        pCardBasicInfo.iValidTime = s_CardBaseData.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
        //基本扇区第1块内容
        System.arraycopy(s_CardBaseData.bBasic1Context, 0, pCardBasicInfo.bBasic1Context, 0, 16);

        pCardBasicInfo.lngAccountID = s_CardBaseData.lngAccountID;       //帐号	4	1～4294967296
        pCardBasicInfo.lngPaymentPsw = s_CardBaseData.lngPaymentPsw;       //交易密码	3	六位数字密码
        pCardBasicInfo.cCardStructVer = s_CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
        //基本扇区第2块内容
        System.arraycopy(s_CardBaseData.bBasic2Context, 0, pCardBasicInfo.bBasic2Context, 0, 16);

        //扩展扇区
        //卡户姓名
        System.arraycopy(s_CardExtentData.cAccName, 0, pCardBasicInfo.cAccName, 0, 16);
        //性别
        System.arraycopy(s_CardExtentData.cSexState, 0, pCardBasicInfo.cSexState, 0, 2);
        //开户日期
        System.arraycopy(s_CardExtentData.cCreateCardDate, 0, pCardBasicInfo.cCreateCardDate, 0, 3);
        pCardBasicInfo.iDepartID = s_CardExtentData.iDepartID;            //部门编号
        //第三方对接关键字
        System.arraycopy(s_CardExtentData.cOtherLinkID, 0, pCardBasicInfo.cOtherLinkID, 0, 10);
        //个人编号
        System.arraycopy(s_CardExtentData.cCardPerCode, 0, pCardBasicInfo.cCardPerCode, 0, 16);

        //钱包信息
        //工作钱包信息
        pCardBasicInfo.lngWorkBurseMoney = s_CardBurseInfo.lngBurseMoney;             //工作钱包余额
        pCardBasicInfo.iWorkSubsidySID = s_CardBurseInfo.iSubsidySID;               //补助流水号
        pCardBasicInfo.iWorkBurseSID = s_CardBurseInfo.iBurseSID;                    //工作钱包流水号
        pCardBasicInfo.iWorkLastPayDate = s_CardBurseInfo.iLastPayDate;              //工作钱包末笔交易日期
        pCardBasicInfo.lngWorkDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额
        pCardBasicInfo.iWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;           //末笔交易营业号
        pCardBasicInfo.iWorkDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardBurseInfo.cBurseAuthen, 0, pCardBasicInfo.cWorkBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardBurseInfo.bBurseContext, 0, pCardBasicInfo.bWorkBurseContext, 0, 16);  //当前钱包块的内容

        pCardBasicInfo.lngPayMoney = lngSubMoney;                        //交易金额
        pCardBasicInfo.lngManageMoney = 0;                            //管理费金额
        pCardBasicInfo.lngPriMoney = 0;                                //优惠金额
        pCardBasicInfo.lngSubMoney = lngSubMoney;                     //补助金额
        pCardBasicInfo.wSubsidySum = wSubsidySum;                     //补助笔数

        return 0;
    }

    //钱包补助校验检测
    public static int Burse_SubsidyCheck(long lngSubMoney) {
        long lngBurseMoney;

        lngBurseMoney = s_CardBurseInfo.lngBurseMoney + lngSubMoney;

        if (lngBurseMoney > 0x7FFFFF) {
            return 3;
        }
        return 0;
    }

    //钱包补助操作
    public static int Burse_SubsidyOperate(int wSubSID, long lngSubMoney) {
        int cResult;
        CardBurseInfo pCardBurseInfo = new CardBurseInfo();

        //设置钱包信息
        try {
            pCardBurseInfo = (CardBurseInfo) s_CardBurseInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        //更新余额
        pCardBurseInfo.lngBurseMoney = (int) (s_CardBurseInfo.lngBurseMoney + lngSubMoney);        //钱包余额	3		单位分，考虑正负数
        pCardBurseInfo.iSubsidySID = wSubSID;            //补助流水号	2
        pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 1;            //交易流水号

        //末笔交易日期6位年4位月5位日5位时6位分6位秒
        pCardBurseInfo.iLastPayDate = CardPublic.GetCurrentCardDate(s_cCurDateTime);

        pCardBurseInfo.lngDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额   2
        pCardBurseInfo.iLastBusinessID = s_CardBurseInfo.iLastBusinessID;            //末笔营业号(1~127)
        pCardBurseInfo.iDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次   1
        //写钱包交易信息区数据
        cResult = WriteBurseInfoData(pCardBurseInfo, g_CardAttr);
        if (cResult == 0) {
            //写钱包交易信息成功
            try {
                s_CardBurseInfo = (CardBurseInfo) pCardBurseInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            return 62;
        }
        return 0;
    }

//--------------------------充值功能集合----------------------------//


    //--------------------------信息圈存功能集合----------------------------//
    //信息圈存操作
    public static int Card_InfoSyncOperate(CardBasicParaInfo pCardBasicInfo) {
        int cResult = 0;

        //基础应用信息
        CardBaseInfo pCardBaseData = new CardBaseInfo();
        //扩展应用信息
        CardExtentInfo pCardExtentData = new CardExtentInfo();

        try {
            pCardBaseData = (CardBaseInfo) s_CardBaseData.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        try {
            pCardExtentData = (CardExtentInfo) s_CardExtentData.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        //组织卡片数据
        /*
        园区号	1字节
        身份编号	1字节
        卡户姓名	16字节
        性别	2字节
        开户日期	3字节
        部门编号	2字节
        第三方关键字	10字节
        个人编号	16字节
        有效时限	3字节
        主卡帐号	4字节
        交易密码	3字节
        */
        //基础应用信息
        pCardBaseData.cCampusID = pCardBasicInfo.cCampusID;         //园区号	1	范围为1~250
        pCardBaseData.cStatusID = pCardBasicInfo.cStatusID;          //身份编号	1	最大64种，1~64
        pCardBaseData.iValidTime = pCardBasicInfo.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)

        pCardBaseData.lngAccountID = pCardBasicInfo.lngAccountID;       //帐号	4	1～4294967296
        pCardBaseData.lngPaymentPsw = pCardBasicInfo.lngPaymentPsw;       //交易密码	3	六位数字密码


        //扩展应用信息
        System.arraycopy(pCardBasicInfo.cAccName, 0, pCardExtentData.cAccName, 0, 16);//卡户姓名
        System.arraycopy(pCardBasicInfo.cSexState, 0, pCardExtentData.cSexState, 0, 2);  //性别
        System.arraycopy(pCardBasicInfo.cCreateCardDate, 0, pCardExtentData.cCreateCardDate, 0, 3);    //开户日期 年月日
        pCardExtentData.iDepartID = pCardBasicInfo.iDepartID;            //部门编号
        System.arraycopy(pCardBasicInfo.cOtherLinkID, 0, pCardExtentData.cOtherLinkID, 0, 10);       //第三方对接关键字
        System.arraycopy(pCardBasicInfo.cCardPerCode, 0, pCardExtentData.cCardPerCode, 0, 16);      //个人编号

//        //判断同步内容是否一致,不一致则写卡
//        if((memcmp((void*)(&pCardBaseData), (void*)(&s_CardBaseData), sizeof(ST_CARDBASEINFO))==0)
//        &&(memcmp((void*)(&pCardExtentData), (void*)(&s_CardExtentData), sizeof(ST_CARDEXTENTINFO))==0))
//        //if((pCardBaseData==s_CardBaseData)&&(pCardExtentData==s_CardExtentData))
//        {
//            Log.i(TAG,"同步数据相同，不需要写卡");
//            return 1;
//        }
//        else
//        {
//            Log.i(TAG,"同步数据不相同，需要写卡");
//        }

        //simpass uimpass NFC、湖南建行卡
//        if ((g_CardAttr.cCardType == 4) || (g_CardAttr.cCardType == 5) || (g_CardAttr.cCardType == 6)) {
//            Log.i(TAG, "写单钱包信息区数据");
//            //WriteSingleBurseCardInfoData(pCardBaseData,pCardExtentData,g_CardAttr);
//            if (cResult != 0) {
//                return 62;
//            }
//        }

        //写基础应用信息区数据
        Log.i(TAG, "写基础应用信息区数据");
        cResult = WriteBaseInfoData(pCardBaseData, g_CardAttr);
        if (cResult != 0) {
            return 62;
        }
        try {
            s_CardBaseData = (CardBaseInfo) pCardBaseData.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        //写扩展应用信息
        Log.i(TAG, "写扩展应用信息数据");
        cResult = WriteExtentInfoData(pCardExtentData, g_CardAttr);
        if (cResult != 0) {
            return 62;
        }
        try {
            s_CardExtentData = (CardExtentInfo) pCardExtentData.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //--------------------------工作钱包冲正功能集合----------------------------//
    //工作钱包冲正流程
    public static int Burse_WorkConsumeReverProcess(long lngRePayMoney, long lngReManageMoney, CardBasicParaInfo CardBasicInfo) {
        int cResult;

        Log.i(TAG, "卡片类型:" + g_CardAttr.cCardType);
        //钱包冲正
        cResult = Burse_WorkConsumeReverOperate(lngRePayMoney, lngReManageMoney);
        if (cResult != 0) {
            return cResult;
        }

        //组织需要传出的参数数据
        //基本扇区
        CardBasicInfo.cAgentID = s_CardBaseData.cAgentID;                //代理号
        CardBasicInfo.iGuestID = s_CardBaseData.iGuestID;               //客户号
        CardBasicInfo.cAuthenVer = s_CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
        //卡认证码	4
        System.arraycopy(s_CardBaseData.cCardAuthenCode, 0, CardBasicInfo.cCardAuthenCode, 0, 4);
        CardBasicInfo.cCardState = s_CardBaseData.cCardState;         //卡片状态	1
        CardBasicInfo.lngCardID = s_CardBaseData.lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
        CardBasicInfo.cCampusID = s_CardBaseData.cCampusID;         //园区号	1	范围为1~250
        CardBasicInfo.cStatusID = s_CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64
        CardBasicInfo.iValidTime = s_CardBaseData.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
        //基本扇区第1块内容
        System.arraycopy(s_CardBaseData.bBasic1Context, 0, CardBasicInfo.bBasic1Context, 0, 16);

        CardBasicInfo.lngAccountID = s_CardBaseData.lngAccountID;       //帐号	4	1～4294967296
        CardBasicInfo.lngPaymentPsw = s_CardBaseData.lngPaymentPsw;       //交易密码	3	六位数字密码
        CardBasicInfo.cCardStructVer = s_CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
        //基本扇区第2块内容
        System.arraycopy(s_CardBaseData.bBasic2Context, 0, CardBasicInfo.bBasic2Context, 0, 16);

        //扩展扇区
        //卡户姓名
        System.arraycopy(s_CardExtentData.cAccName, 0, CardBasicInfo.cAccName, 0, 16);
        //性别
        System.arraycopy(s_CardExtentData.cSexState, 0, CardBasicInfo.cSexState, 0, 2);
        //开户日期
        System.arraycopy(s_CardExtentData.cCreateCardDate, 0, CardBasicInfo.cCreateCardDate, 0, 3);
        CardBasicInfo.iDepartID = s_CardExtentData.iDepartID;            //部门编号
        //第三方对接关键字
        System.arraycopy(s_CardExtentData.cOtherLinkID, 0, CardBasicInfo.cOtherLinkID, 0, 10);
        //个人编号
        System.arraycopy(s_CardExtentData.cCardPerCode, 0, CardBasicInfo.cCardPerCode, 0, 16);

        //钱包信息
        //工作钱包信息
        CardBasicInfo.lngWorkBurseMoney = s_CardBurseInfo.lngBurseMoney;             //工作钱包余额
        CardBasicInfo.iWorkSubsidySID = s_CardBurseInfo.iSubsidySID;               //补助流水号
        CardBasicInfo.iWorkBurseSID = s_CardBurseInfo.iBurseSID;                    //工作钱包流水号
        CardBasicInfo.iWorkLastPayDate = s_CardBurseInfo.iLastPayDate;              //工作钱包末笔交易日期
        CardBasicInfo.lngWorkDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额
        CardBasicInfo.iWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;           //末笔交易营业号
        CardBasicInfo.iWorkDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardBurseInfo.cBurseAuthen, 0, CardBasicInfo.cWorkBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardBurseInfo.bBurseContext, 0, CardBasicInfo.bWorkBurseContext, 0, 16);  //当前钱包块的内容

        CardBasicInfo.lngPayMoney = lngRePayMoney + lngReManageMoney;
        CardBasicInfo.lngWorkPayMoney = lngRePayMoney + lngReManageMoney;
        CardBasicInfo.lngManageMoney = lngReManageMoney;

        return 0;
    }

    //工作钱包冲正
    public static int Burse_WorkConsumeReverOperate(long lngRePayMoney, long lngReManageMoney) {
        int cResult;
        long lngDayTotalMon = 0;         //日累计金额
        int iDayPaymentCount = 0;           //日累次数
        int cBurseID;

        CardBurseInfo pCardBurseInfo = new CardBurseInfo();
        cBurseID = g_StationInfo.cWorkBurseID - 1;

        //组织钱包信息
        /*
        long lngBurseMoney;             //钱包余额   3
        int iSubsidySID;               //补助流水号  2
        int iBurseSID;		        	//钱包流水号 2
        int iLastPayDate;              //钱包末笔交易日期   2
        long lngDayPaymentTotal;         //当日消费累计额   2
        int  iLastBusinessID;            //最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
        int  iDayPaymentCount;			//当日消费累计次   1
        int  cBurseAuthen[3];            //钱包认证码     3

        int bBurseContext[16];           //当前钱包块的内容
        */

        Log.i(TAG, "卡片类型:" + g_CardAttr.cCardType);
        pCardBurseInfo.lngBurseMoney = (int) (s_CardBurseInfo.lngBurseMoney + (lngRePayMoney + lngReManageMoney));        //钱包余额	3		单位分，考虑正负数

        //判断是否有管理费
        if (lngReManageMoney != 0) {
            Log.i(TAG, "有管理费");
            pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 2;            //交易流水号
        } else {
            pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 1;            //交易流水号
        }

        pCardBurseInfo.iSubsidySID = s_CardBurseInfo.iSubsidySID;            //补助版本号

        //末笔交易日期6位年4位月5位日5位时6位分6位秒
        pCardBurseInfo.iLastPayDate = CardPublic.GetCurrentCardDate(s_cCurDateTime);

        //判断是否是当日的
        //日累次数、日累金额
        cResult = CardPublic.CompareDateSame(s_CardBurseInfo.iLastPayDate, s_cCurDateTime);
        if (cResult == 0) {
            Log.i(TAG, "同一天");
            iDayPaymentCount = s_CardBurseInfo.iDayPaymentCount + 1;
            if (iDayPaymentCount > 0xff) {
                iDayPaymentCount = 0xff;
            }
            pCardBurseInfo.iDayPaymentCount = (iDayPaymentCount & 0xff);        //日累次数	1

            if (s_CardBurseInfo.lngDayPaymentTotal >= lngRePayMoney) {
                lngDayTotalMon = (s_CardBurseInfo.lngDayPaymentTotal - lngRePayMoney);
                if (lngDayTotalMon > 0xffff) {
                    lngDayTotalMon = 0xffff;
                }
            } else {
                Log.i(TAG, "lngDayTotalMon:" + lngDayTotalMon);
                lngDayTotalMon = 0;
            }
            Log.i(TAG, String.format("同一天：%d %d", lngDayTotalMon, s_CardBurseInfo.lngDayPaymentTotal));

            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0x00ffff);//日累金额	2		以角为单位
            Log.i(TAG, String.format("CardBurseInfo.wDayTotalMon: %d", pCardBurseInfo.lngDayPaymentTotal));
        } else {
            Log.i(TAG, "不是同一天");
            pCardBurseInfo.iDayPaymentCount = 1;                        //日累次数	1

            lngDayTotalMon = (lngRePayMoney);
            if (lngDayTotalMon > 0xffff) {
                lngDayTotalMon = 0xffff;
            }
            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0xffff);//日累金额	2		以角为单位
        }

        pCardBurseInfo.iLastBusinessID = s_CardBurseInfo.iLastBusinessID;

        Log.i(TAG, String.format("日累次数:%d,日累金额:%d,卡片类型%d", pCardBurseInfo.iDayPaymentCount, pCardBurseInfo.lngDayPaymentTotal, g_CardAttr.cCardType));
        //写钱包交易信息区数据
        cResult = WriteBurseInfoData(pCardBurseInfo, g_CardAttr);
        if (cResult == 0) {
            //写钱包交易信息成功
            try {
                s_CardBurseInfo = (CardBurseInfo) pCardBurseInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            return 62;
        }
        return 0;
    }

    //--------------------------追扣钱包冲正功能集合----------------------------//
    //追扣钱包冲正流程
    public static int Burse_ChaseConsumeReverProcess(long lngRePayMoney, CardBasicParaInfo CardBasicInfo) {
        int cResult;

        //追扣钱包冲正
        cResult = Burse_ChaseConsumeReverOperate(lngRePayMoney);
        if (cResult != 0) {
            return cResult;
        }
        //组织需要传出的参数数据
        //钱包交易信息区
        CardBasicInfo.lngChaseBurseMoney = s_CardChaseBurseInfo.lngBurseMoney;        //钱包余额	3		单位分，考虑正负数
        CardBasicInfo.iChaseBurseSID = s_CardChaseBurseInfo.iBurseSID;            //交易流水号	2
        CardBasicInfo.iChaseLastPayDate = s_CardChaseBurseInfo.iLastPayDate;    //末笔交易日期	4		6位年4位月5位日5位时6位分6位秒

        CardBasicInfo.lngChasePayMoney = lngRePayMoney;
        return 0;
    }

    //追扣钱包冲正
    public static int Burse_ChaseConsumeReverOperate(long lngRePayMoney) {
        int cResult;
        long lngDayTotalMon = 0;
        long iDayPaymentCount = 0;

        CardBurseInfo pCardBurseInfo = new CardBurseInfo();
        try {
            pCardBurseInfo = (CardBurseInfo) s_CardChaseBurseInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        //组织钱包信息
        pCardBurseInfo.lngBurseMoney = (int) (s_CardChaseBurseInfo.lngBurseMoney + lngRePayMoney);        //钱包余额	3		单位分，考虑正负数

        pCardBurseInfo.iBurseSID = s_CardChaseBurseInfo.iBurseSID + 1;            //交易流水号	2

        pCardBurseInfo.iSubsidySID = s_CardChaseBurseInfo.iSubsidySID;            //补助版本号

        //末笔交易日期6位年4位月5位日5位时6位分6位秒
        pCardBurseInfo.iLastPayDate = CardPublic.GetCurrentCardDate(s_cCurDateTime);

        //判断是否是当日的
        //日累次数、日累金额
        cResult = CardPublic.CompareDateSame(s_CardChaseBurseInfo.iLastPayDate, s_cCurDateTime);
        if (cResult == 0) {
            Log.i(TAG, "同一天");
            iDayPaymentCount = s_CardChaseBurseInfo.iDayPaymentCount + 1;
            if (iDayPaymentCount > 0xff) {
                iDayPaymentCount = 0xff;
            }
            pCardBurseInfo.iDayPaymentCount = (int) (iDayPaymentCount & 0xff);        //日累次数	1

            if (s_CardChaseBurseInfo.lngDayPaymentTotal >= lngRePayMoney) {
                lngDayTotalMon = (s_CardChaseBurseInfo.lngDayPaymentTotal - lngRePayMoney);
                if (lngDayTotalMon > 0xffff) {
                    lngDayTotalMon = 0xffff;
                }
            } else {
                Log.i(TAG, "lngDayTotalMon:" + lngDayTotalMon);
                lngDayTotalMon = 0;
            }

            Log.i(TAG, String.format("同一天：%d %d", lngDayTotalMon, s_CardChaseBurseInfo.lngDayPaymentTotal));

            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0x00ffff);//日累金额	2		以角为单位
            Log.i(TAG, "CardBurseInfo.wDayTotalMon: " + pCardBurseInfo.lngDayPaymentTotal);
        } else {
            Log.i(TAG, "不是同一天不允许冲正");
            pCardBurseInfo.iDayPaymentCount = 1;                        //日累次数	1
            lngDayTotalMon = 0;
            pCardBurseInfo.lngDayPaymentTotal = (lngDayTotalMon & 0xffff);//日累金额	2		以角为单位
        }

        //末笔营业号
        pCardBurseInfo.iLastBusinessID = s_CardChaseBurseInfo.iLastBusinessID;

        //写钱包交易信息区数据
        cResult = WriteChaseBurseInfoData(pCardBurseInfo, g_CardAttr);
        if (cResult == 0) {
            //写钱包交易信息成功
            try {
                s_CardChaseBurseInfo = (CardBurseInfo) pCardBurseInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            return 62;
        }
        return 0;
    }

    //--------------------------工作钱包现金充值功能集合----------------------------//
    //工作钱包现金充值流程
    public static int Burse_MoneyPointProcess(long lngPayMoney, CardBasicParaInfo CardBasicInfo) {
        int cResult;

        //钱包余额校验
        cResult = Burse_MoneyPointCheck(lngPayMoney);
        if (cResult != 0) {
            return cResult;
        }

        //钱包现金充值
        cResult = Burse_MoneyPointOperate(s_lngPayMoney);
        if (cResult != 0) {
            return cResult;
        }

        //组织卡片数据
        //基本扇区
        CardBasicInfo.cAgentID = s_CardBaseData.cAgentID;                //代理号
        CardBasicInfo.iGuestID = s_CardBaseData.iGuestID;               //客户号
        CardBasicInfo.cAuthenVer = s_CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
        //卡认证码	4
        System.arraycopy(s_CardBaseData.cCardAuthenCode, 0, CardBasicInfo.cCardAuthenCode, 0, 4);
        CardBasicInfo.cCardState = s_CardBaseData.cCardState;         //卡片状态	1
        CardBasicInfo.lngCardID = s_CardBaseData.lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
        CardBasicInfo.cCampusID = s_CardBaseData.cCampusID;         //园区号	1	范围为1~250
        CardBasicInfo.cStatusID = s_CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64
        CardBasicInfo.iValidTime = s_CardBaseData.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
        //基本扇区第1块内容
        System.arraycopy(s_CardBaseData.bBasic1Context, 0, CardBasicInfo.bBasic1Context, 0, 16);

        CardBasicInfo.lngAccountID = s_CardBaseData.lngAccountID;       //帐号	4	1～4294967296
        CardBasicInfo.lngPaymentPsw = s_CardBaseData.lngPaymentPsw;       //交易密码	3	六位数字密码
        CardBasicInfo.cCardStructVer = s_CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
        //基本扇区第2块内容
        System.arraycopy(s_CardBaseData.bBasic2Context, 0, CardBasicInfo.bBasic2Context, 0, 16);

        //扩展扇区
        //卡户姓名
        System.arraycopy(s_CardExtentData.cAccName, 0, CardBasicInfo.cAccName, 0, 16);
        //性别
        System.arraycopy(s_CardExtentData.cSexState, 0, CardBasicInfo.cSexState, 0, 2);
        //开户日期
        System.arraycopy(s_CardExtentData.cCreateCardDate, 0, CardBasicInfo.cCreateCardDate, 0, 3);
        CardBasicInfo.iDepartID = s_CardExtentData.iDepartID;            //部门编号
        //第三方对接关键字
        System.arraycopy(s_CardExtentData.cOtherLinkID, 0, CardBasicInfo.cOtherLinkID, 0, 10);
        //个人编号
        System.arraycopy(s_CardExtentData.cCardPerCode, 0, CardBasicInfo.cCardPerCode, 0, 16);

        //钱包信息
        //工作钱包信息
        CardBasicInfo.lngWorkBurseMoney = s_CardBurseInfo.lngBurseMoney;             //工作钱包余额
        CardBasicInfo.iWorkSubsidySID = s_CardBurseInfo.iSubsidySID;               //补助流水号
        CardBasicInfo.iWorkBurseSID = s_CardBurseInfo.iBurseSID;                    //工作钱包流水号
        CardBasicInfo.iWorkLastPayDate = s_CardBurseInfo.iLastPayDate;              //工作钱包末笔交易日期
        CardBasicInfo.lngWorkDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额
        CardBasicInfo.iWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;           //末笔交易营业号
        CardBasicInfo.iWorkDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardBurseInfo.cBurseAuthen, 0, CardBasicInfo.cWorkBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardBurseInfo.bBurseContext, 0, CardBasicInfo.bWorkBurseContext, 0, 16);  //当前钱包块的内容

        CardBasicInfo.lngPayMoney = s_lngPayMoney;
        CardBasicInfo.lngWorkPayMoney = s_lngPayMoney;       //工作钱包交易金额
        CardBasicInfo.lngChasePayMoney = 0;                  //追扣钱包交易金额
        CardBasicInfo.lngManageMoney = s_lngManageMoney;
        CardBasicInfo.lngPriMoney = s_lngPriMoney;
        return 0;
    }

    //现金充值工作钱包余额校验
    public static int Burse_MoneyPointCheck(long lngPayMoney) {
        long lngMoneyTemp;
        int cWorkBurseID;
        long lngOnPermitLimit;
        long lngOffPermitLimit;
        long lngBurseMoneyLim;

        s_lngWorkPayMoney = 0;        //追扣时工作钱包交易金额
        s_lngChasePayMoney = 0;        //追扣时追扣钱包交易金额
        s_lngPriMoney = 0;
        s_lngManageMoney = 0;
        s_lngPayMoney = lngPayMoney;

        //判断是否收取管理费(优惠和管理费不并行存在)
        cWorkBurseID = g_StationInfo.cWorkBurseID;


        lngMoneyTemp = lngPayMoney - (lngPayMoney * s_StatusInfo.StatusInfolist.get(cWorkBurseID - 1).cFundMoneyRate / 100);

        s_lngManageMoney = lngPayMoney - lngMoneyTemp;
        s_lngPayMoney = lngMoneyTemp;

        lngOnPermitLimit = g_StationInfo.lngOnPermitLimit;
        lngOffPermitLimit = g_StationInfo.lngOffPermitLimit;
        lngBurseMoneyLim = s_StatusInfo.StatusInfolist.get(cWorkBurseID - 1).lngBurseMoneyLim;

        if (g_WorkInfo.cRunState == 1) {
            //交易额大于终端参数中的在线单笔交易限额
            if (lngPayMoney > lngOnPermitLimit) {
                Log.i(TAG, "在线单笔交易限额");
                return 06;
            }
        } else {
            //1.2 交易额大于终端参数中的脱机单笔交易限额
            if (lngPayMoney > lngOffPermitLimit) {
                Log.i(TAG, "脱机单笔交易限额");
                return 06;
            }
        }

        //余额上限检查
        if (s_CardBurseInfo.lngBurseMoney + lngPayMoney > lngBurseMoneyLim) {
            return 07;
        }

        Log.i(TAG, "优惠金额：" + s_lngPriMoney);
        Log.i(TAG, "存款管理费金额：" + s_lngManageMoney);
        Log.i(TAG, "交易金额:" + s_lngPayMoney);
        return 0;
    }

    //工作钱包现金充值
    public static int Burse_MoneyPointOperate(long lngPayMoney) {
        int cResult;
        int cBurseID;

        CardBurseInfo pCardBurseInfo = new CardBurseInfo();

        cBurseID = g_StationInfo.cWorkBurseID - 1;

        pCardBurseInfo.lngBurseMoney = s_CardBurseInfo.lngBurseMoney + ((int) lngPayMoney);        //钱包余额	3		单位分，考虑正负数
        pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 1;            //交易流水号
        pCardBurseInfo.iSubsidySID = s_CardBurseInfo.iSubsidySID;            //补助版本号
        //末笔交易日期6位年4位月5位日5位时6位分6位秒
        pCardBurseInfo.iLastPayDate = CardPublic.GetCurrentCardDate(s_cCurDateTime);
        //日累次数、日累金额
        pCardBurseInfo.iDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;
        pCardBurseInfo.lngDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;
        pCardBurseInfo.iLastBusinessID = s_CardBurseInfo.iLastBusinessID;

        //写钱包交易信息区数据
        cResult = WriteBurseInfoData(pCardBurseInfo, g_CardAttr);
        if (cResult == 0) {
            //写钱包交易信息成功
            try {
                s_CardBurseInfo = (CardBurseInfo) pCardBurseInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            return 62;
        }
        return 0;
    }

    //现金充值冲正流程
    public static int Burse_MoneyPointReverProcess(long lngRePayMoney, CardBasicParaInfo CardBasicInfo) {
        int cResult;

        //钱包冲正
        cResult = Burse_MoneyPointReverOperate(lngRePayMoney);
        if (cResult != 0) {
            return cResult;
        }

        //组织卡片数据
        //基本扇区
        CardBasicInfo.cAgentID = s_CardBaseData.cAgentID;                //代理号
        CardBasicInfo.iGuestID = s_CardBaseData.iGuestID;               //客户号
        CardBasicInfo.cAuthenVer = s_CardBaseData.cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
        //卡认证码	4
        System.arraycopy(s_CardBaseData.cCardAuthenCode, 0, CardBasicInfo.cCardAuthenCode, 0, 4);
        CardBasicInfo.cCardState = s_CardBaseData.cCardState;         //卡片状态	1
        CardBasicInfo.lngCardID = s_CardBaseData.lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
        CardBasicInfo.cCampusID = s_CardBaseData.cCampusID;         //园区号	1	范围为1~250
        CardBasicInfo.cStatusID = s_CardBaseData.cStatusID;          //身份编号	1	最大64种，1~64
        CardBasicInfo.iValidTime = s_CardBaseData.iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
        //基本扇区第1块内容
        System.arraycopy(s_CardBaseData.bBasic1Context, 0, CardBasicInfo.bBasic1Context, 0, 16);

        CardBasicInfo.lngAccountID = s_CardBaseData.lngAccountID;       //帐号	4	1～4294967296
        CardBasicInfo.lngPaymentPsw = s_CardBaseData.lngPaymentPsw;       //交易密码	3	六位数字密码
        CardBasicInfo.cCardStructVer = s_CardBaseData.cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
        //基本扇区第2块内容
        System.arraycopy(s_CardBaseData.bBasic2Context, 0, CardBasicInfo.bBasic2Context, 0, 16);

        //扩展扇区
        //卡户姓名
        System.arraycopy(s_CardExtentData.cAccName, 0, CardBasicInfo.cAccName, 0, 16);
        //性别
        System.arraycopy(s_CardExtentData.cSexState, 0, CardBasicInfo.cSexState, 0, 2);
        //开户日期
        System.arraycopy(s_CardExtentData.cCreateCardDate, 0, CardBasicInfo.cCreateCardDate, 0, 3);
        CardBasicInfo.iDepartID = s_CardExtentData.iDepartID;            //部门编号
        //第三方对接关键字
        System.arraycopy(s_CardExtentData.cOtherLinkID, 0, CardBasicInfo.cOtherLinkID, 0, 10);
        //个人编号
        System.arraycopy(s_CardExtentData.cCardPerCode, 0, CardBasicInfo.cCardPerCode, 0, 16);

        //钱包信息
        //工作钱包信息
        CardBasicInfo.lngWorkBurseMoney = s_CardBurseInfo.lngBurseMoney;             //工作钱包余额
        CardBasicInfo.iWorkSubsidySID = s_CardBurseInfo.iSubsidySID;               //补助流水号
        CardBasicInfo.iWorkBurseSID = s_CardBurseInfo.iBurseSID;                    //工作钱包流水号
        CardBasicInfo.iWorkLastPayDate = s_CardBurseInfo.iLastPayDate;              //工作钱包末笔交易日期
        CardBasicInfo.lngWorkDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;         //当日消费累计额
        CardBasicInfo.iWorkLastBusinessID = s_CardBurseInfo.iLastBusinessID;           //末笔交易营业号
        CardBasicInfo.iWorkDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;            //当日消费累计次

        System.arraycopy(s_CardBurseInfo.cBurseAuthen, 0, CardBasicInfo.cWorkBurseAuthen, 0, 3);     //钱包认证码
        System.arraycopy(s_CardBurseInfo.bBurseContext, 0, CardBasicInfo.bWorkBurseContext, 0, 16);  //当前钱包块的内容

        CardBasicInfo.lngPayMoney = lngRePayMoney;
        CardBasicInfo.lngWorkPayMoney = lngRePayMoney;

        return 0;
    }

    //现金充值冲正
    public static int Burse_MoneyPointReverOperate(long lngRePayMoney) {
        int cResult;
        long lngDayTotalMon;         //日累计金额
        int iDayPaymentCount;           //日累次数
        int cWorkLastBusinessID;
        int cBurseID;

        CardBurseInfo pCardBurseInfo = new CardBurseInfo();

        cBurseID = g_StationInfo.cWorkBurseID - 1;

        pCardBurseInfo.lngBurseMoney = (int) (s_CardBurseInfo.lngBurseMoney - (lngRePayMoney));        //钱包余额	3		单位分，考虑正负数

        pCardBurseInfo.iBurseSID = s_CardBurseInfo.iBurseSID + 1;            //交易流水号

        pCardBurseInfo.iSubsidySID = s_CardBurseInfo.iSubsidySID;            //补助版本号

        //末笔交易日期6位年4位月5位日5位时6位分6位秒
        pCardBurseInfo.iLastPayDate = CardPublic.GetCurrentCardDate(s_cCurDateTime);

        //日累次数、日累金额
        pCardBurseInfo.iDayPaymentCount = s_CardBurseInfo.iDayPaymentCount;
        pCardBurseInfo.lngDayPaymentTotal = s_CardBurseInfo.lngDayPaymentTotal;
        pCardBurseInfo.iLastBusinessID = s_CardBurseInfo.iLastBusinessID;

        Log.i(TAG, String.format("日累次数:%d,日累金额:%d", pCardBurseInfo.iDayPaymentCount, pCardBurseInfo.lngDayPaymentTotal));
        //写钱包交易信息区数据
        cResult = WriteBurseInfoData(pCardBurseInfo, g_CardAttr);
        if (cResult == 0) {
            //写钱包交易信息成功
            try {
                s_CardBurseInfo = (CardBurseInfo) pCardBurseInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            return 62;
        }
        return 0;
    }

//-----------------------------------公用函数---------------------------------------//


    //是否允许优惠
    private static int BusinessCanPriv() {
        //不启用优惠限次
        if (s_StatusWorkBurInfo.cPrivLimitOne == 0) {
            Log.i(TAG, "不启动优惠限次");
            return 1;
        }
        //启用优惠限次
        if (s_StatusWorkBurInfo.cPrivLimitOne == 1) {
            Log.i(TAG, String.format("工作营业号:%02x,卡片末笔营业号:%02x", g_WorkInfo.cBusinessID, s_CardBurseInfo.iLastBusinessID));
            if ((g_WorkInfo.cBusinessID != (s_CardBurseInfo.iLastBusinessID & 0x7F)) || ((s_CardBurseInfo.iLastBusinessID & 0x80) != 0x80)) {
                //未优惠
                Log.i(TAG, "未优惠");
                return 1;
            }
        }
        return 0;
    }

    //判断卡内的园区号是否在范围内
    public static int CampusAreaVaild(int wCampusID) {
        int i, j;
        int cResult;
        //园区范围组
        i = (wCampusID - 1) / 8;
        //取具体的值
        j = (wCampusID - 1) % 8;
        cResult = (g_StationInfo.bCampusArea[i] >> j) & 0x01;
        if (cResult == 1) {
            return 1;
        }
        return 0;
    }

    //写卡的状态
    public static int WriteCardStatus(byte cCardStatus) {
        int i;
        int cResult;
        byte cCardType;
        byte cSectorID;
        byte[] cCardSID = new byte[8];
        byte[] cCardContext = new byte[64];

        System.arraycopy(g_CardAttr.cCardSID, 0, cCardSID, 0, 4);
        cCardType = g_CardAttr.cCardType;
        cSectorID = g_CardAttr.cBasicSectorID;

        if (cCardType == 1)    //M1
        {
            //读出扇区信息
            cResult = g_Nlib.ReadMifareBlock((byte) (cSectorID * 4 + 1), cCardSID, cCardContext);
            if (cResult != 0) {
                Log.i(TAG, "读失败");
                return 2;
            }

            //卡片状态	1		0：黑，1：白
            cCardContext[8] = (byte) (cCardStatus & 0xff);

            //写Mifare卡片块数据
            cResult = g_Nlib.WriteMifareBlock((byte) (cSectorID * 4 + 1), cCardSID, cCardContext);
            if (cResult != 0) {
                Log.i(TAG, "写失败");
                return 2;
            }
        } else if (cCardType == 2)        //其他卡类型
        {
            System.arraycopy(g_CardBasicInfo.bBasic1Context, 0, cCardContext, 0, 16);
            System.arraycopy(g_CardBasicInfo.bBasic2Context, 0, cCardContext, 16, 16);
            cCardContext[8] = (byte) (cCardStatus & 0xff);

            cResult = g_Nlib.CPU_BasicAllInfo_Set(cCardContext, cCardSID);
            if (cResult != 0) {
                return 2;
            }
        } else        //其他卡类型
        {
        }
        return 0;
    }

    //信息更新后再次校验卡片数据
    public static int CheckCardInfoAgain() {
        int cResult;

        Log.i(TAG, "园区号:" + g_CardBasicInfo.cCampusID);
        // 判断园区范围
        cResult = CampusAreaVaild(g_CardBasicInfo.cCampusID);
        if (cResult != 1) {
            Log.i(TAG, "超出园区范围");
            return OUT_OF_CONSUMERANGE;
        }

        Log.i(TAG, "卡类型：" + g_CardInfo.cCardType);
        if ((g_CardInfo.cCardType != 6) && (g_CardInfo.cCardType != 5) && (g_CardInfo.cCardType != 4))//simpass卡、湖南大众建行卡、NFC卡不进行有效时间校验
        {
            // 验证有效时限
            cResult = Publicfun.LimitDateVaild(g_CardBasicInfo.iValidTime);
            if (cResult != OK) {
                return BEYOND_LIMIT_TIM;
            }
        }
        //判断身份有效性
        Log.i(TAG, "消费身份:" + g_CardBasicInfo.cStatusID);
        //判断身份的有效性
        if ((g_CardBasicInfo.cStatusID > g_StatusInfoArray.size())
                || (g_CardBasicInfo.cStatusID < 1)) {
            g_CardInfo.cAuthenState = 0;
            Log.i(TAG, "身份 无效:" + g_CardBasicInfo.cStatusID);
            return OUT_OF_CONSUMERANGE;
        }
        if (g_StatusInfoArray.get(g_CardBasicInfo.cStatusID - 1).cStatusID == 0) {
            g_CardInfo.cAuthenState = 0;
            Log.i(TAG, "身份 无效:" + g_CardBasicInfo.cStatusID);
            return OUT_OF_CONSUMERANGE;
        }
        s_StatusInfo = g_StatusInfoArray.get(g_CardBasicInfo.cStatusID - 1);
        //赋值给工作钱包和追扣钱包
        //工作钱包
        s_StatusWorkBurInfo = g_StatusInfoArray.get(g_CardBasicInfo.cStatusID - 1).StatusInfolist.get(g_StationInfo.cWorkBurseID - 1);
        //PrintStatusBurInfo(&s_StatusWorkBurInfo);
        //追扣钱包
        s_StatusChaseBurInfo = g_StatusInfoArray.get(g_CardBasicInfo.cStatusID - 1).StatusInfolist.get(g_StationInfo.cChaseBurseID - 1);
        //PrintStatusBurInfo(&s_StatusChaseBurInfo);

        if (g_CardBasicInfo.cStatusID > g_StatusPriInfoArray.size()) {
            g_CardInfo.cAuthenState = 0;
            Log.i(TAG, "身份消费限次 无效:" + g_CardBasicInfo.cStatusID);
            return OUT_OF_CONSUMERANGE;
        }
        //身份消费限次
        s_StatusPriInfo = g_StatusPriInfoArray.get(g_CardBasicInfo.cStatusID - 1);
        //PrintStatusLimitTime(&s_StatusInfo);

        return OK;
    }


}
