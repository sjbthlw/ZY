package com.hzsun.mpos.Adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.hzsun.mpos.R;


import java.util.List;

public class MenuItemAdapter extends BaseAdapter {

    private List<ListInfoBean> list;
    private int selectedItem;

    public MenuItemAdapter(List<ListInfoBean> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public ListInfoBean getItem(int position) {
        return list == null ? null : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (holder == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menuitem_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ListInfoBean bean = list.get(position);
        holder.tv_lab.setText(bean.getTitle());
        if (selectedItem == position) {
            holder.tv_lab.setTextSize(TypedValue.COMPLEX_UNIT_SP,37);
        } else {
            holder.tv_lab.setTextSize(TypedValue.COMPLEX_UNIT_SP,33);
        }
        holder.radio1.setText(bean.getItem1());
        holder.radio2.setText(bean.getItem2());
        holder.radio1.setChecked(bean.getValue() == 0);
        holder.radio2.setChecked(bean.getValue() == 1);
        int visibility = (selectedItem == position) ? View.VISIBLE : View.INVISIBLE;
        holder.tv_usetip.setVisibility(visibility);
        return convertView;
    }

    class ViewHolder {

        private TextView tv_lab;
        private RadioButton radio1;
        private RadioButton radio2;
        private TextView tv_usetip;

        public ViewHolder(View itemView) {
            tv_lab = itemView.findViewById(R.id.tv_lab);
            radio1 = itemView.findViewById(R.id.radio1);
            radio2 = itemView.findViewById(R.id.radio2);
            tv_usetip = itemView.findViewById(R.id.tv_usetip);
        }
    }
}