package com.gxl.mediaplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

/**
 * Created by gxl on 2016/1/14.
 * Description: 重写SurfaceView
 */
public class KSurfaceView extends SurfaceView {

    public KSurfaceView(Context context) {
        super(context);
    }

    public KSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /**
     * 清空SurfaceView当前显示内容
     */
    public void clearSurfaceView(){
        Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        } catch (Exception e) {
            Log.e("KSurfaceView","Clear SurfaceVie err",e);
        } finally {
            if(canvas != null) {
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }
}
