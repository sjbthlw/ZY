package com.hzsun.mpos.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.hzsun.mpos.Adapter.BillsDetailAdapter;
import com.hzsun.mpos.Http.DataAnalyze;
import com.hzsun.mpos.Public.NumberFormat;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.BillsInfo;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_LocalNetStrInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.FAIL;


public class QueryPlatRecActivity extends BaseActivity {

    private String TAG = QueryPlatRecActivity.class.getSimpleName();

    private ListView list_bills;
    private StringBuilder builder = new StringBuilder();
    private BillsDetailAdapter adapter;
    private List<BillsInfo.TableItem> list = new ArrayList<>();
    private LoadBillsListTask loadBillsListTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_plat_rec);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loadBillsListTask == null) {
            loadBillsListTask = new LoadBillsListTask();
            loadBillsListTask.execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadBillsListTask != null) {
            loadBillsListTask.cancel(true);
            loadBillsListTask = null;
        }
    }

    private void initView() {
        setTitle("平台流水明细查询");
        list_bills = (ListView) findViewById(R.id.list_bills);
        adapter = new BillsDetailAdapter(list);
        list_bills.setAdapter(adapter);
    }

    /**
     * 查询平台流水明细
     */
    private String getBillsDetail() {
        if ((g_WorkInfo.cRunState != 1) || (g_BasicInfo.cSystemState != 100)) {
            Log.i(TAG, "无法连接服务器，请检查网络");
            return null;
        }
        if (g_CardInfo.cExistState == 1) {
            Log.i(TAG, "交易中无法查询");
            return null;
        }
        long lngBeginPaymentID = 1;
        long lngEndPaymentID = lngBeginPaymentID + 99;
        int iBusinessID = g_WorkInfo.cBusinessID;

        String StrDate = Publicfun.getFullTime("yyyy-MM-dd");
        String StrTime = Publicfun.getFullTime("hh:mm:ss");

        String url = "http://" + g_LocalNetStrInfo.strServerIP1 + ":8080/GetDeviceTrans.aspx";
        Log.i(TAG, url);
        int lngTemp = g_StationInfo.iStationID;
        int type = 0;

        builder.delete(0, builder.length());
        builder.append(lngBeginPaymentID).append("|")
                .append(StrDate).append("|")
                .append(lngTemp).append("|")
                .append(iBusinessID).append("|")
                .append(lngEndPaymentID).append("|")
                .append(StrTime).append("|")
                .append(type).append("|")
                .append("ok15we1@oid8x5afd@");
        String sign = builder.toString();
        try {
            sign = NumberFormat.md5(sign);
            Log.i(TAG, "Sign:" + sign);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Map<String, String> map = new HashMap<>();
        map.put("BeginRecNum", String.valueOf(lngBeginPaymentID));
        map.put("Date", StrDate);
        map.put("DeviceNum", String.valueOf(lngTemp));
        map.put("DurationID", String.valueOf(iBusinessID));
        map.put("EndRecNum", String.valueOf(lngEndPaymentID));
        map.put("Time", StrTime);
        map.put("Type", String.valueOf(type));
        map.put("Sign", sign);
        String jsonData = DataAnalyze.Post(url, map);
        return jsonData;
    }

    class LoadBillsListTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            return getBillsDetail();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (TextUtils.isEmpty(result)) {
                Log.i(TAG, "无法连接服务器，请检查网络");
                ToastUtils.showText(QueryPlatRecActivity.this, "无法连接服务器，请检查网络", FAIL, BOTTOM, Toast.LENGTH_LONG);
            } else {
                Log.i(TAG, result);
                Format format = new Format();
                Serializer serializer = new Persister(format);
                BillsInfo billsInfo;
                try {
                    billsInfo = serializer.read(BillsInfo.class, result);
                    int size = billsInfo.getTables().size();
                    if (size > 0) {
                        list.clear();
                        list.addAll(billsInfo.getTables());
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "获取到的数据不完整");
                }
            }
        }
    }


}
