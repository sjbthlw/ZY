package com.hzsun.mpos.Public;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hzsun.mpos.R;

import java.util.ArrayList;
import java.util.List;

public class SelectPopWindow {

    public static List<Object> showSelectPopWindow(final Activity activity, View view, String selectInfo) {
        final WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.6f;//设置阴影透明度
        activity.getWindow().setAttributes(lp);
        View contentView = LayoutInflater.from(activity).inflate(R.layout.pop_selectlayout, null);

        final PopupWindow popupWindow = new PopupWindow(contentView, 576, 336);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, -150);

        Button bt_cancle = ((Button) contentView.findViewById(R.id.bt_cancle));
        Button bt_confirm = ((Button) contentView.findViewById(R.id.bt_confirm));
        TextView tv_SelectInfo = ((TextView) contentView.findViewById(R.id.tv_selectInfo));
        tv_SelectInfo.setText(selectInfo);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lp.alpha = 1f;//设置阴影透明度
                activity.getWindow().setAttributes(lp);
            }
        });
        bt_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        List<Object> viewList = new ArrayList<>();
        viewList.add(popupWindow);
        viewList.add(bt_confirm);
        return viewList;
    }
}

