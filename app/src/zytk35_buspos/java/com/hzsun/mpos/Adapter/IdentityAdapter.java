package com.hzsun.mpos.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hzsun.mpos.R;
import com.hzsun.mpos.data.IdentityInfo;

import java.util.ArrayList;
import java.util.List;

public class IdentityAdapter extends BaseAdapter {

    private final int MAX_COUNT = 128;
    private String curIDType;   //当前选中的身份类型

    private List<IdentityInfo> list = new ArrayList<>();

    public IdentityAdapter() {
        for (int i = 0; i < MAX_COUNT; i++) {
            IdentityInfo identityInfo = new IdentityInfo();
            identityInfo.setValue(String.valueOf(i));
            identityInfo.setSelected(false);
            list.add(identityInfo);
        }
    }

    @Override
    public int getCount() {
        return MAX_COUNT;
    }

    @Override
    public IdentityInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<IdentityInfo> getList() {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public void setCurIDType(String curIDType) {
        this.curIDType = curIDType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_identity, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        IdentityInfo identityInfo = list.get(position);
        holder.tv_identity.setText(String.valueOf(identityInfo.getValue()));
        holder.tv_identity.setSelected(identityInfo.isSelected());
        if (identityInfo.isSelected()) {
            if (identityInfo.getIdtype().equals(curIDType)) {
                holder.fl_redbox.setVisibility(View.VISIBLE);
            } else {
                holder.fl_redbox.setVisibility(View.GONE);
            }
        } else {
            holder.fl_redbox.setVisibility(View.GONE);
        }
        return convertView;
    }

    class ViewHolder {
        TextView tv_identity;
        FrameLayout fl_redbox;

        public ViewHolder(View itemView) {
            tv_identity = itemView.findViewById(R.id.tv_identity);
            fl_redbox = itemView.findViewById(R.id.fl_redbox);
        }
    }
}
