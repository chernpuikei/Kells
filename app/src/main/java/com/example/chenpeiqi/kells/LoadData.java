package com.example.chenpeiqi.kells;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.example.chenpeiqi.kells.Tool.ii;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LoadData extends Service {

    private Bundle everything;
    private boolean replied;

    @Override
    public IBinder onBind(Intent intent) {
        ii("#LoadData# onBind()");
        requestCanvas();
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ii("service being destroyed");
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {
        return super.onStartCommand(intent,flags,startId);
    }

    float[] getFA(String key) {
        return everything.getFloatArray(key);
    }

    Bundle getEverything() {
        return everything;
    }

    boolean repliedFromServer() {
        return replied;
    }

    void requestCanvas() {
        ii("#LoadData# requestCanvas>>");
        try {
            ExecutorService es = Executors.newSingleThreadExecutor();
            Future<Bundle> preReply = es.submit(new DataLoader(this));
            replied = false;//发送请求后要重置标记为false
            everything = preReply.get();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    void checkOnESMachanicsm() {
        ExecutorService es = Executors.newSingleThreadExecutor();
    }

    class MyBinder extends Binder {

        MyBinder() {
            ii("#LoadData# MyBinder()");
        }

        //todo:iBinder只是提供返回service的接口,无须添加其他方法
        LoadData getService() {
            ii("#LoadData# MyBinder.getService()");
            return LoadData.this;
        }
    }
}
