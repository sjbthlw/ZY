package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hzsun.mpos.Adapter.ListBusinessCountAdapter;
import com.hzsun.mpos.R;

import java.util.ArrayList;

/**
 * 交易流水查询页
 */
public class QueryRecordActivity extends BaseActivity
        implements AdapterView.OnItemSelectedListener {

    private TextView tvCheckedNum;
    private String TAG = getClass().getSimpleName();
    private ImageView ivCheckMenu;
    private TextView textTitle;
    private LinearLayout linearTop;
    private ListView listBusinessCount;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;

    private ListBusinessCountAdapter listBusinessCountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_info);
        isShowPerScreen(true);
        initData();
        initViews();
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        Log.i(TAG, "iType:" + iType + " result:" + iRet);
        intent.putExtra("result", iRet);
        setResult(iType, intent);
        finish();//此处一定要调用finish()方法
    }

    private void initData() {

        iType = getIntent().getIntExtra("Type", -1);
        iPosition = getIntent().getIntExtra("Position", -1);
        strTitle = getIntent().getStringExtra("strTitle");
        OptionItemList = getIntent().getStringArrayListExtra("OptionItemList");
        image = getIntent().getIntExtra("Image", -1);
    }

    private void initViews() {
        tvCheckedNum = ((TextView) findViewById(R.id.tv_checked_num));
        ivCheckMenu = ((ImageView) findViewById(R.id.iv_check_menu));
        textTitle = ((TextView) findViewById(R.id.text_title));
        linearTop = ((LinearLayout) findViewById(R.id.linear_top));
        listBusinessCount = ((ListView) findViewById(R.id.list_business_count));

        textTitle.setText(strTitle);
        ivCheckMenu.setImageResource(image);

        listBusinessCountAdapter = new ListBusinessCountAdapter(this, OptionItemList);
        listBusinessCount.setAdapter(listBusinessCountAdapter);
        listBusinessCount.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int KeyValue;
        KeyValue = event.getKeyCode();
        //LogUtil.e(TAG, "KeyValue:" + KeyValue);
        if (KeyValue == KeyEvent.KEYCODE_FUNCTION ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_DOT ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD ||
                ((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9))) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        tvCheckedNum.setText(position + 1 + "/" + OptionItemList.size());
        listBusinessCountAdapter.setCurrentItem(position);
        listBusinessCountAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
