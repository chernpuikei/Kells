package com.example.chenpeiqi.kells;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.example.chenpeiqi.kells.Tool.array;

public class Kells extends FragmentActivity {

    private static final int mp = Context.MODE_PRIVATE;
    private static final String tag = "Kells";
    private static final int SWITCH_DIARY = 10086;
    private static final int SHOW_MEOW = 10087;
    private SurfaceHolder holder;
    private static MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearDB(true);
        Log.i("start","*************\n*************\n*************");
        myHandler = new MyHandler(new WeakReference<>(this));
        setContentView(new Pangur(this));
    }

    private static class MyHandler extends Handler {

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
            new Thread(new Draw(Kells.this,holder,bundle)).start();
            break;
        }
    }

    class Pangur extends SurfaceView
            implements SurfaceHolder.Callback, View.OnTouchListener {

        private Context context;
        private Bitmap bitmap;
        private float[] posTan, textArea, la, path, ends = new float[2],pt,
                areaZero,areaOne,ori,cps;
        private String[] dirs;
        private int[] date, areaBelong, es,cpArea;
        private boolean lor,timeZone,tob;
        int year,month,verCount;
        private GestureDetector gestureDetector;

        Pangur(Context context) {
            super(context);
            this.context = context;
            getHolder().addCallback(this);
            this.setOnTouchListener(this);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getSharedPreferences("status",mp)
                    .edit().remove("si").putInt("si",0).apply();
            MyGestureListener listener = new MyGestureListener();
            gestureDetector = new GestureDetector(context,listener);

        }

        class MyGestureListener extends SimpleOnGestureListener {

            @Override
            public boolean onFling(MotionEvent e1,MotionEvent e2,float vX,float vY) {
                Log.i(tag,"—————————————————onFling—————————————————");
                float e1x = e1.getX(), e2x = e2.getX(),
                        e1y = e1.getY(), e2y = e2.getY();
                String gesDir = e2x-e1x<-150? "right": e2x-e1x>150? "left":
                        e2y-e1y>150? "up": e2y-e1y<-150? "down": "nah";
                Log.i(tag,"gesDir/(inDir|outDir):"+gesDir+"/"+dirs[0]+"|"+dirs[1]);
                try {
                    invalidateSP(gesDir);
                    loadCurrentCanvas();
                    Bundle bundle = new Bundle();
                    bundle.putFloatArray("posTan",posTan);
                    bundle.putFloatArray("path",path);
                    bundle.putFloatArray("last",new float[]{path[4],path[5]});
                    bundle.putString("operation","FP");
                    SharedPreferences sp = SP.getSP(context);
                    requestContent(-1);
//                    new Thread(new Draw(Kells.this,holder,bundle)).start();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return super.onFling(e1,e2,vX,vY);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.i(tag,"onDoubleTaped>");
//            Bundle bundle = new Bundle();
//            bundle.putString("operation","DT");
//            bundle.putFloatArray("posTan",posTan);
//            bundle.putIntArray("date",date);
//            new Thread(new Draw(context,holder,bundle)).start();
                return super.onDoubleTap(e);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                Kells.this.holder = holder;
                //第一个checkWidthHeight的意思是是否需要查询
                //第二个代表查询结果是否相符
                if (getIntent().getBooleanExtra("checkWidthHeight",false)
                        || checkWidthHeight(holder)) {
                    loadCurrentCanvas();
                    Bundle firstCanvas = new Bundle();
                    firstCanvas.putString("operation","FP");
                    firstCanvas.putFloatArray("posTan",posTan);
                    firstCanvas.putFloatArray("path",path);
                    new Thread(new Draw(context,holder,firstCanvas)).start();

                } else {
                    Toast.makeText(context,"ur in trouble",Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        private void requestContent(int pi) {
            try {
                Bundle bundle = new Bundle();
                SharedPreferences sp = SP.getSP(context);
                int si = sp.getInt("si",0);
                bundle.putString("email",sp.getString("email","aaa"));
                bundle.putInt("si",sp.getInt("si",0));
                bundle.putInt("pi",pi);
                bundle.putString("requestType","requestContent");
                ExecutorService es = Executors.newSingleThreadExecutor();
                Bundle drawCon = es.submit(new DataLoader(context,bundle)).get();
                drawCon.putFloatArray("posTan",posTan);
                drawCon.putFloatArray("path",path);
                drawCon.putString("operation","TX");
                drawCon.putBoolean("lor",lor);
                drawCon.putIntArray("areaBelong",areaBelong);
                drawCon.putFloatArray("pt",pt);
                drawCon.putInt("year",year);drawCon.putInt("month",month);
                drawCon.putBoolean("timeZone",timeZone);
                drawCon.putFloatArray("ta",textArea);
                drawCon.putIntArray("es",this.es);
                drawCon.putInt("verCount",verCount);
                drawCon.putIntArray("cpArea",cpArea);
                drawCon.putFloatArray("areaZero",areaZero);
                drawCon.putFloatArray("areaOne",areaOne);
                drawCon.putFloatArray("ori",ori);
                drawCon.putFloatArray("cps",cps);
                drawCon.putBoolean("tob",tob);
                es.shutdown();
                if (drawCon.getBoolean("result")) {
                    new Thread(new Draw(Kells.this,holder,drawCon)).start();
                } else {
                    Intent intent = new Intent(Kells.this,Diary.class);
                    intent.putExtra("si",si); intent.putExtra("pi",0);
                    startActivityForResult(intent,0);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        private boolean checkWidthHeight(SurfaceHolder holder) throws Exception {
            //get width height
            Canvas canvas = holder.lockCanvas();
            int width = canvas.getWidth(), height = canvas.getHeight();
            holder.unlockCanvasAndPost(canvas);
            //write width height
            SharedPreferences sp = context.getSharedPreferences("status",mp);
            sp.edit().putInt("width",width).putInt("height",height).apply();
            //create bundle
            Bundle bundle = new Bundle();
            bundle.putString("email",sp.getString("email","aaa"));
            bundle.putString("requestType","checkWidthHeight");
            bundle.putInt("si",sp.getInt("si",0));
            bundle.putInt("width",width);
            bundle.putInt("height",height);
            //pullRequestAndPushResult
            ExecutorService es = Executors.newSingleThreadExecutor();
            Bundle returnedBundle = es.submit(new DataLoader(context,bundle)).get();
            return returnedBundle.getBoolean("result");

        }

        @Override
        public void surfaceChanged(SurfaceHolder hd,int format,int wid,int hei) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {}

        @Override
        public boolean onTouch(View v,MotionEvent event) {
            Log.i(tag,"motionEvent:"+event);
            Log.i(tag,"#onTouch>");
            float touchX = event.getX(), touchY = event.getY();
            int length = posTan.length/4;
            double a = 37.5;
            for (int i = 0;i<length;i++) {
                if (touchX>posTan[i*4]-a && touchX<posTan[i*4]+a &&
                        touchY>posTan[i*4+1]-a && touchY<posTan[i*4+1]+a
                        && event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(tag,"##posTan being Touch");
                    requestContent(i);
                }
            }
            return false;
        }

        void invalidateSP(String gesDir) {
            SharedPreferences sp = getSharedPreferences("status",mp);
            int si = sp.getInt("si",0);
            if (gesDir.equals(dirs[0])) {
                si--;
            } else if (gesDir.equals(dirs[1])) {
                si++;
            }
            sp.edit().remove("si").putInt("si",si).apply();
            Toast.makeText(context,"current si:"+si,Toast.LENGTH_SHORT).show();
        }

        //刷新用于定义当前canvas的若干变量
        private void loadCurrentCanvas() throws Exception {
            SharedPreferences sp = context.getSharedPreferences("status",mp);
            String email = sp.getString("email","aaa");
            int si = sp.getInt("si",0), width = sp.getInt("width",0),
                    height = sp.getInt("height",0);
            ExecutorService es = Executors.newSingleThreadExecutor();
            Bundle toDataLoader = new Bundle();
            toDataLoader.putString("requestType","requestCanvas");
            toDataLoader.putString("email",email);
            toDataLoader.putInt("si",si);
            toDataLoader.putFloatArray("ends",ends);//无法初始化意味着从(0,0)开始
            toDataLoader.putInt("width",width);
            toDataLoader.putInt("height",height);
            Future<Bundle> future = es.submit(new DataLoader(context,toDataLoader));
            es.shutdown();
            Bundle currentCanvas = future.get();
            Log.i(tag,"current canvas load(ed):"+currentCanvas);
            this.dirs = currentCanvas.getStringArray("dirs");
            this.posTan = currentCanvas.getFloatArray("posTan");
            this.textArea = currentCanvas.getFloatArray("ta");
            this.la = currentCanvas.getFloatArray("la");
            this.path = currentCanvas.getFloatArray("path");
            this.ends = new float[]{path[4],path[5]};
            this.date = currentCanvas.getIntArray("date");
            this.areaBelong = currentCanvas.getIntArray("areaBelong");
            this.lor = currentCanvas.getBoolean("lor");
            this.es = currentCanvas.getIntArray("es");
            this.pt = currentCanvas.getFloatArray("pt");
            this.year = currentCanvas.getInt("year");
            this.month = currentCanvas.getInt("month");
            this.verCount = currentCanvas.getInt("verCount");
            this.timeZone = currentCanvas.getBoolean("timeZone");
            this.cpArea = currentCanvas.getIntArray("cpArea");
            this.areaZero = currentCanvas.getFloatArray("areaZero");
            this.areaOne = currentCanvas.getFloatArray("areaOne");
            this.ori = currentCanvas.getFloatArray("ori");
            this.cps = currentCanvas.getFloatArray("cps");
            this.tob = currentCanvas.getBoolean("tob");
            array("current path",path,5);
        }
    }

    private void clearDB(boolean toTeOrNot) {
        if (toTeOrNot){
            ExecutorService es = Executors.newSingleThreadExecutor();
            Bundle bundle = new Bundle();
            bundle.putString("requestType","clearDB");
            es.submit(new CMT(bundle));
        }
    }
}
