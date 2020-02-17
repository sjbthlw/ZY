package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hzsun.mpos.Adapter.MenuListviewAdapter;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.BuinessInfo;
import com.hzsun.mpos.data.WasteBooks;
import com.hzsun.mpos.data.WasteBooksRW;
import com.hzsun.mpos.data.WasteFacePayBooks;
import com.hzsun.mpos.data.WasteFacePayBooksRW;
import com.hzsun.mpos.data.WasteQrCodeBooks;
import com.hzsun.mpos.data.WasteQrCodeBooksRW;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.hzsun.mpos.Global.Global.CAMERA_NUM;
import static com.hzsun.mpos.Global.Global.LAN_EP_CONSUMEPOS;
import static com.hzsun.mpos.Global.Global.LAN_EP_MONEYPOS;
import static com.hzsun.mpos.Global.Global.MAXBOOKSCOUNT;
import static com.hzsun.mpos.Global.Global.MAXCARDCONUT;
import static com.hzsun.mpos.Global.Global.SOFTWAREVER;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_BlackWList;
import static com.hzsun.mpos.Global.Global.g_BuinessInfo;
import static com.hzsun.mpos.Global.Global.g_FaceCodeInfo;
import static com.hzsun.mpos.Global.Global.g_FaceIdentInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_LocalNetStrInfo;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;
import static com.hzsun.mpos.Global.Global.g_WasteFaceBookInfo;
import static com.hzsun.mpos.Global.Global.g_WasteQrBookInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.ByteToString;
import static com.hzsun.mpos.Public.Publicfun.getAllIp;


/**
 * 查询总界面
 */
