package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hzsun.mpos.Adapter.MenuListviewAdapter;
import com.hzsun.mpos.R;

import java.util.ArrayList;

/**
 * 高级总界面
 */
public class SeniorSysAllActivity extends BaseActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private String TAG = getClass().getSimpleName();
    private ImageView ivSetImage;
    private TextView titleTv;
    private TextView tvCheckedNum;
    private ListView seniorSysAllListview;
    private int image = R.mipmap.s_local_senior;
    private String title = null;
    private ArrayList<String> stringList;
    private MenuListviewAdapter menuListviewAdapter;
    private int positionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_sys_all);
        isShowPerScreen(true);
        initViews();
        initListView();
    }

    private void initViews() {
        ivSetImage = ((ImageView) findViewById(R.id.local_business_set_image));
        titleTv = ((TextView) findViewById(R.id.title_tv));
        tvCheckedNum = ((TextView) findViewById(R.id.tv_checked_num));
        seniorSysAllListview = ((ListView) findViewById(R.id.senior_sys_all_listview));
        ivSetImage.setImageResource(image);
        title = getResources().getString(R.string.senior);
        titleTv.setText(title);
    }

    private void initListView() {
        stringList = new ArrayList<>();
        stringList.add(getResources().getString(R.string.local_Twomenu_senior_2));
        stringList.add(getResources().getString(R.string.local_Twomenu_senior_4));
        menuListviewAdapter = new MenuListviewAdapter(this, stringList);
        seniorSysAllListview.setAdapter(menuListviewAdapter);
        seniorSysAllListview.setOnItemSelectedListener(this);
        seniorSysAllListview.setOnItemClickListener(this);
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

    private void netParams(int position) {
        startActivity(new Intent(SeniorSysAllActivity.this, SetNetParamsActivity.class)
                .putExtra("TwoMenuTitle", title)
                .putExtra("SeniorMenuTitle", stringList.get(position))
                .putExtra("Image", image));
    }

    //关于本机参数
    private void GotoAboutThisMac(int position) {
        startActivity(new Intent(SeniorSysAllActivity.this, AboutPosActivity.class));
    }

    //高级参数设置
    private void GotoAdvanced(int position) {
        startActivity(new Intent(SeniorSysAllActivity.this, SeniorParamsActivity.class));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
//            case 0:  //关于本机
//                GotoAboutThisMac(position);
//                break;
//            case 1:  //高级参数
//                GotoAdvanced(position);
//                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.d(TAG, "dispatchKeyEvent KeyValue:" + KeyValue);
        //Log.d("按键",event.getAction()+"");

        if ((KeyValue == KeyEvent.KEYCODE_ENTER) && (event.getAction() == 0)) {
            switch (positionId) {
                case 0:  //关于本机
                    GotoAboutThisMac(positionId);
                    break;
                case 1:  //高级参数
                    GotoAdvanced(positionId);
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
        //LogUtil.e(TAG, "KeyValue:" + KeyValue);
        if (KeyValue == KeyEvent.KEYCODE_5 ||
                KeyValue == KeyEvent.KEYCODE_6 ||
                KeyValue == KeyEvent.KEYCODE_7 ||
                KeyValue == KeyEvent.KEYCODE_8 ||
                KeyValue == KeyEvent.KEYCODE_9 ||
                KeyValue == KeyEvent.KEYCODE_PERIOD ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD ||
                KeyValue == KeyEvent.KEYCODE_0) {
            return true;
        }
        switch (KeyValue) {

            case KeyEvent.KEYCODE_1:  //关于本机
                GotoAboutThisMac(KeyValue - KeyEvent.KEYCODE_1);
                break;
            case KeyEvent.KEYCODE_2:  //高级参数
                GotoAdvanced(KeyValue - KeyEvent.KEYCODE_1);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}
