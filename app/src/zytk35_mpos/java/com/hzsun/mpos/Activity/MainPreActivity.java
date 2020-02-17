package com.hzsun.mpos.Activity;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.R;

import static com.hzsun.mpos.Global.Global.SOFTWAREVER;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.getProp;

public class MainPreActivity extends Presentation {

    private static final String TAG = "MainPreActivity";

    private ImageView iv_NetStatePer;
    private TextView tv_DateTime;
    private TextView tv_CpuTemp;
    private TextView tv_version;

    public MainPreActivity(Context outerContext, Display display) {
        super(outerContext, display);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pre);
        iv_NetStatePer = ((ImageView) findViewById(R.id.iv_netStatePer));
        tv_DateTime = (TextView) findViewById(R.id.tv_DateTime);
        tv_CpuTemp=findViewById(R.id.tv_cput);
        tv_version=findViewById(R.id.tv_version);
        ShowNetState(g_WorkInfo.cNetlinkStatus);
        ShowDateTime();
        ShowSystemVer();
    }

    //显示时间日期
    public void ShowDateTime(String strDateTime) {
        tv_DateTime.setText(strDateTime);
    }

    //显示时间
    private void ShowDateTime() {
        String strData = "";
        String strWeek = "";
        String strTime = "";
        String strHour = "";
        String strMin = "";
        String strSec = "";

        String strTmp = Publicfun.GetFullDateWeekTime(Publicfun.toData(System.currentTimeMillis()));
        strData = strTmp.substring(0, 10);
        strTime = strTmp.substring(15, 20);
        strWeek = Publicfun.GetWeekCHNName(strTmp);
        //2018-06-27 09:10 星期四
        String strDataTime = strData + " " + strTime + " " + strWeek;
        tv_DateTime.setText(strDataTime);
    }

    //显示温度
    public void ShowCPUTemp(String strTemp) {
        tv_CpuTemp.setText(strTemp);
    }

    //显示系统版本号和固件版本
    private void ShowSystemVer() {
        String value  = getProp("ro.product.zytkdevice");
        tv_version.setText(value+"-"+SOFTWAREVER);
    }

    //显示网络状态
    public void ShowNetState(int iNetState) {
        int iState = 0;
        if (iNetState == 1) {
            if (g_WorkInfo.cRunState == 1)
                iState = 2;//联网
            else
                iState = 1;//脱网
        } else if (iNetState == 2) {
            if (g_WorkInfo.cRunState == 1)
                iState = 4;//联网
            else
                iState = 5;//脱网
        } else {
            iState = 0;
        }

        switch (iState) {
            case 0: //没有网络
                iv_NetStatePer.setImageResource(R.mipmap.s_net_null);
                break;
            case 1: //以太网络 网络故障 脱机
                iv_NetStatePer.setImageResource(R.mipmap.s_net_etherr);
                break;
            case 2: //以太网络 联机运行
                iv_NetStatePer.setImageResource(R.mipmap.s_net_eth);
                break;
            case 3: //wifi网络 网络故障 脱机
                iv_NetStatePer.setImageResource(R.mipmap.s_net_wifierr);
                break;
            case 4: //wifi网络 联机运行
                iv_NetStatePer.setImageResource(R.mipmap.s_net_wifi);
                break;
        }
    }

}
