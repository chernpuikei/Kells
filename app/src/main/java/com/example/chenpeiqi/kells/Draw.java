package com.example.chenpeiqi.kells;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;

import static com.example.chenpeiqi.kells.Tool.*;

/**
 * Created on 16/9/8.
 */
class Draw implements Runnable {

    private static final String tag = "Draw";
    private Context context;
    private SurfaceHolder holder;
    private Bundle bundle;

    Draw(Context context,SurfaceHolder holder,Bundle bundle) {
        this.context = context;
        this.holder = holder;
        this.bundle = bundle;
    }

    @Override
    public void run() {
        float[] posTan = bundle.getFloatArray("posTan");
        Paint paint = new Paint();
        Bitmap footprint = sample(context.getResources(),R.drawable.fp,20,20);
        float[] pathSE = bundle.getFloatArray("path");
        SharedPreferences sp = SP.getSP(context);
        int width = sp.getInt("width",0), height = sp.getInt("height",0);
        //cal delta*******
        float whole = 0;
        int fpCount = posTan.length/4;
        for (int i = 0;i<fpCount;i++) {
            float current_degree = (float) Math.toDegrees(Math.asin(posTan[3]));
            whole += current_degree;
        }
        boolean a = whole>0, b = posTan[1]>height/2;
        //********这之间的逻辑要移到DataLoader
        float deltaT = (a && b) || ((!a) && (!b))? -90: 90;
        try {
            switch (bundle.getString("operation")) {
            case "FP":
                int duration = 25*posTan.length/4+75;
                for (int i = 0;i<duration;i += 10) { //iterator1
                    Canvas canvas = holder.lockCanvas();
                    drawFrame(canvas,i,true,paint,deltaT,20);
                    holder.unlockCanvasAndPost(canvas);
                }
                break;
            case "TX":
                String province = bundle.getString("province"),
                        city = bundle.getString("city"),
                        whether = "sunny";//bundle.getString("whether");
                int year = bundle.getInt("year"), month = bundle.getInt("month"),
                        verCount = bundle.getInt("verCount");
                float[] ta = bundle.getFloatArray("ta"),
                        path = bundle.getFloatArray("path");
                boolean lor = bundle.getBoolean("lor"),
                        tz = bundle.getBoolean("timeZone");
                int[] es = bundle.getIntArray("staEnd");
                //******下面逻辑同样要移到DataLoader中
                String con_str = bundle.getString("content");
                String[] symbols = new String[]{",",".",":",";","，","。"};
                for (String symbol : symbols) {
                    //每for一次就调用原来的string一次并覆盖之前的replace
                    //最后是"。"在do not go gentle中没有出现，所以还是原来的字符串
                    con_str = con_str.replace(symbol,symbol+" #");
                }
                String[] content = con_str.split("#");
                int animatorLength = (posTan.length/4)*25+75;
                //******
                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(40);
                i("end",es[0]); i("start",es[1]);
                //需要有两个循环,外循环为动画时长,每次迭代都是一帧
                //内循环为line数目,每次迭代都是一条line
                for (int i = 0;i<animatorLength;i += animatorLength-1) {
                    //内循环,每次画一帧并提交
                    Canvas canvas = holder.lockCanvas();
                    drawFrame(canvas,0,false,paint,deltaT,20);
                    String date = "1001.10";
//                    drawDate(canvas,ta,tz,lor,es,width,height,date,verCount);
                    drawTAPosTan(canvas,bundle.getFloatArray("pt"));
                    drawTextFrame(canvas,content,i,textPaint,ta,width,height,lor,es);
                    drawVerCount(canvas,verCount,width,height);
                    splitRect(lor,tz,posTan,path,verCount,width,height,es,ta,canvas);
                    holder.unlockCanvasAndPost(canvas);
                }
                break;
            case "DT":
                showDate(holder); break;
            }
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void drawFrame(Canvas canvas,int i,boolean ad,Paint p,float dT,int dP) {
        canvas.drawRGB(33,33,33);
        float[] posTan = bundle.getFloatArray("posTan");
        for (int j = 0;j<posTan.length/4;j++) {
            int temp = i-25*j; //单个脚印所处的生命周期
            int sinDur = temp>100 || temp<0? 0: temp; //大于100或者小于0都是0
            //ad如果false则alpha一直是100
            int ca = ad? sinDur*2>100? 200-sinDur*2: sinDur*2: 100;
            float[] cpt = new float[4];
            System.arraycopy(posTan,j*4,cpt,0,4);
            float x = cpt[0], y = cpt[1], t = cpt[2], g = cpt[3];
            Matrix matrix = new Matrix();
            float a = (float) (Math.toDegrees(Math.acos(t)));
            float degrees = g>0? a+dT: 360-a+dT;
            matrix.setTranslate(x-dP,y-dP);
            matrix.postRotate(degrees,x,y);
            p.setColor(Color.GREEN);
            canvas.drawCircle(x,y,20,p);
            //p.setAlpha(ca);
            //canvas.drawBitmap(footprint,matrix,p);
        }
    }

    private void drawDate(Canvas canvas,float[] ta,boolean timeZone,boolean lor,
            int[] es,int width,int height,String date,int verCount) {
        int end = es[0], start = es[1];
        Paint datePaint = new Paint(); datePaint.setColor(Color.WHITE);
        int are = Tool.same(lor,timeZone)? ++end: --start;
        float xBound = timeZone? ta[are*2]: ta[are*2+1];
        float areHor = timeZone? xBound-50: width-xBound;
        float corX = timeZone? 50: xBound;
        float corY = (are+1)*height/verCount;
        int textSize = 40; datePaint.setTextSize(textSize);
        //将textSize设定到能容纳的最大大小
        while (datePaint.breakText(date.toCharArray(),0,date.length(),areHor,null)
                == date.length()) {
            datePaint.setTextSize(++textSize);
        }
        canvas.drawText(date.toCharArray(),0,date.length(),corX,corY,datePaint);
    }

    private void drawTextFrame(Canvas canvas,String[] sentences,int i,Paint tp,float[] area,int width,int height,boolean lor,int[] staEnd) {
        Tool.header();
        int verCount = area.length/2;
        int currentEnd = staEnd[0];
        boolean currentLOR = lor;
        i("currentLOR",currentLOR);
        float curX = lor? 50: area[1];
        int curY = 0;
        for (int j = 0;j<sentences.length;j++) {
            int alpha = i>j*25? i>100+j*25? 100: i-(j*25): 0;//确定当前句子的alpha
            //增加中断？滚动显示？
            //先用迭代的方式往当前句子的标点符号后面插入一个不常用的标点符号
            String[] words = sentences[j].replace(" "," #").split("#");//单词间插入#
            for (String temp : words) {//迭代单词
                i("word",temp);
                char[] word = temp.toCharArray();
                float[] ms = new float[2];
                float areLen = currentLOR? area[curY*2]-curX: width-curX;//area剩余长度
                tp.setAlpha(alpha);
                //unbreakable
                while (tp.breakText(word,0,word.length,areLen,ms) != word.length) {
                    i("curY",curY);
                    i("curX",curX);
                    i("areLen",areLen);
                    //totally unbreakable
                    if (++curY>currentEnd && !same(lor,currentLOR)) {
                        return;
                    } else {
                        //switch another side if unbreakable at current side
                        if (curY>currentEnd) {//未越界
                            currentLOR = !currentLOR;//换边
                            i("switched",currentLOR);
                            curY = staEnd[1];//换边后需要重设Y
                            currentEnd = verCount-1;
                        }
                        //reset X and areLen anyway
                        curX = currentLOR? 50: area[curY*2+1]+50;//换没换边都需要重设X
                        areLen = currentLOR? area[curY*2]-curX: width-curX;
                        i("areLen",areLen);
                    }
                }
                i("curX",curX);
                i("curY",curY);
                i("area",area[curY*2+1]+50);
                canvas.drawText(word,0,word.length,curX,((float) curY+0.66f)*height/verCount,tp);
                curX += ms[0];//当出现越界情况未breakText直接draw导致此处ms为0
            }
        }
    }

    private void splitRect(boolean lor,boolean tz,float[] posTan,float[] path,
            int verCount,int width,int height,int[] es,float[] ta,Canvas canvas) {
        Tool.s();
        i("end",es[0]); i("start",es[1]);
        float ah = height/verCount;
        boolean same = Tool.same(lor,tz);
        //第一个esCounter用于指示y取开始结束区域中[开始,结束]中的哪一个,
        // 第二个esCounter用于指示取该区域中上边还是下边的边界
        int deltaY = 0, esCounter1 = same? 0: 1, esCounter2 = same? 1: 0,
                yCalculator = es[esCounter1]+esCounter2;
        float oriY = yCalculator*ah;
        Paint paint = new Paint(); paint.setColor(Color.MAGENTA);
//        float oriX = ta[2*((es[esCounter1]+(same? 1: -1)))+(tz? 0: 1)];
        float oriX = ta[2*es[esCounter1]+(tz?0:1)];
        i("(oriX,oriY)","("+oriX+","+oriY+")");
        Log.i("check",getClass().getName());
        locatePoint(canvas,oriX,oriY,width,height);
        //获取所需area里面的所有posTan
//        int requireArea = es[esCounter1]+(same? 1: -1);
//        float minY = ah*requireArea, maxY = ah*(requireArea+1);
        float[] ori = new float[]{oriX,oriY};
        int[] size = new int[]{width,height};
        firstSelect(posTan,path,ori,size,canvas);
    }

    private void firstSelect(
            float[] posTan,float[] path,float[] ori,int[] size,Canvas canvas) {
        int width = size[0],height = size[1];
        Paint paint = new Paint(); paint.setColor(Color.YELLOW);
        float[] des = new float[2];
        boolean which = path[0] == 0 || path[0] == width;
        des[0] = which? path[0]: path[4];des[1] = which? path[1]: path[5];
        boolean oReHor = des[0]>ori[0], oReVer = des[1]>ori[1];
        ArrayList<float[]> chosen = new ArrayList<>();
        for (int i = 0;i<posTan.length/4;i++) {
            boolean reHor = posTan[i*4]>ori[0], reVer = posTan[i*4+1]>ori[1];
            canvas.drawCircle(ori[0],ori[1],10,paint);
            s();
            i("iterator",i);
            i("(oriX/Y)",ori[0]+"/"+ori[1]);
            i("curX/Y",posTan[i*4]+"/"+posTan[i*4+1]);
            i("reHor/reVer",reHor+"/"+reVer);
            i("oReHor/oReVer",oReHor+"/"+oReVer);
            if (same(oReHor,reHor) && same(oReVer,reVer)) {
                chosen.add(new float[]{posTan[i*4],posTan[i*4+1]});
//                locatePoint(canvas,posTan[i*4],posTan[i*4+1],width,height);
                canvas.drawCircle(posTan[i*4],posTan[i*4+1],20,paint);
                i("(x,y)","("+posTan[i*4]+","+posTan[i*4+1]+")");
            }
        }
        selectSorted(sortSelected(chosen,width,height),oReHor,oReVer,canvas,ori,width);
    }

    private ArrayList<float[]> sortSelected(ArrayList<float[]> chosen,int width,int height) {
        ArrayList<float[]> sorted = new ArrayList<>();
        sorted.add(new float[]{-1,-1}); sorted.add(new float[]{width+1,height+1});
        for (int i = 0;i<chosen.size();i++) {
            float[] choCho = new float[]{chosen.get(i)[0],chosen.get(i)[1]};
            for (int j = 0;j<sorted.size();j++) {
                //比当前小且比前面大，插入当前位置
                if (choCho[0]<sorted.get(j)[0] && choCho[0]>sorted.get(j-1)[0]) {
                    sorted.add(j,choCho);
                }
            }
        }
        //返回前将用于辅助计算的两个点删掉
        sorted.remove(0); sorted.remove(sorted.size()-1);
        return sorted;
    }

    private ArrayList<float[]> selectSorted(ArrayList<float[]> sorted,boolean hor,boolean ver,Canvas canvas,float[] ori,int width) {
        ArrayList<float[]> chosen = new ArrayList<>();
        Paint paint = new Paint(); paint.setColor(Color.BLUE);
        i("curLOC",(ver? "bottom": "top")+" "+(hor? "right": "left"));
        Paint number = new Paint(); number.setColor(Color.WHITE);
        number.setTextSize(50);
        //参数的ArrayList的排列方式
        int colCou = -1;
        for (int i = hor? sorted.size()-1: 0;hor? i>-1: i<sorted.size();i += hor? -1: 1) {
            s();
            i("i",i);
            //当前选点>/<所有迭代点就将当前点添加到chosen中
            float[] curCho = sorted.get(i);
            boolean notFailYet = true;
            int delta = hor? 1: -1;
            for (int j = i+delta;(hor? j<sorted.size(): j>0) && notFailYet;j += delta) {
                i("j",j);
                if (ver? curCho[1]>sorted.get(j)[1]: curCho[1]<sorted.get(j)[1]) {
                    //评估过程，一票否决
                    notFailYet = false;
                }
            }
            if (notFailYet) {
                chosen.add(curCho);
                canvas.drawCircle(curCho[0],curCho[1],5,paint);
                colCou = draw_reload(paint,canvas,hor,ver,ori,curCho,colCou,width);
                canvas.drawText((""+i).toCharArray(),0,1,curCho[0],curCho[1],number);
            }
        }
        return chosen;
    }

    private int draw_reload(Paint paint,Canvas canvas,boolean hor,boolean ver,float[] ori,float[] current,int colorCounter,int width) {
        int[] rgb = new int[]{Color.RED,Color.GREEN,Color.BLUE};
        paint.setColor(rgb[++colorCounter%3]); paint.setAlpha(50);
        canvas.drawRect(hor? current[0]: 0,ver? ori[1]: current[1],
                hor? width: current[0],ver? current[1]: ori[1],paint);
        return colorCounter;
    }

    private void locatePoint(Canvas canvas,float x,float y,int width,int height) {
        Paint paint = new Paint(); paint.setColor(Color.CYAN);
        canvas.drawRect(x-2,0,x+2,height,paint);
        canvas.drawRect(0,y-2,width,y+2,paint);
    }

    private void drawTAPosTan(Canvas canvas,float[] taPosTan) {
        Paint paint = new Paint(); paint.setColor(Color.RED);
        for (int i = 0;i<taPosTan.length/4;i++) {
            canvas.drawRect(taPosTan[i*4]-10,taPosTan[i*4+1]-10,
                    taPosTan[i*4]+10,taPosTan[i*4+1]+10,paint);
        }
    }

    private void drawVerCount(Canvas canvas,int verCount,int width,int height) {
        Paint paint = new Paint();
        paint.setTextSize(50);
        paint.setColor(Color.WHITE);
        for (int i = 0;i<verCount;i++) {
            char[] check = (i+"").toCharArray();
            canvas.drawText(check,0,check.length,0,(i+0.66f)*height/verCount,paint);
            canvas.drawLine(0,height/verCount*i,width,height/verCount*i,paint);
        }
    }

    private void showDate(SurfaceHolder holder) {
        int[] current = bundle.getIntArray("date");
        float[] posTan = bundle.getFloatArray("posTan");
        Paint red = new Paint();
        red.setColor(Color.RED);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        int deltaA = 2;
        for (int a = 0;a<200 && a>-1;a += deltaA) {
            if (a == 100) {
                deltaA = -2;
            }
            paint.setAlpha(a);
            int[] date_loop = current;
            Canvas canvas = holder.lockCanvas();
            canvas.drawRGB(33,33,33);
            for (int i = 0;i<posTan.length/4;i++) {
                String date = ""+date_loop[0]+date_loop[1]+date_loop[2];
                canvas.drawText(date,posTan[4*i]-100,posTan[4*i+1],paint);
//            canvas.drawRect(posTan[4*b]-25,posTan[4*b+1]-25,
//                    posTan[4*b]+25,posTan[4*b+1]+25,red);
                date_loop = getProperDate(date_loop[0],date_loop[1],date_loop[2]);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private int[] getProperDate(int year,int month,int day) {

        int curMonDay = 30;
        switch (month) {
        case 1: case 3: case 5: case 7: case 8: case 10: case 12:
            curMonDay = 31;
            break;
        case 2:
            curMonDay = year%4 == 0? 29: 28;
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

    static Bitmap sample(Resources res,int resId,int reqWidth,int reqHeight) {

        //First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);

        // Calculate inSampleSize
        options.inSampleSize = calSamSize(options,reqWidth,reqHeight);
        BitmapFactory.decodeResource(res,resId,options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
//      options.inPreferredConfig = Bitmap.Config.ARGB_4444;//为解决OOM
        return BitmapFactory.decodeResource(res,resId,options);
    }

    private static int calSamSize(BitmapFactory.Options options,int rw,int rh) {
        // Raw height and width of image
        final int h = options.outHeight;
        final int w = options.outWidth;
        int inSampleSize = 1;
        while ((h/inSampleSize)>rh && (w/inSampleSize)>rw) inSampleSize++;
        return inSampleSize;
    }

}
