package com.example.chenpeiqi.kells;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import static com.example.chenpeiqi.kells.Tool.ii;

/**
 * Created on 2016/12/15.
 */
public class Intro extends FragmentActivity {

    private static final String tag = "intro";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ii("Intro created>>");
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        ViewPager intro_pager = (ViewPager) findViewById(R.id.intro_pager);
        intro_pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        intro_pager.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private class MyAdapter extends FragmentPagerAdapter {

        int[] ids = {R.drawable.g0,R.drawable.g1,R.drawable.g2};

        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.i(tag,"getItem/position:"+position);
            IntroFragment introFragment = new IntroFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("pid",ids[position]);
            introFragment.setArguments(bundle);
            return introFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}
