package com.gxl.searchword;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by gxl on 2016/1/15.
 * Description: Create Service to play Media avoid ANR
 */
public class BBCMediaPlayerService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
