package com.hzsun.mpos.nativelib;

/**
 * Created by wcp on 2018/7/3.
 */

public class nativelib {

    private static final String TAG = "nativelib";

    public String nativelibTest() {
        String strTemp = "";

        strTemp = stringFromJNI();
        return strTemp;
    }

    /*-----------------------Public 公共驱动-----------------------*/
    //IO口控制
    public int IOPortCtrl(String path, int val) {
        return nIOPortCtrl(path, val);
    }

    //LED显示
    public void LedShow(char color, int level) {
        nLedShow(color, level);
    }

    /*-----------------------Printer 小票打印机-----------------------*/
    //初始化小票打印机
    public int UartPrinter_Init(int BaudID) {
        return nUartPrinter_Init(BaudID);
    }

    //发送数据
    public int UartPrinter_SendData(byte[] data, int len) {
        return nUartPrinter_SendData(data, len);
    }

    public int UartPrinter_RecvData(byte[] data, int len, int wait_time) {
        return nUartPrinter_RecvData(data, len, wait_time);
    }

    /*----------------------对接机-------------------------------*/
    public int UartDockpos_Init(int BaudID) {
        return nUartDockpos_Init(BaudID);
    }

    //发送数据
    public int UartDockpos_SendData(byte[] data, int len) {
        return nUartDockpos_SendData(data, len);
    }

    public int UartDockpos_RecvData(byte[] data) {
        return nUartDockpos_RecvData(data);
    }

    public int UartDockpos_ClearData() {
        return nUartDockpos_ClearData();
    }

    /*-----------------------RTC驱动-----------------------*/
    public int RTCTest() {
        return nRTCTest();
    }

    public int GetRTCDateTime(byte[] bCurrDateTime) {
        return nGetRTCDateTime(bCurrDateTime);
    }

    /*-----------------------密钥-----------------------*/

    //客户设密卡设用例程
    public void SCardSecret(byte[] bCardSerialID, byte[] bSCardSecret) {
        nSCardSecret(bCardSerialID, bSCardSecret);
    }

    //生成卡片密钥A
    public void UCardDecryptA(byte[][] bUCardSecret, int ClientNum, int AgentNum) {
        int Sector;
        byte[] pKey = new byte[6];
        //bArray=nUCardDecryptA(bSCardData);
        //int ilen=bArray.length;

        for (int i = 0; i < 32; i++) {
            Sector = i;
            nGet_SectKeyUCard(Sector, ClientNum, AgentNum, 1, pKey);
            System.arraycopy(pKey, 0, bUCardSecret[i], 0, 6);
        }
    }

    //生成卡片密钥B
    public void UCardDecryptB(byte[][] bUCardSecret, int ClientNum, int AgentNum) {
        int Sector;
        byte[] pKey = new byte[6];
        //bArray=nUCardDecryptA(bSCardData);
        //int ilen=bArray.length;

        for (int i = 0; i < 32; i++) {
            Sector = i;
            nGet_SectKeyUCard(Sector, ClientNum, AgentNum, 0, pKey);
            System.arraycopy(pKey, 0, bUCardSecret[i], 0, 6);
        }
    }

    //客户交易卡的钱包认证码
    public void UCardBurAuthen(byte[] bCardSerID, int iAgentID, int iUserID, int iSector, long lngBurMoney, int iBurseNoteID, byte[] bBurseAuthen) {
        long dwCardNum = (bCardSerID[0] & 0xff) + ((bCardSerID[1] & 0xff) << 8) + ((bCardSerID[2] & 0xff) << 16) + ((bCardSerID[3] & 0xff) << 24);
        long lngBurseAuthen = nGet_AuthenCodeUWallet(dwCardNum, iUserID, iAgentID, lngBurMoney, iSector, iBurseNoteID);

        bBurseAuthen[0] = (byte) (lngBurseAuthen & 0x000000ff);
        bBurseAuthen[1] = (byte) ((lngBurseAuthen & 0x0000ff00) >> 8);
        bBurseAuthen[2] = (byte) ((lngBurseAuthen & 0x00ff0000) >> 16);
        //bBurseAuthen[3]= (byte) ((lngBurseAuthen&0xff000000)>>24);

    }

