package com.hzsun.mpos.Public;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzsun.mpos.R;


/**
 * Created by dell on 2018/1/16.
 */

public class ToastUtils {

    public static final int FAIL = 0;
    public static final int SUCCESS = 1;
    public static final int WARN = 2;
    public static final int CENTER = 3;
    public static final int BOTTOM = 4;

    private static Toast mToast;

    public static void showText(Context context, String msg, int state, int locationId, int duration) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //自定义布局
        View view = inflater.inflate(R.layout.toast_layout, null);
        //自定义toast文本
        TextView mTextView = (TextView) view.findViewById(R.id.toast_msg);
        mTextView.setText(msg);
        ImageView mImageView = (ImageView) view.findViewById(R.id.toast_iv);

        switch (state) {
            case FAIL:
                mImageView.setImageResource(R.mipmap.s_toast_fail);
                mTextView.setTextColor(context.getResources().getColor(R.color.colorToastFail));
                break;
            case SUCCESS:
                mImageView.setImageResource(R.mipmap.s_toast_success);
                mTextView.setTextColor(context.getResources().getColor(R.color.colorToastSuccess));
                break;
            case WARN:
                mImageView.setImageResource(R.mipmap.s_toast_warn);
                mTextView.setTextColor(context.getResources().getColor(R.color.colorToastWarn));
                break;
        }
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, duration);
        }
        if (locationId == CENTER) {
            //设置toast居中显示
            mToast.setGravity(Gravity.CENTER, 0, 0);
        } else if (locationId == BOTTOM) {
            mToast.setGravity(Gravity.BOTTOM, 0, 100);
        }
        mToast.setView(view);
        if (mToast != null) {
            mToast.show();
        }
    }

}
