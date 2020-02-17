package com.hzsun.mpos.Adapter;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzsun.mpos.MyApplication;
import com.hzsun.mpos.R;

import java.util.ArrayList;
import java.util.List;

public class IdentityTypeAdapter extends BaseAdapter {

    private List<String> list = new ArrayList<>();
    private List<String> values = new ArrayList<>();
    private int selectedIndex;
    private Resources resources;
    private int width;

    public IdentityTypeAdapter() {
        resources = MyApplication.myApp.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        width = dm.widthPixels;

        list.add("临时卡");
        list.add("学生卡");
        list.add("校园卡");
        list.add("教工卡");
        list.add("校友卡");

        values.add("linshicard");
        values.add("xueshengcard");
        values.add("xiaoyuancard");
        values.add("jiaogongcard");
        values.add("xiaoyoucard");
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    public String getValue(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_identity_types, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RelativeLayout.LayoutParams params =new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.width = width / 5;
        holder.tv_item.setLayoutParams(params);

        holder.tv_item.setText(list.get(position));
        holder.tv_item.setSelected(selectedIndex == position);
        return convertView;
    }

    class ViewHolder {
        TextView tv_item;

        public ViewHolder(View itemView) {
            tv_item = itemView.findViewById(R.id.tv_item);
        }
    }
}
