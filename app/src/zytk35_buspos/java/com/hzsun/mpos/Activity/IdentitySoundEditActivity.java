package com.hzsun.mpos.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hzsun.mpos.Adapter.IdentityAdapter;
import com.hzsun.mpos.Adapter.IdentityTypeAdapter;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.IdentityInfo;
import com.hzsun.mpos.data.IdentitySoundRW;
import com.hzsun.mpos.views.HorizontalListView;

import static com.hzsun.mpos.Global.Global.g_MapIDSound;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.FAIL;

import java.util.LinkedHashMap;
import java.util.Map;


public class IdentitySoundEditActivity extends BaseActivity {
    private String TAG = IdentitySoundEditActivity.class.getSimpleName();
    private HorizontalListView typelist;
    private GridView identitylist;
    private TextView tv_currenttype;
    private TextView tv_identity;
    private TextView tv_result;
    private IdentityTypeAdapter typeAdapter;
    private IdentityAdapter identityAdapter;
    //格式如下:map.put(1,"linshicard");
    private Map<String, String> map = new LinkedHashMap<>();
    private StringBuilder builder = new StringBuilder();
    private StringBuilder builder2 = new StringBuilder();
    private String curIdtype;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identitysoundedit);
        initView();
        showListView();
        showSavedIdentity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveIdentityParams();
    }

    private void initView() {
        setTitle("身份语音设置");
        typelist = (HorizontalListView) findViewById(R.id.cardtype_gridview);
        identitylist = (GridView) findViewById(R.id.identity_gridview);
        tv_currenttype = (TextView) findViewById(R.id.tv_currenttype);
        tv_identity = (TextView) findViewById(R.id.tv_identity);
        tv_result = (TextView) findViewById(R.id.tv_result);
    }

    private void showListView() {
        typeAdapter = new IdentityTypeAdapter();
        typelist.setAdapter(typeAdapter);
        tv_currenttype.setText(typeAdapter.getItem(0));
        curIdtype = typeAdapter.getValue(0);
        typelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                typeAdapter.setSelectedIndex(position);
                typeAdapter.notifyDataSetChanged();
                curIdtype = typeAdapter.getValue(position);
                identityAdapter.setCurIDType(curIdtype);
                identityAdapter.notifyDataSetChanged();
                tv_currenttype.setText(typeAdapter.getItem(position));
                showSelectedIdentity();
            }
        });

        identityAdapter = new IdentityAdapter();
        identityAdapter.setCurIDType(curIdtype);
        identitylist.setAdapter(identityAdapter);
        identitylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IdentityInfo identityInfo = identityAdapter.getItem(position);
                boolean isSelected = identityInfo.isSelected();
                String key = identityInfo.getValue();
                if (isSelected) {
                    if (identityInfo.getIdtype().equals(curIdtype)) {
                        map.remove(key);
                        identityInfo.setSelected(!isSelected);
                        identityAdapter.notifyDataSetChanged();
                        showSelectedIdentity();
                        showAllSelectedIdentity();
                    } else {
                        ToastUtils.showText(IdentitySoundEditActivity.this, "请先切换到对应的类型再删除", FAIL, BOTTOM, Toast.LENGTH_LONG);
                    }
                } else {
                    int index = typeAdapter.getSelectedIndex();
                    String value = typeAdapter.getValue(index);
                    map.put(key, typeAdapter.getValue(index));
                    identityInfo.setSelected(!isSelected);
                    identityInfo.setIdtype(value);
                    identityAdapter.notifyDataSetChanged();
                    showSelectedIdentity();
                    showAllSelectedIdentity();
                }

            }
        });
    }

    /**
     * 显示当前选择的身份
     */
    private void showSelectedIdentity() {
        int curIndex = typeAdapter.getSelectedIndex();
        String curType = typeAdapter.getValue(curIndex);
        builder.replace(0, builder.length(), "");
        if (map == null) {
            return;
        }
        for (String key : map.keySet()) {
            String value = map.get(key);
            if (value.equals(curType)) {
                builder.append(key + ",");
            }
        }
        int endPosition;
        if (builder.toString().length() - 1 < 0) {
            endPosition = 0;
        } else {
            endPosition = builder.toString().length() - 1;
        }
        String result = builder.toString().substring(0, endPosition);
        tv_identity.setText(result);
    }

    /**
     * 显示所有选择的身份
     */
    private void showAllSelectedIdentity() {
        builder2.replace(0, builder2.length(), "");
        int len;
        for (int i = 0; i < typeAdapter.getCount(); i++) {
            String item = typeAdapter.getItem(i);
            String curidtype = typeAdapter.getValue(i);
            len = 0;
            if (map == null) {
                return;
            }
            for (String key : map.keySet()) {
                String value = map.get(key);
                if (value.equals(curidtype)) {
                    if (len == 0) {
                        builder2.append(item).append(":");
                    }
                    builder2.append(key).append(",");
                    len++;
                }
            }
            if (len > 0 && i < typeAdapter.getCount() - 1) {
                builder2.append("\n");
            }
        }
        tv_result.setText(builder2.toString());
    }

    /**
     * 显示本地保存的身份语音参数
     */
    private void showSavedIdentity() {
        map = IdentitySoundRW.ReadIdentitySoundFile();
        if (map != null && map.size() > 0) {
            int len = identityAdapter.getList().size();
            for (int i = 0; i < len; i++) {
                String idtype = map.get(String.valueOf(i));
                IdentityInfo identityInfo = identityAdapter.getList().get(i);
                if (!TextUtils.isEmpty(idtype)) {
                    identityInfo.setIdtype(idtype);
                    identityInfo.setSelected(true);
                }
            }
            identityAdapter.notifyDataSetChanged();
            showSelectedIdentity();
            showAllSelectedIdentity();
        }
    }

    //保存语音参数
    private void saveIdentityParams() {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        if (IdentitySoundRW.WriteIdentitySoundFile(json) == 0) {
            Log.i(TAG, "写入成功");
        } else {
            Log.i(TAG, "写入失败");
        }
        Log.i(TAG, "读取身份语音信息");
        g_MapIDSound = IdentitySoundRW.ReadIdentitySoundFile();
    }

}
