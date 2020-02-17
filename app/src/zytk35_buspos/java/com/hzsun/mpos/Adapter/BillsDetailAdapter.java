package com.hzsun.mpos.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzsun.mpos.R;
import com.hzsun.mpos.data.BillsInfo;

import java.util.List;


public class BillsDetailAdapter extends BaseAdapter {

    private List<BillsInfo.TableItem> list;

    public BillsDetailAdapter(List<BillsInfo.TableItem> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list == null ? null : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_bills_detail_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //解析交易流水字段
        BillsInfo.TableItem item = list.get(position);
        viewHolder.tvOrderNum.setText(String.valueOf(position + 1));//序号
        String cShowTemp = String.format("%d.%02d", (-item.getAmount()) / 100, (-item.getAmount()) % 100);
        viewHolder.tvMoney.setText(cShowTemp);
        viewHolder.tvName.setText(item.getName());//姓名
        viewHolder.tvRecordtype.setText(getTradeType(item.getType()));//交易类型
        viewHolder.tvBusinessTime.setText(item.getTime().substring(5));//交易时间

        if (position % 2 == 0) {
            viewHolder.linearItem.setBackgroundColor(parent.getContext().getResources().getColor(R.color.colorWhite));
        } else {
            viewHolder.linearItem.setBackgroundColor(parent.getContext().getResources().getColor(R.color.color_blue_pale_gray));
        }
        return convertView;
    }

    private String getTradeType(int type) {
        String tradeType;
        switch (type) {
            case 0:
                tradeType = "所有类型";
                break;
            case 1:
                tradeType = "平台卡";
                break;
            case 2:
                tradeType = "虚拟卡+微信+支付宝";
                break;
            case 3:
                tradeType = "虚拟卡";
                break;
            case 4:
                tradeType = "支付宝";
                break;
            case 5:
                tradeType = "微信";
                break;
            default:
                tradeType = "未知";
                break;
        }
        return tradeType;
    }

    static class ViewHolder {
        private LinearLayout linearItem;
        private TextView tvOrderNum;
        private TextView tvMoney;
        private TextView tvName;
        private TextView tvRecordtype;
        private TextView tvBusinessTime;

        public ViewHolder(View itemView) {
            linearItem = itemView.findViewById(R.id.linear_item);
            tvOrderNum = itemView.findViewById(R.id.tv_orderNum);//序号
            tvMoney = itemView.findViewById(R.id.tv_money);//交易金额
            tvName = itemView.findViewById(R.id.tv_name);//姓名
            tvRecordtype = itemView.findViewById(R.id.tv_Recordtype);//交易类型
            tvBusinessTime = itemView.findViewById(R.id.tv_businessTime);//交易时间
        }
    }
}
