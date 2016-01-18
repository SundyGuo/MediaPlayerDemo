package com.gxl.mediaplayer;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by gxl on 2016/1/18.
 * Description: Base Fragment
 */
public class BaseFragment extends Fragment{

    protected Context mContext;

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        super.onAttach(activity);
    }
}