    //设置卡片密钥
    public void SetCardKey(byte[][] bUCardAuthKeyA, byte[][] bUCardAuthKeyB) {
        nSetCardKey(bUCardAuthKeyA, bUCardAuthKeyB);
    }

    /*-----------------------读卡器驱动-----------------------*/
    public int ReaderInit() {
        return nReaderInit();
    }

    public int ReaderClose() {
        return nReaderClose();
    }

    public int ReaderCardUID(byte[] bCardUID) {
        int iRet = 0;

        iRet = nReaderCardUID(bCardUID);
        return iRet;
    }

    public int ReadCardAttrib(byte[] bCardUID, byte[] SAK) {
        int iRet = 0;

        iRet = nReadCardAttrib(bCardUID, SAK);
        return iRet;
    }

    public int CardSelectProCard(byte[] resp) {
        int iRet = 0;

        iRet = nCardSelectProCard(resp);
        return iRet;
    }

    public int ReadCPUCardSID(byte[] cCardSID) {
        int iRet = 0;

        iRet = nReadCPUCardSID(cCardSID);
        return iRet;
    }


    /*-----------------------Mifare Card Fun-----------------------*/

    //读卡片数据
    public int ReadMifareBlock(byte cBlockID, byte[] cCardSID, byte[] cCardContext) {

        return nReadMifareBlock(cBlockID, cCardSID, cCardContext);
    }

    //写卡片数据
    public int WriteMifareBlock(byte cBlockID, byte[] cCardSID, byte[] cCardContext) {

        return nWriteMifareBlock(cBlockID, cCardSID, cCardContext);
    }

    //写卡片数据(无认证)
    public int WriteMifareBlockNOAuthen(byte cBlockID, byte[] cCardContext) {

        return nWriteMifareBlockNOAuthen(cBlockID, cCardContext);
    }

    //读Mifare卡片扇区数据
    public int ReadMifareSector(byte cSectorID, byte[] cCardSID, byte cCardContext[][]) {

        return nReadMifareSector(cSectorID, cCardSID, cCardContext);
    }

    //读Mifare卡片钱包扇区数据
    public int ReadMifareBurseSector(byte[] cCardSID, byte cBlockID, byte[] cCardContext, byte cBlockBakID, byte[] cCardBakContext) {

        return nReadMifareBurseSector(cCardSID, cBlockID, cCardContext, cBlockBakID, cCardBakContext);
    }

    //写Mifare卡片扇区数据
    public int WriteMifareSector(byte cSectorID, byte[] cCardSID, byte cCardContext[][]) {

        return nWriteMifareSector(cSectorID, cCardSID, cCardContext);
    }

    //写Mifare卡片钱包扇区数据
    public int WriteMifareBurseSector(byte[] cCardSID, byte cBlockID, byte[] cCardContext, byte cBlockBakID, byte[] cCardBakContext) {

        return nWriteMifareBurseSector(cCardSID, cBlockID, cCardContext, cBlockBakID, cCardBakContext);
    }

    //读Mifare卡片扇区数据(固定密钥a0-a5)
    public int ReadMifareOpenSector(byte cSectorID, byte[] cCardSID, byte cCardContext[][]) {

        return nReadMifareOpenSector(cSectorID, cCardSID, cCardContext);
    }

    //读卡片数据
    public int ReadPSCard(byte[] bCardKey, byte[] bCardSerID, int iBlockID, byte[] bCardContext) {

        return nReadPSCard(bCardKey, bCardSerID, iBlockID, bCardContext);
    }

    //当出现E061时，重新读卡
    public int ReReadPSCard(byte[] bCardKey, byte[] bCardSerID, int iBlockID, byte[] bCardContext) {

        return nReReadPSCard(bCardKey, bCardSerID, iBlockID, bCardContext);
    }

    /*-----------------------CPU Card Fun-----------------------*/

