package com.hzsun.mpos.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzsun.mpos.Adapter.MenuItemAdapter;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.Usb.USBManager;
import com.hzsun.mpos.Adapter.ListInfoBean;
import com.hzsun.mpos.progressutils.ProgressManage;
import com.hzsun.mpos.thread.WriteDataToUsbThread;

import java.util.ArrayList;
import java.util.List;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

/**
    * @author  Lijie
    * @time  2020/1/9
    *@description  POS机数据导出类
 */

public class DataExportActivity extends BaseActivity {
    private String TAG = DataExportActivity.class.getSimpleName();
    private ListView menu_listview;
    private TextView textTitle;
    private MenuItemAdapter adapter;

    private List<ListInfoBean> items;
    private List<String> itemsExport;
    private ProgressManage progressManage;
    private WriteDataToUsbThread ThreadDataToUsbWork;
    public Handler UISetMenuHandler;
    private USBManager usbManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_export);
        isShowPerScreen(true);
        initData();
        initView();
        UISetMenuHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        int iRate = (int) msg.obj;
                        progressManage.setProgress(iRate);
                        break;

                    case 5:
                        progressManage.setMessage("导出数据完成");
                        progressManage.setProgress(100);

                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                progressManage.dismiss();
                                finish();
                            }
                        }, 1000);
                        break;

                    case 10:
                        progressManage.dismiss();
                        ToastUtils.showText(DataExportActivity.this, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
                        break;

                    case 20:
                        progressManage.dismiss();
                        ToastUtils.showText(DataExportActivity.this, "未找到对应文件！", WARN, BOTTOM, Toast.LENGTH_LONG);
                        break;
                }
            }
        };
    }

    private void initData() {
        ArrayList list = (ArrayList) getIntent().getSerializableExtra("list");
        items = new ArrayList<>();
        itemsExport = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ListInfoBean data = (ListInfoBean) list.get(i);
            if(!data.getTitle().contains(".ini")) { //ini和sh不显示
                items.add(data);
            }
            if (data.getValue() == 1) {
                itemsExport.add(data.getTitle());
            }
        }
    }

    private void initView() {
        menu_listview = (ListView) findViewById(R.id.menu_listview);
        textTitle = (TextView) findViewById(R.id.title_tv);
        textTitle.setText("数据导出");
        adapter = new MenuItemAdapter(items);
        menu_listview.setAdapter(adapter);
        menu_listview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedItem(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
                saveIsExport(0);
                break;
            case KeyEvent.KEYCODE_1:
                saveIsExport(1);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_ENTER:

                    usbManager = new USBManager(DataExportActivity.this);
                    progressManage = new ProgressManage(DataExportActivity.this, textTitle, getWindow());
                    progressManage.setMessage("正在导出数据文件...");
                    progressManage.show();

                    ThreadDataToUsbWork = new WriteDataToUsbThread(DataExportActivity.this, usbManager, UISetMenuHandler, itemsExport);
                    ThreadDataToUsbWork.start();

                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    //将要导出的文件路径名保存到集合
    private void saveIsExport(int value) {
        int selectedIndex = adapter.getSelectedItem();
        ListInfoBean listInfoBean = items.get(selectedIndex);
        if ((value == 0) && (listInfoBean.getValue() == 1)) {
            listInfoBean.setValue(0);
            itemsExport.remove(listInfoBean.getTitle());
        }
        if ((value == 1) && (listInfoBean.getValue() == 0)) {
            listInfoBean.setValue(1);
            itemsExport.add(listInfoBean.getTitle());
        }
        adapter.notifyDataSetChanged();
        Log.e(TAG, "saveIsExport: " + itemsExport);
    }


}
