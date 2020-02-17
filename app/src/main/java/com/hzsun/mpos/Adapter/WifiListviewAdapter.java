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

import java.util.List;


public class WifiListviewAdapter extends BaseAdapter {

    private Context context;
    private List<String> stringList;
    private LayoutInflater inflater;
    private int currentItem = -1;

    public WifiListviewAdapter(Context context, List<String> stringList) {
        this.context = context;
        this.stringList = stringList;
        inflater = LayoutInflater.from(context);
    }

    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }

    @Override
    public int getCount() {
        return stringList.size();
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
        viewHolder.num.setText(position + 1 + "");
        viewHolder.itemText.setText(stringList.get(position));
        TextPaint tpText = viewHolder.itemText.getPaint();
        RelativeLayout.LayoutParams lpText = new RelativeLayout.LayoutParams(viewHolder.itemText.getLayoutParams());
        RelativeLayout.LayoutParams lpNum = new RelativeLayout.LayoutParams(viewHolder.num.getLayoutParams());
        if (currentItem == position) {  //被选中
            tpText.setFakeBoldText(true); //加粗
            viewHolder.itemText.setTextColor(context.getResources().getColor(R.color.colorBlue));
            viewHolder.num.setTextColor(context.getResources().getColor(R.color.colorBlue));
            lpText.setMargins(128, 20, 0, 0);
            lpNum.setMargins(45, 20, 0, 0);
        } else {  //未被选中
            tpText.setFakeBoldText(false);  //取消加粗
            viewHolder.itemText.setTextColor(context.getResources().getColor(R.color.colorWhite));
            viewHolder.num.setTextColor(context.getResources().getColor(R.color.colorWhite));
            lpText.setMargins(150, 20, 0, 0);
            lpNum.setMargins(67, 20, 0, 0);
        }
        viewHolder.itemText.setLayoutParams(lpText);
        viewHolder.num.setLayoutParams(lpNum);

        return convertView;
    }


    static class ViewHolder {
        private TextView num;
        private TextView itemText;
        private RelativeLayout itemBg;

        ViewHolder(View view) {
            num = ((TextView) view.findViewById(R.id.num));
            itemText = ((TextView) view.findViewById(R.id.item_text));
            itemBg = ((RelativeLayout) view.findViewById(R.id.item_bg));
        }
    }

}