public class QueryPayAllActivity extends BaseActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private String TAG = getClass().getSimpleName();
    private ImageView ivSetImage;
    private TextView tvTitle;
    private TextView tvCheckedNum;
    private ListView queryPayAllListview;
    private ArrayList<String> stringList;
    private MenuListviewAdapter menuListviewAdapter;
    private int image = R.mipmap.s_local_check;
    private String title = null;
    private int positionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_pay_all);
        isShowPerScreen(true);
        initViews();
        initListView();
    }

    private void initViews() {
        ivSetImage = ((ImageView) findViewById(R.id.local_business_set_image));
        tvTitle = ((TextView) findViewById(R.id.title_tv));
        tvCheckedNum = ((TextView) findViewById(R.id.tv_checked_num));
        queryPayAllListview = ((ListView) findViewById(R.id.query_pay_all_listview));

        ivSetImage.setImageResource(image);
        title = getResources().getString(R.string.query);
        tvTitle.setText(title);
    }

    private void initListView() {
        stringList = new ArrayList<>();
        stringList.add("餐累统计");
        stringList.add("日累统计");
        stringList.add("最近流水查询");
        stringList.add("未传流水信息");
        stringList.add("名单记录信息");
        stringList.add("本机系统信息");
        stringList.add("人脸流水查询");
        stringList.add("人脸识别信息");

        menuListviewAdapter = new MenuListviewAdapter(this, stringList);
        queryPayAllListview.setAdapter(menuListviewAdapter);
        queryPayAllListview.setOnItemSelectedListener(this);
        queryPayAllListview.setOnItemClickListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        tvCheckedNum.setText(position + 1 + "/" + stringList.size());
        menuListviewAdapter.setCurrentItem(position);
        menuListviewAdapter.notifyDataSetChanged();
        positionId = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    //1.餐累统计
    private void QueryTimeAmountTotal(int iPosition) {
        int i;
        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "时段营业统计";

        //死机测试
        //int iID=Integer.parseInt("123456789112");

        //是否允许查汇总
        if (g_StationInfo.cCanPermitStat == 1) {
            //判断是否是同一天
            cResult = Publicfun.CompareStatLastDate(g_RecordInfo.cLastPaymentDate);
            if (cResult == 0) {
                //时段营业笔数 - 时段营业总额
                strTemp = String.format("时段营业笔数：%d      时段营业总额：%d.%02d", g_RecordInfo.wTotalBusinessSum,
                        g_RecordInfo.lngTotalBusinessMoney / 100, g_RecordInfo.lngTotalBusinessMoney % 100);
                OptionItemList.add(strTemp);

                //交易营业号
                strTemp = String.format("交易营业号：%d", g_RecordInfo.cLastBusinessID);
                OptionItemList.add(strTemp);

                //营业分组时间
                strTemp = String.format("当餐营业时间：%02d:%02d - %02d:%02d", g_WorkInfo.cStartTime[0], g_WorkInfo.cStartTime[1]
                        , g_WorkInfo.cEndTime[0], g_WorkInfo.cEndTime[1]);
                OptionItemList.add(strTemp);
            } else if (cResult == 1) {
                //时段营业笔数 - 时段营业总额
                strTemp = String.format("时段营业笔数：%d      时段营业总额：%d.%02d", 0, 0, 0);
                OptionItemList.add(strTemp);

                //交易营业号
                strTemp = String.format("交易营业号：%d", g_RecordInfo.cLastBusinessID);
                OptionItemList.add(strTemp);

                //营业分组时间
                strTemp = String.format("当餐营业时间：%02d:%02d - %02d:%02d", g_WorkInfo.cStartTime[0], g_WorkInfo.cStartTime[1]
                        , g_WorkInfo.cEndTime[0], g_WorkInfo.cEndTime[1]);
                OptionItemList.add(strTemp);
            } else {
                OptionItemList.add("查询此类失败, 请重新查询");
            }
            List<BuinessInfo> pBuinessInfo = new ArrayList<BuinessInfo>();
            for (i = 0; i < 128; i++) {
                if (g_BuinessInfo.get(i).cBusinessID != 0)
                    pBuinessInfo.add(g_BuinessInfo.get(i));
            }
            Log.d(TAG,"营业分组数:"+pBuinessInfo.size());
//            BuinessInfo pBuinessData = new BuinessInfo();
//            if ((pBuinessInfo.size() % 2) != 0)
//                pBuinessInfo.add(pBuinessData);
            //营业分组时间
            for (i = 0; i < (pBuinessInfo.size() / 2); i++) {
                strTemp = String.format("ID:%d - 时段：%02d:%02d-%02d:%02d", pBuinessInfo.get(i * 2).cBusinessID,
                        pBuinessInfo.get(i * 2).cStartTime[0], pBuinessInfo.get(i * 2).cStartTime[1],
                        pBuinessInfo.get(i * 2).cEndTime[0], pBuinessInfo.get(i * 2).cEndTime[1])
                        + String.format("     ID:%d - 时段：%02d:%02d-%02d:%02d", pBuinessInfo.get(i * 2 + 1).cBusinessID,
                        pBuinessInfo.get(i * 2 + 1).cStartTime[0], pBuinessInfo.get(i * 2 + 1).cStartTime[1],
                        pBuinessInfo.get(i * 2 + 1).cEndTime[0], pBuinessInfo.get(i * 2 + 1).cEndTime[1]);

                OptionItemList.add(strTemp);
            }
            if(pBuinessInfo.size() % 2!=0)
            {
                strTemp = String.format("ID:%d - 时段：%02d:%02d-%02d:%02d", pBuinessInfo.get(i * 2).cBusinessID,
                        pBuinessInfo.get(i * 2).cStartTime[0], pBuinessInfo.get(i * 2).cStartTime[1],
                        pBuinessInfo.get(i * 2).cEndTime[0], pBuinessInfo.get(i * 2).cEndTime[1]);
                OptionItemList.add(strTemp);
            }
        } else {
            OptionItemList.add("不允许查询此类");
        }
        startActivityForResult(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //2.日累统计
    private void QueryDayAmountTotal(int iPosition) {

        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "当日营业统计";

        //是否允许查汇总
        if (g_StationInfo.cCanPermitStat == 1) {
            //判断是否是同一天
            cResult = Publicfun.CompareStatLastDate(g_RecordInfo.cLastPaymentDate);
            if (cResult == 0) {
                //当日交易笔数
                strTemp = String.format("当日交易笔数：%d", g_RecordInfo.wTodayPaymentSum);
                OptionItemList.add(strTemp);

                //当日交易总额
                strTemp = String.format("当日交易总额：%d.%02d", g_RecordInfo.lngTodayPaymentMoney / 100, g_RecordInfo.lngTodayPaymentMoney % 100);
                OptionItemList.add(strTemp);
            } else if (cResult == 1) {
                //当日交易笔数
                strTemp = String.format("当日交易笔数：%d", 0);
                OptionItemList.add(strTemp);

                //当日交易总额
                strTemp = String.format("当日交易总额：%d.%02d", 0, 0);
                OptionItemList.add(strTemp);
            } else {
                OptionItemList.add("查询此类失败, 请重新查询");
            }
        } else {
            OptionItemList.add("不允许查询此类");
        }
        startActivityForResult(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //3.最近流水查询
    private void QueryLastRecord(int iPosition) {
        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "最近流水查询";

        OptionItemList = GetToRecordList();

        if (OptionItemList == null) {
            ArrayList<String> ItemList = new ArrayList<String>();
            ItemList.add("无交易流水");
            startActivityForResult(new Intent(this, CheckMenuActivity.class)

                    .putExtra("Type", iType)
                    .putExtra("Position", iPosition)
                    .putExtra("strTitle", strTitle)
                    .putStringArrayListExtra("OptionItemList", ItemList)
                    .putExtra("Image", image), iType);
        } else {
            startActivityForResult(new Intent(QueryPayAllActivity.this, QueryRecordActivity.class)

                    .putExtra("Type", iType)
                    .putExtra("Position", iPosition)
                    .putExtra("strTitle", strTitle)
                    .putStringArrayListExtra("OptionItemList", OptionItemList)
                    .putExtra("Image", image), iType);
        }
    }

    //获取流水数据列表
    private ArrayList<String> GetToRecordList() {
        int i;
        int iItem = 1;
        int iResult;
        int iRecordCount = 0;
        int iReadIndex = 0;
        int iQRReadIndex = 0;
        int lngRecordIndex;
        int lngQRRecordIndex = 0;
        WasteBooks pWasteBooks = new WasteBooks();
        WasteQrCodeBooks pQRWasteBooks = new WasteQrCodeBooks();
        ArrayList<String> InfoList = new ArrayList<String>();

        int Year, Month, Day, Hour, Min, Sec;
        long lngPayMoney;
        long lngManageMoney;
        long LastPayDate;
        long QRLastPayDate;

        iReadIndex = 0;
        iQRReadIndex = 0;
        iRecordCount = 200;    //查看流水条数

        lngRecordIndex = (int) g_WasteBookInfo.WriterIndex;
        lngRecordIndex = (lngRecordIndex % MAXBOOKSCOUNT);

        lngQRRecordIndex = (int) g_WasteQrBookInfo.WriterIndex;
        lngQRRecordIndex = (lngQRRecordIndex % MAXBOOKSCOUNT);

        if (iRecordCount > (lngRecordIndex + lngQRRecordIndex)) {
            iRecordCount = (lngRecordIndex + lngQRRecordIndex);
        }
        Log.i(TAG, String.format("显示流水数iRecordCount:%d,%d,%d", iRecordCount, lngRecordIndex, lngQRRecordIndex));
        for (i = 0; i < iRecordCount; i++) {
            LastPayDate = 0;
            QRLastPayDate = 0;
            //卡记录流水
            if ((lngRecordIndex - iReadIndex) >= 1) {
                pWasteBooks = WasteBooksRW.ReadWasteBooksData((int) (lngRecordIndex - iReadIndex));

                LastPayDate = (pWasteBooks.bPaymentDate[3] & 0xff) +
                        (pWasteBooks.bPaymentDate[2] & 0xff) * 256 +
                        (pWasteBooks.bPaymentDate[1] & 0xff) * 256 * 256 +
                        ((long) pWasteBooks.bPaymentDate[0] & 0xff) * 256 * 256 * 256;
            }
            //二维码记录流水
            if ((lngQRRecordIndex - iQRReadIndex) >= 1) {
                pQRWasteBooks = WasteQrCodeBooksRW.ReadWasteQrCodeBooksData((int) (lngQRRecordIndex - iQRReadIndex));

                QRLastPayDate = (pQRWasteBooks.cPaymentTime[3] & 0xff) +
                        (pQRWasteBooks.cPaymentTime[2] & 0xff) * 256 +
                        (pQRWasteBooks.cPaymentTime[1] & 0xff) * 256 * 256 +
                        ((long) pQRWasteBooks.cPaymentTime[0] & 0xff) * 256 * 256 * 256;
            }
            if ((LastPayDate == 0) && (QRLastPayDate == 0)) {
                iReadIndex++;
                iQRReadIndex++;
                continue;
            }

            String strID = "";    //序号
            String strMoney = "";    //交易金额
            String strName = "";    //姓名
            String strType = "";    //交易类型
            String strDateTime = "";    //交易时间
            strID = "" + iItem;//序号

            //比较卡记录流水和二维码记录流水的先后顺序
            if (LastPayDate >= QRLastPayDate) //卡交易记录流水
            {
                iReadIndex++;
                //姓名
                try {
                    strName = ByteToString(pWasteBooks.cAccName, "GB2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                strType = "卡消费";//交易类型

                //交易时间
                Year = (int) ((LastPayDate & 0xFC000000) >> 26);
                Month = (int) ((LastPayDate & 0x03C00000) >> 22);
                Day = (int) ((LastPayDate & 0x003E0000) >> 17);

                Hour = (int) ((LastPayDate & 0x0001F000) >> 12);
                Min = (int) ((LastPayDate & 0x00000FC0) >> 6);
                Sec = (int) (LastPayDate & 0x0000003F);
                strDateTime = String.format("%02d-%02d %02d:%02d:%02d", Month, Day, Hour, Min, Sec);

                //交易金额
                if ((pWasteBooks.cPaymentType == 0)//商务消费;30：以角为单位 60：元
                        || (pWasteBooks.cPaymentType == 30)
                        || (pWasteBooks.cPaymentType == 60)
                        || (pWasteBooks.cPaymentType == 8)//08:追扣消费；38角 68元
                        || (pWasteBooks.cPaymentType == 38)
                        || (pWasteBooks.cPaymentType == 68)) {
                    lngPayMoney = (pWasteBooks.cPaymentMoney[0] & 0xff) +
                            (pWasteBooks.cPaymentMoney[1] & 0xff) * 256;
                    if ((pWasteBooks.cPaymentType == 30) || (pWasteBooks.cPaymentType == 38)) {
                        strMoney = String.format("%d.%01d", lngPayMoney / 10, lngPayMoney % 10);
                    } else if ((pWasteBooks.cPaymentType == 60) || (pWasteBooks.cPaymentType == 68)) {
                        strMoney = String.format("%d.00", lngPayMoney);
                    } else {
                        strMoney = String.format("%d.%02d", lngPayMoney / 100, lngPayMoney % 100);
                    }
                } else {
                    Log.i(TAG, "不是消费流水");
                    continue;
                }
            } else    //二维码交易记录流水
            {
                iQRReadIndex++;
                //姓名
                try {
                    strName = ByteToString(pQRWasteBooks.cAccName, "GB2312");
                    //strName=new String(pQRWasteBooks.cAccName, "GB2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //交易类型 0:正元二维码主扫> 1：正元二维码被扫<   2：支付宝连接正元卡余额二维码> 3：支付宝二维码> 4：微信二维码>
                if (pQRWasteBooks.cPayType == 0) {
                    //交易类型
                    strType = "二维码消费>";
                } else if (pQRWasteBooks.cPayType == 1) {
                    //交易类型
                    strType = "二维码消费<";
                } else if (pQRWasteBooks.cPayType == 2) {
                    //交易类型
                    strType = "二维码消费A>";
                } else if (pQRWasteBooks.cPayType == 3) {
                    //交易类型
                    strType = "支付宝消费>";
                } else if (pQRWasteBooks.cPayType == 4) {
                    //交易类型
                    strType = "微信消费>";
                } else if (pQRWasteBooks.cPayType == 5) {
                    //交易类型
                    strType = "第三方代扣";
                } else if (pQRWasteBooks.cPayType == 6) {
                    //交易类型
                    strType = "卡在线交易";
                } else if (pQRWasteBooks.cPayType == 7) {
                    //交易类型
                    strType = "二维码离线消费";
                } else if (pQRWasteBooks.cPayType == 8) {
                    //交易类型
                    strType = "人脸离线消费";
                } else if (pQRWasteBooks.cPayType == 9) {
                    //交易类型
                    strType = "人脸在线消费";
                } else {
                    //交易类型
                    strType = "第三方二维码消费";
                }
                //交易时间
                Year = (int) ((QRLastPayDate & 0xFC000000) >> 26);
                Month = (int) ((QRLastPayDate & 0x03C00000) >> 22);
                Day = (int) ((QRLastPayDate & 0x003E0000) >> 17);

                Hour = (int) ((QRLastPayDate & 0x0001F000) >> 12);
                Min = (int) ((QRLastPayDate & 0x00000FC0) >> 6);
                Sec = (int) (QRLastPayDate & 0x0000003F);
                strDateTime = String.format("%02d-%02d %02d:%02d:%02d", Month, Day, Hour, Min, Sec);

                lngPayMoney = (pQRWasteBooks.cPaymentMoney[0] & 0xff) +
                        (pQRWasteBooks.cPaymentMoney[1] & 0xff) * 256 +
                        (pQRWasteBooks.cPaymentMoney[2] & 0xff) * 256 * 256;

                lngManageMoney = (pQRWasteBooks.cManageMoney[0] & 0xff) +
                        (pQRWasteBooks.cManageMoney[1] & 0xff) * 256 +
                        (pQRWasteBooks.cManageMoney[2] & 0xff) * 256 * 256;

                lngPayMoney = lngPayMoney - lngManageMoney;
                strMoney = String.format("%d.%02d", lngPayMoney / 100, lngPayMoney % 100);//交易金额
            }
            iItem++;
            //序号,交易金额,姓名,交易类型,交易时间
            InfoList.add(strID + "," + strMoney + "," + strName + "," + strType + "," + strDateTime);
            if (InfoList.size() >= iRecordCount)
                break;
        }
        return InfoList;
    }

    //4.未传流水信息
    private void QueryNoUpRecord(int iPosition) {

        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "未传流水信息";

        long lngTemp = 0;
        long lngNoUpCount;
        int cPayRecordNum;
        long lngPaymentTempID;
        long lngPaymentSendID;
        long lngPaymentRecordID;
        WasteBooks pConsumeWasteBooks = new WasteBooks();

        //判断是否存在未上传流水
        lngTemp = g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex;
        if (lngTemp == 0) {
            OptionItemList.add("无未上传流水");
        } else {
            lngPaymentSendID = g_WasteBookInfo.TransferIndex;
            lngPaymentRecordID = g_WasteBookInfo.WriterIndex;

            lngTemp = WasteBooksRW.ReadWasteBooksMoney(lngPaymentSendID, lngPaymentRecordID);
            //未传流水信息笔数
            strTemp = String.format("未传流水笔数：%d", (g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex));
            OptionItemList.add(strTemp);

            //未传流水信息总额
            if (g_StationInfo.cPaymentUnit == 1) {
                strTemp = String.format("未传流水总额：%d.%01d", lngTemp / 10, lngTemp % 10);
            } else if (g_StationInfo.cPaymentUnit == 2) {
                strTemp = String.format("未传流水总额：%d.%00", lngTemp);
            } else {
                strTemp = String.format("未传流水总额：%d.%02d", lngTemp / 100, lngTemp % 100);
            }
            OptionItemList.add(strTemp);
        }
        startActivityForResult(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
    }

    //5.名单记录信息
    private void QueryListRecordInfo(int iPosition) {
        int cResult;
        int iType = iPosition;
        long WriterIndex = 0;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "名单记录信息";

        //黑白名单数量        
        strTemp = String.format("黑白名单数量：%d/%d", g_BlackWList.BlackWListInfo.lngBWCount, MAXCARDCONUT);
        OptionItemList.add(strTemp);

        //最大卡内编号
        strTemp = String.format("最大卡内编号：%d", g_BlackWList.BlackWListInfo.lngMaxCardID);
        OptionItemList.add(strTemp);

        //交易流水记录
        WriterIndex = g_WasteBookInfo.WriterIndex % MAXBOOKSCOUNT;
        if ((WriterIndex == 0) && (g_WasteBookInfo.WriterIndex != 0)) {
            WriterIndex = MAXBOOKSCOUNT;
        }
        strTemp = String.format("交易记录数量：%d/%d", WriterIndex, MAXBOOKSCOUNT);
        OptionItemList.add(strTemp);

        //脱机交易记录
        strTemp = String.format("脱机交易记录数量：%d", (g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex));
        OptionItemList.add(strTemp);

        //末次签到日期
        strTemp = String.format("末次签到日期：%d-%d-%d", g_RecordInfo.cLastPaymentDate[0], g_RecordInfo.cLastPaymentDate[1], g_RecordInfo.cLastPaymentDate[2]);
        OptionItemList.add(strTemp);

        //脱机天数
        strTemp = String.format("允许脱机天数：%d", g_StationInfo.cCanOffCount);
        OptionItemList.add(strTemp);

        startActivityForResult(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
        return;
    }

    //6.本机系统参数
    private void QueryDeviceSystemPare(int iPosition) {
        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "本机系统参数";

        //固件版本号
        OptionItemList.add("固件版本号：" + SOFTWAREVER.substring(0, 11));

        //终端序列号
        strTemp = String.format("终端序列号：%02x.%02x.%02x.%02x.%02x.%02x.",
                g_BasicInfo.cTerminalSerID[0], g_BasicInfo.cTerminalSerID[1], g_BasicInfo.cTerminalSerID[2],
                g_BasicInfo.cTerminalSerID[3], g_BasicInfo.cTerminalSerID[4], g_BasicInfo.cTerminalSerID[5]);
        OptionItemList.add(strTemp);

        //服务器IP
        OptionItemList.add("服务器IP：" + g_LocalNetStrInfo.strServerIP1);
        //服务器端口
        OptionItemList.add("服务器端口：" + g_LocalNetStrInfo.ServerPort1);

        //本机IP 
        String strIPaddr = "";
        if (g_WorkInfo.cNetlinkStatus == 1) {
            strIPaddr = getAllIp("eth0");
            if (g_LocalNetStrInfo.IPMode == 0) {
                OptionItemList.add("本机IP(DHCP)：" + strIPaddr);
            } else {
                OptionItemList.add("本机IP(Static)：" + strIPaddr);
            }
        } else if (g_WorkInfo.cNetlinkStatus == 2) {
            strIPaddr = getAllIp("wlan0");
            OptionItemList.add("本机IP(WIFI)：" + strIPaddr);
        } else {
            OptionItemList.add("本机IP：" + g_LocalNetStrInfo.strLocalIP);
        }
        //当前营业分组号
        strTemp = String.format("当前营业分组号：%d", g_WorkInfo.cBusinessID);
        OptionItemList.add(strTemp);

        //站点号
        if (g_StationInfo.iStationClass == LAN_EP_CONSUMEPOS) {
            if (g_SystemInfo.cOnlyOnlineMode == 1) {
                strTemp = String.format("站点号：%d (消费机-在线交易)", g_StationInfo.iStationID);
                OptionItemList.add(strTemp);
            } else {
                strTemp = String.format("站点号：%d (消费机)", g_StationInfo.iStationID);
                OptionItemList.add(strTemp);
            }
        } else if (g_StationInfo.iStationClass == LAN_EP_MONEYPOS) {
            strTemp = String.format("站点号：%d (充值机)", g_StationInfo.iStationID);
            OptionItemList.add(strTemp);
        } else {
            strTemp = String.format("站点号：%d", g_StationInfo.iStationID);
            OptionItemList.add(strTemp);
        }
        strTemp = String.format("工作钱包：%d", g_StationInfo.cWorkBurseID);
        OptionItemList.add(strTemp);

        startActivityForResult(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return;
    }

    //7.人脸流水查询
    private void QueryFaceLastRecord(int iPosition) {
        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "人脸流水查询";

        OptionItemList = GetFaceRecordList();

        if (OptionItemList == null) {
            ArrayList<String> ItemList = new ArrayList<String>();
            ItemList.add("无人脸交易流水");
            startActivityForResult(new Intent(this, CheckMenuActivity.class)

                    .putExtra("Type", iType)
                    .putExtra("Position", iPosition)
                    .putExtra("strTitle", strTitle)
                    .putStringArrayListExtra("OptionItemList", ItemList)
                    .putExtra("Image", image), iType);
        } else {
            startActivityForResult(new Intent(QueryPayAllActivity.this, QueryFaceRecActivity.class)

                    .putExtra("Type", iType)
                    .putExtra("Position", iPosition)
                    .putExtra("strTitle", strTitle)
                    .putStringArrayListExtra("OptionItemList", OptionItemList)
                    .putExtra("Image", image), iType);
        }
    }

    //获取人脸流水数据列表
    private ArrayList<String> GetFaceRecordList() {
        int i;
        int iItem = 1;
        int iResult;
        int iRecordCount = 0;
        int iReadIndex = 0;
        int lngRecordIndex;
        WasteFacePayBooks pWasteFacePayBooks = new WasteFacePayBooks();
        ArrayList<String> InfoList = new ArrayList<String>();

        int Year, Month, Day, Hour, Min, Sec;
        long lngPayMoney;
        long lngManageMoney;
        long lngAccID;
        long LastPayDate;

        iReadIndex = 0;
        iRecordCount = 50;    //查看流水条数
        lngRecordIndex = (int) g_WasteFaceBookInfo.WriterIndex;
        lngRecordIndex = (lngRecordIndex % MAXBOOKSCOUNT);

        if (iRecordCount > (lngRecordIndex)) {
            iRecordCount = (lngRecordIndex);
        }
        Log.i(TAG, String.format("显示流水数iRecordCount:%d,%d", iRecordCount, lngRecordIndex));

        for (i = 0; i < iRecordCount; i++) {
            LastPayDate = 0;
            //卡记录流水
            if ((lngRecordIndex - iReadIndex) >= 1) {
                pWasteFacePayBooks = WasteFacePayBooksRW.ReadWasteFacePayBooksData((int) (lngRecordIndex - iReadIndex));
                LastPayDate = pWasteFacePayBooks.cPaymentTime[0];
            }
            if (LastPayDate == 0) {
                iReadIndex++;
                continue;
            }

            String strID = "";    //序号
            String strMoney = "";    //交易金额
            String strName = "";    //姓名
            String strType = "";    //交易类型
            String strDateTime = "";    //交易时间
            String strAccID = "";    //账号
            String strMatchScore = "";    //人脸相识度
            strID = "" + iItem;//序号

            //人脸交易记录流水
            iReadIndex++;
            //姓名
            try {
                strName = ByteToString(pWasteFacePayBooks.cAccName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //交易类型
            if (pWasteFacePayBooks.cPayType == 0) {
                strType = "人脸识别消费";//交易类型
            }
            //交易时间
            Year = (int) pWasteFacePayBooks.cPaymentTime[0];
            Month = (int) pWasteFacePayBooks.cPaymentTime[1];
            Day = (int) pWasteFacePayBooks.cPaymentTime[2];

            Hour = (int) pWasteFacePayBooks.cPaymentTime[3];
            Min = (int) pWasteFacePayBooks.cPaymentTime[4];
            Sec = (int) pWasteFacePayBooks.cPaymentTime[5];
            strDateTime = String.format("%02d%02d%02d%02d%02d%02d", Year, Month, Day, Hour, Min, Sec);

            lngPayMoney = (pWasteFacePayBooks.cPaymentMoney[0] & 0xff) +
                    (pWasteFacePayBooks.cPaymentMoney[1] & 0xff) * 256 +
                    (pWasteFacePayBooks.cPaymentMoney[2] & 0xff) * 256 * 256;

            lngManageMoney = (pWasteFacePayBooks.cManageMoney[0] & 0xff) +
                    (pWasteFacePayBooks.cManageMoney[1] & 0xff) * 256 +
                    (pWasteFacePayBooks.cManageMoney[2] & 0xff) * 256 * 256;

            lngAccID = (pWasteFacePayBooks.cAccountID[0] & 0xff) +
                    (pWasteFacePayBooks.cAccountID[1] & 0xff) * 256 +
                    (pWasteFacePayBooks.cAccountID[2] & 0xff) * 256 * 256 +
                    ((long) pWasteFacePayBooks.cAccountID[3] & 0xff) * 256 * 256 * 256;

            lngPayMoney = lngPayMoney - lngManageMoney;
            strMoney = String.format("%d.%02d", lngPayMoney / 100, lngPayMoney % 100);//交易金额
            strAccID = String.format("%d", lngAccID);//账号
            strMatchScore = "" + pWasteFacePayBooks.fMatchScore;//相识度
            iItem++;
            //序号,交易金额,姓名,交易类型,交易时间
            InfoList.add(strID + "," + strMoney + "," + strName + "," + strType + "," + strDateTime + "," + strAccID + "," + strMatchScore);
        }
        return InfoList;
    }


    //8.人脸识别信息
    private void QueryFaceDiscernInfo(int iPosition) {
        int cResult;
        int iType = iPosition;
        int image = R.mipmap.s_local_check;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "人脸识别信息";

        //是否启用人脸识别
        if (g_SystemInfo.cFaceDetectFlag == 1) {
            if (g_WorkInfo.cFaceInitState == 1)
                strTemp = String.format("是否启用人脸识别：启用(初始化成功)");
            else if (g_WorkInfo.cFaceInitState == 2)
                strTemp = String.format("是否启用人脸识别：启用(数据加载完成)");
            else
                strTemp = String.format("是否启用人脸识别：启用(初始化失败)");
        } else {
            if ((g_WorkInfo.cFaceInitState == 1) || (g_WorkInfo.cFaceInitState == 2))
                strTemp = String.format("是否启用人脸识别：不启用(初始化成功)");
            else
                strTemp = String.format("是否启用人脸识别：不启用(初始化失败)");
        }
        OptionItemList.add(strTemp);

        //人脸名单数量
        strTemp = String.format("人脸名单数量：%d/%d", g_FaceIdentInfo.iListNum, g_LocalInfo.iMaxFaceNum);
        OptionItemList.add(strTemp);

        //人脸瞳距
        strTemp = String.format("瞳距：%d      相识率：%1.3f      活体率：%1.3f", g_LocalInfo.iPupilDistance,g_LocalInfo.fFraction,g_LocalInfo.fLiveThrehold);
        OptionItemList.add(strTemp);

        //版本号
        strTemp = String.format("本机版本号：%d", g_FaceCodeInfo.lngLocalVer)+
          "    "+String.format("平台版本号：%d", g_FaceCodeInfo.lngPlatVer);
        OptionItemList.add(strTemp);

        //是否支持红外双目
        if (CAMERA_NUM == 2) {
            strTemp = "支持红外双目";
            OptionItemList.add(strTemp);
        }

        String strFacepathURL = "";
        if ((g_SystemInfo.FaceHTTPServerAdr[0] == 0x00)
                || (g_SystemInfo.iFacehttpLength == 0)) {
            Log.e(TAG, "http地址无效");
            OptionItemList.add("http地址：" + "null");
        } else {
            strFacepathURL = new String(g_SystemInfo.FaceHTTPServerAdr, 0, g_SystemInfo.iFacehttpLength);
            OptionItemList.add("http地址：" + strFacepathURL);
        }
        startActivityForResult(new Intent(this, CheckMenuActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);
        return;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();

        if ((KeyValue == KeyEvent.KEYCODE_ENTER) && (event.getAction() == 0)) {
            switch (positionId) {
                case 0: //餐累统计
                    QueryTimeAmountTotal(positionId);
                    break;
                case 1:  //日累统计
                    QueryDayAmountTotal(positionId);
                    break;
                case 2:   //最近流水查询
                    QueryLastRecord(positionId);
                    break;
                case 3: //未传流水信息
                    QueryNoUpRecord(positionId);
                    break;
                case 4:  //名单记录信息
                    QueryListRecordInfo(positionId);
                    break;
                case 5:  //本机系统信息
                    QueryDeviceSystemPare(positionId);
                    break;
                case 6: //人脸流水查询
                    QueryFaceLastRecord(positionId);
                    break;
                case 7:  //人脸识别信息
                    QueryFaceDiscernInfo(positionId);
                    break;
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.e(TAG, "KeyValue:" + KeyValue);
        if (KeyValue == KeyEvent.KEYCODE_PERIOD ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD ||
                KeyValue == KeyEvent.KEYCODE_0) {
            return true;
        }
        switch (KeyValue) {
            case KeyEvent.KEYCODE_1:  //餐累统计
                QueryTimeAmountTotal(1);
                break;
            case KeyEvent.KEYCODE_2:  //日累统计
                QueryDayAmountTotal(2);
                break;
            case KeyEvent.KEYCODE_3: //最近流水查询
                QueryLastRecord(3);
                break;
            case KeyEvent.KEYCODE_4: //未传流水信息
                QueryNoUpRecord(4);
                break;
            case KeyEvent.KEYCODE_5:  //名单记录信息
                QueryListRecordInfo(5);
                break;
            case KeyEvent.KEYCODE_6://本机系统信息
                QueryDeviceSystemPare(6);
                break;
            case KeyEvent.KEYCODE_7://人脸流水查询
                QueryFaceLastRecord(7);
                break;
            case KeyEvent.KEYCODE_8://人脸识别信息
                QueryFaceDiscernInfo(8);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
