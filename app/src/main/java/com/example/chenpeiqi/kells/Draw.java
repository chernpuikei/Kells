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
class Draw implements Runnable {

    private static final String tag = "Draw";
    private Context context;
    private SurfaceHolder holder;
    private Bundle bundle;
    private int width, height;

    Draw(Context context,SurfaceHolder holder,Bundle bundle) {
        this.context = context;
        this.holder = holder;
        this.bundle = bundle;
        SharedPreferences sp = SP.getSP(context);
        width = sp.getInt("width",0);
        height = sp.getInt("height",0);
    }

    @Override
    public void run() {
        float[] posTan = bundle.getFloatArray("posTan");
        Paint paint = new Paint();
        Bitmap footprint = sample(context.getResources(),R.drawable.fp,20,20);
        float[] pathSE = bundle.getFloatArray("path");
        array("check","path@Draw",pathSE);
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
                        path = bundle.getFloatArray("path"),
                        areaZero = bundle.getFloatArray("areaZero"),
                        ori = bundle.getFloatArray("ori"),
                        cps = bundle.getFloatArray("cps"),
                        areaOne = bundle.getFloatArray("areaOne");
                array("ori",ori); array("cps",cps);
                array("areaEnd",areaZero); array("areaSta",areaOne);
                boolean lor = bundle.getBoolean("lor"),
                        tz = bundle.getBoolean("timeZone"),
                        tob = bundle.getBoolean("tob");
                int[] es = bundle.getIntArray("es");
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
                //需要有两个循环,外循环为动画时长,每次迭代都是一帧
                //内循环为line数目,每次迭代都是一条line
                for (int i = 0;i<animatorLength;i += 10) {
                    //内循环,每次画一帧并提交
                    Canvas canvas = holder.lockCanvas();
                    drawFrame(canvas,0,false,paint,deltaT,20);
                    String date = "1001.10";
//                    drawDate(canvas,ta,tz,lor,es,width,height,date,verCount);
                    drawTAPosTan(canvas,bundle.getFloatArray("pt"));
                    drawSelected(areaZero,canvas,lor,true);
                    lineCPS(canvas,cps);
                    drawSelected(areaOne,canvas,lor,false);
                    drawTextFrame(canvas,content,i,textPaint,ta,width,height,lor,es);
                    drawVerCount(canvas,verCount,width,height);
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

    private void drawTextFrame(Canvas canvas,String[] sentences,int i,Paint tp,
            float[] ta,int width,int height,boolean lor,int[] staEnd) {
        int verCount = ta.length/2;
        int currentEnd = staEnd[0];
        boolean currentLOR = lor;
        i("currentLOR",currentLOR);
        float curX = lor? 50: ta[1];
        int curY = 0;
//        drawTA(canvas,ta,width,height);
        for (int j = 0;j<sentences.length;j++) {
            int alpha = i>j*25? i>100+j*25? 100: i-(j*25): 0;//确定当前句子的alpha
            //增加中断？滚动显示？
            //先用迭代的方式往当前句子的标点符号后面插入一个不常用的标点符号
            String[] words = sentences[j].replace(" "," #").split("#");//单词间插入#
            for (String temp : words) {//迭代单词
                char[] word = temp.toCharArray();
                float[] ms = new float[2];
                float areLen = currentLOR? ta[curY*2]-curX: width-curX;//area剩余长度
                tp.setAlpha(alpha);
                //unbreakable
                while (tp.breakText(word,0,word.length,areLen,ms) != word.length) {
                    //totally unbreakable
                    if (++curY>currentEnd && !same(lor,currentLOR)) {
                        return;
                    } else {
                        //switch another side if unbreakable at current side
                        if (curY>currentEnd) {//未越界
                            currentLOR = !currentLOR;//换边
                            curY = staEnd[1];//换边后需要重设Y
                            currentEnd = verCount-1;
                        }
                        //reset X and areLen anyway
                        curX = currentLOR? 50: ta[curY*2+1]+50;//换没换边都需要重设X
                        areLen = currentLOR? ta[curY*2]-curX: width-curX;
                    }
                }
                canvas.drawText(word,0,word.length,curX,((float) curY+0.66f)*height/verCount,tp);
//                drawWhether(canvas,context);
                curX += ms[0];//当出现越界情况未breakText直接draw导致此处ms为0
            }
        }
    }

    private int draw_reload(Paint paint,Canvas canvas,boolean hor,boolean ver,
            float[] ori,float[] current,int colorCounter,int width) {
        //要根据计数器设定笔刷颜色以及选定drawRect起点
        int[] rgb = new int[]{Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW};
        paint.setColor(rgb[++colorCounter%4]); paint.setAlpha(70);
        canvas.drawRect(hor? current[0]: 0,ver? ori[1]: current[1],
                hor? width: current[0],ver? current[1]: ori[1],paint);
        return colorCounter;
    }

    private void drawWhether(Canvas canvas,Context context) {
        Bitmap check = sample(context.getResources(),R.drawable.whether,200,200);
        canvas.drawBitmap(check,0,0,new Paint());
    }

    private void drawSelected(float[] area,Canvas canvas,boolean lor,boolean eos) {
        i("which area?",eos?"end":"sta");
        array("area",area);
        //取相邻两点，第一点的水平坐标移至相应的屏幕边界
        //这里还要在参数中标记出当前area处于屏幕左边还是右边
        boolean flag = same(lor,eos);
        int counter = 0;
        int[] rgb = new int[]{Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW};
        Paint yellow = new Paint(); yellow.setColor(Color.YELLOW);
        Paint white = new Paint(); white.setColor(Color.WHITE);
        Paint paint = new Paint();
        Paint cyan = new Paint();cyan.setColor(Color.CYAN);
        white.setTextSize(50);
        for (int i = 0;i<area.length/2;i++) {
            canvas.drawText(""+i,area[i*2],area[i*2+1],white);//画圆上的数字提示
            canvas.drawCircle(area[i*2],area[i*2+1],20,yellow);//画圆
            paint.setColor(rgb[counter++%4]); paint.setAlpha(20);
            float floatingX = flag? 0: width;
            if (i<area.length/2-1){
                canvas.drawRect(floatingX,area[i*2+1],area[(i+1)*2],area[(i+1)*2+1],paint);
            }
            canvas.drawCircle(floatingX,area[i*2+1],20,cyan);
        }
    }

    private void locates(Canvas canvas,float[] locate) {
        float x = locate[0], y = locate[1], width = locate[0], height = locate[1];
        Paint paint = new Paint(); paint.setColor(Color.CYAN);
        canvas.drawRect(x-2,0,x+2,height,paint);
        canvas.drawRect(0,y-2,width,y+2,paint);
        paint.setColor(Color.MAGENTA);
        canvas.drawCircle(x,y,10,paint);
    }

    private void drawTAPosTan(Canvas canvas,float[] taPosTan) {
        Paint paint = new Paint(); paint.setColor(Color.RED);
        for (int i = 0;i<taPosTan.length/4;i++) {
            canvas.drawRect(taPosTan[i*4]-10,taPosTan[i*4+1]-10,
                    taPosTan[i*4]+10,taPosTan[i*4+1]+10,paint);
        }
    }

    private void drawCircleFromArray(Canvas canvas,float[] array,int gap,int color) {
        Paint paint = new Paint(); paint.setColor(color);
        for (int i = 0;i<array.length/gap;i++) {
            drawCross(canvas,array[i*gap],array[i*gap+1]);
            canvas.drawCircle(array[i*gap],array[i*gap+1],20,paint);
        }
    }

    private void drawCross(Canvas canvas,float x,float y) {
        Paint paint = new Paint(); paint.setColor(Color.MAGENTA);
        canvas.drawRect(x-2,0,x+2,1920,paint);
        canvas.drawRect(0,y-2,1080,y+2,paint);
    }

    private void lineCPS(Canvas canvas,float[] cps) {
        Paint paint = new Paint(); paint.setColor(Color.RED);
        for (int i = 0;i<cps.length/4;i++) {
            canvas.drawLine(cps[i*4],cps[i*4+1],cps[i*4+2],cps[i*4+3],paint);
        }
    }

    private void drawTA(Canvas canvas,float[] ta,int width,int height) {
        int ah = height/(ta.length/2);
        Paint cyan = new Paint(); cyan.setColor(Color.CYAN); cyan.setAlpha(20);
        for (int i = 0;i<ta.length/2;i++) {
            canvas.drawRect(0,ah*i,ta[2*i],ah*(i+1),cyan);
            canvas.drawRect(ta[2*i+1],ah*i,width,ah*(i+1),cyan);
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

}
