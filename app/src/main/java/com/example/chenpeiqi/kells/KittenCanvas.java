package com.example.chenpeiqi.kells;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Bundle;
import android.view.SurfaceHolder;

import static com.example.chenpeiqi.kells.Tool.*;

/**
 * Created on 16/9/8.
 */
class KittenCanvas implements Runnable {

    private SurfaceHolder holder;
    private int width, height;
    private Bitmap footprint;
    private static final int verCount = 30;
    private Paint paint = new Paint(), textPaint = new Paint();
    private float[] posTan, path, ta;
    private String[] content;

    //Todo：将drawCanvas过程所需数据加载到成员变量data中
    KittenCanvas(Context context,SurfaceHolder holder,Bundle canvasBundle) {
        this.holder = holder;
        //Todo:initialize width & height
        SharedPreferences sp = SP.getSP(context);
        width = sp.getInt("width",0); height = sp.getInt("height",0);
        footprint = sample(context.getResources(),R.drawable.fp,20,20);
        paint.setColor(Color.GREEN);
        textPaint.setColor(Color.WHITE); textPaint.setTextSize(50);
        posTan = canvasBundle.getFloatArray("posTan");
        path = canvasBundle.getFloatArray("path");
        ta = canvasBundle.getFloatArray("ta");
        content = splitContent(canvasBundle.getString("content"));
    }

    private String[] splitContent(String content) {
        i("content to split",content);
        //Todo:split content into array with empty space
        String[] symbols = new String[]{",",".",":",";","，","。"};
        for (String symbol : symbols) {
            content = content.replace(symbol,symbol+" #");
        }
        return content.split("#");
    }

