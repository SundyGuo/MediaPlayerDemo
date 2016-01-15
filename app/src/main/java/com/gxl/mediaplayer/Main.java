package com.gxl.mediaplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class Main extends FragmentActivity {

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment mediaPlayerFragment = new MediaPlayerFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, mediaPlayerFragment)
                .commit();
    }
}
