package com.hzsun.mpos.Global;


public class WorkInfo {

//    // 授权常量
//    public static final String AUTHORITY = "com.hzsun.pmos.Global.WorkInfo";
//    // 构造方法
//    public WorkInfo() {}

    //public static List<byte[]> bUCardAuthKeyA=new ArrayList<byte[]>();
    public byte[][] bUCardAuthKeyA = new byte[32][6];//用户交易卡密钥A
    public byte[][] bUCardAuthKeyB = new byte[32][6];//用户交易卡密钥B

    public byte cReConnectState;        //重新连接服务器状态
    public byte cConnState;                //连接状态(0:未连接,1:重新连接,2:连接成功,3:,4:)
    public byte cRunState;                //运行的状态(0:不运行,1:联机,2:脱机,3:联机(营业时段外),4:重新联机5:开机自动联机6:连接签到服务)
    public byte cNetlinkStatus;         //物理网线状态
    public byte cDownParaState;         //下载参数状态
    public byte cCheckOutState;         //结帐状态(0:无,1:结帐完成)
    public byte cSelfPressOk;           //0:没有按确定键,1:按下确定键
    public long lngPaymentMoney;        //输入的交易金额整行(最大60000)
    public byte cYearCheckState;        //年检状态
    public byte cSubsidyState;          //补助状态
    public byte cInfoStepState;         //信息圈存状态
    public long lngTotalMoney;          //交易总金额
    public byte cBusinessID;            //消费时段号
    public byte ChasePaymentState;      //追扣方式
    public byte cIsInterDay;            //是否跨天  0:不跨天 1:跨天
    public byte cShopUserNum;            //商户数量
    public byte[] cStartTime = new byte[2];          //营业分组时间开始
    public byte[] cEndTime = new byte[2];            //营业分组时间结束
    public byte[] OpetorSerID = new byte[8];        //操作员卡号
    public long lngOpetorKey;                       //操作员密码
    public byte[] cCasherID = new byte[4];           //出纳员编号
    public byte cOptionMark;            //键盘加减标志
    public byte cShowErrCount;          //卡片读卡信息失败次数
    public int wBookMoney;              //定额值
    public long lngOnLineTime;          //联机计时时长
    public long lngOffLineTime;         //脱机计时时长
    public long lngOSTime;              //定时器100ms计数

    public byte cInputKeyState;         //输入金额状态(0:无,1:输入金额中)
    public byte[] cPaymentMoney = new byte[12];   //输入的交易金额字符形(最大600.00)
    public byte cPaymentMoneyLen;       //输入金额的长度
    public byte cPaymentDotPos;         //输入小数点的位置
    public byte cUpPamentSum;           //上传交易记录打包笔数
    public int iSelfDownPara;           //下载参数状态

    //--------------------冲正时使用----------------------------
    public int lngReManageMoney;       //冲正管理费金额
    public int lngReWorkPayMoney;      //冲正工作钱包交易金额
    public int lngReChasePayMoney;     //冲正追扣钱包交易金额
    public long lngReManageRecordID;    //被冲正管理费的流水号
    public byte IsDispelRecord;         //是否存在需要冲正的流水
    public byte IsRecordDispelOver;        //冲正是否结束

    public byte cBusinessState;         //营业状态 1:联机时 2:脱机时
    public long iBusinessCount;         //营业时段
    public byte[] cRecordDataTime = new byte[8];     //记录当前Pos时间
    public byte[] cCurDateTime = new byte[8];        //当前Pos时间
    public byte[] cOKDataTime = new byte[6];            //记录当前正确时间基线
    public byte cKeyDownState;          //按键按下状态
    public byte cTestState;             //测试模式 1：进入测试模式 0：退出测试模式
    public byte cUpdateState;            //自动升级状态 1：有升级程序 0：无升级程序
    public byte[] cConfigAppSWVer = new byte[12];    //受卡方终端软件版本号	12字节	软件配置文件

    public long lngStartRecordID;       //发送流水的开始流水号
    public long lngEndRecordID;         //发送流水的结束流水号
    public long lngStartFRecordID;       //发送流水的开始流水号
    public long lngEndFRecordID;         //发送流水的结束流水号
    public int iUpBooksCount;       //单次上传流水笔数 <=10

    public byte cInMainMenuState;           //是否在主界面状态
    public byte cNetworkErrFlag;        //网络通信业务出错 0:无 1:有
    public byte cRecordErrFlag;         //记录流水文件故障标记
    public String strAppSoftWareVer;    //APP程序版本号
    public String strAppFileName;       //APP程序名

    public byte cCardEnableFlag;        //是否允许卡片刷卡
    public byte cInDispelCardFlag;      //是否进入刷卡冲正界面
    public byte cInUserPWDFlag;         //是否进入用户密码输入界面

    public byte cStartReWDialogFlag;     //启动重写提示框标记;
    public byte cStartWCardFlag;            //启动写卡标记;
    public byte cQrCodestatus;          //获取到的订单号状态 0:没有获取 1:正在获取 2:已经获取
    public long lngLastOrderNum;        //记录上次订单号
    public long lngOrderNum;             //二维码订单号
    public byte cPayDisPlayStatus;      //显示支付成功状态
    public byte cPlatAppVerson;          //在线交易大金额开始的平台版本号
    public byte cScanQRCodeFlag;        //QR扫描状态
    public byte cQRDevStatus;           //QR设备状态
    public byte cOtherQRFlag;       //0:正元QR  1:第三方QR  2:人脸支付  3:在线卡片支付 4:代扣
    public byte cFaceInitState;     //人脸初始化状态
    public long lngAccountID;       //主卡账号
    public String strAccCode;    //显示照片账号
    public String strUserPwd;       //键盘密码
    public byte cReCordDispelState;      //在线冲正标志
    public byte cBackActivityState;      //进入省电模式前界面 1:主界面 2:刷卡界面
    public long lngPowerSaveCnt;      //省电模式计数
    public byte cUDiskState;      //是否进入U盘界面
}
