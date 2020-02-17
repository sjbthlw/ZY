package com.hzsun.mpos.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;

import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.ToastUtils.CENTER;
import static com.hzsun.mpos.Public.ToastUtils.FAIL;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

/**
 * 主菜单选择页面
 */
public class MenuActivity extends BaseActivity implements View.OnClickListener, PopupWindow.OnDismissListener {

    private String TAG = getClass().getSimpleName();
    private Button btElect, btSet, bt_cancle, bt_confirm;
    private PopupWindow popupWindow;
    private EditText et_passWord;
    private TextView text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initViews();
        initListener();
    }

    private void initListener() {
        btElect.setOnClickListener(this);
        btSet.setOnClickListener(this);
    }

    private void initViews() {
        setTitle("");
        btElect = ((Button) findViewById(R.id.bt_elect));
        btSet = ((Button) findViewById(R.id.bt_set));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_elect:  //查询点击
                GotoQueryPayAllMenu();
                break;
            case R.id.bt_set:    //设置点击
                ShowPopWindow(this, btSet);
                break;
            case R.id.bt_cancle: //弹窗取消
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                break;
            case R.id.bt_confirm:  //弹窗确定
                judgePassword();
                break;
        }
    }

    private void judgePassword() {
        String passWord = et_passWord.getText().toString();
        if (!passWord.equals("")) {
            String strTemp = "" + g_StationInfo.lngAdvancePsw;
            if(strTemp.length()!=6)//默认密码为333333
                strTemp="333333";
            Log.i(TAG, "密钥：" + strTemp);
            if (passWord.equals(strTemp)) {
                GotoSetPayAllMenu();
                popupWindow.dismiss();
            } else {
                ToastUtils.showText(this, "密码错误", FAIL, CENTER, Toast.LENGTH_LONG);
            }
        } else {
            ToastUtils.showText(this, "请输入密码！", WARN, CENTER, Toast.LENGTH_LONG);
        }
    }

    //进入查询界面
    private void GotoQueryPayAllMenu() {
        Log.i(TAG, "进入查询界面");
        startActivity(new Intent(MenuActivity.this, QueryPayAllActivity.class));
    }

    //进入交易统计设置界面
    private void GotoSetPayAllMenu() {
        Log.i(TAG, "进入交易统计设置界面");
        startActivity(new Intent(MenuActivity.this, SetPayAllActivity.class));
    }

    private void ShowPopWindow(Context context, View view) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.6f;//设置阴影透明度
        getWindow().setAttributes(lp);
        View contentView = LayoutInflater.from(context).inflate(R.layout.pwdpop_layout, null);

        popupWindow = new PopupWindow(contentView, 576, 398);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, -150);
        et_passWord = ((EditText) contentView.findViewById(R.id.et_password));
        bt_cancle = ((Button) contentView.findViewById(R.id.bt_cancle));
        bt_confirm = ((Button) contentView.findViewById(R.id.bt_confirm));
        bt_cancle.setOnClickListener(MenuActivity.this);
        bt_confirm.setOnClickListener(MenuActivity.this);
        et_passWord.setOnClickListener(MenuActivity.this);
        popupWindow.setOnDismissListener(MenuActivity.this);

    }

    @Override
    public void onDismiss() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;//设置阴影透明度
        getWindow().setAttributes(lp);
    }

    //在onResume()方法注册
    @Override
    protected void onResume() {

        Log.i(TAG, "MenuActivity 注册");
        g_WorkInfo.cCardEnableFlag = 0;
        super.onResume();
    }

    //onPause()方法注销
    @Override
    protected void onPause() {

        Log.i(TAG, "MenuActivity 注销");
        super.onPause();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        // TODO Auto-generated method stub
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
//                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//            }
//        }
//        return super.onTouchEvent(event);
//    }

//    private void showSoft() {
//        Handler handle = new Handler();
//        handle.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                InputMethodManager inputMethodManager = (InputMethodManager) et_passWord.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.showSoftInput(et_passWord, 0);
//            }
//        }, 0);
//    }
}
