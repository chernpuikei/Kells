package com.example.chenpeiqi.kells;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.chenpeiqi.kells.Tool.*;

/**
 * Created on 16/11/9.
 */
class DataLoader implements Callable<Bundle> {

    private Bundle request;
    private static final int verCount = 15;
    private static final int pw = 40;
    private static int width, height;//放在bundle中,为了方便设定成为成员变量,构造函数中初始化

    DataLoader(Context context,Bundle origin) {
        this.request = origin;
        width = origin.getInt("width");
        height = origin.getInt("height");
    }

    @Override
    public Bundle call() throws Exception {
        Bundle bundleFromServer = pullRequestPushResult();
        if (bundleFromServer.getString("respondType").equals("requestCanvas")) {
            if (bundleFromServer.getBoolean("result")) {
                return finalEdit(bundleFromServer);
            } else {
                return firstEdit(bundleFromServer);
            }
        }
        return bundleFromServer;
    }

    private Bundle pullRequestPushResult() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(new CMT(this.request)).get();
    }

    //requestCanvas从服务器获取结果为false时产生创建结果返回上层且同步至服务器
    private Bundle firstEdit(Bundle bFromS) {
        Bundle output = new Bundle();
        String[] date = new Date().toString().split(" ");
        float[] ends = request.getFloatArray("ends");
        Cord start = (ends[0] == 0 && ends[1] == 0)?
                new Cord(Math.random()>0.5? AREA.ONE: AREA.TWO):
                getNextStart(ends);//<---here come the problem
        float[] path = createPath(start);
//        float[] path = generatePath(start);
        output.putFloatArray("path",path);
        float[] posTan = initPosTan(path,Integer.parseInt(date[5]),date[1]);
        output.putFloatArray("posTan",posTan);
        synchronizeCanvas(output);
        output = finalEdit(output);
        return output;
    }

    private Bundle finalEdit(Bundle bFromS) {
        float[] path = bFromS.getFloatArray("path");
        float[] posTan = bFromS.getFloatArray("posTan");
        float[] posTanForTA = new float[posTan.length*4];
        int extraCordCounter = 0;
        for (int i = 0;i<posTan.length/4;i++) {
            float[] xs = new float[]{posTan[i*4]-pw/2,posTan[i*4]+pw/2},
                    ys = new float[]{posTan[i*4+1]-pw/2,posTan[i*4+1]+pw/2};
            for (float x : xs) {
                for (float y : ys) {
                    posTanForTA[extraCordCounter*4] = x;
                    posTanForTA[extraCordCounter*4+1] = y;
                    extraCordCounter++;

                }
            }
        }
        float[] ta = calTA(posTanForTA,verCount);
        String[] dirs = getDirs(path);
        array("check","dirs",dirs);
        int[] areaBelong = calBelong(ta,verCount);
        Cord cord = new Cord(new float[]{path[0],path[1]});
        boolean lor = calLOR(cord.getArea());
        boolean timeZone = calTimeZone(cord.getArea());
        Log.i("DataLoader","calLOR:"+lor);
        int[] es = calEndStart(ta.length/2,areaBelong,lor);
        float[] ori = calOri(ta,es,lor);
        float[] cps = findCP(path,ori);
        boolean tob = calTOB(path);
        float[][] selected = splitRect(posTan,cps,path);//check
        bFromS.putFloatArray("areaZero",selected[0]);
        bFromS.putFloatArray("areaOne",selected[1]);
        bFromS.putFloatArray("ta",ta);
        bFromS.putFloatArray("path",path);
        bFromS.putStringArray("dirs",dirs);
        bFromS.putFloatArray("pt",posTanForTA);
        bFromS.putIntArray("areaBelong",areaBelong);
        bFromS.putFloatArray("ori",ori);
        bFromS.putBoolean("tob",tob);
        array("es",es,2);
        bFromS.putIntArray("es",es);
        bFromS.putFloatArray("cps",cps);
        bFromS.putBoolean("timeZone",timeZone);
        bFromS.putInt("verCount",verCount);
        //lor就是content写在哪里的依据,但是同样写在左边/右边,类型不同,起止ta也会有所不同
        bFromS.putBoolean("lor",lor);
        return bFromS;
    }

    private float[][] splitRect(float[] posTan,float[] cps,float[] path) {
        start();
        float[][] result = new float[2][];
        for (int i = 0;i<2;i++) {
            result[i] = select(posTan,new float[]{cps[i*4],cps[i*4+1]},
                    new float[]{cps[i*4+2],cps[i*4+3]},i == 0,path);
        }
        end();
        return result;
    }

    private float[] calOri(float[] ta,int[] es,boolean lor) {
        float ah = height/verCount;
        float endX = ta[2*es[0]+(lor? 0: 1)], staX = ta[2*es[1]+(lor? 1: 0)],
                endY = (es[0]+1)*ah, staY = es[1]*ah;
        return new float[]{endX,endY,staX,staY};
    }

    //每次只能选择一侧
    private float[] select(float[] posTan,float[] ori,float[] des,boolean which,float[] path) {
        i("starting to select>");
        array("ori",ori); array("des",des);
        boolean hor = des[0]>ori[0], ver = des[1]>ori[1];//确定起终点关系
        ArrayList<float[]> firstSelect = new ArrayList<>();
        for (int i = 0;i<posTan.length/4;i++) {//迭代posTan,同关系者添加至chosen
            boolean reHor = posTan[i*4]>ori[0], reVer = posTan[i*4+1]>ori[1];
            if (same(hor,reHor) && same(ver,reVer)) {
                firstSelect.add(new float[]{posTan[i*4],posTan[i*4+1]});
            }
        }
        //对上面过程选出来的点升序排列
        ArrayList<float[]> sorted = new ArrayList<>();
        sorted.add(new float[]{-1,-1});//初始化出起终点用以排序
        sorted.add(new float[]{width+1,height+1});
        for (int i = 0;i<firstSelect.size();i++) {
            float[] choCho = new float[]{firstSelect.get(i)[0],firstSelect.get(i)[1]};
            for (int j = 0;j<sorted.size();j++) {
                //比当前小且比前面大，插入当前位置
                if (choCho[0]<sorted.get(j)[0] && choCho[0]>sorted.get(j-1)[0]) {
                    sorted.add(j,choCho);
                }
            }
        }
        sorted.remove(0); sorted.remove(sorted.size()-1);
        //对排序后的数组选出符合"水平越靠近,垂直越靠近"的点
        ArrayList<float[]> finalSelect = new ArrayList<>();
        Paint blue = new Paint(); blue.setColor(Color.BLUE);
        Paint number = new Paint(); number.setColor(Color.WHITE);
        number.setTextSize(50);
        //参数的ArrayList的排列方式
//        int colorCounter = -1;
        for (int i = hor? sorted.size()-1: 0;hor? i>-1: i<sorted.size();i += hor? -1: 1) {
            //当前选点>/<所有迭代点就将当前点添加到result中
            float[] curCho = sorted.get(i);
            boolean notFailYet = true;
            int delta = hor? 1: -1;
            for (int j = i+delta;(hor? j<sorted.size(): j>0) && notFailYet;j += delta) {
                if (ver? curCho[1]>sorted.get(j)[1]: curCho[1]<sorted.get(j)[1]) {
                    //评估过程，一票否决
                    notFailYet = false;
                }
            }
            if (notFailYet) finalSelect.add(curCho);
        }
        float[] result = new float[(finalSelect.size()+1)*2];
        result[result.length-2] = ori[0]; result[result.length-1] = ori[1];
        for (int i = 0;i<finalSelect.size();i++) {
            result[i*2] = finalSelect.get(i)[0];
            result[i*2+1] = finalSelect.get(i)[1];
            //log出来看看
        }
        i("<");
        boolean tob = calTOB(path);
        return selectResorted(resortResult(result),height/verCount,ori,tob,which);
    }

    private float[] resortResult(float[] originResult) {
        float[] resorted = new float[originResult.length];
        for (int i = 0;i<originResult.length/2;i++) {
            int transCounter = originResult.length/2-1-i;
            resorted[i*2] = originResult[transCounter*2];
            resorted[i*2+1] = originResult[transCounter*2+1];
        }
        array("fuck","resort from selected",resorted);
        return resorted;
    }

    private float[] selectResorted(float[] sorted,int ah,float[] ori,boolean tob,boolean which) {
        //先写入ArrayList,再根据ArrayList长度创建floatArray
        ArrayList<float[]> setForACheck = new ArrayList<>();
        float lastHeight = ori[1];
        for (int i = 0;i<sorted.length/2;i++) {
            if (Math.abs(sorted[i*2+1]-lastHeight)>ah) {
                setForACheck.add(new float[]{sorted[i*2],sorted[i*2+1]});
                lastHeight = sorted[i*2+1];
            }
        }
        float[] returnResult = new float[(setForACheck.size()+1)*2];
        returnResult[0] = ori[0]; returnResult[1] = ori[1];
        for (int i = 0;i<setForACheck.size();i++) {
            returnResult[(i+1)*2] = setForACheck.get(i)[0];
            returnResult[(i+1)*2+1] = setForACheck.get(i)[1];
        }
        array("before edit",returnResult);
        i("tob",tob); i("which",which);
        if (!same(tob,which)) returnResult[returnResult.length-1] = which? height: 0;
        array("after edit",returnResult);
        return returnResult;
    }

    private float[] createPath(Cord start) {
        Cord end = new Cord(start.getArea().switchStatus());
        float[] cp = new float[2];
        float sx = start.getX(), sy = start.getY(), ex = end.getX(), ey = end.getY();
        cp[0] = sx == 0 || ex == 0? width: 0;
        cp[1] = sy == 0 || ey == 0? height: 0;
        return new float[]
                {start.getX(),start.getY(),cp[0],cp[1],end.getX(),end.getY()};
    }

    private float[] generatePath(Cord start) {
        AREA areaSta = start.getArea(), areaEnd = areaSta.switchStatus();

        Cord end = new Cord(areaEnd);
        //1.创建起终点。2.获取起终点象限。3.算出起终点余下象限。4.得到控制点。
        int[] seQuad = new int[]{areaSta.getQuadrant(),areaEnd.getQuadrant()},
                conQuad = new int[2];
        int currentEdit = 0;
        for (int i = 1;i<5;i++) {
            boolean thisIsIt = true;
            for (int j = 0;j<2;j++) {
                if (i == seQuad[j]) {
                    thisIsIt = false;
                    break;
                }
            }
            if (thisIsIt) conQuad[currentEdit++] = i;
        }
        i("conQuad",conQuad[0]+"/"+conQuad[1]);
        float[] consCord = new float[4];//用于储存生成的坐标
        for (int i = 0;i<2;i++) {//i for iterating conQuad
            //first horizontal,then vertical
            switch (conQuad[i]) {
            case 1: case 2:
                consCord[i*2] = 0; break;
            case 3: case 4:
                consCord[i*2] = width; break;
            }
            //then vertical
            switch (conQuad[i]) {
            case 1: case 4:
                consCord[i*2+1] = 0; break;
            case 2: case 3:
                consCord[i*2+1] = height; break;
            }
        }
        array("control",consCord);
        return new float[]{start.getX(),start.getY(),consCord[0],consCord[1],
                consCord[2],consCord[3],end.getX(),end.getY()};
    }

    //同步至服务器
    private void synchronizeCanvas(Bundle bundle) {
        bundle.putString("requestType","initCanvas");
        bundle.putString("email",request.getString("email"));
        bundle.putInt("si",request.getInt("si"));
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new CMT(bundle));
        es.shutdown();
    }

    private float[] initPosTan(float[] fPath,int year,String month) {
        float[] posTan = new float[]{};
        int totalDistance = 25;
        Path path = new Path();
        path.moveTo(fPath[0],fPath[1]);
        path.quadTo(fPath[2],fPath[3],fPath[4],fPath[5]);
//        path.cubicTo(fPath[2],fPath[3],fPath[4],fPath[5],fPath[6],fPath[7]);
        array("check","path",fPath);
        PathMeasure pathMeasure = new PathMeasure(path,false);
        float length = pathMeasure.getLength();
        int day_count = howManyDays(year,transMon(month));
        float stepLength = length/day_count;
        boolean sod = true;//第单数还是复数个脚印
        float[] last = {-50,-50};
        while (totalDistance<length) {
            float[] pos_t = new float[2], tan_t = new float[2];
            pathMeasure.getPosTan(totalDistance,pos_t,tan_t);
//        int delta = (int) (150+(Math.random()-0.5)*100);
            totalDistance += stepLength;
            pos_t = trans(pos_t,tan_t,sod);
            pos_t = randomizePosition(pos_t);//test for randomizing footprint position
            last = pos_t;
            sod = !sod;
            float[] singleFP = merge(pos_t,tan_t);
            posTan = merge(posTan,singleFP);
        }
        return posTan;
    }

    private float[] randomizePosition(float[] originalPosition) {
        //1.random x with origin cord radius
        //2.calculate y arrange with x above
        //3.random y in the arrange
        float x = originalPosition[0], y = originalPosition[1];
        int radius = 38;
        float randomX = (float) (x-radius+Math.random()*radius*2);
        float distanceX = Math.abs(randomX-x);
        float delta = (float) Math.sqrt((radius*radius)-distanceX*distanceX);
        float minY = y-delta, maxY = y+delta;
        float randomY = (float) (minY+Math.random()*delta*2);
        return new float[]{randomX,randomY};

    }

    private float[] trans(float[] pos,float[] tan,boolean sod) {
        float ap = -tan[0]/tan[1], bp = pos[1]-ap*pos[0];
        float a = ap*ap+1, b = 2*(ap*bp-ap*pos[1]-pos[0]),
                c = (bp-pos[1])*(bp-pos[1])+pos[0]*pos[0]-2500;
        float x1 = (float) (-b+Math.sqrt(b*b-4*a*c))/(2*a);
        float x2 = (float) (-b-Math.sqrt(b*b-4*a*c))/(2*a);
        float y1 = ap*x1+bp, y2 = ap*x2+bp;
        return new float[]{cure(sod? x1: x2,width),cure(sod? y1: y2,height)};
    }

    private float cure(float oneCor,int otherMax) {
        return Math.max(50,Math.min(oneCor,otherMax-50));
    }

    private float[] merge(float[] a,float[] b) {
        int c1 = a.length;
        int c2 = b.length;
        int c3 = c1+c2;
        float[] res = new float[c3];
        System.arraycopy(a,0,res,0,c1);
        System.arraycopy(b,0,res,c1,c2);
        return res;
    }

    private float[] getAll(Path path) {

        PathMeasure pathMeasure = new PathMeasure(path,false);
        int length = (int) pathMeasure.getLength();   //测量路径长度
        float[] positions = new float[length*4];
        for (int i = 0;i<length;i++) {
            float[] cord = new float[2];
            float[] tan = new float[2];
            if (pathMeasure.getPosTan(i,cord,tan)) {
                positions[i*2] = cord[0];
                positions[i*2+1] = cord[1];
                positions[i*4+2] = tan[0];
                positions[i*4+3] = tan[1];
            }
        }
        return positions;
    }

    private float[] calTA(float[] posTan,int verCount) {
        float[] acd = new float[verCount*2];//储存text area左右界
        for (int j = 0;j<verCount;j++) {
            acd[j*2] = width;//左边找最小值
            acd[j*2+1] = 0;//右边找最大值
        }
        int length = posTan.length/4;
        for (int i = 0;i<length;i++) {
            int a = (int) posTan[i*4+1]*verCount/height;//--->/(size[1]/10)
            a = a>=verCount? verCount-1: a;
            acd[a*2] = Math.min(posTan[i*4]-10,acd[a*2]);
            acd[a*2+1] = Math.max(posTan[i*4]+10,acd[a*2+1]);
        }
        return acd;
    }

    private int[] calEndStart(int verCount,int[] belong,boolean lor) {
        int[] es = new int[]{0,verCount};
        for (int i = 0;i<verCount;i++) {
            switch (belong[i]) {
            case 1://仅左边可用,如lor为true应将此时的i更新为es[0]的最大值,如false
                if (lor) {
                    es[0] = Math.max(es[0],i);
                } else {
                    es[1] = Math.min(es[1],i);
                }
                break;
            case 2:
                if (lor) {
                    es[1] = Math.min(es[1],i);
                } else {
                    es[0] = Math.max(es[0],i);
                }
                break;
            case 3:
                es[0] = Math.max(es[0],i);
                es[1] = Math.min(es[1],i);
                break;
            }
        }
        array("es",es);
        return es;
    }

    private float[] findCP(float[] path,float[] ori) {
        float[] check = new float[]{path[0],path[1],path[4],path[5]};
        array("selected path",check);
        float[] wp = new float[]{-1,-1}, hp = new float[]{-1,-1};
        for (int i = 0;i<2;i++) {
            //一点被确定为wp,则另一点必然为hp
            if (check[i*2] == 0 || check[i*2] == width) {
                i("which is wp",i);
                wp[0] = check[i*2]; wp[1] = check[i*2+1];
                hp[0] = check[(1-i)*2]; hp[1] = check[(1-i)*2+1];
                array("certain wp",wp); array("certain hp",hp);
                break;
            }
        }
        //我要space_tob为true的时候为top
        //所以在top的时候要怎样判断才能让space_tob为true呢
        //==height
        boolean space_tob = hp[1] == height;
        i("space_tob",space_tob);
        //lor就是对space_lor,space_tob求same
        //无论如何,返回结果中ori的位置都是确定了的
        float[] result = new float[]{ori[0],ori[1],space_tob? hp[0]: wp[0],
                space_tob? hp[1]: wp[1],ori[2],ori[3],space_tob? wp[0]: hp[0],
                space_tob? wp[1]: hp[1]};
        array("cps",result);
        return result;
    }

    private boolean calTOB(float[] path) {
        //端点在上面是true，在下面是false
        for (int i = 0;i<path.length/2;i++) {
            if (path[i*2+1] == 0) {
                return true;
            } else if (path[i*2+1] == height) {
                return false;
            }
        }
        return false;
    }

    private String[] getDirs(float[] path) {
        String[] dirs = new String[2];
        dirs[0] = calDir(new Cord(new float[]{path[0],path[1]}).getArea());
        dirs[1] = calDir(new Cord(new float[]{path[4],path[5]}).getArea());
        return dirs;
    }

    private String calDir(AREA area) {
        switch (area) {
        case ONE: case EIGHT:
            return "up";
        case TWO: case THREE:
            return "left";
        case FOUR: case FIVE:
            return "down";
        case SIX: case SEVEN:
            return "right";
        default:
            return "nah";
        }
    }

    private int[] calBelong(float[] ta,int verCount) {
        int[] belonging = new int[20];
        for (int i = 0;i<verCount;i++) {
            if (ta[i*2] == width && ta[i*2+1] == 0) {
                belonging[i] = 0;
            } else {
                boolean leftAvailable = false;
                if (ta[i*2]>width*2/5) {
                    belonging[i] = 1;
                    leftAvailable = true;
                }//左边可用
                if (width-ta[i*2+1]>width*2/5) {
                    belonging[i] = leftAvailable? 3: 2;
                }
            }
            //0.original both available
            //1.left available
            //2.right available
            //3.edited both available
        }

        for (int i = 0;i<verCount;i++) {//从上到下
            if (belonging[i] != 0) {//找出第一个不是全部放空
                for (int j = 0;j<i;j++) belonging[j] = belonging[i];
                break;
            }
        }
        for (int i = verCount-1;i>-1;i--) {//将上面过程从下到上再来一次
            if (belonging[i] != 0) {
                for (int j = 19;j>i;j--) belonging[j] = belonging[i];
                break;
            }
        }
        return belonging;
    }

    private boolean calLOR(AREA area) {
        Tool.i("calLOR para",area);
        switch (area) {
        case THREE: case FOUR: case SEVEN: case EIGHT:
            return true;
        default:
            return false;
        }
    }

    private boolean calTimeZone(AREA area) {
        switch (area) {
        case TWO: case THREE: case FIVE: case EIGHT:
            return true;
        default:
            return false;
        }
    }

    private Cord getNextStart(float[] cord) {
        i("check","getNextStart() being called","check");
        array("check","getNextStart().para",cord);
        //想要获得下一个屏幕的起点只需要知道当前屏幕的终点'
        float[] newCord = new float[2];
        newCord[0] = cord[0] == 0? width: cord[0] == width? 0: cord[0];
        newCord[1] = cord[1] == 0? height: cord[1] == height? 0: cord[1];
        return new Cord(newCord);
    }

    private int howManyDays(int year,int month) {
        switch (month) {
        case 1: case 3: case 5: case 7: case 8: case 10: case 12:
            return 31;
        case 4: case 6: case 9: case 11:
            return 30;
        case 2:
            if (year%4 == 0) {
                return 29;
            } else {
                return 28;
            }
        }
        return 0;
    }

    private static int transMon(String month) {
        switch (month) {
        case "Jan": return 1;
        case "Feb": return 2;
        case "Mar": return 3;
        case "Apr": return 4;
        case "May": return 5;
        case "Jun": return 6;
        case "Jul": return 7;
        case "Aug": return 8;
        case "Sep": return 9;
        case "Oct": return 10;
        case "Nov": return 11;
        case "Dec": return 12;
        default: return 0;
        }
    }

    private class Cord {

        float x, y;
        AREA area;

        Cord(AREA area) {
            this.area = area;
            boolean woh = area.getWoh(), qoh = area.getQoh(), oom = area.getOom();
            x = woh? oom? 0: width:
                    (float) (Math.random()/4)*width+(qoh? width/4: width/2);
            y = woh? (float) (Math.random()/4)*height+(qoh? height/4: height/2):
                    oom? 0: height;
        }

        Cord(float[] cords) {
            this.x = cords[0];
            this.y = cords[1];
            boolean qoh = (x != 0 && x != width && x<width/2) ||
                    (y != 0 && y != height && y<height/2),
                    woh = x == 0 || x == width, oom = x == 0 || y == 0;
            for (AREA temp : AREA.values()) {
                //比对出新起点在哪个AREA
                if (same(temp.getQoh(),qoh) && same(temp.getWoh(),woh)
                        && same(temp.getOom(),oom)) {
                    this.area = temp;
                }
            }

        }

        boolean bothZero() {
            return x == 0 && y == 0;
        }

        AREA getArea() {
            return this.area;
        }

        float getX() {
            return x;
        }

        float getY() {
            return y;
        }
    }

    private enum AREA {
        ONE(true,false,true),
        TWO(true,true,true),
        THREE(false,true,true),
        FOUR(true,false,false),
        FIVE(false,false,false),
        SIX(false,true,false),
        SEVEN(true,true,false),
        EIGHT(false,false,true);

        private boolean qoh, woh, oom;

        AREA(boolean qoh,boolean woh,boolean oom) {
            this.qoh = qoh; this.woh = woh; this.oom = oom;
        }

        public AREA switchStatus() {
            switch (this) {
            case ONE:
                return SIX;
            case TWO:
                return FIVE;
            case THREE:
                return EIGHT;
            case FOUR:
                return SEVEN;
            case FIVE:
                return TWO;
            case SIX:
                return ONE;
            case SEVEN:
                return FOUR;
            case EIGHT:
                return THREE;
            default:
                return THREE;
            }
        }

        public boolean getWoh() {
            return woh;
        }

        public boolean getQoh() {
            return qoh;
        }

        public boolean getOom() {
            return oom;
        }

        public int getQuadrant() {//获取该AREA所处的坐标系
            switch (this) {
            case ONE: case TWO:
                return 1;
            case THREE: case FOUR:
                return 2;
            case FIVE: case SIX:
                return 3;
            case SEVEN: case EIGHT:
                return 4;
            default: return 0;
            }
        }
    }

}
