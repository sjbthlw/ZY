package com.hzsun.mpos.Global;

import android.os.Environment;
import android.os.Handler;
import android.view.Display;

import com.hzsun.mpos.CardWork.CardAttrInfo;
import com.hzsun.mpos.CardWork.CardBasicParaInfo;
import com.hzsun.mpos.CardWork.CardInfo;
import com.hzsun.mpos.Http.HttpCommInfo;
import com.hzsun.mpos.data.BWListAllInfo;
import com.hzsun.mpos.data.BasicInfo;
import com.hzsun.mpos.data.BuinessInfo;
import com.hzsun.mpos.data.BurseInfo;
import com.hzsun.mpos.data.FTPInfo;
import com.hzsun.mpos.data.FaceCodeInfo;
import com.hzsun.mpos.data.FaceIdentInfo;
import com.hzsun.mpos.data.FacePayInfo;
import com.hzsun.mpos.data.LastOrderInfo;
import com.hzsun.mpos.data.LastRecordPayInfo;
import com.hzsun.mpos.data.LocalInfo;
import com.hzsun.mpos.data.LocalNetInfo;
import com.hzsun.mpos.data.LocalNetStrInfo;
import com.hzsun.mpos.data.OddKeyInfo;
import com.hzsun.mpos.data.OnlinePayInfo;
import com.hzsun.mpos.data.QRCodeALIInfo;
import com.hzsun.mpos.data.QRCodeCardHInfo;
import com.hzsun.mpos.data.QrCodeResultInfo;
import com.hzsun.mpos.data.RecordInfo;
import com.hzsun.mpos.data.SecretKeyInfo;
import com.hzsun.mpos.data.ShopQRCodeInfo;
import com.hzsun.mpos.data.StationInfo;
import com.hzsun.mpos.data.StatusBurInfo;
import com.hzsun.mpos.data.StatusInfo;
import com.hzsun.mpos.data.StatusPriInfo;
import com.hzsun.mpos.data.SystemInfo;
import com.hzsun.mpos.data.VersionInfo;
import com.hzsun.mpos.data.WasteBookInfo;
import com.hzsun.mpos.data.WasteFaceBookInfo;
import com.hzsun.mpos.data.WasteQrBookInfo;
import com.hzsun.mpos.data.WifiParaInfo;
import com.hzsun.mpos.nativelib.nativelib;

import java.util.LinkedHashMap;
import java.util.List;

public class Global {

    //TAG
    public static final String TAGAPP = "zytk35_buspos_";
    //引导app名称
    public static final String IAPAPPFILE_NAME = "ZYTK_IAP_APP.apk";
    //app名称
    public static final String APPFILE_NAME = "zytk35_buspos.apk";
    //软件版本号(中间用.分开)
    public static final String SOFTWAREVER = "3.5.19.12250";

    //以太网电子现金消费机
    public static final int LAN_EP_CONSUMEPOS = 301;
    //以太网电子现金充值机
    public static final int LAN_EP_MONEYPOS = 302;
    //设备类型 DeviceType
    //0x24  正元YT224消费机
    //0X27  正元YT324人脸对接机  zytk40_dockpos.apk
    //0X28  正元YT324人脸车载一体机  zytk40_buspos.apk
    public static final int LAN_DEVTYPE = 0x28;
    //设备类型 DeviceType
    public static final int LAN_OLDDEVTYPE = 0x24;
    //判断营业分组时长
    public static final int BUSINESS_TIME = 10 * 10;
    //最大交易流水数
    public static final int MAXBOOKSCOUNT = 100000;
    //最大卡内编号，卡数量
    public static final int MAXCARDCONUT = 1000000;
    public static final int MAXCARDBIT = 126976;//1000000/8(4096*31=126976)

    //测试模式间隔时长
    public static final int TESTMODETIMEER = 1;
    //交易流水长度
    public static final int PAYMENTRECORD_LEN = 44;
    //二维码交易记录长度
    public static final int QRPAYMENTRECORD_LEN = 46;
    //人脸交易记录长度
    public static final int FACEPAYMENTRECORD_LEN = 48;
    //上传交易记录打包笔数
    public static final int UPRECORDSUM = 10;
    //是否开启测试模式
    public static final int TESTMODE = 0;
    //人脸特征码大小
    public static final int FACEFEASIZE = 2560;
    //是否复制人脸动态库
    public static final int FACELIBCOPY = 0;
    //保留日志数量
    public static final int LOGCOUNT = 15;
    //保留抓拍人脸照片数量
    public static final int FACEPICCOUNT = 200;

