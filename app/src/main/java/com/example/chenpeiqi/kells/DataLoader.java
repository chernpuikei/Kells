package com.example.chenpeiqi.kells;

import android.content.Context;
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
 * Created on 16/11/9.
 */
class DataLoader implements Callable<Bundle> {

    private Bundle request;
    private static final int verCount = 15;
    private static final int pw = 40;
    private static int width, height;

    DataLoader(Context context,Bundle origin) {
        this.request = origin;
        width = origin.getInt("width");
        height = origin.getInt("height");
    }

    @Override
    public Bundle call() throws Exception {
        Bundle BFS = pullRequestPushResult();//BundleFromServer
        String resType = BFS.getString("respondType");
        boolean result = BFS.getBoolean("result");
        return resType.equals("requestCanvas")? result?
                plusTA(BFS): initPathAndPos(): BFS;
    }

    private Bundle pullRequestPushResult() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Bundle result = executor.submit(new CMT(this.request)).get();
        executor.shutdown();
        return result;
    }

    //requestCanvas从服务器获取结果为false时产生创建结果返回上层且同步至服务器
    private Bundle initPathAndPos() {//here to generate path
        i("drawTextFrame","initPathAndPos>");
        Bundle output = new Bundle();
        String[] date = new Date().toString().split(" ");
        Cord start = new Cord(0,0);//chose a crush position
        float[] path = generatePath(start);
        array("path generated",path,2);
        float[] controls = getControls(path);
        array("controls",controls,2);
        float[] wholePath = new float[path.length+controls.length];
        wholePath[0] = path[0]; wholePath[1] = path[1];
        for (int i = 0;i<path.length/2-1;i++) {
            wholePath[i*4+2] = controls[i*2]; wholePath[i*4+3] = controls[i*2+1];
            wholePath[i*4+4] = path[i*2+2]; wholePath[i*4+5] = path[i*2+3];
        }
        array("wholePath",wholePath,2);
        output.putFloatArray("path",path);
        float[] posTan = initPosTan(wholePath,Integer.parseInt(date[5]),date[1]);
        output.putFloatArray("posTan",posTan);
        array("posTan",posTan,4);
        synchronizeCanvas(output);
        output = plusTA(output);
        return output;
    }

    static float[] getControls(float[] fPath) {
        float[] con = new float[fPath.length-2];
        int quaCount = fPath.length/2-1;
        for (int i = 0;i<quaCount;i++) {
            float[][] cons = new float[][]{new float[]{fPath[i*2],fPath[i*2+3]},
                    new float[]{fPath[i*2+2],fPath[i*2+1]}};
            float[] curCon;
            curCon = notOnAxis(cons[0])? cons[0]: cons[1];
            con[i*2] = curCon[0]; con[i*2+1] = curCon[1];
        }
        return con;
    }

    /**
     * 从传入的两个控制点中找出不在坐标轴上的那一个
     */
    static boolean notOnAxis(float[] cord) {
        return cord[0]!=width/2 && cord[1]!=height/2;
    }

    static boolean getHOVByEnd(float endX) {
        return endX==0 || endX==width/2;
    }

    static Path arrayToActual(float[] pathArray) {
        float[] dxy = getCurrentDelta((int) pathArray[6]);
        float dx = dxy[0], dy = dxy[1];
        Path path = new Path();
        path.moveTo(pathArray[0]+dx,pathArray[1]+dy);
        path.quadTo(pathArray[2]+dx,pathArray[3]+dy,pathArray[4]+dx,pathArray[5]+dy);
        return path;
    }

    private static float[] getCurrentDelta(int quadrant) {
        float[] deltas = new float[2];
        switch (quadrant) {
        case 0:
        case 1:
            deltas[0] = 0;
            break;
        default:
            deltas[0] = width/2;
            break;
        }
        switch (quadrant) {
        case 0:
        case 3:
            deltas[1] = 0;
            break;
        default:
            deltas[1] = height/2;
            break;
        }
        return deltas;
    }

    private boolean htv(float[] subPath) {//Horizontal To Vertical
        return !(Math.abs(subPath[2]-subPath[0])==width/2 ||
                Math.abs(subPath[3]-subPath[1])==height/2);
    }

    private Bundle plusTA(Bundle bFromS) {
        i("drawTextFrame","plusTA>");
        //requestContent中的posTan为空
        float[] path = bFromS.getFloatArray("path");
        bFromS.putFloatArray("path",path);

        float[] posTan = bFromS.getFloatArray("posTan");
        bFromS.putFloatArray("pt",posTan);
        i("drawTextFrame","ta 2b put");
        float[] ta = listToArray(calTA(posTanX4(posTan),0,width,true));
        bFromS.putFloatArray("ta",ta);
        i("drawTextFrame","ta put");

        bFromS.putInt("verCount",verCount);

        String[] dirs = getDirs(path);
        bFromS.putStringArray("dirs",dirs);

        int[] belong = calBelong(ta,verCount);
        bFromS.putIntArray("areaBelong",belong);
//        boolean lor = Math.random()>0.5;
//        bFromS.putBoolean("lor",lor);
//
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
        i("drawTextFrame","<plusTA");
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
        start();
        float[][] result = new float[2][];
        for (int i = 0;i<2;i++) {
            result[i] = select(posTan,new float[]{cps[i*4],cps[i*4+1]},
                    new float[]{cps[i*4+2],cps[i*4+3]},i==0,path);
        }
        end();
        return result;
    }

    private float[] calOri(float[] ta,int[] es,boolean lor) {
        array("ta",ta);
        array("es",es);
        i("lor",lor);
        i("ta.length",ta.length);
        float ah = height/verCount;
        i("check","es[1]",es[1]);
        int endXCounter = 2*es[0]+(lor? 0: 1), staXCounter = 2*es[1]+(lor? 1: 0);
        i("check","endXCounter/staXCounter",endXCounter+"/"+staXCounter);
        float endX = ta[endXCounter], staX = ta[staXCounter],
                endY = (es[0]+1)*ah, staY = es[1]*ah;
        return new float[]{endX,endY,staX,staY};
    }

    private float[] select(float[] posTan,float[] ori,float[] des,boolean which,float[] path) {
        i("starting to select>");
        array("ori",ori);
        array("des",des);
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
        array("fuck","resort from selected",resorted);
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
        array("before edit",returnResult);
        i("tob",tob);
        i("which",which);
        if (!same(tob,which)) returnResult[returnResult.length-1] = which? height: 0;
        array("after edit",returnResult);
        return returnResult;
    }

    private float[] generatePath(Cord preEnd) {
        int maxCounter = Math.random()>0.66? 4: 3;
        float[] path = new float[(maxCounter+1)*2];
        Cord newSta = new Cord(preEnd);
        path[0] = newSta.getX(); path[1] = newSta.getY();
        for (int i = 0;i<maxCounter;i++) {
            i(i+">>>>>>>>>>>>>>>>>>>");
            i("sta","("+newSta.getX()+","+newSta.getY()+")");
            boolean toCrush = i==maxCounter-1;
            i("onEdge",newSta.onEdge());
//            int curQua = newSta.onEdge()? newSta.getQuad():
//                    calQuadrant(newSta,new Cord(path[(i-1)*2],path[(i-1)*2+1]));
            int curQua = 0;
            if (newSta.onEdge()) {
                curQua = newSta.getQuad();
                i("newSta.getQuad()",newSta.getQuad());
            } else {
                int preQua = calQuadrant(
                        newSta,new Cord(path[(i-1)*2],path[(i-1)*2+1]));
                i("preQua",preQua);
                int[] siblingQua = newSta.getSiblingQuad();
                array("siblingQua",siblingQua,2);
                for (int j = 0;j<2;j++) {
                    if (siblingQua[j]!=preQua) {
                        curQua = siblingQua[j];
                    }
                }
                i("inner curQua",curQua);
            }
            i("curQua",curQua);
            Cord curEnd = new Cord(newSta,toCrush,curQua);
            i("end","("+curEnd.getX()+","+curEnd.getY()+")");
            path[(i+1)*2] = curEnd.getX(); path[(i+1)*2+1] = curEnd.getY();
            newSta = curEnd;
        }
        i("<<<<<<<<<<<<<<<<<<<<<");
        array("path generated",path,2);
        return path;
    }

    private int calQuadrant(Cord current,Cord previous) {
        float x = (current.getX()+previous.getX())/2,
                y = (current.getY()+previous.getY())/2;
        Cord middle = new Cord(x,y);
        return middle.getQuad();
    }

    boolean locSame(String[] locs) {
        boolean[] hovs = new boolean[2];
        for (int i = 0;i<locs.length;i++) {
            switch (locs[i]) {
            case "l":
            case "r":
                hovs[i] = true;
                break;
            case "t":
            case "b":
                hovs[i] = false;
                break;
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
        Path[] paths = getPaths(fPath);
        array("fPath",fPath,2);
        float totalLength = 0;
        float[] curQuadDis = new float[(fPath.length-2)/4];//each quad path's length
        for (int i = 0;i<paths.length;i++) {//whole length
            curQuadDis[i] = new PathMeasure(paths[i],false).getLength();
            totalLength += curQuadDis[i];
        }
        int dayCount = howManyDays(year,transMon(month));
        float stepLength = totalLength/(dayCount+1);
        i("path length",totalLength);
        i("dayCount",dayCount);
        i("stepLength",stepLength);
        float[] posTan = new float[dayCount*4];
        int curStep = 0;
        boolean sod = true;
        float steppedDis = stepLength;//?????WHY '0' error?
        for (int i = 0;i<paths.length;i++) {//try each of the quadrant
            i("currentPath/all",i+"/"+paths.length);
            i("current disTotals",curQuadDis[i]);
            while (steppedDis<curQuadDis[i] && curStep<dayCount) {
                i("steppedDis/currentTotal",steppedDis+"/"+curQuadDis[i]);
                i("curStep/day_count",curStep+"/"+dayCount);
                float[] pos_t = new float[2], tan_t = new float[2];
                new PathMeasure(paths[i],false).getPosTan(steppedDis,pos_t,tan_t);
                pos_t = trans(pos_t,tan_t,sod); pos_t = randomizePosition(pos_t);
                sod = !sod;
                posTan[4*curStep] = pos_t[0]; posTan[4*curStep+1] = pos_t[1];
                posTan[4*curStep+2] = tan_t[0]; posTan[4*curStep+3] = tan_t[1];
                curStep++;
                steppedDis += stepLength;
                i("inner steppedDis",steppedDis);
            }
            steppedDis -= curQuadDis[i];//将余下距离算入下一个quadrant的起点距离
            i("outer stepped dis",steppedDis);
        }
        array("posTan",posTan,4);
        return posTan;
    }

    private Path[] getPaths(float[] fPath) {
        Path[] paths = new Path[(fPath.length-2)/4];
        for (int i = 0;i<paths.length;i++) {
            Path curContour = new Path();
            curContour.moveTo(fPath[i*4],fPath[i*4+1]);
            curContour.quadTo(fPath[i*4+2],fPath[i*4+3],fPath[i*4+4],fPath[i*4+5]);
            paths[i] = curContour;
        }
        return paths;
    }

    private float[] calDeltaFromQuad(int quadrant) {
        int deltaX = 0, deltaY = 0;
        switch (quadrant) {
        case 1:
            deltaY = height/2;
            break;
        case 2:
            deltaX = width/2;
            deltaY = height/2;
            break;
        case 3:
            deltaX = width/2;
            break;
        }
        return new float[]{deltaX,deltaY};
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

    /**
     * 原理：遍历posTan，根据当前fp的纵坐标确定所属verCount然后和当前最大最小值比对
     * 小于最小值则更新最小值，大于最大值则更新最大值,
     * 对于占用四个quadrant的path要左右分开算然后合并
     */
    private ArrayList<ArrayList<float[]>> calTA(float[] posTan,float l,float r,boolean split) {
        ArrayList result;
        //目前根据traQuaCount的不同返回的不是同一样东西
        if (split) {
            float[] multiPosTan = multiPosTan(posTan);//一变四
            ArrayList<float[]> dlr = deliverLeftRight(multiPosTan);//DeliverLeftRight
            float[] tal = getPathBound(dlr.get(0),0,width/2),
                    tar = getPathBound(dlr.get(1),width/2,width);//算出左右TA

            ArrayList left = rTA(tal,0,width/2), right = rTA(tar,width/2,width);
            arrayListX2("left",left); arrayListX2("right",right);
            result = mergeTAS(left,right);
        } else {
            float[] pathBound = getPathBound(posTan,0,width);
            result = rTA(pathBound,l,r);
        }
        arrayListX2("calTA result",result);
        return result;
    }

    private float[] getPathBound(float[] posTan,float l,float r) {
        float[] acd = new float[verCount*2];//储存text area左右界
        for (int j = 0;j<verCount;j++) {
            acd[j*2] = r; acd[j*2+1] = l;//左边找最小值右边找最大值
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

    private float[] multiPosTan(float[] posTan) {
        float[] result = new float[posTan.length*2];
        for (int i = 0;i<posTan.length/4;i++) {
            float[] xWithDelta = new float[]{posTan[i*4]-5,posTan[i*4]+5},
                    yWithDelta = new float[]{posTan[i*4+1]-5,posTan[i*4+1]+5};
            float[] cur = new float[8];
            for (int j = 0;j<2;j++) {
                for (int k = 0;k<2;k++) {
                    cur[j*4+k*2] = xWithDelta[j]; cur[j*4+k*2+1] = yWithDelta[k];
                }
            }
            System.arraycopy(cur,0,result,i*8,8);
        }
        return result;
    }

    private ArrayList<float[]> deliverLeftRight(float[] multi) {
        ArrayList<float[]> left = new ArrayList<>(), right = new ArrayList<>();
        for (int i = 0;i<multi.length/2;i++) {
            (multi[i*2]<width/2? left: right)
                    .add(new float[]{multi[i*2],multi[i*2+1]});
        }
        ArrayList<float[]> result = new ArrayList<>();
        result.add(arrayListToArray(left)); result.add(arrayListToArray(right));
        return result;
    }

    private float[] arrayListToArray(ArrayList<float[]> origin) {
        float[] result = new float[origin.size()*2];
        for (int i = 0;i<origin.size();i++) {
            result[i*2] = origin.get(i)[0]; result[i*2+1] = origin.get(i)[1];
        }
        return result;
    }

    private ArrayList<ArrayList<float[]>> rTA(float[] ta,float l,float r) {
        array("ta 2b r",ta,2); i("l",l); i("r",r);
        ArrayList<ArrayList<float[]>> reversed = new ArrayList<>();
        float vcHeight = height/verCount;
        for (int i = 0;i<ta.length/2;i++) {
            ArrayList<float[]> curVC = new ArrayList<>();
            float curY = (float) (i+0.66)*vcHeight;
            curVC.add(new float[]{l,curY,ta[i*2],curY});//未加Y坐标
            if (ta[i*2+1]!=l && r!=ta[i*2]) {
                curVC.add(new float[]{ta[i*2+1],curY,r,curY});
            }
            reversed.add(curVC);
        }
        arrayListX2("Red",reversed);
        return reversed;
    }

    private ArrayList<ArrayList<float[]>> mergeTAS(
            ArrayList<ArrayList<float[]>> left,ArrayList<ArrayList<float[]>> right) {
        ArrayList<ArrayList<float[]>> totalVC = new ArrayList<>();
        for (int i = 0;i<left.size();i++) {//left.size(),right.size()都是15
            ArrayList<float[]> curVC = mergeTA(left.get(i),right.get(i));
            totalVC.add(curVC);
            i("current vc",i);
            arrayListX1("left",left.get(i));
            arrayListX1("right",right.get(i));
            arrayListX1("total",curVC);
        }
        arrayListX2("total",totalVC);
        return totalVC;
    }

    //单行合并
    private ArrayList<float[]> mergeTA(ArrayList<float[]> left,ArrayList<float[]> right) {
        int ls = left.size();
        float[] leftLast = left.get(ls-1), rightSta = right.get(0);
        if (leftLast[2]==rightSta[0]) {
            float[] md = new float[]{leftLast[0],leftLast[1],rightSta[2],rightSta[3]};
            left.remove(ls-1); right.remove(0);
            left.add(md); left.addAll(right);
        }
        return left;
    }

    private float[] listToArray(ArrayList<ArrayList<float[]>> taJustMerged) {
        arrayListX2("taJustMerged",taJustMerged);
        int howMany = 0;
        for (int i = 0;i<taJustMerged.size();i++) {
            i("curVC",i);
            ArrayList<float[]> curVC = taJustMerged.get(i);
            for (int j = 0;j<curVC.size();j++) {
                float[] curPiece = curVC.get(j);
                if (curPiece[2]-curPiece[0]>width/3) {
                    howMany++;
                }
            }
        }
        float[] arraylized = new float[howMany*4];
        i("how many ta piece",howMany);
        int cur = 0;
        for (int i = 0;i<taJustMerged.size();i++) {
            ArrayList<float[]> curVC = taJustMerged.get(i);
            for (int j = 0;j<curVC.size();j++) {
                float[] curPiece = curVC.get(j);
                if (curPiece[2]-curPiece[0]>width/3) {
                    System.arraycopy(curPiece,0,arraylized,cur*4,4);
                    cur++;
                }
            }
        }
        array("listToArray result",arraylized,4);
        return arraylized;
    }

    private ArrayList<ArrayList<float[]>> remove(ArrayList<ArrayList<float[]>> merged) {
        ArrayList<ArrayList<float[]>> result = new ArrayList<>();
        for (int i = 0;i<merged.size();i++) {
            ArrayList<float[]> curLineToGetFrom = merged.get(i),
                    curLineToPutIn = new ArrayList<>();
            for (int j = 0;j<curLineToGetFrom.size();j++) {
                float[] curPiece = curLineToGetFrom.get(j);
                if (curPiece[2]-curPiece[0]>width/3) {
                    curLineToPutIn.add(curPiece);
                }
            }
            result.add(curLineToPutIn);
        }
        return result;
    }

    private ArrayList<ArrayList<String>> calRelations(ArrayList<ArrayList<float[]>> list) {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (int i = 0;i<list.size();i++) {
            ArrayList<String> curLine = new ArrayList<>();
            ArrayList<float[]> restorePara;
            for (int j = 0;j<(restorePara = list.get(i)).size();j++) {
                curLine.add(calBelong(restorePara.get(j)));
            }
            result.add(curLine);
        }
        return result;
    }

//    private ArrayList<ArrayList<String>> calLMR(ArrayList<ArrayList<String>> relations) {
//        String startOn, endOn;
//        for (int i = 0;i<relations.size();i++) {
//            String cf = relations.get(i).get(0);//current first
//            if (!cf.equals("whole")) {
//                startOn = cf; break;
//            }
//        }
//        switch (startOn){
//        case "left": endOn = "right";break;
//        case"right":startOn = "left";break;
//        }
//    }

    private String calBelong(float[] taPiece) {
        boolean leftBound = taPiece[0]==0, rightBound = taPiece[2]==1080;
        return leftBound? rightBound? "whole": "left": rightBound? "right": "middle";
    }

    float[] subPosTan(int i,float[] posTan) {
        return new float[]{posTan[i*4],posTan[i*4+1],posTan[i*4+2],posTan[i*4+3]};
    }

    private int[] calEndStart(int verCount,int[] belong,boolean lor) {
        i("belong.length",belong.length);
        array("belong",belong);
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
        i("end/start calculated",es[0]+"/"+es[1]);
        return es;
    }

    private float[] findCP(float[] path,float[] ori) {
        int pl = path.length;
        float[] check = new float[]{path[0],path[1],path[pl-2],path[pl-1]};
        array("selected path",check);
        float[] wp = new float[]{-1,-1}, hp = new float[]{-1,-1};
        for (int i = 0;i<2;i++) {
            //一点被确定为wp,则另一点必然为hp
            if (check[i*2]==0 || check[i*2]==width) {
                i("which is wp",i);
                wp[0] = check[i*2]; wp[1] = check[i*2+1];
                hp[0] = check[(1-i)*2]; hp[1] = check[(1-i)*2+1];
                array("certain wp",wp);
                array("certain hp",hp);
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
        array("cps",result);
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
        array("ta",ta,4);
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
        //下面log不出来？
        array("belonging",belonging,2);
        return belonging;
    }

    private int howManyDays(int year,int month) {
        switch (month) {
        case 1:
        case 3:
        case 5:
        case 7:
        case 8:
        case 10:
        case 12:
            return 31;
        case 4:
        case 6:
        case 9:
        case 11:
            return 30;
        case 2:
            if (year%4==0) {
                return 29;
            } else {
                return 28;
            }
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

    private class Cord {

        float x, y;

        Cord(float x,float y) {
            this.x = x; this.y = y;
        }

        /**
         * 获取当前屏幕的起点
         */
        Cord(Cord previous) {
            float px = previous.getX(), py = previous.getY();
            if (px==0 && py==0) {
                boolean hov = randomBoolean();
                this.x = hov? randomBoolean()? 0: width: randomCord(width);
                this.y = !hov? randomBoolean()? 0: height: randomCord(height);
            } else {
                boolean changeOnX = px==width || px==0;
                if (changeOnX) {
                    this.x = px==width? 0: width;
                    this.y = py;
                } else {
                    this.x = px;
                    this.y = py==height? 0: height;
                }
            }
        }

        Cord(Cord start,boolean crushOrNot,int curQuad) {
            //所有终点生成区间
            i("para.start",start.getX()+","+start.getY());
            i("para.crushOrNot",crushOrNot);
            i("para.curQuad",curQuad);
            String[] AL = new String[2];//All Locations
            switch (curQuad) {
            case 0: case 1:
                AL[0] = crushOrNot? "l": "r"; break;
            default://here for case 2,case 3
                AL[0] = crushOrNot? "r": "l";
            }
            switch (curQuad) {
            case 0: case 3:
                AL[1] = crushOrNot? "t": "b"; break;
            default:
                AL[1] = crushOrNot? "b": "t";
            }
            array("AL",AL,2);
            String selfLoc = start.getLocInQua(curQuad);
            i("self",selfLoc);
            ArrayList<String> asd = new ArrayList<>();//Possible After Self Deleted
            for (int i = 0;i<2;i++) {
                if (!AL[i].equals(selfLoc)) {
                    asd.add(AL[i]);
                }
            }
            String finalLoc = asd.get(asd.size()==2? (int) (Math.random()/0.5): 0);
            i("finalLoc",finalLoc);
            //还没有生成坐标
            //loc X quad=>(x,y)
            float[] deltas = calDeltaFromQuad(curQuad);
            float dx = deltas[0], dy = deltas[1];
            i("dx",dx); i("dy",dy);
            switch (finalLoc) {
            case "l": this.x = dx; break;
            case "r": this.x = width/2+dx; break;
            default: this.x = (float) (Math.random()*width/4+width/8+dx);
            }
            switch (finalLoc) {
            case "t": this.y = dy; break;
            case "b": this.y = height/2+dy; break;
            default: this.y = (float) (Math.random()*height/4+height/8+dy);
            }
            i("init","("+x+","+y+")");
        }

        float randomCord(float woh) {
            return (float) (woh/8+Math.random()*woh/4+(randomBoolean()? 0: woh/2));
        }

        boolean onEdge() {return x==width || x==0 || y==height || y==0;}

        int[] getSiblingQuad() {//这里的cord必然是坐标轴上的点
            String loc = this.getLocOnAxis();
            int[] result = new int[2];
            switch (loc) {
            case "t": case "l":
                result[0] = 0; break;
            default:
                result[0] = 2;
            }
            switch (loc) {
            case "l": case "b":
                result[1] = 1; break;
            default:
                result[1] = 3;
            }
            return result;
        }

        String getLocInQua(int quadrant) {
            //确定坐标轴上的点相对于当前quad处在什么位置
            String l_c = getLocOnAxis();
            switch (l_c) {
            case "t":
                return quadrant==0? "r": "l";
            case "b":
                return quadrant==1? "r": "l";
            case "l":
                return quadrant==0? "b": "t";
            default:
                return quadrant==3? "b": "t";
            }
        }

        String getLocOnAxis() {//确定在坐标轴上的点是那个
            boolean hov = y==height/2;
            return hov? x<width/2? "l": "r": y<height/2? "t": "b";
        }

        int getQuad() {
            boolean hor = x<width/2;
            return same(hor,y<height/2)? hor? 0: 2: hor? 1: 3;
        }

        float getX() { return this.x;}

        float getY() { return this.y;}
    }

}