    //基本信息设置(全部)
    public int CPU_BasicAllInfo_Set(byte[] bBasicContext, byte[] cCardSID) {
        return nCPU_BasicAllInfo_Set(bBasicContext, cCardSID);
    }

    //扩展信息设置(全部)
    public int CPU_ExternAllInfo_Set(byte[] bExternContext, byte[] cCardSID) {
        return nCPU_ExternAllInfo_Set(bExternContext, cCardSID);

    }

    //钱包信息设置
    public int CPU_BurseInfo_Set(int cBurseID, byte[] bBurseContext, byte[] cCardSID) {
        return nCPU_BurseInfo_Set(cBurseID, bBurseContext, cCardSID);
    }

    //基本扩展信息读取
    public int CPU_BasicExternInfo_Get(byte[] bBasicContext, byte[] bExternContext, byte[] cCardSID) {
        return nCPU_BasicExternInfo_Get(bBasicContext, bExternContext, cCardSID);
    }

    //基本信息读取
    public int CPU_BasicInfo_Get(byte[] bBasicContext, byte[] cCardSID) {
        return nCPU_BasicInfo_Get(bBasicContext, cCardSID);
    }

    //扩展信息读取
    public int CPU_ExternInfo_Get(byte[] bExternContext, byte[] cCardSID) {
        return nCPU_ExternInfo_Get(bExternContext, cCardSID);
    }

    //钱包信息读取
    public int CPU_BurseInfo_Get(int cBurseID, byte[] bBurseContext, byte[] cCardSID) {
        return nCPU_BurseInfo_Get(cBurseID, bBurseContext, cCardSID);
    }

    /*-----------------------QRCode Uart Fun-----------------------*/
    public int UartQR_Init(int iBaudID) {
        return nUartQR_Init(iBaudID);
    }

    //====================QR设备应用==========================

    //QRCode 扫描
    public int QR_ScanQRCode(byte[] cQRCodeInfo) {
        return nQR_ScanQRCode(cQRCodeInfo);
    }

    //$010500-EE19 设备重启
    public int QR_DeviceReset() {
        return nQR_DeviceReset();
    }

    //识读提示音 cMode 0:$150100-9A28 关闭识读提示音 1:$150101-A919 *开启识读提示音
    public int QR_SetDevicePrompt(int cMode) {
        return QR_SetDevicePrompt(cMode);
    }

    //识读LED灯指示 cMode 0:$150200-01F4 关闭LED灯指示 1:$150201-32C5 *开启LED灯指示
    public int QR_SetDeviceLed(int cMode) {
        return nQR_SetDeviceLed(cMode);
    }

    //识读模式
    //$100000-AF9D 一次触发
    //$100001-9CAC 按键保持
    //$100002-C9FF 开关持续
    //$100003-FACE 持续识读
    //$100004-6359 自动感应
    public int QR_SetDeviceReadMode(int cMode) {
        return nQR_SetDeviceReadMode(cMode);
    }

    // 0:$108001-9E81 开始识读2   1:$108003-F8E3 结束识读
    public int QR_SetDeviceReadEnable(int cMode) {
        return nQR_SetDeviceReadEnable(cMode);
    }

    //识读间隔
    public int QR_SetDeviceReadInterval(int cType) {
        return nQR_SetDeviceReadInterval(cType);
    }

    //串口波特率设置
    public int QR_SetBAUD(int BaudID) {
        return nQR_SetBAUD(BaudID);
    }

    //$010300-C980 读取设备信息
    public int QR_GetDeviceInfo(byte[] cDeviceInfo) {
        return nQR_GetDeviceInfo(cDeviceInfo);
    }

    // 自动感应灵敏度
    public int QR_SetAutoSenLevel(int cLevel) {
        return nQR_SetAutoSenLevel(cLevel);
    }

    //DisSense 扫描  0:无变化 1:有变化
    //移动侦测状态查询（只有识读模式为自动感应时结果才准确）
    //$380000-C23C
    public int QR_GetDisSenseRet(int iCount) {
        return nQR_GetDisSenseRet(iCount);
    }

    //主机命令应答模式 $020B00-33A1 *无应答
    public int QR_SetHostCommand() {
        return nQR_SetHostCommand();
    }

