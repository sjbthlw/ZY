package com.hzsun.mpos.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzsun.mpos.R;

import java.util.ArrayList;


public class CheckMenuAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private Context context;
    private ArrayList<String> infoList;

    public CheckMenuAdapter(Context context, ArrayList<String> infoList) {
        this.context = context;
        this.infoList = infoList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return infoList.size();
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
            convertView = inflater.inflate(R.layout.list_system_parameter_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvValue.setText(infoList.get(position));

        if ((position % 2) == 0) {
            viewHolder.relativeBg.setBackgroundResource(R.drawable.item_deep_bg);
        } else {
            viewHolder.relativeBg.setBackgroundResource(R.drawable.item_light_bg);
        }
        if (position == 0) {
            viewHolder.relativeBg.setBackgroundResource(R.drawable.item_deep_up_bg);
        } else if (position == infoList.size() - 1 && (position % 2) == 0) {
            viewHolder.relativeBg.setBackgroundResource(R.drawable.item_deep_down_bg);
        } else if (position == infoList.size() - 1 && (position % 2) != 0) {
            viewHolder.relativeBg.setBackgroundResource(R.drawable.item_light_down_bg);
        }
        return convertView;
    }

    class ViewHolder {
        private TextView tvValue;
        private RelativeLayout relativeBg;

        ViewHolder(View view) {
            tvValue = ((TextView) view.findViewById(R.id.tv_value));
            relativeBg = ((RelativeLayout) view.findViewById(R.id.relative_bg));
        }
    }
}
