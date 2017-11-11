package com.example.chenpeiqi.kells;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.chenpeiqi.kells.Tool.*;

/**
 * 返回所需数据，有则传递，无则创建
 * Created on 16/11/9.
 */
class DataLoader implements Callable<Bundle> {

    private static final int verCount = 30;
    private static final int pw = 40;
    private static int width, height;
    private static final int taMinWidth = 1080/3;
    private Bundle request = new Bundle();

    //Todo:所有DataLoader构造方法的意义都在于将非context数据放入requestBundle中
    DataLoader(Context context) {
        ii("#DataLoader");
        SharedPreferences sp = SP.getSP(context);
        width = sp.getInt("width",0); height = sp.getInt("height",0);
        request.putString("email",sp.getString("email","aaa"));
        request.putInt("si",sp.getInt("si",0));
        request.putInt("pi",sp.getInt("pi",-1));
        request.putString("requestType","requestCanvas");
    }

    DataLoader(Context context,float[] posTan,float[] path) {//initCanvas
        new DataLoader(context);
        request.remove("requestType");
        request.putString("requestType","initCanvas");
        request.putFloatArray("posTan",posTan);
        request.putFloatArray("path",path);
    }

    DataLoader(Context context,int pi) {
        new DataLoader(context);
        request.remove("requestType");
        request.putString("requestType","requestContent");
    }

    DataLoader(Context context,String content) {
        new DataLoader(context);
        request.remove("requestType");
        request.putString("requestType","initContent");
        request.putString("content",content);
    }

    @Override
    public Bundle call() throws Exception {
        ii("#DataLoader# call()>>");
        //Todo:send requestService
        Bundle reply = sendRequest(request);

        boolean reqCan = reply.getString("respondType").equals("requestCanvas"),
                conExi = reply.getBoolean("content_exist"),
                canExi = reply.getBoolean("canvas_exist");
        //todo:canvas_exist && content_exist > edit(reply)
        //todo:canvas_exist & !content_exist > return
        //todo:!canvas_exist > init()
        return reqCan? canExi? conExi? edit(reply): reply: edit(init()): reply;
    }