    //$201000-DD4E *关闭结束符
    public int QR_SetEndMark() {
        return nQR_SetEndMark();
    }

    //清除QR串口缓存
    public int QR_ClearRecvData(int iTime) {
        return nQR_ClearRecvData(iTime);
    }

    /*-----------------------PSAM卡-----------------------*/
    public int PSAMSP_Init() {
        return nPSAMSP_Init();
    }

    //psam发送接收数据
    public int SAM_EXCHANGE_APDU(byte[] APDU_Buf, int APDU_Len, byte[] response, int[] data_len, int wait_time) {
        return nSAM_EXCHANGE_APDU(APDU_Buf, APDU_Len, response, data_len, wait_time);
    }


    // Used to load the 'nativelib' library on application startup.
    static {
        System.loadLibrary("nativelib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private native String stringFromJNI();


    /*-----------------------密钥管理-----------------------*/
    //客户设密卡设用例程
    private native void nSCardSecret(byte[] bCardSerialID, byte[] bSCardSecret);
//
//    //解密函数调用例(A密钥)
//    private native  byte[][] nUCardDecryptA(byte[] bSCardData);
//
//    //解密函数调用例(B密钥)
//    private native  byte[][] nUCardDecryptB(byte[] bSCardData);
//
//    //客户交易卡的钱包认证码
//    private native void nUCardBurAuthen(byte[] strCardSerID,int iAgentID,int iUserID,int iSector,long lngBurMoney,int iBurseNoteID,byte[] bBurseAuthen);

    /*-----------------------Public 公共驱动-----------------------*/
    private native int nIOPortCtrl(String path, int val);

    private native void nLedShow(char color, int level);

    /*-----------------------Printer 小票打印机-----------------------*/
    private native int nUartPrinter_Init(int baudID);

    private native int nUartPrinter_SendData(byte[] data, int len);

    private native int nUartPrinter_RecvData(byte[] data, int len, int wait_time);

    /*-----------------------对接机---------------------------------*/
    private native int nUartDockpos_Init(int baudID);

    private native int nUartDockpos_SendData(byte[] data, int len);

    private native int nUartDockpos_RecvData(byte[] data);

    private native int nUartDockpos_ClearData();

    /*-----------------------RTC驱动-----------------------*/
    private native int nRTCTest();

    private native int nGetRTCDateTime(byte[] bCurrDateTime);

    //生成卡片密钥
    //参数 ：扇区号[0~15]，客户号,代理号,1:A密钥 0:B密钥
    private native void nGet_SectKeyUCard(int Sector, int ClientNum, int AgentNum,
                                          int isKeyA, byte[] pKey);

    //生成钱包认证码
    private native long nGet_AuthenCodeUWallet(long dwCardNum, int wClientNum,
                                               int AgentNum, long lMonCurr,
                                               int Sector, int wWalletSID);

    //设置卡片密钥
    private native void nSetCardKey(byte[][] bUCardAuthKeyA, byte[][] bUCardAuthKeyB);

    /*-----------------------读卡器驱动-----------------------*/
    private native int nReaderInit();

    private native int nReaderClose();

    private native int nReaderCardUID(byte[] bCardUID);

    private native int nReadCardAttrib(byte[] bCardUID, byte[] SAK);

    private native int nCardSelectProCard(byte[] resp);

    private native int nReadCPUCardSID(byte[] cCardSID);

    /*-----------------------Mifare Card Fun-----------------------*/
    //读卡片数据
    private native int nReadMifareBlock(byte cBlockID, byte[] cCardSID, byte[] cCardContext);

    //写卡片数据
    private native int nWriteMifareBlock(byte cBlockID, byte[] cCardSID, byte[] cCardContext);

    //写卡片数据(无认证)
    private native int nWriteMifareBlockNOAuthen(byte cBlockID, byte[] cCardContext);

    //读Mifare卡片扇区数据
    private native int nReadMifareSector(byte cSectorID, byte[] cCardSID, byte cCardContext[][]);

    //读Mifare卡片钱包扇区数据
    private native int nReadMifareBurseSector(byte[] cCardSID, byte cBlockID, byte[] cCardContext, byte cBlockBakID, byte[] cCardBakContext);

    //写Mifare卡片扇区数据
    private native int nWriteMifareSector(byte cSectorID, byte[] cCardSID, byte cCardContext[][]);

    //写Mifare卡片钱包扇区数据
    private native int nWriteMifareBurseSector(byte[] cCardSID, byte cBlockID, byte[] cCardContext, byte cBlockBakID, byte[] cCardBakContext);

    //读Mifare卡片扇区数据(固定密钥a0-a5)
    private native int nReadMifareOpenSector(byte cSectorID, byte[] cCardSID, byte cCardContext[][]);

    //读卡片数据
    private native int nReadPSCard(byte[] bCardKey, byte[] bCardSerID, int iBlockID, byte[] bCardContext);

    //当出现E061时，重新读卡
    private native int nReReadPSCard(byte[] bCardKey, byte[] bCardSerID, int iBlockID, byte[] bCardContext);


    /*-----------------------CPU Card Fun-----------------------*/

    //基本信息设置(全部)
    private native int nCPU_BasicAllInfo_Set(byte[] bBasicContext, byte[] cCardSID);

    //扩展信息设置(全部)
    private native int nCPU_ExternAllInfo_Set(byte[] bExternContext, byte[] cCardSID);

    //钱包信息设置
    private native int nCPU_BurseInfo_Set(int cBurseID, byte[] bBurseContext, byte[] cCardSID);

    //基本扩展信息读取
    private native int nCPU_BasicExternInfo_Get(byte[] bBasicContext, byte[] bExternContext, byte[] cCardSID);

    //基本信息读取
    private native int nCPU_BasicInfo_Get(byte[] bBasicContext, byte[] cCardSID);

    //扩展信息读取
    private native int nCPU_ExternInfo_Get(byte[] bExternContext, byte[] cCardSID);

    //钱包信息读取
    private native int nCPU_BurseInfo_Get(int cBurseID, byte[] bBurseContext, byte[] cCardSID);


    /*-----------------------QRCode Uart Fun-----------------------*/
    //初始化QR串口
    private native int nUartQR_Init(int iBaudID);

    private native int nQR_ScanQRCode(byte[] cQRCodeInfo);

    //$010500-EE19 设备重启
    private native int nQR_DeviceReset();

    //识读提示音 cMode 0:$150100-9A28 关闭识读提示音 1:$150101-A919 *开启识读提示音
    private native int nQR_SetDevicePrompt(int cMode);

    //识读LED灯指示 cMode 0:$150200-01F4 关闭LED灯指示 1:$150201-32C5 *开启LED灯指示
    private native int nQR_SetDeviceLed(int cMode);

    //识读模式
    private native int nQR_SetDeviceReadMode(int cMode);

    // 0:$108001-9E81 开始识读2   1:$108003-F8E3 结束识读
    private native int nQR_SetDeviceReadEnable(int cMode);

    //识读间隔
    private native int nQR_SetDeviceReadInterval(int cType);

    //串口波特率设置
    private native int nQR_SetBAUD(int BaudID);

    //$010300-C980 读取设备信息
    private native int nQR_GetDeviceInfo(byte[] cDeviceInfo);

    // 自动感应灵敏度
    private native int nQR_SetAutoSenLevel(int cLevel);

    //移动侦测状态查询（只有识读模式为自动感应时结果才准确）
    private native int nQR_GetDisSenseRet(int iCount);

    //主机命令应答模式 $020B00-33A1 *无应答
    private native int nQR_SetHostCommand();

    //$201000-DD4E *关闭结束符
    private native int nQR_SetEndMark();

    //清除串口缓存
    private native int nQR_ClearRecvData(int iTime);

    /*-----------------------PSAM卡-----------------------*/
    //PSAM卡初始化
    private native int nPSAMSP_Init();

    private native int nSAM_EXCHANGE_APDU(byte[] apdu_buf, int apdu_len, byte[] response, int[] data_len, int wait_time);

}
