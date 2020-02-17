package com.hzsun.mpos.Adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hzsun.mpos.R;

import java.util.List;

public class WifiListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private List<ScanResult> scanResults;
    private Context context;
    private WifiManager wifiManager;
    private int resId[] = {R.mipmap.wifi_level_1, R.mipmap.wifi_level_2,
            R.mipmap.wifi_level_3, R.mipmap.wifi_level_4, R.mipmap.wifi_level_5};

    public WifiListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void addAll(WifiManager wifiManager, List<ScanResult> scanResults) {
        this.wifiManager = wifiManager;
        this.scanResults = scanResults;
        notifyDataSetChanged();
    }

    public void clear() {
        if (scanResults != null) {
            scanResults.clear();
        }
    }

    @Override
    public int getCount() {

        return scanResults != null ? scanResults.size() : 0;
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
            convertView = inflater.inflate(R.layout.wifi_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvWifiNum.setText(position + 1 + "");
        viewHolder.tvWifiName.setText(scanResults.get(position).SSID);

        if ((position % 2) == 0) {
            viewHolder.relativeBg.setBackgroundColor(context.getResources().getColor(R.color.color_blue_pale_gray));
        } else {
            viewHolder.relativeBg.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
        }
        if (position == 0) {
            viewHolder.relativeBg.setBackgroundResource(R.drawable.system_parameter_up);
        } else if (position == scanResults.size()) {
            viewHolder.relativeBg.setBackgroundResource(R.drawable.system_parameter_down_gary);
        }
        if (wifiManager != null) {
            int level = wifiManager.calculateSignalLevel(scanResults.get(position).level, 5);
            viewHolder.iv_wifiLevel.setImageResource(resId[level]);
        }
        return convertView;
    }

    class ViewHolder {

        private RelativeLayout relativeBg;
        private TextView tvWifiNum, tvWifiName;
        private ImageView iv_wifiLevel;

        ViewHolder(View view) {
            relativeBg = ((RelativeLayout) view.findViewById(R.id.relative_bg));
            tvWifiNum = ((TextView) view.findViewById(R.id.tv_wifiNum));
            tvWifiName = ((TextView) view.findViewById(R.id.tv_wifiName));
            iv_wifiLevel = (ImageView) view.findViewById(R.id.iv_wifiLevel);
        }
    }
}
