package com.example.chenpeiqi.kells;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.chenpeiqi.kells.Tool.ii;

public class MainActivity extends AppCompatActivity {

    private LoadData loadData;
    private boolean serviceConnected;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,IBinder iBinder) {
            ii("#MainActivity# onServiceConnected");
            LoadData.MyBinder check = (LoadData.MyBinder) iBinder;
            loadData = check.getService();
            serviceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("start","******************************");
        clearDB(false);
        bindService(new Intent(this,LoadData.class),serviceConnection,BIND_AUTO_CREATE);
        startACTWhenReady();
    }

    private void startACTWhenReady(){
        ii("#MainActivity# startACTWhenReady>>");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!serviceConnected){
                    try{
                        ii("#MainActivity# 2.5.service not connected");
                        Thread.sleep(100);
                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
                ii("#MainActivity# 3.service connected,about to start Kells");                startActivity(new Intent(MainActivity.this,Kells.class));
            }
        }).start();
    }

    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0;i<publicKey.length;i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length()==1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0,result.length()-1);
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearDB(boolean toTeOrNot) {
        if (toTeOrNot) {
            ExecutorService es = Executors.newSingleThreadExecutor();
            Bundle bundle = new Bundle();
            bundle.putString("requestType","clearDB");
            es.submit(new CMT(bundle));
        }
    }

}
