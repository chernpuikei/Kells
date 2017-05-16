package com.example.chenpeiqi.kells;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import static com.example.chenpeiqi.kells.Tool.sample;

/**
 * Created on 2016/12/15.
 */

public class IntroFragment extends Fragment {

    private static final String tag = "IntroFragment";

    @Nullable
    public View onCreateView(LayoutInflater li,ViewGroup ct,Bundle bd) {
        Log.i(tag,"fragment.onCreateView");
        int pid = getArguments().getInt("pid");
        Bitmap bitmap = sample(getResources(),pid,1080,1920);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        return imageView;
    }

}