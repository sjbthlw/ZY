package com.hzsun.mpos.progressutils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hzsun.mpos.R;

public class ProgressManage {

    private final TextView tvNotice;
    private final CustomCircleProgressBar progressBar;
    private final PopupWindow popupWindow;
    private Context context;
    private View view;

    public ProgressManage(Context context, View view, final Window window) {
        this.context = context;
        this.view = view;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.7f;//设置阴影透明度
        window.setAttributes(lp);
        View contentView = LayoutInflater.from(context).inflate(R.layout.progress_layout, null);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvNotice = ((TextView) contentView.findViewById(R.id.tv_notice));
        progressBar = ((CustomCircleProgressBar) contentView.findViewById(R.id.progress));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.alpha = 1f;//设置阴影透明度
                window.setAttributes(lp);
            }
        });
    }

    public void setMessage(String str) {
        tvNotice.setText(str);
    }

    public void show() {
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    public void dismiss() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }


    /**
     * dp转px
     */
    public static int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