    public static int STYLE=0;  //界面样式


    public static final String SDPath = Environment.getExternalStorageDirectory().toString();
    public static final String ZYTKPath = SDPath + "/zytk/";
    public static final String ZYTK35Path = SDPath + "/zytk/zytk35_buspos/";
    public static final String ZYTKFacePath = SDPath + "/zytk/facepath/";
    public static final String DATAPath = SDPath + "/zytk/zytk35_buspos/data/";
    public static final String PhotoPath = SDPath + "/zytk/zytk35_buspos/photo/";
    public static final String LogPath = SDPath + "/zytk/zytk35_buspos/log1/";
    public static final String LogCrashPath = SDPath + "/zytk/zytk35_buspos/logcrash1/";
    public static final String PicPath = SDPath + "/zytk/zytk35_buspos/pic/";
    public static final String ShellPath = SDPath + "/zytk/zytk35_buspos/sh/";
	public static final String IAPPath = SDPath + "/zytk/iap/";
    /**
     * 人脸特征点建模存放路径
     */
    public static final String EXTFACEPATH = SDPath + "/FeaturePath/";

    /**
     * 激活人脸算法错误码
     **/
    public static final int ERROR_INVALID_DEVICE = -1001;       // 无法匹配到设备类型
    public static final int ERROR_INVALID_DATE = -1002;       // 请求时间超出授权期限
    public static final int ERROR_INVALID_COUNT = -1003;       // 超出该设备的授权数量

    public static BasicInfo g_BasicInfo;
    public static LocalInfo g_LocalInfo;
    public static LocalNetInfo g_LocalNetInfo;
    public static LocalNetStrInfo g_LocalNetStrInfo;
    public static VersionInfo g_VersionInfo;
    public static VersionInfo g_NetVersionInfo;
    public static SecretKeyInfo g_SecretKeyInfo;
    public static SystemInfo g_SystemInfo;
    public static StationInfo g_StationInfo;
    public static OddKeyInfo g_OddKeyInfo;
    public static List<BurseInfo> g_EP_BurseInfo;
    public static List<BuinessInfo> g_BuinessInfo;
    public static StatusBurInfo g_StatusBurInfo;
    public static StatusInfo g_StatusInfo;
    public static List<StatusInfo> g_StatusInfoArray;
    public static List<StatusPriInfo> g_StatusPriInfoArray;

    public static BWListAllInfo g_BlackWList;
    public static RecordInfo g_RecordInfo;
    public static WasteBookInfo g_WasteBookInfo;
    public static WasteQrBookInfo g_WasteQrBookInfo;
    public static WasteFaceBookInfo g_WasteFaceBookInfo;
    public static FTPInfo g_FTPInfo;
    public static WifiParaInfo g_WifiParaInfo;
    public static CommInfo g_CommInfo;
    public static WorkInfo g_WorkInfo;
    public static NetEventMsg s_NetEventMsg;

    public static CardInfo g_CardInfo;
    public static CardAttrInfo g_CardAttr;
    public static CardBasicParaInfo g_CardBasicInfo;
    public static CardBasicParaInfo g_CardBasicTmpInfo;
    public static CardBasicParaInfo g_CardBasicTmpInfoA;

    public static LastOrderInfo g_LastOrderInfo;
    public static QRCodeCardHInfo g_CardHQRCodeInfo;
    public static QrCodeResultInfo g_QrCodeResultInfo;
    public static QrCodeResultInfo g_ThirdCodeResultInfo;
    public static QRCodeALIInfo g_ThirdQRCodeInfo;
    public static FacePayInfo g_FacePayInfo;
    public static FaceIdentInfo g_FaceIdentInfo;
    public static FaceCodeInfo g_FaceCodeInfo;
    public static HttpCommInfo gHttpCommInfo;
    public static OnlinePayInfo g_OnlinePayInfo;
    public static LastRecordPayInfo g_LastRecordPayInfo;
    public static ShopQRCodeInfo g_ShopQRCodeInfo;
    public static nativelib g_Nlib;

    public static Handler gUIMainHandler;//主界面UI刷新
    public static Handler gUIStartHandler;//开始界面UI刷新
    public static Handler gUICardHandler;//刷卡界面UI刷新
    public static Handler gUIRDCardHandler;//冲正刷卡界面UI刷新

    public static Display gExtDisplay;      //副屏显示
    public static LinkedHashMap<String, String> g_MapIDSound = new LinkedHashMap<>();   //身份语音map表

