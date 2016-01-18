package com.gxl.mediaplayer;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by gxl on 2016/1/18.
 * Description: Toast to show message
 */
public class SToast {

    private static Toast mToast = null;
    public static void show(Context context, String str) {
        if (context == null) {
            return;
        }
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            if(mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

}
