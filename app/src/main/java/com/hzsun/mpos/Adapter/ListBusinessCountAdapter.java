package com.hzsun.mpos.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzsun.mpos.R;

import java.util.ArrayList;


public class ListBusinessCountAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private int currentItem = -1;

    private String strTemp = "";

    private ArrayList<String> infoList;

    public ListBusinessCountAdapter(Context context, ArrayList<String> infoList) {
        this.context = context;
        this.infoList = infoList;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_business_count_item, parent, false);
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
        viewHolder.tvRecordtype.setText(strArr[3]);//交易类型
        viewHolder.tvBusinessTime.setText(strArr[4]);//交易时间

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
        private TextView tvRecordtype;
        private TextView tvBusinessTime;

        ViewHolder(View view) {
            linearItem = ((LinearLayout) view.findViewById(R.id.linear_item));

            tvOrderNum = ((TextView) view.findViewById(R.id.tv_orderNum));//序号
            tvMoney = ((TextView) view.findViewById(R.id.tv_money));//交易金额
            tvName = ((TextView) view.findViewById(R.id.tv_name));//姓名
            tvRecordtype = ((TextView) view.findViewById(R.id.tv_Recordtype));//交易类型
            tvBusinessTime = ((TextView) view.findViewById(R.id.tv_businessTime));//交易时间
        }
    }
}
