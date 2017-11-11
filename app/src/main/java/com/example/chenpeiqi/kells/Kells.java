package com.example.chenpeiqi.kells;

import android.content.*;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import static com.example.chenpeiqi.kells.Tool.ii;

public class Kells extends FragmentActivity {

    private static final int SWITCH_DIARY = 2;
    private static final int SHOW_MEOW = 1;

    private SurfaceHolder holder;
    static MyHandler myHandler;
    private Bundle canvasBundle;
    private LoadData loadData;
    private boolean serviceConnected;
    private ServiceConnection sc = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,IBinder iBinder) {
            ii("#Kells# onServiceConnected>>");
            loadData = ((LoadData.MyBinder) iBinder).getService();
            //todo:init canvas data necessary
            canvasBundle = loadData.getEverything();
            serviceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        myHandler = new MyHandler(new WeakReference<>(this));
        setContentView(new Pangur(this));
        bindService(new Intent(this,LoadData.class),sc,BIND_AUTO_CREATE);
        ii("#Kells# service bind");
    }

    static class MyHandler extends Handler {

        WeakReference<Kells> ref;

        MyHandler(WeakReference<Kells> ref) {
            super();
            this.ref = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SWITCH_DIARY:
                    Intent intent = new Intent(ref.get(),Diary.class);
                    this.ref.get().startActivityForResult(intent,0);
                    break;
                case SHOW_MEOW:
                    Toast.makeText(ref.get(),"Meow",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int rsc,Intent data) {
        super.onActivityResult(requestCode,rsc,data);
        switch (rsc) {
            case 0:
                //同步服务器的过程放在diary中，这里只需要将所打content现实在屏幕上
                Bundle bundle = new Bundle();
                bundle.putString("operation","FP");
//            bundle.putString("content",data.getStringExtra("content"));
                new Thread(new KittenCanvas(Kells.this,holder,bundle)).start();
                break;
        }
    }

    class Pangur extends SurfaceView
            implements SurfaceHolder.Callback, View.OnTouchListener {

        private Context context;
        GestureDetector gestureDetector;

        Pangur(Context context) {
            super(context);
            this.context = context;
            getHolder().addCallback(this);
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            SP.getSP(context).edit().remove("si").putInt("si",0).apply();
            gestureDetector = new GestureDetector(context,new MyGestureDetector());
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //todo:init width/height,then draw
            ii("#Kells# surfaceCreated>>");
            //todo:if service not serviceConnected binding,pause until so
            try {
                while (!serviceConnected) {
                    ii("about to sleep");
                    Thread.sleep(100);
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            ii("#Kells# finally,stop sleeping");
            Canvas canvas = holder.lockCanvas();
            SharedPreferences sp = SP.getSP(context);
            int cw = canvas.getWidth(), ch = canvas.getHeight();
            sp.edit().putInt("width",cw).putInt("height",ch).apply();
            holder.unlockCanvasAndPost(canvas);
            ii("canvasBundle?",canvasBundle);
            new Thread(new KittenCanvas(context,holder,canvasBundle)).start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder hd,int format,int wid,int hei) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {}

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public boolean onTouch(View v,MotionEvent event) {
//        float touchX = event.getX(), touchY = event.getY();
//        int length = posTan.length/4; double a = 37.5;
//        for (int i = 0;i<length;i++) {
//            if (touchX>posTan[i*4]-a && touchX<posTan[i*4]+a &&
//                    touchY>posTan[i*4+1]-a && touchY<posTan[i*4+1]+a
//                    && event.getAction()==MotionEvent.ACTION_UP) {
//                Log.i(tag,"##posTan being Touch");
//                requestContent(i);
//            }
//        }
            return false;
        }

        class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onFling(MotionEvent e1,MotionEvent e2,float vX,float vY) {
                float e1x = e1.getX(), e2x = e2.getX(), e1y = e1.getY(), e2y = e2.getY();
                float horDis = e2x-e1x, verDis = e2y-e1y;
                boolean hov = horDis>verDis;
                float disToUse = hov? horDis: verDis;
                boolean act = Math.abs(disToUse)>150, pon = disToUse>0;
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putBoolean("act",act);
                if (act) bundle.putString("dir",hov? pon? "l": "r": pon? "t": "p");
                message.setData(bundle);
                return super.onFling(e1,e2,vX,vY);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);
            }

        }

//    void invalidateSP(String gesDir) {
//        SharedPreferences sp = SP.getSP(context);
//        int si = sp.getInt("si",0);
//        if (gesDir.equals(dirs[0])) {
//            si--;
//        } else if (gesDir.equals(dirs[1])) {
//            si++;
//        }
//        sp.edit().remove("si").putInt("si",si).apply();
//        Toast.makeText(context,"current si:"+si,Toast.LENGTH_SHORT).show();
//    }
    }

}