    public static final int OK = 0;
    public static final int CARD_INVALIED = 1;    //无效卡黑名单	1
    public static final int OUT_OF_CONSUMERANGE = 2;    //超出消费范围	2
    public static final int NOT_ENOUGH_MONEY = 3;    //余额超限	3
    public static final int ILLEGAL_CARD = 4;    //非法卡	4
    public static final int BEYOND_LIMIT_TIM = 5;    //超出有效期	5
    public static final int BEYOND_SINGLE_MONEY = 6;    //超出单笔消费限额	6
    public static final int BEYOND_DAY_TOTAL = 7;    //超出日累、单笔限额或者日累次数	7
    public static final int PSW_ERROR = 8;    //操作员卡或密码错误	8
    public static final int CHASE_FAIL = 9;    //追扣失败	9
    public static final int CONSUME_LIMIT_COUNT = 10;    //消费限次	10
    public static final int BURSE_CHASE = 11;//钱包去追扣11
    public static final int NOTSIGN = 12;    //未签到	12
    public static final int CANNOT_OFFLINE = 13;    //不允许脱机	13
    public static final int OFFLINE_TIME_BEYOND7 = 14;    //脱机时间超过7天不允许脱机	14
    public static final int CANNOT_CANCEL_PAYMENT = 15;    //不允许冲正	15
    public static final int CANCEL_PAYMENT_FAIL = 16;    //冲正失败	16
    public static final int TERMINAL_NO_REGISTER = 17;    //终端设备未注册	17
    public static final int BITMAP_ERROR = 18;    //指令标识的位图与标准位图不一致	18
    public static final int REPLY_ERROR = 19;    //组织回复报文失败	19
    public static final int SIGNIN_TIMEOUT = 20;    //设备接入服务未开启或物理连接断开	20
    public static final int USE_INTERFACE_FAIL = 21;    //调用接口失败	21
    public static final int COMMUCATION_CODE_ERROR = 22;    //通信认证码验证失败	22
    public static final int NOFOUND_NEW_SOFTWARE = 23;    //不存在最新的软件程序版本	23
    public static final int PAY_PSWAUTHEN_FAIL = 24;    //账户交易密码验证失败	24
    public static final int GET_WORKKEY_FAIL = 25;    //获取工作密钥失败	25
    public static final int GET_FACTOR_ERROR = 26;    //获取通信安全因子失败	26
    public static final int CUR_VER_ERROR = 27;    //当前版本号异常	27
    public static final int BALANCE_BEYOND_LIMIT = 28;    //余额超出账户余额限额	28
    public static final int LINK_REMOTESEVICE_FAIL = 29;    //连接远程服务失败	29
    public static final int NOTENOUGH_SAFE_GRADE = 30;    //当前安全等级不允许下载工作密钥	30
    public static final int NO_RECORD = 31;    //无流水	31
    public static final int NO_FIND_CARD = 32;    //未找到卡片	32
    public static final int CRC16_CHECK_ERROR = 33;    //CRC16校验失败	33
    public static final int INVALIED_VALUE = 34;    //非法值	34
    public static final int SET_FAIL = 35;    //设置失败	35
    public static final int IP_ERROR = 36;    //IP值不正确	36
    public static final int READ_CARDNUM_FAIL = 37;    //读卡号失败	37
    public static final int CRC8_CHAECK_ERROR = 38;    //CRC8校验失败	38
    public static final int STATUS_NUM_ERROR = 39;    //身份号错误	39
    public static final int NOPASSWORD = 40;    //用户密码未输入	40
    public static final int COMMUCATION_TIMEOUT = 41;    //通信超时	41
    public static final int UPSEND_RECORD_FAIL = 42;    //上传流水失败	42
    public static final int SERVICE_RECORD_FULL = 43;    //上传流水时(设备接入服务返回流水已满)	43
    public static final int NOFOUND_CHASEBURSE = 44;    //允许追扣情况下没找到追扣钱包	44
    public static final int CANNOT_CHANGE_TERMINALID = 45;    //有未上传流水不允许修改机号	45
    public static final int OVER_MONEY = 46;    //余额超出上限	46
    public static final int NORUSHRECORD = 47;     //无冲正交易记录 47
    public static final int CARDCANTOFFLINE = 48;   //卡片不允许脱机交易 48
    public static final int ERROR49 = 49;
    public static final int DEVICEID_ERROR = 50;//设备编号不一致50
    public static final int PLAT_CONSUMENUM_ERROR = 51;//平台客户号不一致51
    public static final int RECIVE_DATA_ERROR = 52;//接收数据错误52
    public static final int SEND_DATA_ERROR = 53;//发送数据失败53
    public static final int DATE_TIME_ERROR = 54;//时钟模块故障54
    public static final int SHOPER_DISABLE = 55;//商户失效55
    public static final int MONEY_OVER = 56;//余额超限56
    public static final int ERROR57 = 57;
    public static final int ERROR58 = 58;
    public static final int MEMORY_FAIL = 59;//读写FLASH失败59
    public static final int READ_CARDTYPE_INVALIED = 60;//系统参数"识别卡种模式"非法60
    public static final int READ_CARD_ERROR = 61;//读卡片（密码不正确或校验码错误）61
    public static final int WRITE_CARD_ERROR = 62;//写卡片（密码不正确或校验码错误）62
    public static final int ERROR63 = 63;
    public static final int BURSEBLOCK_DATA_ERROR = 64;    //钱包区块数据损坏	64
    public static final int ERROR65 = 65;
    public static final int NOFOUND_PSAM = 66;    //未插SIMPASS/UIMPASS卡的PSAM卡	66
    public static final int CONFIG_RFUIM_ERROR = 67;    //配置RFUIM卡读头参数失败	67
    public static final int ERROR68 = 68;
    public static final int ERROR69 = 69;
    public static final int HASDISPEL = 70;     //已经冲正
    public static final int DISPELFAIL = 71;     //冲正失败
    public static final int DISPELTOUT = 72;     //冲正超时
    public static final int ERROR73 = 73;
    public static final int ERROR74 = 74;
    public static final int ERROR75 = 75;
    public static final int ERROR76 = 76;
    public static final int ERROR77 = 77;
    public static final int ERROR78 = 78;
    public static final int ERROR79 = 79;
    public static final int ERROR80 = 80;
    public static final int READ_TIMERECORD_FAIL = 81;    //读计时流水单据失败(计时消费模式)	81
    public static final int ERROR82 = 82;
    public static final int ERROR83 = 83;
    public static final int ERROR84 = 84;
    public static final int ERROR85 = 85;
    public static final int ERROR86 = 86;
    public static final int ERROR87 = 87;
    public static final int ERROR88 = 88;
    public static final int ERROR89 = 89;
    public static final int HARDWARE_INIT_FAIL = 90;    //硬件初始化失败	90
    public static final int RefuseCode = 91;    //通讯返回拒绝码
    public static final int ERROR92 = 92;
    public static final int FACECODEINIT = 93; //特征码数据加载中不支持扫脸 93
    public static final int NETCOM_ERR = 94;//通讯故障 94
    public static final int LING_DATABASE_FAIL = 95;    //设备签到时连接数据库失败	95
    public static final int NOFOUND_STATION = 96;    //设备签到时站点不存在	96
    public static final int INVALID_QRCODE = 97;    //无效二维码	97
    public static final int DISABLE_QRCODE = 98;    //通讯故障不支持扫码	98
    public static final int RECORD_FULL = 99;    //脱机未交帐流水达2000笔	99

