package com.hzsun.mpos.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.hzsun.mpos.R;

public class HorizontalListViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;

    public HorizontalListViewAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 4;
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
            convertView = inflater.inflate(R.layout.horizontal_menu_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            viewHolder.relativeLayout.setBackgroundResource(R.drawable.s_elect);
        } else if (position == 1) {
            viewHolder.relativeLayout.setBackgroundResource(R.drawable.s_set);
        } else if (position == 2) {
            viewHolder.relativeLayout.setBackgroundResource(R.drawable.s_chongzheng);
        } else if (position == 3) {
            viewHolder.relativeLayout.setBackgroundResource(R.drawable.s_senior);
        }

        return convertView;
    }

    class ViewHolder {

        private RelativeLayout relativeLayout;

        ViewHolder(View view) {
            relativeLayout = ((RelativeLayout) view.findViewById(R.id.relative_bg));

        }
    }
}