    private Bundle sendRequest(Bundle bundle) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Bundle result = executor.submit(new CMT(bundle)).get();
        executor.shutdown();
        return result;
    }

    //requestCanvas从服务器获取结果为false时产生创建结果返回上层且同步至服务器
    private Bundle init() {//here to generate path
        ii("#DataLoader# init()>>");
        Bundle output = new Bundle();
        String[] date = new Date().toString().split(" ");
        Cord start = new Cord(width,height,0,0);//chose a crush position
        float[] path = generatePath(start); array("path generated",path,2);
        float[] wholePath = addCpAndReturn(path); array("wholePath",wholePath,2);
        float[] posTan = initPosTan(wholePath,Integer.parseInt(date[5]),date[1]);
        array("posTan init",posTan,4);
        output.putFloatArray("path",path);
        output.putFloatArray("posTan",posTan);
        synchronizeCanvas(output);
        return output;
    }

    private static float[] addCpAndReturn(float[] fPath) {
        array("para",fPath,2);
        int quaCount = fPath.length/2-1;
        float[] controls = new float[quaCount*2];
        for (int i = 0;i<quaCount;i++) {
            float[][] cons = new float[][]{new float[]{fPath[i*2],fPath[i*2+3]},
                    new float[]{fPath[i*2+2],fPath[i*2+1]}};
            float[] curCon;
            curCon = notOnAxis(cons[0])? cons[0]: cons[1];
            controls[i*2] = curCon[0]; controls[i*2+1] = curCon[1];
        }
        float[] wholePath = new float[fPath.length+controls.length];
        wholePath[0] = fPath[0]; wholePath[1] = fPath[1];
        for (int i = 0;i<fPath.length/2-1;i++) {
            wholePath[i*4+2] = controls[i*2];
            wholePath[i*4+3] = controls[i*2+1];
            wholePath[i*4+4] = fPath[i*2+2];
            wholePath[i*4+5] = fPath[i*2+3];
        }
        array("result",wholePath,2);
        return wholePath;
    }

    /**
     * 从传入的两个控制点中找出不在坐标轴上的那一个
     */
    static boolean notOnAxis(float[] cord) {
        return cord[0]!=width/2 && cord[1]!=height/2;
    }

    static Path[] arrayToActual(float[] pathArrays) {
        array("path array",pathArrays,2);
        int quadCount = (pathArrays.length-2)/4;
        Path[] paths = new Path[quadCount];
        for (int i = 0;i<quadCount;i++) {
            Path current = new Path();
            current.moveTo(pathArrays[i*4],pathArrays[i*4+1]);
            current.quadTo(pathArrays[i*4+2],pathArrays[i*4+3],
                    pathArrays[i*4+4],pathArrays[i*4+5]);
            paths[i] = current;
        }
        return paths;
    }

    private Bundle edit(Bundle bFromS) {
        ii("#Dataloader edit>>");
        float[] path = bFromS.getFloatArray("path");
        bFromS.putFloatArray("path",path);
        float[] posTan = bFromS.getFloatArray("posTan");
        bFromS.putFloatArray("posTan",posTan);
        ArrayList<ArrayList<float[]>> textArea = calTA(path,posTan);
        aoaofa("ta calculated",textArea,2);
        float[] ta = listToArray(textArea);
        array("ta to put",ta,4);
        bFromS.putFloatArray("ta",ta);
        array("ta calculated",ta,4);
        bFromS.putInt("verCount",verCount);

        String[] dirs = getDirs(path);
        bFromS.putStringArray("dirs",dirs);

//        int[] belong = calBelong(ta,verCount);
//        bFromS.putIntArray("areaBelong",belong);
//        int[] es = calEndStart(ta.length/2,belong,lor);
//        bFromS.putIntArray("es",es);
//        float[] ori = calOri(ta,es,lor);
//        bFromS.putFloatArray("ori",ori);
//        float[] cps = findCP(path,ori);
//        bFromS.putFloatArray("cps",cps);
//
//        boolean tob = calTOB(path);
//        bFromS.putBoolean("tob",tob);
//        float[][] selected = splitRect(posTan,cps,path);//check
//        bFromS.putFloatArray("areaZero",selected[0]);
//        bFromS.putFloatArray("areaOne",selected[1]);
        i("bFromS to return",bFromS);
        return bFromS;
    }

    private float[] posTanX4(float[] posTan) {
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
        return posTanForTA;
    }

    private String[] getDirs(float[] path) {
        int pc = path.length;
        return new String[]{getDir(path[0],path[1]),getDir(path[pc-2],path[pc-1])};
    }

    private String getDir(float x,float y) {
        return x==0? "l": x==width? "r": y==0? "t": "b";
    }

    private float[][] splitRect(float[] posTan,float[] cps,float[] path) {
        float[][] result = new float[2][];
        for (int i = 0;i<2;i++) {
            result[i] = select(posTan,new float[]{cps[i*4],cps[i*4+1]},
                    new float[]{cps[i*4+2],cps[i*4+3]},i==0,path);
        }
        return result;
    }

    private float[] calOri(float[] ta,int[] es,boolean lor) {
        float ah = height/verCount;
        int endXCounter = 2*es[0]+(lor? 0: 1), staXCounter = 2*es[1]+(lor? 1: 0);
        float endX = ta[endXCounter], staX = ta[staXCounter],
                endY = (es[0]+1)*ah, staY = es[1]*ah;
        return new float[]{endX,endY,staX,staY};
    }

    private float[] select(float[] posTan,float[] ori,float[] des,boolean which,float[] path) {
        i("starting to select>");
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
        sorted.remove(0);
        sorted.remove(sorted.size()-1);
        //对排序后的数组选出符合"水平越靠近,垂直越靠近"的点
        ArrayList<float[]> finalSelect = new ArrayList<>();
        Paint blue = new Paint();
        blue.setColor(Color.BLUE);
        Paint number = new Paint();
        number.setColor(Color.WHITE);
        number.setTextSize(50);
        //参数的ArrayList的排列方式
//        int colorCounter = -1;
        int ss = sorted.size();
        for (int i = hor? ss-1: 0;hor? i>-1: i<ss;i += hor? -1: 1) {
            //当前选点>/<所有迭代点就将当前点添加到result中
            float[] curCho = sorted.get(i);
            boolean notFailYet = true;
            int delta = hor? 1: -1;
            for (int j = i+delta;(hor? j<ss: j>0) && notFailYet;j += delta) {
                if (ver? curCho[1]>sorted.get(j)[1]: curCho[1]<sorted.get(j)[1]) {
                    //评估过程，一票否决
                    notFailYet = false;
                }
            }
            if (notFailYet) finalSelect.add(curCho);
        }
        float[] result = new float[(finalSelect.size()+1)*2];
        result[result.length-2] = ori[0];
        result[result.length-1] = ori[1];
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
        return resorted;
    }

    private float[] selectResorted(float[] sorted,int ah,float[] ori,
            boolean tob,boolean which) {
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
        returnResult[0] = ori[0];
        returnResult[1] = ori[1];
        for (int i = 0;i<setForACheck.size();i++) {
            returnResult[(i+1)*2] = setForACheck.get(i)[0];
            returnResult[(i+1)*2+1] = setForACheck.get(i)[1];
        }
        if (!same(tob,which)) returnResult[returnResult.length-1] = which? height: 0;
        return returnResult;
    }

    //todo:getting total path array from previous end
    private float[] generatePath(Cord preEnd) {
        i("generatePath>");
        int maxCounter = Math.random()>0.66? 4: 3;
        float[] path = new float[(maxCounter+1)*2];
        Cord newSta = new Cord(width,height,preEnd);//switch from end to start
        path[0] = newSta.getX(); path[1] = newSta.getY();//storage new start
        for (int i = 0;i<maxCounter;i++) {
            i(i+">>>>>>>>>>>>>>>");
            boolean toCrush = i==maxCounter-1;
            i("toCrush?",toCrush);
//            int curQua = newSta.onEdge()? newSta.getQuad():
//                    calQuadrant(newSta,new Cord(path[(i-1)*2],path[(i-1)*2+1]));
            int curQua = newSta.getQuad();
            i("curQua",curQua);
            if (newSta.onEdge()) {//iterating each quadrant,sometimes middle
                i("onEdge()");
                //!onEdge() means it's the first quadrant init on this screen
                curQua = newSta.getQuad();
            } else {
                i("!onEdge()");
                //todo:1.find out every single sibling quadrant of current
                int[] siblingQua = newSta.getSiblingQuad();
                array("siblingQua",siblingQua,2);
                //todo:2.find out the very quadrant that preCord belongs
                Cord preCord = new Cord(width,height,path[(i-1)*2],path[(i-1)*2+1]);
                int preQua = calQuadrant(newSta,preCord);
                i("preQua",preQua);
                //todo:3.exclude (2) from (1)
                for (int j = 0;j<2;j++) {
                    if (siblingQua[j]!=preQua) {
                        curQua = siblingQua[j];
                    }
                }
                i("curQua(sibling-pre)",curQua);
            }
            Cord curEnd = new Cord(newSta,toCrush,curQua,width,height);
            i("curEndX",curEnd.getX()); i("curEndY",curEnd.getY());
            path[(i+1)*2] = curEnd.getX(); path[(i+1)*2+1] = curEnd.getY();
            newSta = curEnd;
        }
        array("path/return",path,2);
        return path;
    }

    //todo:用当前点和之前点的中点算出所在象限
    private int calQuadrant(Cord current,Cord previous) {
        float x = (current.getX()+previous.getX())/2,
                y = (current.getY()+previous.getY())/2;
        Cord middle = new Cord(width,height,x,y);
        return middle.getQuad();
    }

    boolean locSame(String[] locs) {
        boolean[] hovs = new boolean[2];
        for (int i = 0;i<locs.length;i++) {
            switch (locs[i]) {
                case "l": case "r": hovs[i] = true; break;
                case "t": case "b": hovs[i] = false; break;
            }
        }
        return same(hovs[0],hovs[1]);
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
        array("para-fPath",fPath,2); i("para-year",year); i("para_month",month);
        Path[] paths = getPaths(fPath);
        float totalLength = 0;
        float[] quaDis = new float[(fPath.length-2)/4];//each quad path's length
        i("quaDis.length(aka quadCount)",quaDis.length);
        for (int i = 0;i<quaDis.length;i++) {//whole length
            i("which quad?"+i);
            //todo:calculate path length of each quadrant then sum it together
            quaDis[i] = new PathMeasure(paths[i],false).getLength();
            i("cur dis",quaDis[i]);
            totalLength += quaDis[i];
            i("current total",totalLength);
        }
        i("total length",totalLength);
        int dayCount = howManyDays(year,transMon(month));
        i("dayCount?",dayCount);
        float stepLength = totalLength/(dayCount+1);
        float[] posTan = new float[dayCount*4];
        int curStep = 0;
        boolean sod = true;
        float steppedDis = stepLength;//?????WHY '0' error?
        for (int i = 0;i<paths.length;i++) {//iterate each path in the quad
            i("quad ordinal",i);
            while (steppedDis<quaDis[i] && curStep<dayCount) {//generate footsteps
                i("steppedDis/curQuadDis",steppedDis+"/"+quaDis[i]);
                float[] pos_t = new float[2], tan_t = new float[2];
                new PathMeasure(paths[i],false).getPosTan(steppedDis,pos_t,tan_t);
                pos_t = trans(pos_t,tan_t,sod); pos_t = randomizePosition(pos_t);
                sod = !sod;
                posTan[4*curStep] = pos_t[0]; posTan[4*curStep+1] = pos_t[1];
                posTan[4*curStep+2] = tan_t[0]; posTan[4*curStep+3] = tan_t[1];
                curStep++;
                steppedDis += stepLength;
            }
            steppedDis -= quaDis[i];//将余下距离算入下一个quadrant的起点距离
        }
        array("posTan to return",posTan,4);
        return posTan;
    }

    private Path[] getPaths(float[] fPath) {
        array("fPath(para) of getPaths()",fPath,2);
        Path[] paths = new Path[(fPath.length-2)/4];
        for (int i = 0;i<paths.length;i++) {
            Path curContour = new Path();
            curContour.moveTo(fPath[i*4],fPath[i*4+1]);
            curContour.quadTo(fPath[i*4+2],fPath[i*4+3],fPath[i*4+4],fPath[i*4+5]);
            PathMeasure pm = new PathMeasure(curContour,false);
            i("curContour.length",pm.getLength());
            paths[i] = curContour;
        }
        i("paths.length",paths.length);
        return paths;
    }

    private float[] randomizePosition(float[] originalPosition) {
        float x = originalPosition[0], y = originalPosition[1];
        int radius = 20;
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
        int c1 = a.length, c2 = b.length, c3 = c1+c2;
        float[] res = new float[c3];
        System.arraycopy(a,0,res,0,c1);
        System.arraycopy(b,0,res,c1,c2);
        return res;
    }

    private float[] getAoA(Path[] paths) {
        //todo:from path array to posTan multi
        ArrayList<float[]> storeFpOfEachQua = new ArrayList<>();
        for (int i = 0;i<paths.length;i++) {
            storeFpOfEachQua.add(getAll(paths[i]));
        }
        float[] result = posTansOnPaths(storeFpOfEachQua); array("result",result,4);
        return result;
    }

    private float[] posTansOnPaths(ArrayList<float[]> mergedQuadrant) {
        int qc = mergedQuadrant.size(), total = 0;
        for (int i = 0;i<qc;i++) {//先计算出所包含的总浮点数
            total += mergedQuadrant.get(i).length;
        }
        float[] result = new float[total];
        int previousBase = 0;
        for (int i = 0;i<qc;i++) {
            float[] curToCopy = mergedQuadrant.get(i);
            for (int j = 0;j<mergedQuadrant.get(i).length;j++) {
                result[previousBase+j] = curToCopy[j];
            }
            previousBase += curToCopy.length;
        }
        return result;
    }

    private float[] getAll(Path path) {
        //todo:chop Path of a quadrant into a lot of footprints
        PathMeasure pathMeasure = new PathMeasure(path,false);
        int length = (int) pathMeasure.getLength();   //测量路径长度
        ArrayList<float[]> positions = new ArrayList<>();
        for (int i = 0;i<length;i += 75) {
            float[] cord = new float[2];
            float[] tan = new float[2];
            if (pathMeasure.getPosTan(i,cord,tan)) {
                positions.add(merge(cord,tan));
            }
        }
        float[] result = new float[positions.size()*4];
        for (int i = 0;i<positions.size();i++) {
            System.arraycopy(positions.get(i),0,result,i*4,4);
        }
        return result;
    }

    private ArrayList<ArrayList<float[]>> calTA(float[] path,float[] posTan) {
        //1.add cp for path(float[])
        //2.transform float array into Path
        //3.get posTan every 10 pixels on Path
        //4.split posTan into left side and right side
        //5.get path bound from existing posTan on both side
        //6.reverse path bound into ta pieces
        //7.merge both side ta pieces into one
        float[] multiPosTan = oneToOne(getAoA(arrayToActual(addCpAndReturn(path))));
        float[] newPosTan = merge(multiPosTan,oneToFour(posTan));
        ArrayList<float[]> dlr = split(newPosTan);//四变二
        float[] pbl = getPB(dlr.get(0),1), pbr = getPB(dlr.get(1),2);
        ArrayList<ArrayList<float[]>> left = rPB(pbl,0,width/2),
                right = rPB(pbr,width/2,width);
        ArrayList<ArrayList<float[]>> merge = mergeTAS(left,right);
        ArrayList<ArrayList<String>> directions = calSequence(merge);
        ArrayList<ArrayList<float[]>> chosen = chooseProper(directions,merge);
        aoaofa("chosen",chosen,4);
        return chosen;
    }

    private boolean bothUoD(float[] path) {
        int quadCount = path.length/2-1, hh = height/2;
        boolean q4 = quadCount==4;
        float staY = path[1], endY = path[path.length-1];
        return q4 && ((staY>hh && endY>hh) || (staY<hh && endY<hh));
    }

    //status有三种状态:0代表全部，1代表左，2代表右
    private float[] getPB(float[] posTan,int status) {
        int max, min;
        switch (status) {
            default: max = width; min = 0; break;
            case 1: max = width/2; min = 0; break;
            case 2: max = width; min = width/2; break;
        }
        int taCount = verCount, fpCount = posTan.length/2;
        float taWH = height/taCount;
        float[] posTanBound = new float[taCount*2];//储存text area左右界
        for (int j = 0;j<taCount;j++) {
            posTanBound[j*2] = max; posTanBound[j*2+1] = min;//左边找最小值右边找最大值
        }
        for (int i = 0;i<fpCount;i++) {
            int a = (int) (posTan[i*2+1]/taWH);//用posTan的纵坐标得出verCount
            a = a>=taCount? taCount-1: a;//算出来的verCount如果刚好等于15算15
            posTanBound[a*2] = Math.min(posTan[i*2],posTanBound[a*2]);//左界取最小
            posTanBound[a*2+1] = Math.max(posTan[i*2],posTanBound[a*2+1]);//右界取最大
        }
        return posTanBound;
    }

    private float[] oneToOne(float[] posTan) {
        float[] result = new float[posTan.length/2];
        for (int i = 0;i<result.length/2;i++) {
            result[i*2] = posTan[i*4]; result[i*2+1] = posTan[i*4+1];
        }
        return result;
    }

    private float[] oneToFour(float[] posTan) {//(posTan)One To Four
        float[] result = new float[posTan.length*2];
        int r = 20;
        for (int i = 0;i<posTan.length/4;i++) {
            float[] xWithDelta = new float[]{posTan[i*4]-r,posTan[i*4]+r},
                    yWithDelta = new float[]{posTan[i*4+1]-r,posTan[i*4+1]+r};
            float[] cur = new float[8];
            for (int j = 0;j<2;j++) {
                for (int k = 0;k<2;k++) {
                    cur[j*4+k*2] = xWithDelta[j];
                    cur[j*4+k*2+1] = yWithDelta[k];
                }
            }
            System.arraycopy(cur,0,result,i*8,8);
        }
        return result;
    }

    private ArrayList<float[]> split(float[] multi) {
        ArrayList<float[]> left = new ArrayList<>(), right = new ArrayList<>();
        for (int i = 0;i<multi.length/2;i++) {
            (multi[i*2]<width/2? left: right)
                    .add(new float[]{multi[i*2],multi[i*2+1]});
        }
        aofa("left split",left,2); aofa("right split",right,2);
        ArrayList<float[]> result = new ArrayList<>();
        result.add(toArray(left)); result.add(toArray(right));
        return result;
    }

    private float[] toArray(ArrayList<float[]> origin) {
        float[] result = new float[origin.size()*2];
        for (int i = 0;i<origin.size();i++) {
            result[i*2] = origin.get(i)[0];
            result[i*2+1] = origin.get(i)[1];
        }
        return result;
    }

    private ArrayList<ArrayList<float[]>> rPB(float[] pb,float l,float r) {
        array("pb para",pb,2);
        //Todo:reverse过程中不考虑TA长度
        ArrayList<ArrayList<float[]>> reversed = new ArrayList<>();
        float vcHeight = height/verCount;
        for (int i = 0;i<pb.length/2;i++) {//遍历VC
            ArrayList<float[]> curVC = new ArrayList<>();
            float curY = (float) (i+0.66)*vcHeight;
            curVC.add(new float[]{l,curY,pb[i*2],curY});//加上左边
            if (pb[i*2]!=r && pb[i*2+1]!=l) {//不是一整块
                curVC.add(new float[]{pb[i*2+1],curY,r,curY});//加上右边
            }
            reversed.add(curVC);
        }
        return reversed;
    }

    private ArrayList<ArrayList<float[]>> mergeTAS(
            ArrayList<ArrayList<float[]>> left,ArrayList<ArrayList<float[]>> right) {
        ArrayList<ArrayList<float[]>> totalVC = new ArrayList<>();
        for (int i = 0;i<left.size();i++) {
            ArrayList<float[]> curVC;
            totalVC.add(curVC = mergeTA(left.get(i),right.get(i)));
            i("VC-"+i,curVC);
        }
        return totalVC;
    }

    //单行合并
    private ArrayList<float[]> mergeTA(
            ArrayList<float[]> left,ArrayList<float[]> right) {
        //right如果有两块，这里只加了一块
        int ls = left.size();
        float[] leftLast = left.get(ls-1), rightSta = right.get(0);//area on the left
        if (leftLast[2]==rightSta[0]) {//两个接的上
            float[] md = new float[]{leftLast[0],leftLast[1],rightSta[2],rightSta[3]};
            left.remove(ls-1); right.remove(0);
            left.add(md); left.addAll(right);
        }
        return left;
    }

    private ArrayList<ArrayList<String>> calSequence(
            ArrayList<ArrayList<float[]>> mergedTAS) {
        aoaofa("para-mergedTAS",mergedTAS,4);
        ArrayList<ArrayList<String>>
                firstRs = new ArrayList<>(), finalRs = new ArrayList<>();
        int verCounter = -1;
        for (ArrayList<float[]> mergedTA : mergedTAS) {//每层
            i("curVer:"+(++verCounter));
            ArrayList<String> curVC = new ArrayList<>();
            //Todo:先将满足长度要求的ta pieces转化成direction
            int pieceCounter = -1;
            for (float[] curPiece : mergedTA) {//每片
                i("curPiece:"+(++pieceCounter));
                String nullCheck;
                if ((nullCheck = calRelation(curPiece,true))!=null) {
                    curVC.add(nullCheck);
                }
            }
            //Todo:如果整层都没有满足长度要求的ta piece,去掉长度要求,全数添加
            if (curVC.size()==0) {
                for (float[] curPiece : mergedTA) {
                    curVC.add(calRelation(curPiece,false));
                }
            }
            firstRs.add(curVC);
        }
        aoaos("firstRs",firstRs);
        for (int i = 0;i<verCount;i++) {
            i("vc:"+i);
//            i("+----vertical ordinal:",i);
            //Todo: $last u $next -> $whole
            ArrayList<String> last = i-1>=0? firstRs.get(i-1): null,
                    next = i+1<verCount? firstRs.get(i+1): null;
            i("finalRs.size():"+finalRs.size());
            if (last==null) {
                i("last null");
            } else {
                aos("last",last);
            }
            if (next==null) {
                i("next null");
            } else {
                aos("next",next);
            }
            ArrayList<String> whole = last!=null && next!=null?
                    merges(last,next,i): last==null? next: last;
            //Todo: $whole n $current -> new $current
            i("current i:"+i);
            i("firstRs.size():"+firstRs.size());
            ArrayList<String> curPR = separates(whole,firstRs.get(i),i);
            finalRs.add(curPR);
        }
        aoaos("finalRs",finalRs);
        return finalRs;
    }

    private ArrayList<ArrayList<float[]>> chooseProper(ArrayList<ArrayList<String>> directions,ArrayList<ArrayList<float[]>> oriTA) {
        i("chooseProper>");
        ArrayList<ArrayList<float[]>> result = new ArrayList<>();
        for (int i = 0;i<directions.size();i++) {//i=>verPiece
            ArrayList<String> curDir = directions.get(i);
            ArrayList<float[]> curPie = oriTA.get(i);
            result.add(curPie.size()==curDir.size()?
                    curPie: choProper(curDir,curPie));
        }
        return result;
    }

    private ArrayList<float[]> choProper(ArrayList<String> curDir,ArrayList<float[]> curTA) {
        i("choProper>");
        ArrayList<float[]> result = new ArrayList<>();
        //Todo:find right foot step matching current direction
        for (int i = 0;i<curDir.size();i++) {
            String sinDir = curDir.get(i);
            for (int j = 0;j<curTA.size();j++) {
                float[] sinTA = curTA.get(j);
                String curTARel = calRelation(sinTA,true);
                if (curTARel!=null && curTARel.equals(sinDir)) {
                    result.add(curTA.get(j));
                    break;
                }
            }
        }
        return result;
    }

    private ArrayList<String> merges(
            ArrayList<String> last,ArrayList<String> next,int curPiece) {
        //Todo:chose result as existing objects,elements of next as to-be-append objects
        i(curPiece+">");
        ArrayList<String> result = new ArrayList<>();
        for (String aLast : last) {
            result.add(aLast);
        }
        for (String currentToAppend : next) {
            //Todo:set a flag as true
            boolean addable = true;
            //Todo:once compare object equals existing object,flag false
            for (String currentToCompare : result) {
                if (currentToAppend.equals(currentToCompare)) {
                    addable = false;
                }
                i(currentToAppend+":"+currentToCompare+">>>"+addable);
            }
            //Todo:when compare completed,if flag still true append compare object
            i(currentToAppend+":"+addable);
            if (addable) result.add(currentToAppend);
        }
        return result;
    }

    private ArrayList<String> separates(
            ArrayList<String> whole,ArrayList<String> current,int curVer) {
        //Todo:逐个迭代current，删去和R的非公共元素
        i(curVer+">"); aos("R",whole); aos("current",current);
        ArrayList<String> result = new ArrayList<>();
        for (String curToAdd : current) {
            i(">>"); i("curToAdd/current",curToAdd+"/"+current);
            //Todo:默认不添加,当等于(上下)全集中的任何一个元素时,添加
            boolean addAble = false;
            for (String currentToCompare : whole) {
                i("curToCompare/whole>",currentToCompare+"/"+whole);
                if (curToAdd.equals(currentToCompare)) {
                    i(curToAdd+" addable");
                    addAble = true;
                }
                i("<");
            }
            if (addAble) {
                result.add(curToAdd);
            }
            i("current edit",result);
            i("<<");
        }
        aos("result",result);
        return result;
    }

    private String calRelation(float[] taPiece,boolean lengthLimit) {
        boolean leftBound = taPiece[0]==0, rightBound = taPiece[2]==1080,
                valuable = taPiece[2]-taPiece[0]>width/3;
        i("valuable"+valuable);
        String inGeneral = leftBound? rightBound? "whole": "left":
                rightBound? "right": "middle";
        return lengthLimit? valuable? inGeneral: null: inGeneral;
    }

    private float[] listToArray(ArrayList<ArrayList<float[]>> taJustMerged) {
        aoaofa("ta",taJustMerged,2);
        int howMany = 0;
        for (ArrayList<float[]> curVC : taJustMerged) {
            for (float[] curPiece : curVC) {
                if (curPiece[2]-curPiece[0]>taMinWidth) {howMany++;}
            }
        }
        float[] arraylized = new float[howMany*4];
        int cur = 0;
        for (ArrayList<float[]> curVC : taJustMerged) {
            for (float[] curPiece : curVC) {
                if (curPiece[2]-curPiece[0]>taMinWidth) {
                    System.arraycopy(curPiece,0,arraylized,cur*4,4);
                    cur++;
                }
            }
        }
        return arraylized;
    }

    private ArrayList<ArrayList<float[]>> remove(ArrayList<ArrayList<float[]>> merged) {
        ArrayList<ArrayList<float[]>> result = new ArrayList<>();
        for (ArrayList<float[]> aMerged : merged) {
            ArrayList<float[]> curLineToPutIn = new ArrayList<>();
            for (float[] curPiece : aMerged) {
                if (curPiece[2]-curPiece[0]>width/3) {
                    curLineToPutIn.add(curPiece);
                }
            }
            result.add(curLineToPutIn);
        }
        return result;
    }

    float[] subPosTan(int i,float[] posTan) {
        return new float[]{posTan[i*4],posTan[i*4+1],posTan[i*4+2],posTan[i*4+3]};
    }

    private int[] calEndStart(int verCount,int[] belong,boolean lor) {
        i("belong.length",belong.length);
        i("verCount",verCount);
        i("lor",lor);
        int[] es = new int[]{0,verCount-1};
        for (int i = 0;i<verCount;i++) {
            i("current i",i);
            switch (belong[i]) {
                case 1://仅左边可用,如lor为true应将此时的i更新为es[0]的最大值,如false
                    es[lor? 0: 1] = lor? Math.max(es[0],i): Math.min(es[1],i);
                    break;
                case 2:
                    es[lor? 1: 0] = lor? Math.min(es[1],i): Math.max(es[0],i);
                    break;
                case 3:
                    es[0] = Math.max(es[0],i); es[1] = Math.min(es[1],i);
                    break;
            }
        }
        i("se/se calculated",es[0]+"/"+es[1]);
        return es;
    }

    private float[] findCP(float[] path,float[] ori) {
        int pl = path.length;
        float[] check = new float[]{path[0],path[1],path[pl-2],path[pl-1]};
        float[] wp = new float[]{-1,-1}, hp = new float[]{-1,-1};
        for (int i = 0;i<2;i++) {
            //一点被确定为wp,则另一点必然为hp
            if (check[i*2]==0 || check[i*2]==width) {
                i("which is wp",i);
                wp[0] = check[i*2]; wp[1] = check[i*2+1];
                hp[0] = check[(1-i)*2]; hp[1] = check[(1-i)*2+1];
                break;
            }
        }
        //我要space_tob为true的时候为top
        //所以在top的时候要怎样判断才能让space_tob为true呢
        //==height
        boolean space_tob = hp[1]==height;
        i("space_tob",space_tob);
        //lor就是对space_lor,space_tob求same
        //无论如何,返回结果中ori的位置都是确定了的
        float[] result = new float[]{ori[0],ori[1],space_tob? hp[0]: wp[0],
                space_tob? hp[1]: wp[1],ori[2],ori[3],space_tob? wp[0]: hp[0],
                space_tob? wp[1]: hp[1]};
        return result;
    }

    private boolean calTOB(float[] path) {
        //端点在上面是true，在下面是false
        for (int i = 0;i<path.length/2;i++) {
            if (path[i*2+1]==0) {
                return true;
            } else if (path[i*2+1]==height) {
                return false;
            }
        }
        return false;
    }

    private int[] calBelong(float[] ta,int verCount) {
        int[] belonging = new int[verCount];
        for (int i = 0;i<verCount;i++) {
            if (ta[i*2]==width && ta[i*2+1]==0) {
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
        }
        for (int i = 0;i<verCount;i++) {//从上到下
            if (belonging[i]!=0) {//找出第一个不是全部放空
                for (int j = 0;j<i;j++) belonging[j] = belonging[i];
                break;
            }
        }

        for (int i = verCount-1;i>-1;i--) {//将上面过程从下到上再来一次
            if (belonging[i]!=0) {
                for (int j = verCount-1;j>i;j--) belonging[j] = belonging[i];
                break;
            }
        }
        return belonging;
    }

    private int howManyDays(int year,int month) {
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                return year%4==0? 29: 28;
        }
        return 0;
    }

    private static int transMon(String month) {
        switch (month) {
            case "Jan": return 1; case "Feb": return 2; case "Mar": return 3;
            case "Apr": return 4; case "May": return 5; case "Jun": return 6;
            case "Jul": return 7; case "Aug": return 8; case "Sep": return 9;
            case "Oct": return 10; case "Nov": return 11; case "Dec": return 12;
            default: return 0;
        }
    }

}
