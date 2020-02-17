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


public class MenuListviewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> infoList;
    private LayoutInflater inflater;

    public MenuListviewAdapter(Context context, ArrayList<String> infoList) {
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
            convertView = inflater.inflate(R.layout.menu_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.itemText.setText(infoList.get(position));
        viewHolder.num.setText(position + 1 + "");
        return convertView;
    }

    class ViewHolder {
        private TextView itemText, num;
        private RelativeLayout itemBg;

        ViewHolder(View view) {
            itemText = ((TextView) view.findViewById(R.id.item_text));
            num = ((TextView) view.findViewById(R.id.num));
            itemBg = ((RelativeLayout) view.findViewById(R.id.item_bg));
        }
    }
}
