package com.hzsun.mpos.Activity;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hzsun.mpos.Adapter.WifiListAdapter;
import com.hzsun.mpos.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


/**
 * wifi列表界面
 */
public class WifiListActivity extends BaseActivity {

    private int image;
    private String title, proTitle;
    private ImageView localBusinessSetImage;
    private TextView titleTv;
    private ListView wifiListView;
    private WifiListAdapter wifiListAdapter;
    private WifiManager wifiManager;
    private TextView emptyText;
    private List<ScanResult> wifiList;// wifi列表
    private List<ScanResult> temp = new ArrayList<>();
    private HashMap<String, ScanResult> map = new HashMap<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0: //检测到wifi
                    wifiListAdapter.clear();
//                    sortByLevel(wifiList);
                    wifiListAdapter.addAll(wifiManager, wifiList);
                    break;
            }
        }
    };
    private ScanWifiThread scanWifiThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        isShowPerScreen(true);
        initData();
        initViews();
    }

    @Override
    protected void onResume() {
        openWifi();
        scanWifiThread = new ScanWifiThread();
        scanWifiThread.start();
        super.onResume();
    }

    /**
     * 根据wifi信号强度排序
     *
     * @param list
     */
    private void sortByLevel(List<ScanResult> list) {

        Collections.sort(list, new Comparator<ScanResult>() {

            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return rhs.level - lhs.level;
            }
        });

    }

    /**
     * 打开wifi
     */
    public void openWifi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 扫描wifi线程
     *
     * @author passing
     */
    class ScanWifiThread extends Thread {
        private boolean isScan = true;

        public void exit() {
            isScan = false;
        }

        @Override
        public void run() {
            while (isScan) {
                startScan();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * 扫描wifi
     */
    public void startScan() {
        wifiManager.startScan();
        // 获取扫描结果
        wifiList = wifiManager.getScanResults();
        removeUnAvalibleWifi();
        sortByLevel(wifiList);
        handler.sendEmptyMessage(0);
    }


    private void removeUnAvalibleWifi() {
        //去除wifi名为空的wifi
        temp.clear();
        for (ScanResult wifi : wifiList) {
            if (TextUtils.isEmpty(wifi.SSID)) {
                temp.add(wifi);
            }
        }
        wifiList.removeAll(temp);

        //合并wifi名相同的wifi
        map.clear();
        for (ScanResult wifi : wifiList) {
            map.put(wifi.SSID, wifi);
        }

        wifiList.clear();
        for (String ssid : map.keySet()) {
            wifiList.add(map.get(ssid));
        }
    }

    private void initData() {
        image = getIntent().getIntExtra("Image", -1);
        title = getIntent().getStringExtra("Title");
        proTitle = getIntent().getStringExtra("proTitle");

    }

    private void initViews() {
        setTitle("WIFI列表");
        localBusinessSetImage = ((ImageView) findViewById(R.id.local_business_set_image));
        titleTv = ((TextView) findViewById(R.id.title_tv));
        wifiListView = ((ListView) findViewById(R.id.lv_wifi));
        emptyText = ((TextView) findViewById(R.id.empty_text));
        localBusinessSetImage.setImageResource(image);
        titleTv.setText(proTitle + " > " + title);
        wifiListView.setEmptyView(emptyText);
        wifiListAdapter = new WifiListAdapter(this);
        wifiListView.setAdapter(wifiListAdapter);
        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanWifiThread.exit();
    }
}
