package com.hzsun.mpos.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hzsun.mpos.R;
import com.hzsun.mpos.views.MarkSeekBar;
import com.hzsun.mpos.views.SwitchButton;

import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.data.LocalInfoRW.WriteAllLocalInfo;

/**
 * 人脸检测设置
 */
public class FaceInfoSetActivity extends BaseActivity implements View.OnClickListener,
        SwitchButton.OnCheckedChangeListener {

    private Button bt_confirm, bt_cancle;
    private SwitchButton sb_immortalDetect;
    private MarkSeekBar msb_faceResolution, msb_immortalResolution;
    private TextView tv_immortalResolution;
    private short cFaceLiveFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_info_set);
        initViews();
        initListener();
        initData();
    }

    private void initViews() {
        setTitle("设置人脸特征参数");
        tv_immortalResolution = ((TextView) findViewById(R.id.tv_immortalResolution));
        sb_immortalDetect = ((SwitchButton) findViewById(R.id.sb_immortalDetect));//是否开启活体
        msb_faceResolution = ((MarkSeekBar) findViewById(R.id.msb_faceResolution)); //人脸识别度
        msb_immortalResolution = ((MarkSeekBar) findViewById(R.id.msb_immortalResolution));//活体识别度

        bt_confirm = (Button) findViewById(R.id.bt_confirm);
        bt_cancle = (Button) findViewById(R.id.bt_cancle);
    }

    private void initListener() {
        sb_immortalDetect.setOnCheckedChangeListener(this);
        bt_confirm.setOnClickListener(this);
        bt_cancle.setOnClickListener(this);
    }

    private void initData() {
        msb_faceResolution.setMarkPercent(0.6);
        msb_immortalResolution.setMarkPercent(0.3);
        if (g_LocalInfo.cFaceLiveFlag == 0)   //人脸活体设置 0:打开活体 1:关闭活体
        {
            sb_immortalDetect.setChecked(true);
        } else {
            sb_immortalDetect.setChecked(false);
        }
        msb_faceResolution.setProgress((int) (g_LocalInfo.fFraction * 100));    //相识度
        msb_immortalResolution.setProgress((int) (g_LocalInfo.fLiveThrehold * 100));     //活体度
    }


    /**
     * @param buttonView
     * @param isChecked  true 开,false 关
     */
    @Override
    public void onCheckedChanged(SwitchButton buttonView, boolean isChecked) {
        if (!isChecked) {
            tv_immortalResolution.setTextColor(getResources().getColor(R.color.color_seekBar_unenable));
            msb_immortalResolution.setEnabled(false);
            cFaceLiveFlag = 1;  //关闭
        } else {
            tv_immortalResolution.setTextColor(getResources().getColor(R.color.colorWhite));
            msb_immortalResolution.setEnabled(true);
            cFaceLiveFlag = 0;  //开启
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_cancle:
                finish();
                break;
            case R.id.bt_confirm:
                g_LocalInfo.cFaceLiveFlag = cFaceLiveFlag;
                g_LocalInfo.fFraction = ((float) (msb_faceResolution.getProgress()) / 100);//相识度
                g_LocalInfo.fLiveThrehold = ((float) (msb_immortalResolution.getProgress()) / 100);//活体
                WriteAllLocalInfo(g_LocalInfo);
                finish();
                break;
        }
    }

}
