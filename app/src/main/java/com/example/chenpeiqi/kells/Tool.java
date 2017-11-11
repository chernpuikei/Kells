package com.example.chenpeiqi.kells;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created on 2017/3/12.
 */
class Tool {

    //call manual with tab/delLay default 0
    static void array(String description,float[] array,int cpg) {
        array(getDMN(1),description,array,cpg,-1);
    }

    //tag default
    static void array(String description,float[] array,int delLay,int cpg,int tab) {
        delLay++;
        array(getDMN(delLay),description,array,cpg,tab);
    }

    //origin
    //header>VC内片号,array>单片
    static void array(String tag,String header,float[] array,int cpg,int tab) {
        tab++;
        int groupCount = array.length/cpg;
        String logContent = insertTabs(tab)+"+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            logContent += insertTabs(tab)+"|(";
            for (int j = 0;j<cpg;j++) {
                logContent += array[i*cpg+j]+(j==cpg-1? ")\n": ",");
            }
        }
        Log.i(tag,logContent);
    }

    static void Array(String header,float[] array,int cpg) {
        array("check",header,array,cpg,-1);
    }

    //*****************
    //*****************
    //*****************

    //手动调用
    static void array(String description,String[] array) {
        array(getDMN(1),description,array,2,0);
    }

    //被aofa间接调用
    static void array(String description,String[] array,int delLay) {
        array(getDMN(delLay+1),description,array,2,delLay+1);
    }

    static void array(String tag,String header,String[] array,int cpg,int delLay) {
        int groupCount = array.length/cpg;
        String finalPrint = insertTabs(delLay)+"+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            //一次循环就是一行
            String currentLine = insertTabs(delLay)+"|"+i+"(";
            for (int j = 0;j<cpg;j++) {
                currentLine += array[i*cpg+j]+(j==cpg-1? ")": ",");
            }
            finalPrint += currentLine+"\n";
        }
        finalPrint += insertTabs(delLay)+"+";
        Log.i(tag,finalPrint);
    }

    static void Array(String header,String[] array,int cpg) {
        array("check",header,array,cpg,-1);
    }

    //*****************
    //*****************
    //*****************


    //call manual with tab/delLay default 0
    static void array(String description,int[] array,int cpg) {
        array(getDMN(1),description,array,cpg,-1);
    }

    //tag default
    static void array(String description,int[] array,int delLay,int cpg,int tab) {
        delLay++;
        array(getDMN(delLay),description,array,cpg,tab);
    }

    //origin
    //header>VC内片号,array>单片
    static void array(String tag,String header,int[] array,int cpg,int tab) {
        tab++;
        int groupCount = array.length/cpg;
        String logContent = insertTabs(tab)+"+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            logContent += insertTabs(tab)+"|(";
            for (int j = 0;j<cpg;j++) {
                logContent += array[i*cpg+j]+(j==cpg-1? ")\n": ",");
            }
        }
        Log.i(tag,logContent);
    }

    //*****************
    //*****************
    //*****************

    //被调用情况下，传入description代表当前VC序号，传入ArrayList表示当前VC
    static void aofa(
            String description,ArrayList<float[]> check,int delLay,int cpg) {
        delLay++;
        i(delLay,"+"+description);
        for (int i = 0;i<check.size();i++) {
            float[] curPiece = check.get(i);
            array(i+"",curPiece,delLay,cpg,0);
        }
    }

    //用于手动调用,免去填写tabLay
    static void aofa(String description,ArrayList<float[]> check,int cpg) {
        aofa(description,check,1,cpg);//-1 in absent so that it could be 0 after ++
    }

    static void aos(String description,ArrayList<String> check) {
        aos(description,check,1);
    }

    static void aos(String description,ArrayList<String> check,int delLay) {//5
        delLay++;
        aos(getDMN(delLay),description,check);
    }

    static void aos(String tag,String description,ArrayList<String> curPie) {//4
        String result = "+"+description+":";
        for (String one : curPie) {
            result += one+"/";
        }
        Log.i("stackTrace","currentTAG:"+tag);
        Log.i(tag,result);
    }

    //*****************
    //*****************
    //*****************

    static void aoaofa(String mess,ArrayList<ArrayList<float[]>> show,int cpg) {//a
        aoaofa(mess,show,1,cpg);
    }

    static void aoaofa(String mess,ArrayList<ArrayList<float[]>> show,int delLay,int cpg) {//b
        delLay++;
        aoaofa(getDMN(delLay),mess,show,delLay,cpg);
    }

    static void aoaofa(String tag,String mes,ArrayList<ArrayList<float[]>> whole,int delLay,int cpg) {
        delLay++;
        Log.i(tag,"+"+mes);
        for (int i = 0;i<whole.size();i++) {
            aofa(i+"",whole.get(i),delLay,cpg);//111111
        }
    }

    static void aoaos(String description,ArrayList<ArrayList<String>> check) {
        aoaos(description,check,1);
    }

    //no tag
    static void aoaos(String description,ArrayList<ArrayList<String>> check,int delLay) {
        delLay++;
        i(delLay,description+">");
        for (int i = 0;i<check.size();i++) {//每一个vc
            aos(i+":",check.get(i),delLay);
        }
    }

    //tag
    static void aoaos(String tag,String description,ArrayList<ArrayList<String>> check) {
        Log.i(tag,description+">");
        for (int i = 0;i<check.size();i++) {//每一个vc
            aos(tag,i+":",check.get(i));
        }
        Log.i(tag,"<");
    }

    //*****************
    //*****************
    //*****************

    static void i(int delLay,String message) {
        delLay++;
        Log.i(getDMN(delLay),message);
    }

    static void i(String message) {
        Log.i(getDMN(1),message);
    }

    static void i(String key,Object value) {
        Log.i(getDMN(1),key+" : "+value);
    }

    static void i(String tag,String key,Object value) {
        Log.i(tag,key+" : "+value);
    }

//    static void I(Object message) {
//        Log.i("check",message+"");
//    }

    static void ii(Object message){
        Log.i("bilibili",message+"");
    }

    static void ii(String key,Object value){
        Log.i("bilibili",key+":"+value);
    }

//    static void I(String key,Object value) {
//        Log.i("check",key+":"+value);dd
//    }
    //*****************
    //*****************
    //*****************

    static String getDMN(int totalDelLay) {
        return Thread.currentThread().getStackTrace()[3+totalDelLay].getMethodName();
    }

    static void showStackTrace() {
        int length = Thread.currentThread().getStackTrace().length;
        StackTraceElement[] se = Thread.currentThread().getStackTrace();
        for (int i = 0;i<length;i++) {//Todo:iterating every single element
            Log.i("stackTrace",i+":"+se[i].getMethodName());
        }
    }

    private static String arrayHeader(int layer,String description) {
        String result = "";
        for (int i = 0;i<layer*4;i++) {
            result += " ";
        }
        result += "+----"+description;
        return result;
    }

    protected static String insertTabs(int layer) {
        String result = "";
        for (int i = 0;i<layer*2;i++) {
            result += " ";
        }
        return result;
    }

    static boolean same(boolean a,boolean b) {
        return (a && b) || (!a && !b);
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
//        i("check","final inSampleSize",inSampleSize);
//        i("check","width/height calculated",w/inSampleSize+"/"+h/inSampleSize);
        return inSampleSize;
    }

    static boolean sameHOV(float[] cor1,float[] cor2) {
        return cor1[0]==cor2[0] || cor1[1]==cor2[1];
    }

    static boolean randomBoolean() {
        return Math.random()>0.5;
    }

}
