package com.example.chenpeiqi.kells;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.os.Bundle;
import android.view.SurfaceHolder;

import java.util.ArrayList;

/**
 * Created on 16/9/8.
 */
class Draw implements Runnable {

    private Context context;
    private SurfaceHolder holder;
    private Bundle bundle;
    private Bitmap footprint;
    private static final int deltaP = 20;
    private float deltaT;
    private static final int mp = Context.MODE_PRIVATE;
    private float[] pathSE;
    private int width, height;
//    private float[] posTan, ta;
//    private boolean lor;
//    private int[] conStaEnd;

    Draw(Context context,SurfaceHolder holder,Bundle bundle) {
        this.context = context;
        this.holder = holder;
        this.bundle = bundle;
//        footprint = sample(context.getResources(),R.drawable.footprint,10,10);
        footprint = sample(context.getResources(),R.drawable.fp,20,20);
        deltaT = calDelta(bundle.getFloatArray("posTan"));
        pathSE = bundle.getFloatArray("path");
        SharedPreferences sp = SP.getSP(context);
        width = sp.getInt("width",0); height = sp.getInt("height",0);

    }

    @Override
    public void run() {
        try {
            switch (bundle.getString("operation")) {
            case "FP":
                drawStep(holder,true);
                drawStep(holder,false);
                break;
            case "TX":
                drawText(holder); break;
            case "DT":
                drawDate(holder); drawStep(holder,false); break;
            }
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void drawStep(SurfaceHolder holder,boolean ad) {
        //ad for ActivateDuration
        float[] posTan = bundle.getFloatArray("posTan");
        int duration = ad? 25*posTan.length/4+75: 1;
        for (int i = 0;i<duration;i += 2) { //iterator1
            Canvas canvas = holder.lockCanvas();
            drawFrame(canvas,i,ad);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawFrame(Canvas canvas,int i,boolean ad) {
        canvas.drawRGB(33,33,33);
        float[] posTan = bundle.getFloatArray("posTan");
        for (int j = 0;j<posTan.length/4;j++) {
            int temp = i-25*j; //单个脚印所处的生命周期
            int sinDur = temp>100 || temp<0? 0: temp; //大于100或者小于0都是0
            //ad如果false则alpha一直是100
            int currentAlpha = ad? sinDur*2>100? 200-sinDur*2: sinDur*2: 100;
            float[] currentFpPt = new float[4];
            System.arraycopy(posTan,j*4,currentFpPt,0,4);
            drawFootprint(canvas,currentFpPt,currentAlpha);
        }
//        canvas.drawRect();
    }

    private void drawFootprint(Canvas canvas,float[] pt,int alpha) {
        Paint paint = new Paint(); paint.setAlpha(alpha);
        Paint red = new Paint(); red.setColor(Color.MAGENTA); //red.setAlpha(alpha);
        float x = pt[0], y = pt[1], t = pt[2], g = pt[3];
        Matrix matrix = new Matrix();
        float a = (float) (Math.toDegrees(Math.acos(t)));
        float degrees = g>0? a+deltaT: 360-a+deltaT;
        matrix.setTranslate(x-deltaP,y-deltaP);
        matrix.postRotate(degrees,x,y);
//        canvas.drawBitmap(footprint,matrix,paint);
        canvas.drawCircle(x,y,20,red);
    }

    private float calDelta(float[] posTan) {
        int height = context.getSharedPreferences("status",mp).getInt("height",0);
        boolean a = calWholeSinA(posTan), b = posTan[1]>height/2;
        if ((a && b) || ((!a) && (!b))) return -90;
        return 90;
    }

    private boolean calWholeSinA(float[] pt) {
        //计算处所有fp的平均转角
        float whole = 0;
        int fpCount = pt.length/4;
        for (int i = 0;i<fpCount;i++) {
            float current_degree = (float) Math.toDegrees(Math.asin(pt[3]));
            whole += current_degree;
        }
        return whole>0;
    }

    private void anotherWayToDrawText(SurfaceHolder holder,float[] ta){
        Canvas canvas = holder.lockCanvas();


    }

    //调用一次画一帧
    private void drawText(SurfaceHolder surfaceHolder) {
        String con_str = bundle.getString("content");
        String province = bundle.getString("province");
        String city = bundle.getString("city");
        float[] ta = bundle.getFloatArray("ta");
        boolean lor = bundle.getBoolean("lor");
        float[] la = bundle.getFloatArray("la");
        int[] wlcStaEnd = bundle.getIntArray("wlcStaEnd");
        int[] conStaEnd = bundle.getIntArray("conStaEnd");
        String whether = "sunny";
//        String whether = bundle.getString("whether");
        String[] wheLocCit = new String[]{province,city,whether};
        String[] symbols = new String[]{",",".",":",";","，","。"};
        for (String symbol : symbols) {
            //每for一次就调用原来的string一次并覆盖之前的replace
            //最后是"。"在do not go gentle中没有出现，所以还是原来的字符串
            con_str = con_str.replace(symbol,symbol+" #");
        }
        assert con_str != null;
        String[] content = con_str.split("#");
        float[] posTan = bundle.getFloatArray("posTan"); assert posTan != null;
        int animatorLength = 300,//(posTan.length/4)*25+75;
                wlcAniLength = la.length*25+75;
        Paint tp = new Paint();
        tp.setTextSize(40);
        tp.setColor(Color.WHITE);
        Paint lp = new Paint();
        lp.setTextSize(40);
        lp.setColor(Color.WHITE);
        //需要有两个循环,外循环为动画时长,每次迭代都是一帧
        //内循环为line数目,每次迭代都是一条line
        for (int i = 0;i<animatorLength;i++) {
            Bundle conPara = initPara(bundle.getBoolean("lor"),ta,conStaEnd);
            Bundle wlcPara = initPara(!bundle.getBoolean("lor"),la,wlcStaEnd);
            //内循环,每次画一帧并提交
//            drawStep(holder,false);
            Canvas canvas = surfaceHolder.lockCanvas();
            //背景
            drawFrame(canvas,0,false);
            //下面是天气和地理部分
            drawSomething(i,wheLocCit,canvas,wlcPara,lp,!lor,la,wlcStaEnd,5,true);
            //下面是content部分
            drawSomething(i,content,canvas,conPara,tp,lor,ta,conStaEnd,15,false);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    //先迭代句子，再迭代词语，执行一次得一帧
    private void drawSomething(int i,String[] content,Canvas canvas,Bundle para,Paint tp,boolean lor,float[] area,int[] staEnd,int verCount,boolean autoSwi) {
        boolean continues = true;
        for (int j = 0;j<content.length;j++) {//迭代整个句子
            if (continues) {
                int alpha = i>j*25? i>100+j*25? 100: i-(j*25): 0;//确定当前句子的alpha
                //增加中断？滚动显示？
                //先用迭代的方式往当前句子的标点符号后面插入一个不常用的标点符号
                String[] words = content[j].replace(" "," #").split("#");//单词间插入#
                for (String word : words) {//迭代单词
                    //每调用完一次都会返回下一次调用所需要的参数
                    para = drawWord(canvas,word.toCharArray(),para,alpha,tp,lor,staEnd,area,verCount,autoSwi);
                    if (para == null) {
                        continues = false;
                        break;
                    }
                }
            } else {
                break;
            }
        }
    }

    private Bundle drawWord(Canvas canvas,char[] line,Bundle par,int alpha,
            Paint paint,boolean lor,int[] staEnd,float[] area,int verCount,boolean autoSwi) {
        //直接break，break得过就draw，break不过就换行
        float curX = par.getFloat("cursorX");
        int curY = par.getInt("cursorY");
        float[] ms = new float[2];
        float areLen = lor? area[curY*2]-curX: width-curX;
        paint.setAlpha(alpha);
        //无论breakText的结果是true还是false，都要画当前行
        int breakLength = paint.breakText(line,0,line.length,areLen,ms);
        if (breakLength == line.length) {
            //如果在当前行draw，直接画，画完后将x坐标加上ms后返回
            canvas.drawText(line,0,line.length,curX,(curY+1)*height/verCount,paint);
            if (autoSwi) {
                par.remove("cursorY"); par.putInt("cursorY",++curY);
                if (curY>staEnd[1]) return null;
            } else {
                curX += ms[0];
                par.remove("cursorX"); par.putFloat("cursorX",curX);
            }
            return par;
        } else {
            //如果在下一行draw，y++，x重置，画，画完后x坐标加上ms，返回
            if (++curY>staEnd[1]) {
                return null;
            } else {
                curX = lor? 50: area[curY*2+1];//y++&cur重置
                par.remove("cursorY"); par.putInt("cursorY",curY);
                if (autoSwi) L.s("curY after invalidating:"+curY);
                par.remove("cursorX"); par.putFloat("cursorX",curX);
                par = drawWord(canvas,line,par,alpha,paint,lor,staEnd,area,verCount,autoSwi);
                return par;
            }
        }
    }

    private Bundle initPara(Boolean lor,float[] texAre,int[] conStaEnd) {
        Bundle para = new Bundle();
        para.putInt("cursorY",conStaEnd[0]);
        //cursorX is current position
        para.putFloat("cursorX",lor? 50: texAre[conStaEnd[0]*2+1]);
        para.putInt("whiLine",0);
        para.putInt("staChar",0);
        return para;
    }

    private void drawTextInOldWay(SurfaceHolder holder) {
        char[] content = bundle.getString("content").toCharArray();
        float[] ta = bundle.getFloatArray("textArea");
        int[] belonging = bundle.getIntArray("belonging");
        float[] posTan = bundle.getFloatArray("posTan");
        int width = context.getSharedPreferences("status",mp).getInt("width",0),
                height = context.getSharedPreferences("status",mp).getInt("height",0);
        Paint pt = new Paint(); pt.setTextSize(50); pt.setColor(Color.WHITE);
        ArrayList<float[]> previous = new ArrayList<>();

        int chSta = 0;
        int reLen = content.length;
        int conPos = bundle.getBoolean("lor")? 1: 2;

        for (int poVer = 0;poVer<20;poVer++) {
            if (belonging[poVer] == conPos || belonging[poVer] == 3) {
                float areLen = conPos == 1? ta[poVer*2]: width-ta[poVer*2+1];
                float staXCor = conPos == 2? ta[poVer*2+1]: 0;
                float staYCor = (int) (height*(poVer+0.75)/20);
                int count = Math.min(
                        pt.breakText(content,chSta,reLen,areLen,null),reLen);
                for (int l = 0;l<count;l++) {
                    Canvas canvas = holder.lockCanvas();
                    canvas.drawRGB(43,43,43);
                    for (int i = 0;i<posTan.length/4;i++) {
                        float[] s_fp = new float[]{posTan[i*4],posTan[i*4+1],
                                posTan[i*4+2],posTan[i*4+3]};
                        drawFootprint(canvas,s_fp,100);
                    }
//                drawTA(canvas,ta);
                    for (float[] t : previous) {
                        canvas.drawText(content,(int) t[2],(int) t[3],t[0],t[1],pt);
                    }
                    canvas.drawText(content,chSta,l,staXCor,staYCor,pt);
                    holder.unlockCanvasAndPost(canvas);
                    try {
                        Thread.sleep(1);//不sleep会造成poVer/count计数器回推
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                previous.add(new float[]{staXCor,staYCor,chSta,count});
                chSta += count; reLen -= count;
            }
        }
    }

    //此处代码需要实现在text不得不分行显示后调用自身以完成Draw
//    private Bundle drawLine(Canvas canvas,char[] line,Bundle draPar,Paint paint) {
//        //此处需要用到两个数据包,其一为bundle,在创建draw()所传入,静态数据
//        //其二为drawing para,用来指示当前帧的生成进度,为动态数据,包括如下条目:
//        //1.staCha:指示当前ta当前line的开始drawText位置
//        //2.cursorX:用于指示在当前ta开始drawLine的起始位置
//        //3.cursorY:用于指示当前line在垂直方向上所处的ta,用此参数结合ta可以获得一部分cursorX
//        //4.remCou:一般情况下,remainLength的数值等同于一条line的字符数,
//        //  但当一条line不足以在一个ta中显示完成,剩余部分就会被切割到下一个ta中显示
//        //  此时的remainLength就等于被切割后的剩余长度
//        //先从drawingPara中取出所有参数
//        if (draPar == null) return null;
//        int staCha = draPar.getInt("staChar");
//        int remCou = line.length-staCha;
//        float curX = draPar.getFloat("cursorX");
//        int curY = draPar.getInt("cursorY");
//        float corY = (curY+1)*height/15;
//        float areLen = lor? ta[curY*2]-curX: width-curX;
//        float[] msWidth = new float[4];//'ms' for measure
//        //'ct' for Container
//        int wriCou = paint.breakText(line,staCha,remCou,areLen,msWidth);
//        canvas.drawText(line,staCha,wriCou,curX,corY,paint);
//        P.t(P.line()+P.kv("left or right?",lor? "left": "right")
//                +P.kv("current cursorY",curY)
//                // available area always start from current cursor position
//                +P.kv("current ta",
//                "["+0+","+ta[curY*2]+"],["+ta[curY*2+1]+","+width+"]")
//                +P.kv("remain ta",
//                lor? "["+curX+","+ta[curY*2]+"]": "["+curX+","+width+"]")
//                +P.kv("remain area",areLen)
//                +P.kv("whole chars",line.length)
//                +P.kv("start char num",staCha)
//                +P.kv("remain chars",remCou)
//                +P.kv("broken chars",wriCou)+P.line());
//        P.t("right before canvas drawText,curX,corY:"+curX+","+corY);
//        //charInvolve是当前Ta所能break出来的字符数，是容器容量
//        //remainLength初始情况下是一条line的字符数目
//        //如果字符数目大于容器容量，即remainLength>wriCou，即为过长，显示不全
//        if (remCou>wriCou) {
//            //not fully display
//            //1.RePalace CursorY(cursorY++)
//            //2.RePalace CursorX(50/ta[cursorY*2+1])
//            //3.RePalace cursor of current text
//            //4.Recalculate remain length of current text
//            if (++curY>conStaEnd[1]) {
//                P.t("curY>content ta maxCounter:"+curY+"/"+conStaEnd[1]);
//                P.t("returning null");
//                draPar = null;
//            } else {
//                curX = lor? 50: ta[curY*2+1];
//                staCha += wriCou;
//                //对上面变量进行修改后将原来para中的相应参数移除并将新的参数写入后提交返回
//                draPar.remove("cursorY"); draPar.putInt("cursorY",curY);
//                draPar.remove("cursorX"); draPar.putFloat("cursorX",curX);
//                draPar.remove("staChar");
//                draPar.putInt("staChar",staCha);
//                //超出显示范围后更新bundle后继续draw
//                draPar = drawLine(canvas,line,draPar,paint);
//            }
//        } else {
//            //容器长度大于line长度,应当执行的操作：
//            //1.cursorX顺延 2.cursorY不变 3.staChar归零
//            draPar.remove("cursorX");
//            draPar.putFloat("cursorX",curX+msWidth[0]);
//            draPar.remove("staChar"); draPar.putInt("staChar",0);
//        }
//        if (draPar != null) {
//            P.t("drawing para refreshed——cursorX/cursorY/staChar:"
//                    +draPar.getFloat("cursorX")+"/"
//                    +draPar.getInt("cursorY")+"/"+draPar.getInt("staChar")+"\n");
//        }
//        P.t(P.line()+P.ta(ta)+P.line());
//        return draPar;
//    }

    private void drawDate(SurfaceHolder holder) {
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
//            canvas.drawRect(posTan[4*i]-25,posTan[4*i+1]-25,
//                    posTan[4*i]+25,posTan[4*i+1]+25,red);
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

    private void drawTA(Canvas canvas,float[] textArea) {
        Paint pft = new Paint(); pft.setTextSize(100);
        Paint blue = new Paint(); blue.setColor(Color.WHITE); blue.setAlpha(25);
        for (int i = 0;i<20;i++) {
            canvas.drawRect(0,i*1920/20,textArea[i*2],(i+1)*1920/20,blue);
            canvas.drawRect(textArea[i*2+1],i*1920/20,1080,(i+1)*1920/20,blue);
            canvas.drawText("AREA<"+i+">",0,(i+1)*1920/20,pft);
        }
    }

    static Bitmap sample(Resources res,int resId,int reqWidth,int reqHeight) {

        //First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        BitmapFactory.decodeResource(res,resId,options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
//      options.inPreferredConfig = Bitmap.Config.ARGB_4444;//为解决OOM
        return BitmapFactory.decodeResource(res,resId,options);
    }

    static int calculateInSampleSize(
            BitmapFactory.Options options,int reqWidth,int reqHeight) {
        // Raw height and width of image
        final int h = options.outHeight;
        final int w = options.outWidth;
        int inSampleSize = 1;
        while ((h/inSampleSize)>reqHeight && (w/inSampleSize)>reqWidth)
            inSampleSize++;
        return inSampleSize;
    }

}
