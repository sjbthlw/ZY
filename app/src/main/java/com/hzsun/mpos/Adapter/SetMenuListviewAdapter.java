package com.hzsun.mpos.Adapter;

import android.content.Context;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzsun.mpos.R;

import java.util.ArrayList;


public class SetMenuListviewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> infoList;
    private LayoutInflater inflater;
    private int currentItem = -1;

    public SetMenuListviewAdapter(Context context, ArrayList<String> infoList) {
        this.context = context;
        this.infoList = infoList;
        inflater = LayoutInflater.from(context);
    }


    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
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
            convertView = inflater.inflate(R.layout.set_menu_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.itemText.setText(infoList.get(position));
        TextPaint tpText = viewHolder.itemText.getPaint();
        if (currentItem == position) {  //被选中
            tpText.setFakeBoldText(true); //加粗
            viewHolder.itemText.setTextColor(context.getResources().getColor(R.color.colorBlue));
            viewHolder.itemBg.setBackgroundResource(R.mipmap.s_title_checked);
        } else {  //未被选中
            tpText.setFakeBoldText(false);  //取消加粗
            viewHolder.itemText.setTextColor(context.getResources().getColor(R.color.colorWhite));
            viewHolder.itemBg.setBackgroundResource(R.mipmap.s_title_uncheck);
        }
        return convertView;
    }

    class ViewHolder {
        private TextView itemText;
        private RelativeLayout itemBg;

        ViewHolder(View view) {
            itemText = ((TextView) view.findViewById(R.id.item_text));
            itemBg = ((RelativeLayout) view.findViewById(R.id.item_bg));
        }
    }
}
