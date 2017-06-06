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
//                return finalEdit(bundleFromServer);
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
    private Bundle firstEdit(Bundle bFromS) {//here to generate path
        Bundle output = new Bundle();
        String[] date = new Date().toString().split(" ");
        float[] ends = request.getFloatArray("ends");
        Cord start = new Cord();//chose a crush position
        float[] path = generatePath(start);
        float[] controls = getControls(path);
        float[] wholePath = new float[path.length+controls.length];
        for (int i = 0;i<path.length/5;i++) {
            wholePath[i*7] = path[i*5];
            wholePath[i*7+1] = path[i*5+1];
            wholePath[i*7+2] = controls[i*2];
            wholePath[i*7+3] = controls[i*2+1];
            wholePath[i*7+4] = path[i*5+2];
            wholePath[i*7+5] = path[i*5+3];
            wholePath[i*7+6] = path[i*5+4];
        }
//        float[] path = generatePath(start);
        output.putFloatArray("path",wholePath);
        array("wholePath",wholePath,7);
        float[] posTan = initPosTan(wholePath,Integer.parseInt(date[5]),date[1]);
        output.putFloatArray("posTan",posTan);
        array("posTan",posTan,4);
        synchronizeCanvas(output);
//        output = finalEdit(output);
        return output;
    }

    static float[] getControls(float[] fPath) {
        //刚刚将generatePath生成部分转换成绝对坐标
        //看看这里是也转换成绝对坐标还是换回去
        float[] con = new float[fPath.length/5*2];
        array("fPath",fPath,5);
        //relations between quadrants
        int quaCount = fPath.length/5;
        i("quaCount",quaCount);
        //quadrants x relation for
        float[] allCon = new float[fPath.length/5*4],
                rightCon = new float[fPath.length/5*2];
        for (int i = 0;i<quaCount;i++) {
            i("current i",i);
            float[][] cons = new float[][]{new float[]{fPath[i*5],fPath[i*5+3]},
                    new float[]{fPath[i*5+2],fPath[i*5+1]}};
            float[] curCon;
            boolean check;
            curCon = (check = notOnAxis(cons[0],(int) fPath[i*5+4]))? cons[0]: cons[1];
            i("check",check);
            con[i*2] = curCon[0];
            con[i*2+1] = curCon[1];
        }
        return con;
    }

    /**
     * 从传入的两个控制点中找出不在坐标轴上的那一个
     */
    static boolean notOnAxis(float[] cord,int quadrant) {
        i("quadrant",quadrant);
        array("cord",cord);
        switch (quadrant) {
        case 0: return cord[0]!=width/2 && cord[1]!=height/2;
        case 1: return cord[0]!=width/2 && cord[1]!=0;
        case 2: return cord[0]!=0 && cord[1]!=0;
        case 3: return cord[0]!=0 && cord[1]!=height/2;
        }
        return false;
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

//    private Bundle finalEdit(Bundle bFromS) {
//        float[] path = bFromS.getFloatArray("path");
//        float[] posTan = bFromS.getFloatArray("posTan");
//        float[] posTanForTA = new float[posTan.length*4];
//        int extraCordCounter = 0;
//        for (int i = 0;i<posTan.length/4;i++) {
//            float[] xs = new float[]{posTan[i*4]-pw/2,posTan[i*4]+pw/2},
//                    ys = new float[]{posTan[i*4+1]-pw/2,posTan[i*4+1]+pw/2};
//            for (float x : xs) {
//                for (float y : ys) {
//                    posTanForTA[extraCordCounter*4] = x;
//                    posTanForTA[extraCordCounter*4+1] = y;
//                    extraCordCounter++;
//
//                }
//            }
//        }
//        float[] ta = calTA(posTanForTA,verCount);
//        String[] dirs = getDirs(path);
//        int[] areaBelong = calBelong(ta,verCount);
//        Cord cord = new Cord(new float[]{path[0],path[1]});
//        boolean lor = calLOR(cord.getArea());
//        Log.i("DataLoader","calLOR:"+lor);
//        int[] es = calEndStart(ta.length/2,areaBelong,lor);
//        float[] ori = calOri(ta,es,lor);
//
//        float[] cps = findCP(path,ori);
//        boolean tob = calTOB(path);
//        float[][] selected = splitRect(posTan,cps,path);//check
//        bFromS.putFloatArray("areaZero",selected[0]);
//        bFromS.putFloatArray("areaOne",selected[1]);
//        bFromS.putFloatArray("ta",ta);
//        bFromS.putFloatArray("path",path);
//        bFromS.putStringArray("dirs",dirs);
//        bFromS.putFloatArray("pt",posTanForTA);
//        bFromS.putIntArray("areaBelong",areaBelong);
//        bFromS.putFloatArray("ori",ori);
//        bFromS.putBoolean("tob",tob);
//        array("es",es,2);
//        bFromS.putIntArray("es",es);
//        bFromS.putFloatArray("cps",cps);
//        bFromS.putInt("verCount",verCount);
//        //lor就是content写在哪里的依据,但是同样写在左边/右边,类型不同,起止ta也会有所不同
//        bFromS.putBoolean("lor",lor);
//        return bFromS;
//    }

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

    //每次只能选择一侧
    private float[] select(
            float[] posTan,float[] ori,float[] des,boolean which,float[] path) {
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
        //提前随机第三个quadrant的方向指示器以确定switch-newCord执行次数
        int maxCounter = Math.random()>0.66? 4: 3;
        float[] pathReturn = new float[(maxCounter+1)*2];
        for (int i = 0;i<maxCounter;i++) {
            i(i+1+"------------------------>");
            Cord newSta = new Cord(preEnd);
            boolean toCrush = i==maxCounter-1;
            Cord curEnd = new Cord(newSta,toCrush);
            float[] endXY = curEnd.getXY();
            float[] deltas = calDeltaFromQuad(preEnd.getQua());
            pathReturn[(i+1)*2] = endXY[0]+deltas[0];
            pathReturn[(i+1)*2+1] = endXY[1]+deltas[1];
            preEnd = curEnd;
            i("<------------------------");
        }
        //got an float array that should be able to generate Path here
        //返回的时候不返回控制点，控制点到时候根据起终点自行生成
        //则path包含的子path数目为path.length/2
        i("path length",pathReturn.length);
        array("path generated",pathReturn,5);
        return pathReturn;
    }

    String[] getProperLoc(Cord start,boolean crushOrNot) {
        if (start.onEdge()) {//如果起点在屏幕边缘，起点所在quadrant就是path quadrant
            int curQuad = start.calQuad();
        } else {//否则取取起点以及起点的起点的中点用以计算quadrant

        }
        String[] result = new String[2];
        switch (curQua) {
        case 0: case 1:
            result[0] = crushOrNot? "l": "r"; break;
        default://here for case 2,case 3
            result[0] = crushOrNot? "r": "l";
        }
        switch (curQua) {
        case 0: case 3:
            result[1] = crushOrNot? "t": "b"; break;
        default:
            result[1] = crushOrNot? "b": "t";
        }
        return result;
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
        float totalLength = 0;
        float[] curQuadDis = new float[fPath.length/7];//each quad path's length
        for (int i = 0;i<paths.length;i++) {//whole length
            curQuadDis[i] = new PathMeasure(paths[i],false).getLength();
            totalLength += curQuadDis[i];
        }
        int dayCount = howManyDays(year,transMon(month));
        float stepLength = totalLength/dayCount;
        i("path length",totalLength);
        i("dayCount",dayCount);
        i("stepLength",stepLength);
        float[] posTan = new float[dayCount*4];
        int curStep = 0;
        boolean sod = true;
        float steppedDis = 0;
        for (int i = 0;i<paths.length;i++) {//try each of the quadrant
            i("currentPath/all",i+"/"+paths.length);
            i("current disTotals",curQuadDis[i]);
            while (steppedDis<curQuadDis[i] && curStep<dayCount) {
                i("steppedDis/currentTotal",steppedDis+"/"+curQuadDis[i]);
                i("curStep/day_count",curStep+"/"+dayCount);
                float[] pos_t = new float[2], tan_t = new float[2];
                new PathMeasure(paths[i],false).getPosTan(steppedDis,pos_t,tan_t);
                pos_t = trans(pos_t,tan_t,sod);
                pos_t = randomizePosition(pos_t);
                sod = !sod;
                posTan[4*curStep] = pos_t[0];
                posTan[4*curStep+1] = pos_t[1];
                posTan[4*curStep+2] = tan_t[0];
                posTan[4*curStep+3] = tan_t[1];
                curStep++;
                steppedDis += stepLength;
            }
            steppedDis -= curQuadDis[i];//将余下距离算入下一个quadrant的起点距离
        }
        return posTan;
    }

    private Path[] getPaths(float[] fPath) {
        Path[] paths = new Path[fPath.length/7];
        for (int i = 0;i<fPath.length/7;i++) {
            Path currentContour = new Path();
            float[] dxy = calDeltaFromQuad((int) fPath[i*7+6]);
            float dx = dxy[0], dy = dxy[1];
            currentContour.moveTo(fPath[i*7]+dx,fPath[i*7+1]+dy);
            currentContour.quadTo(fPath[i*7+2]+dx,fPath[i*7+3]+dy,
                    fPath[i*7+4]+dx,fPath[i*7+5]+dy);
            paths[i] = currentContour;
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
        array("belong@calEndStart",belong);
        i("verCount",verCount);
        i("lor",lor);
        int[] es = new int[]{0,verCount-1};
        for (int i = 0;i<verCount;i++) {
            i("current i",i);
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
        i("end/start calculated",es[0]+"/"+es[1]);
        return es;
    }

    private float[] findCP(float[] path,float[] ori) {
        float[] check = new float[]{path[0],path[1],path[4],path[5]};
        array("selected path",check);
        float[] wp = new float[]{-1,-1}, hp = new float[]{-1,-1};
        for (int i = 0;i<2;i++) {
            //一点被确定为wp,则另一点必然为hp
            if (check[i*2]==0 || check[i*2]==width) {
                i("which is wp",i);
                wp[0] = check[i*2];
                wp[1] = check[i*2+1];
                hp[0] = check[(1-i)*2];
                hp[1] = check[(1-i)*2+1];
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
        i("calBelong being called","check");
        array("ta",ta);
        int[] belonging = new int[verCount];
        for (int i = 0;i<verCount;i++) {
            i("first i loop",i);
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
            //0.original both available
            //1.left available
            //2.right available
            //3.edited both available
        }
        array("belonging after first loop",belonging,3);
        for (int i = 0;i<verCount;i++) {//从上到下
            i("second",i);
            if (belonging[i]!=0) {//找出第一个不是全部放空
                for (int j = 0;j<i;j++) belonging[j] = belonging[i];
                break;
            }
        }

        for (int i = verCount-1;i>-1;i--) {//将上面过程从下到上再来一次
            i("third",i);
            if (belonging[i]!=0) {
                for (int j = verCount-1;j>i;j--) belonging[j] = belonging[i];
                break;
            }
        }
        //下面log不出来？
        array("belong 2b return",belonging);
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

        /**
         * 在当前quadrant根据起点生成一个终点
         *
         * @param start   起点
         * @param quad    在哪个象限生成终点
         * @param toCrush 在该象限生成终点的方式
         */
        Cord(Cord start,boolean toCrush,int quad) {
            //first select positive cord for 'toCrush'
            String[] curCrushLoc = getProperLoc(start,toCrush);

        }

        float randomCord(float woh) {
            return (float) (woh/8+Math.random()*woh/4+(randomBoolean()? 0: woh/2));
        }

        /**
         * 判断当前点是否在屏幕边界
         */
        boolean onEdge() {return x==width || x==0 || y==height || y==0;}

        int[] getSiblingQuad(Cord cord) {//这里的cord必然是坐标轴上的点
            String loc = cord.getLocOnCor();
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

        String getLocInQua(Cord cord,int quadrant) {
            //确定坐标轴上的点相对于当前quad处在什么位置
            String l_c = getLocOnCor();
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

        String getLocOnCor() {//确定在坐标轴上的点是那个
            boolean hov = y==height/2;
            return hov? x<width/2? "l": "r": y<height/2? "t": "b";
        }

        int calQuad() {
            boolean hor = x<width/2;
            return same(hor,y<height/2)? hor? 0: 2: hor? 1: 3;
        }

        float getX() { return this.x;}

        float getY() { return this.y;}

//        //此处的构造方法用于产生当前quadrant的end Cord
//        Cord(String location,int quad,boolean CON) {//
//            i("check","quad",quad);
//            i("check","location",location);
//            i("check","CON",CON);
//            //quad:array's first para
//            //CON:chose the crush location or not,second para
//            //location:self deleted when came to the third para
//            this.quadrant = quad;//quadrant-check
//            i("check","wot's wrong?",quad);
//            String[] endLoc = cruRel[quad][CON? 0: 1];//??
//            ArrayList<String> selectedLoc = new ArrayList<>();
//            for (int i = 0;i<endLoc.length;i++) {
//                if (!location.equals(endLoc[i])) {
//                    selectedLoc.add(endLoc[i]);
//                }
//            }//now gets one end position or two
//            if (selectedLoc.size()!=1) {
//                selectedLoc.remove(Math.random()>0.5? 0: 1);
//            }//now one left
//            this.location = selectedLoc.get(0);//location check
//            generateXY(quad);
//        }

//        Cord(Cord start,Cord end) {
//            //这里的构造函数用于产生控制点
//            String startLoc = start.getLoc(), endLoc = end.getLoc();
//            String[] loc = new String[]{startLoc,endLoc};
//            for (int i = 0;i<2;i++) {
//                switch (loc[i]) {
//                case "t":
//                    this.y = height/2;
//                    break;
//                case "b":
//                    this.y = 0;
//                    break;
//                case "l":
//                    this.x = width/2;
//                    break;
//                case "r":
//                    this.x = 0;
//                    break;
//                }
//            }
//            //控制点需要quadrant用于产生正确的偏移,不需要location
//            this.quadrant = start.getQua();
//        }

//        private void generateXY(int currentQuad) {
//            int hw = width/2, hh = height/2;
//            float[] quadrantDelta = calDeltaFromQuad(currentQuad);
//            float dx = quadrantDelta[0], dy = quadrantDelta[1];
//            i("location in the quadrant",this.location);
//            i("switch-case",this.location);
//            double rc = -1;
//            switch (this.location) {
//            case "t":
//                this.x = (float) (rc = Math.random()*0.5+0.25)*hw+dx;
//                i("t.x",x);
//                this.y = dy;
//                break;
//            case "b":
//                this.x = (float) (rc = Math.random()*0.5+0.25)*hw+dx;
//                i("b.x",x);
//                this.y = hh+dy;
//                break;
//            case "l":
//                this.x = dx;
//                this.y = (float) (rc = Math.random()*0.5+0.25)*hh+dy;
//                i("l.y",y);
//                break;
//            case "r":
//                this.x = hw+dx;
//                this.y = (float) (rc = Math.random()*0.5+0.25)*hh+dy;
//                i("r.y",y);
//                break;
//            }//x,y check
//            i("check",rc);
//            i("cord generated",x+"/"+y);
//        }

//        void alter() {
//            //因为previousEnd和currentSta都是表示的同一个点
//            //且previousEnd在转换成currentSta后毫无利用价值
//            //所以在从previousEnd得到currentSta的过程中不用构造函数
//            // 而是直接对previousEnd的成员变量进行更改
//            int hw = width/2, hh = height/2;//540,960 check
//            float[] previousXY = this.getXY();
//            float px = previousXY[0], py = previousXY[1];
//            i("px&py",px+"/"+py);
//            boolean hov = px==0 || px==hw;//标示出水平坐标发生变化还是垂直坐标发生变化
//            if (hov) { //x,y-check
//                this.x = px==0? hw: 0;
//                this.y = py;
//            } else {
//                this.x = px;
//                this.y = py==0? hh: 0;
//            }
//            this.quadrant = switchQuadrant();//quadrant-check
//            this.location = switchLocation();//location-check
//        }

//        public int switchQuadrant() {
//            int pq = this.getQua();
//            float[] xy = this.getXY();
//            //如果x为0/width/(width/2)则意味着将会产生横向移动
//            boolean hov = xy[0]==0 || xy[0]==width || xy[0]==width/2;
//            switch (pq) {
//            case 0:
//                return hov? 3: 1;
//            case 1:
//                return hov? 2: 0;
//            case 2:
//                return hov? 1: 3;
//            case 3:
//                return hov? 0: 2;
//            }
//            return 0;
//        }
//
//        public String switchLocation() {
//            String previousLocation = this.getLoc();
//            String newLocation = "check";
//            switch (previousLocation) {
//            case "t":
//                return "b";
//            case "l":
//                return "r";
//            case "r":
//                return "l";
//            case "b":
//                return "t";
//            }
//            return "error";
//        }
//
//        public String getLoc() {
//            return this.location;
//        }
    }

}