    @Override
    public void run() {
        ii("#KittenCanvas# run()>>");
        //Todo:use data from canvasData & draw
        //Todo:load Data from canvasData
        try {
            //Todo:calculate delta every footprint should rotate
            float whole = 0;
            int fpCount = posTan.length/4;
            for (int i = 0;i<fpCount;i++) {
                whole += (float) Math.toDegrees(Math.asin(posTan[3]));
            }
            boolean a = whole>0, b = posTan[1]>height/2;
            float deltaT = (a && b) || ((!a) && (!b))? -90: 90;
            //Todo:animates
            try {
                int animatorLength = (posTan.length/4)*25+75;
                for (int aniPro = 0;aniPro<animatorLength;aniPro += 10) {
                    Canvas canvas = holder.lockCanvas();
//                    drawTextFrame(canvas,content,aniPro);
                    drawFrame(canvas,aniPro,true,deltaT,20);
                    holder.unlockCanvasAndPost(canvas);
                }
            } catch(NullPointerException e) {
                e.printStackTrace();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void drawFrame(Canvas canvas,int fpProcess,boolean animating,float dT,int dP) {
        i("progress:"+fpProcess);
        canvas.drawRGB(33,33,33);
        drawQuadrant(canvas);
        drawVerCount(canvas);
        for (int j = 0;j<posTan.length/4;j++) {
            int temp = fpProcess-25*j; //单个脚印所处的生命周期
            int sinDur = temp>100 || temp<0? 0: temp; //大于100或者小于0都是0
            //ad如果false则alpha一直是100
            int ca2 = animating? sinDur*2>100? 200-sinDur*2: sinDur*2: 100;
            float[] cpt = new float[4];
            System.arraycopy(posTan,j*4,cpt,0,4);
            float x = cpt[0], y = cpt[1], t = cpt[2], g = cpt[3];
            Matrix matrix = new Matrix();
            float a = (float) (Math.toDegrees(Math.acos(t)));
            float degrees = g>0? a+dT: 360-a+dT;
            matrix.setTranslate(x-dP,y-dP);
            matrix.postRotate(degrees,x,y);
            paint.setAlpha(ca2);
            canvas.drawCircle(x,y,10,paint);
            //canvas.drawBitmap(footprint,matrix,paint);
        }
    }

    private void drawTextFrame(Canvas canvas,String[] sentences,int aniPro) {
        //status:showing current cursor of the text
        Bundle status = new Bundle();
        status.putFloat("curX",ta[0]);
        status.putInt("curArea",0);
        status.putBoolean("notExceeded",true);
        for (int i = 0;i<sentences.length && status.getBoolean("notExceeded");i++) {
            int alpha = aniPro>i*25? aniPro>100+i*25? 100: aniPro-(i*25): 0;
            status = drawSentence(canvas,sentences[i],alpha,status);
        }
    }

    private Bundle drawSentence(Canvas canvas,String sentence,int alpha,Bundle status) {
        String[] words = sentence.replace(" "," #").split("#");
        for (int i = 0;i<words.length && status.getBoolean("notExceeded");i++) {
            status = drawWord(canvas,words[i],alpha,status);
        }
        return status;
    }

    private Bundle drawWord(Canvas canvas,String word,int alpha,Bundle status) {
        char[] wic = word.toCharArray();//WordInChar
        int curTA = status.getInt("curTA");
        float areaLen = ta[curTA*4+2]-status.getFloat("curX");
        float[] ms = new float[2];
        if (textPaint.breakText(wic,0,wic.length,areaLen,ms)==wic.length) {
            status = drawAndReload(canvas,word,alpha,status,ms);
        } else {
            //exceeded之后还是会不断重复调用drawWord
            if (status.getBoolean("notExceeded")) {
                status = drawWord(canvas,word,alpha,loadNextArea(status));
            } else {
                return status;
            }
        }
        return status;
    }

    //edit curX only and process the word process
    private Bundle drawAndReload(Canvas canvas,String word,int alpha,Bundle status,float[] ms) {
        textPaint.setAlpha(alpha);
        float curX = status.getFloat("curX"); int curTA = status.getInt("curTA");
        canvas.drawText(word,curX,ta[curTA*4+1],textPaint);
        curX += ms[0]; status.remove("curX"); status.putFloat("curX",curX);
        return status;
    }

    //edi curArea only and process the area process
    private Bundle loadNextArea(Bundle status) {
        int curTA = status.getInt("curTA");
        curTA++;
        if (curTA>=ta.length/4) {
            status.remove("notExceeded"); status.putBoolean("notExceeded",false);
        } else {
            status.remove("curTA"); status.putInt("curTA",curTA);
            status.remove("curX"); status.putFloat("curX",ta[curTA*4]);
        }
        return status;
    }

    private void drawQuadrant(Canvas canvas) {
        Paint paint = new Paint(); paint.setColor(Color.MAGENTA);
        paint.setAlpha(50);
        canvas.drawLine(0,height/2,width,height/2,paint);
        canvas.drawLine(width/2,0,width/2,height,paint);
    }

    private void drawVerCount(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextSize(50);
        paint.setColor(Color.WHITE);
        paint.setAlpha(40);
        for (int i = 0;i<verCount;i++) {
            char[] check = (i+"").toCharArray();
            canvas.drawText(check,0,check.length,0,(i+0.66f)*height/verCount,paint);
            canvas.drawLine(0,height/verCount*i,width,height/verCount*i,paint);
        }
    }

//    private void showDate(SurfaceHolder holder) {
//        int[] current = canvasData.getIntArray("date");
//        float[] posTan = canvasData.getFloatArray("posTan");
//        Paint red = new Paint();
//        red.setColor(Color.RED);
//        Paint paint = new Paint();
//        paint.setColor(Color.WHITE);
//        paint.setTextSize(50);
//        int deltaA = 2;
//        for (int a = 0;a<200 && a>-1;a += deltaA) {
//            if (a==100) {
//                deltaA = -2;
//            }
//            paint.setAlpha(a);
//            int[] date_loop = current;
//            Canvas canvas = holder.lockCanvas();
//            canvas.drawRGB(33,33,33);
//            for (int i = 0;i<posTan.length/4;i++) {
//                String date = ""+date_loop[0]+date_loop[1]+date_loop[2];
//                canvas.drawText(date,posTan[4*i]-100,posTan[4*i+1],paint);
////            canvas.drawRect(posTan[4*b]-25,posTan[4*b+1]-25,
////                    posTan[4*b]+25,posTan[4*b+1]+25,red);
//                date_loop = getProperDate(date_loop[0],date_loop[1],date_loop[2]);
//            }
//            holder.unlockCanvasAndPost(canvas);
//        }
//    }

    private int[] getProperDate(int year,int month,int day) {

        int curMonDay = 30;
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                curMonDay = 31;
                break;
            case 2:
                curMonDay = year%4==0? 29: 28;
                break;
        }
        day++;
        if (day>curMonDay) {  //+1的日期后大于当月最大值
            day = 1;
            if ((month++)>12) {
                year++;
                month = 1;
            }
        }
        return new int[]{year,month,day};
    }

}
