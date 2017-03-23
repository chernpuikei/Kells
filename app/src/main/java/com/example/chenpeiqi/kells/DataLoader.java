package com.example.chenpeiqi.kells;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 16/11/9.
 */
class DataLoader implements Callable<Bundle> {

    private Bundle request;
    private Context context;

    DataLoader(Context context,Bundle origin) {
        this.context = context;
        this.request = origin;
    }

    @Override
    public Bundle call() throws Exception {
        Bundle bundleFromServer = pullRequestPushResult();
        SharedPreferences sp = SP.getSP(context);
        int[] size = new int[]{sp.getInt("width",0),sp.getInt("height",0)};
        if (bundleFromServer.getString("respondType").equals("requestCanvas")) {
            if (bundleFromServer.getBoolean("result")) {
                return finalEdit(bundleFromServer,size);
            } else {
                return firstEdit(bundleFromServer,size);
            }
        }
        return bundleFromServer;
    }

    private Bundle pullRequestPushResult() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(new CMT(this.request)).get();
    }

    //requestCanvas从服务器获取结果为false时产生创建结果返回上层且同步至服务器
    private Bundle firstEdit(Bundle bFromS,int[] size) {
        Bundle output = new Bundle();
        String[] date = new Date().toString().split(" ");
        float[] ends = request.getFloatArray("ends");
        Cord start = (ends[0] == 0 && ends[1] == 0)?
                new Cord(Math.random()>0.5? AREA.ONE: AREA.TWO,size):
                getNextStart(ends,size);//<---here come the problem
        float[] path = createPath(start,size);
        output.putFloatArray("path",path);
        float[] posTan = initPosTan(path,Integer.parseInt(date[5]),date[1],size);
        output.putFloatArray("posTan",posTan);
        synchronizeCanvas(output);
        output = finalEdit(output,size);
        return output;
    }

    private Bundle finalEdit(Bundle bFromS,int[] size) {
        float[] ori = bFromS.getFloatArray("path");
        float[] posTan = bFromS.getFloatArray("posTan");
        float[] ta = calTA(posTan,size,15);
        float[] la = calTA(posTan,size,5);
//    float[] ta = calTA(getAll(p2p(ori)),size);
        String[] dirs = getDirs(ori,size);
        int[] belonging = calBelonging(ta,15,size);
//    boolean lor = calLeftRight(belonging);
        Cord cord = new Cord(new float[]{ori[0],ori[1]},size);
        boolean kind = calKind(cord.getArea());
        boolean lor = calLOR(ta,size,belonging);
        int[] conStaEnd = calTAStartEnd(ta,size,lor,kind,15);
        int[] wlcStaEnd = calTAStartEnd(la,size,!lor,kind,5);
        bFromS.putFloatArray("ta",ta);
        bFromS.putFloatArray("la",la);
        bFromS.putStringArray("dirs",dirs);
        bFromS.putIntArray("belonging",belonging);
        bFromS.putIntArray("conStaEnd",conStaEnd);
        bFromS.putIntArray("wlcStaEnd",wlcStaEnd);
        //lor就是content写在哪里的依据,但是同样写在左边/右边,类型不同,起止ta也会有所不同
        bFromS.putBoolean("lor",lor);
        return bFromS;
    }

    private Path p2p(float[] pathArray) {
        Path path = new Path();
        path.moveTo(pathArray[0],pathArray[1]);
        path.quadTo(pathArray[2],pathArray[3],pathArray[4],pathArray[5]);
        return path;
    }

    private float[] createPath(Cord start,int[] size) {
        Cord end = new Cord(start.getArea().switchStatus(),size);
        float[] cp = new float[2];
        float sx = start.getX(), sy = start.getY();
        cp[0] = sx == 0 || sx == size[0]? end.getX(): sx;
        cp[1] = sy == 0 || sy == size[1]? end.getY(): sy;
        //random way initialize CP
//    switch (start.getArea()) {
//    case ONE: case SIX:
//        cp[0] = (float) (Math.random()*size[0]/8);
//        cp[1] = (float) (size[1]*7/8+Math.random()*size[1]/8);
//        break;
//    case TWO: case FIVE:
//        cp[0] = (float) (size[0]*7/8+Math.random()*size[0]/8);
//        cp[1] = (float) Math.random()*size[1]/8;
//        break;
//    case THREE: case EIGHT:
//        cp[0] = (float) (size[0]*7/8+Math.random()*size[0]/8);
//        cp[1] = (float) (size[1]*7/8+Math.random()*size[1]/8);
//        break;
//    case FOUR: case SEVEN:
//        cp[0] = (float) Math.random()*size[0]/8;
//        cp[1] = (float) Math.random()*size[1]/8;
//        break;
//    }
        return new float[]
                {start.getX(),start.getY(),cp[0],cp[1],end.getX(),end.getY()};
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

    private float[] initPosTan(float[] fPath,int year,String month,int[] size) {
        float[] posTan = new float[]{};
        int totalDistance = 25;
        PathMeasure pathMeasure = new PathMeasure(p2p(fPath),false);
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
            pos_t = trans(pos_t,tan_t,size,sod);
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

    private float[] trans(float[] pos,float[] tan,int[] size,boolean sod) {
        float ap = -tan[0]/tan[1], bp = pos[1]-ap*pos[0];
        float a = ap*ap+1, b = 2*(ap*bp-ap*pos[1]-pos[0]),
                c = (bp-pos[1])*(bp-pos[1])+pos[0]*pos[0]-2500;
        float x1 = (float) (-b+Math.sqrt(b*b-4*a*c))/(2*a);
        float x2 = (float) (-b-Math.sqrt(b*b-4*a*c))/(2*a);
        float y1 = ap*x1+bp, y2 = ap*x2+bp;
        return new float[]{cure(sod? x1: x2,size[0]),cure(sod? y1: y2,size[1])};
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
        float[] positions = new float[length*2];
        for (int i = 0;i<length;i++) {
            float[] cord = new float[2];
            float[] tan = new float[2];
            if (pathMeasure.getPosTan(i,cord,tan)) {
                positions[i*2] = cord[0];
                positions[i*2+1] = cord[1];
            }
        }
        return positions;
    }

    private float[] calTA(float[] posTan,int[] size,int verCount) {
        float[] acd = new float[verCount*2];//储存text area左右界
        for (int j = 0;j<verCount;j++) {
            acd[j*2] = size[0];//左边找最小值
            acd[j*2+1] = 0;//右边找最大值
        }
        int length = posTan.length/4;
        for (int i = 0;i<length;i++) {
            int a = (int) posTan[i*4+1]*verCount/size[1];//--->/(size[1]/10)
            a = a>=verCount? verCount-1: a;
            acd[a*2] = Math.min(posTan[i*4]-50,acd[a*2]);
            acd[a*2+1] = Math.max(posTan[i*4]+50,acd[a*2+1]);
        }
        return acd;
    }

    private int[] calTAStartEnd(float[] ta,int[] size,boolean lor,boolean kind,int verCount) {
        int counter = ta.length/2;
        int[] startAndEnd = new int[2];
        //first area not [1080,0]
        if (XOR(lor,kind)) {
            for (int i = 0;i<counter;i++) {
                if (ta[i*2] != size[0] && ta[i*2+1] != 0) {
                    startAndEnd[0] = i;
                    break;
                }
            }
            //last area not [1080,0]
            for (int i = counter-1;i>-1;i--) {
                if (ta[i*2] != size[0] && ta[i*2+1] != 0) {
                    startAndEnd[1] = i;
                    break;
                }
            }
        } else {
            startAndEnd[0] = 0;
            startAndEnd[1] = verCount-1;
        }
        return startAndEnd;
    }

    private float[] calTATraditional(float[] AOP,int[] size,int verCount) {
        float[] acd = new float[verCount*2];//储存text area左右界
        for (int j = 0;j<verCount;j++) {
            acd[j*2] = size[0];//左边找最小值
            acd[j*2+1] = 0;//右边找最大值
        }
        int length = AOP.length/2;
        for (int i = 0;i<length;i++) {
            int a = (int) AOP[i*2+1]*20/size[1];//--->/(size[1]/10)
            a = a>=verCount? verCount: a;
            acd[a*2] = Math.min(AOP[i*2]-150,acd[a*2]);
            acd[a*2+1] = Math.max(AOP[i*2]+150,acd[a*2+1]);
        }
        return acd;
    }

    private String[] getDirs(float[] path,int[] size) {
        String[] dirs = new String[2];
        dirs[0] = calDir(new Cord(new float[]{path[0],path[1]},size).getArea());
        dirs[1] = calDir(new Cord(new float[]{path[4],path[5]},size).getArea());
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

    private int[] calBelonging(float[] ta,int verCount,int[] size) {
        int[] belonging = new int[20];
        for (int i = 0;i<verCount;i++) {
            if (ta[i*2] == size[0] && ta[i*2+1] == 0) {
                belonging[i] = 0;
            } else {
                boolean leftAvailable = false;
                if (ta[i*2]>size[0]/8) {
                    belonging[i] = 1;
                    leftAvailable = true;
                }//左边可用
                if (size[0]-ta[i*2+1]>size[0]/8) {
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
        L.i("belonging 2b return",L.a(belonging));
        return belonging;
    }

    private boolean calLeftRight(int[] belonging) {

        int oneCounter = 0, twoCounter = 0;
        for (int temp : belonging) {
            if (temp == 1) oneCounter++;
            if (temp == 2) twoCounter++;
        }
        return oneCounter>twoCounter;
    }

    private boolean calLOR(float[] ta,int[] size,int[] belonging) {
        int spaLef = 0, spaRig = 0;
        for (int i = 0;i<ta.length/2;i++) {
            //只有当一个ta的belonging是其专属或者3时才将它的长度计算在内
            if (belonging[i] == 1 || belonging[i] == 3) spaLef += ta[i*2];
            if (belonging[i] == 2 || belonging[i] == 3) spaRig += size[0]-ta[i*2+1];
        }
        return spaLef>spaRig;
    }

    private boolean calAnotherKind(AREA area){
        switch (area){
        case THREE:case FOUR:case SEVEN:case EIGHT:
            return true;
        }
        return false;
    }

    private boolean calKind(AREA area) {
        switch (area) {
        case ONE: case FOUR: case SIX: case SEVEN:
            return false;
        default:
            return true;
        }
    }

    private Cord getNextStart(float[] cord,int[] size) {
        //想要获得下一个屏幕的起点只需要知道当前屏幕的终点'
        float[] newCord = new float[2];
        newCord[0] = cord[0] == 0? size[0]: cord[0] == size[0]? 0: cord[0];
        newCord[1] = cord[1] == 0? size[1]: cord[1] == size[1]? 0: cord[1];
        return new Cord(newCord,size);
    }

    private static boolean XOR(boolean a,boolean b) {
        return (a && b) || (!a && !b);
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

        Cord(AREA area,int[] size) {
            this.area = area;
            boolean woh = area.getWoh(), qoh = area.getQoh(), oom = area.getOom();
            x = woh? oom? 0: size[0]:
                    (float) (Math.random()/4)*size[0]+(qoh? size[0]/8: size[0]*5/8);
            y = woh? (float) (Math.random()/4)*size[1]+(qoh? size[1]/8: size[1]*5/8):
                    oom? 0: size[1];
        }

        Cord(float[] cords,int[] size) {
            this.x = cords[0];
            this.y = cords[1];
            boolean qoh = (x != 0 && x != size[0] && x<size[0]/2) ||
                    (y != 0 && y != size[1] && y<size[1]/2),
                    woh = x == 0 || x == size[0],
                    oom = x == 0 || y == 0;
            for (AREA temp : AREA.values()) {
                //比对出新起点在哪个AREA
                if (XOR(temp.getQoh(),qoh) && XOR(temp.getWoh(),woh)
                        && XOR(temp.getOom(),oom)) {
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
        private static boolean whichWay = true;

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
    }

}
