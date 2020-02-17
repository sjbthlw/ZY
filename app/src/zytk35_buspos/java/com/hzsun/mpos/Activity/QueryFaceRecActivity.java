package com.hzsun.mpos.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hzsun.mpos.Adapter.ListFaceRecordAdapter;
import com.hzsun.mpos.Public.BitmapUtils;
import com.hzsun.mpos.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hzsun.mpos.Global.Global.PicPath;

/**
 * 交易流水查询页
 */
public class QueryFaceRecActivity extends BaseActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private TextView tvCheckedNum;
    private String TAG = getClass().getSimpleName();
    private ListView listBusinessCount;
    private PopupWindow popupWindow;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private List<Bitmap> bitmapList;
    private int image;

    private ListFaceRecordAdapter listFaceRecordAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_face_rec);
        initData();
        initViews();
        initListener();
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        Log.i(TAG, "iType:" + iType + " result:" + iRet);
        intent.putExtra("result", iRet);
        setResult(iType, intent);
        finish();//此处一定要调用finish()方法
    }

    private void initListener() {
        listBusinessCount.setOnItemClickListener(this);
        listBusinessCount.setOnItemSelectedListener(this);
    }

    private void initData() {
        iType = getIntent().getIntExtra("Type", -1);
        iPosition = getIntent().getIntExtra("Position", -1);
        strTitle = getIntent().getStringExtra("strTitle");
        OptionItemList = getIntent().getStringArrayListExtra("OptionItemList");
        image = getIntent().getIntExtra("Image", -1);
    }

    private void initViews() {
        setTitle("人脸流水查询");
        tvCheckedNum = ((TextView) findViewById(R.id.tv_checked_num));
        tvCheckedNum.setText(1 + "/" + OptionItemList.size());
        listBusinessCount = ((ListView) findViewById(R.id.list_business_count));
        bitmapList = new ArrayList<>();
        //根据记录找到相关的人脸照片
        bitmapList = GetFaceRecordPic(OptionItemList);

        listFaceRecordAdapter = new ListFaceRecordAdapter(this, OptionItemList, bitmapList);
        listBusinessCount.setAdapter(listFaceRecordAdapter);
        listBusinessCount.setOnItemSelectedListener(this);
    }

    private List<Bitmap> GetFaceRecordPic(ArrayList<String> OptionItemList) {
        List<Bitmap> bitmapList = new ArrayList<>();
        String strDateTime = "";
        String strAccID = "";
        String strFileName = "";
        String strMatchScore = "";

        for (int i = 0; i < OptionItemList.size(); i++) {
            //解析交易流水字段
            String strInfo = OptionItemList.get(i);
            String[] strArr = strInfo.split(",");
            strDateTime = strArr[4];//交易时间
            strAccID = strArr[5];//账号
            strMatchScore= strArr[6];//人脸相识度
            strFileName = strDateTime + "_" + strAccID + ".jpg";
            //判断文件是否存在
            File fFile = new File(PicPath + strFileName);
            if (!fFile.exists() )
            {
                //Log.e("UploadFaceRecord","人脸图片不存在");
                strFileName = strDateTime + "_" + strAccID + "_" + strMatchScore + ".jpg";
                File fFile1 = new File(PicPath + strFileName);
                if (!fFile1.exists() )
                {
                    Log.e("UploadFaceRecord","人脸图片不存在1");
                    bitmapList.add(BitmapFactory.decodeResource(getResources(), R.mipmap.s_head_bg));
                    continue;
                }
            }
            bitmapList.add(BitmapUtils.getLoacalBitmap(PicPath + strFileName));
        }
        return bitmapList;
    }

    //显示具体的内容
    private void ShowPopWindow(Context context, View v, String strInfo, Bitmap bitmap) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;//设置阴影透明度
        getWindow().setAttributes(lp);
        View contentView = LayoutInflater.from(context).inflate(R.layout.pop_layout, null);
        popupWindow = new PopupWindow(contentView, 853, 533);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        ImageView iv_pop = ((ImageView) contentView.findViewById(R.id.iv_pop));
        TextView tv_name = ((TextView) contentView.findViewById(R.id.tv_name));
        TextView tv_money = ((TextView) contentView.findViewById(R.id.tv_money));
        TextView tv_time = ((TextView) contentView.findViewById(R.id.tv_time));
        contentView.findViewById(R.id.iv_exit).setOnClickListener(V -> {
            popupWindow.dismiss();
        });

        //解析交易流水字段
        String[] strArr = strInfo.split(",");
        tv_money.setText(strArr[1] + "元");//交易金额
        tv_name.setText(strArr[2]);//姓名
        //处理时间
        String Year, Month, Day, Hour, Min, Sec;
        Year = strArr[4].substring(0, 2);
        Month = strArr[4].substring(2, 4);
        Day = strArr[4].substring(4, 6);
        Hour = strArr[4].substring(6, 8);
        Min = strArr[4].substring(8, 10);
        Sec = strArr[4].substring(10, 12);
        tv_time.setText(Month + "-" + Day + " " + Hour + ":" + Min + ":" + Sec);//交易时间
        iv_pop.setImageBitmap(BitmapUtils.zoomBitmap(bitmap, 480, 640));

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;//设置阴影透明度
                getWindow().setAttributes(lp);
            }
        });
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (bitmapList.size() > 0) {
            ShowPopWindow(this, view, OptionItemList.get(position), bitmapList.get(position));
        }
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
        listFaceRecordAdapter.setCurrentItem(position);
        listFaceRecordAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
