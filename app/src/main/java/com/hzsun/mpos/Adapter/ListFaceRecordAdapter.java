package com.hzsun.mpos.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzsun.mpos.Public.BitmapUtils;
import com.hzsun.mpos.R;

import java.util.ArrayList;
import java.util.List;


public class ListFaceRecordAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private int currentItem = -1;
    private ArrayList<String> infoList;
    private List<Bitmap> bitmapList;
    private String strTemp = "";

    public ListFaceRecordAdapter(Context context, ArrayList<String> infoList, List<Bitmap> bitmapList) {
        this.context = context;
        this.infoList = infoList;
        this.bitmapList = bitmapList;
        inflater = LayoutInflater.from(context);
    }

    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }

    @Override
    public int getCount() {
        return (infoList.size());
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_facerecord_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //解析交易流水字段
        strTemp = infoList.get(position);
        String[] strArr = strTemp.split(",");

        viewHolder.tvOrderNum.setText(strArr[0]);//序号
        viewHolder.tvMoney.setText(strArr[1]);//交易金额
        viewHolder.tvName.setText(strArr[2]);//姓名
        //处理时间
        String Year, Month, Day, Hour, Min, Sec;
        Year = strArr[4].substring(0, 2);
        Month = strArr[4].substring(2, 4);
        Day = strArr[4].substring(4, 6);
        Hour = strArr[4].substring(6, 8);
        Min = strArr[4].substring(8, 10);
        Sec = strArr[4].substring(10, 12);
        viewHolder.tvBusinessTime.setText(Month + "-" + Day + " " + Hour + ":" + Min + ":" + Sec);//交易时间

        if (bitmapList.size() > 0) {
            viewHolder.ivFacePic.setImageBitmap(BitmapUtils.zoomBitmap(bitmapList.get(position),
                    120, 160));
        }
        if (currentItem == position) {
            viewHolder.linearItem.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
        } else {
            viewHolder.linearItem.setBackgroundColor(context.getResources().getColor(R.color.color_blue_pale_gray));
        }
        return convertView;
    }

    class ViewHolder {
        private LinearLayout linearItem;
        private TextView tvOrderNum;
        private TextView tvMoney;
        private TextView tvName;
        private TextView tvBusinessTime;
        private ImageView ivFacePic;

        ViewHolder(View view) {
            linearItem = ((LinearLayout) view.findViewById(R.id.linear_item));
            tvOrderNum = ((TextView) view.findViewById(R.id.tv_orderNum));//序号
            tvMoney = ((TextView) view.findViewById(R.id.tv_money));//交易金额
            tvName = ((TextView) view.findViewById(R.id.tv_name));//姓名
            tvBusinessTime = ((TextView) view.findViewById(R.id.tv_businessTime));//交易时间
            ivFacePic = ((ImageView) view.findViewById(R.id.iv_facepic));//交易人脸图片
        }
    }
}