    public static final int FACE_UNREGIST = 101;    //人脸信息未注册	101
    public static final int FACE_INVALID = 102;    //非法人脸	102
    public static final int FACE_SAMEACC = 103;    //相同账号人脸	132


    public static final int CONNECTOK = 1;     //连接服务成功
    public static final int ONLINEACK = 2;     //在线服务应答
    public static final int DOWNPARAOVER = 3;     //下载参数结束
    public static final int MONEYDEVNO = 4;     //不支持充值机

    public static final int APPSTARTUP = 14;     //开始更新应用程序
    public static final int APPUPING = 15;     //更新应用程序中
    public static final int APPUPABN = 16;     //更新应用程序异常
    public static final int APPUPOVER = 20;     //应用程序APP更新完成
    public static final int ROMUPING = 21;     //更新系统OTA中
    public static final int ROMUPABN = 22;     //系统OTA异常
    public static final int ROMUPOVER = 23;     //系统OTA更新完成
    public static final int APPINSTALL = 24;     //应用程序安装中

    public static final int CAMERADEAL = 30;
    public static final int POWERSAVEDEAL = 40;
    public static final int SETERR_MSG = 50;

    public static int CAMERA_NUM = 1;  //摄像头数量

    //对接机
    public static final int GOTO_PAYCARD = 70;
    public static final int FINISH_QR_SCAN = 80;
    public static final int HIDE_CAMERAVIEW = 90;

    //照片
    public static final int IMG_HEIGHT = 600;
    public static final int IMG_WIDTH = 800;
}
