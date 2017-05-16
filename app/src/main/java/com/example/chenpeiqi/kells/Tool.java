package com.example.chenpeiqi.kells;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created on 2017/3/12.
 */
class Tool {

    static void array(String header,float[] array) {
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/2;
        for (int i=0;i<counter;i++){
            String currentLine = "|"+i+".(";
            for(int j=0;j<2;j++){
                currentLine+=array[i*2+j]+(j==1?")\n":",");
            }
            finalPrint+=currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(getDirectMethodName(),finalPrint);
    }

    static void array(String header,int[] array) {
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/2;
        for (int i=0;i<counter;i++){
            String currentLine = "|"+i+".(";
            for(int j=0;j<2;j++){
                currentLine+=array[i*2+j]+(j==1?")\n":",");
            }
            finalPrint+=currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(getDirectMethodName(),finalPrint);
    }

    static void array(String tag,String header,float[] array){
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/2;
        for (int i=0;i<counter;i++){
            String currentLine = "|"+i+".(";
            for(int j=0;j<2;j++){
                currentLine+=array[i*2+j]+(j==1?")\n":",");
            }
            finalPrint+=currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(tag,finalPrint);
    }

    static void array(String tag,String header,String[] array){
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/2;
        for (int i=0;i<counter;i++){
            String currentLine = "|"+i+".(";
            for(int j=0;j<2;j++){
                currentLine+=array[i*2+j]+(j==1?")\n":",");
            }
            finalPrint+=currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(tag,finalPrint);
    }

    static void check(String key,Object value){
        Log.i("check",key+":"+value);
    }

    static void array(String header,int[] array,int countPerGroup) {
        int groupCount = array.length/countPerGroup;
        String finalPrint = "+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            //一次循环就是一行
            String currentLine = "|"+i+"(";
            for (int j = 0;j<countPerGroup;j++) {
                currentLine += array[i*groupCount+j]+(j==countPerGroup-1?")":",");
            }
            finalPrint += currentLine+"\n";
        }
        finalPrint += "+";
        Log.i(getDirectMethodName(),finalPrint);
    }

    static void array(String header,float[] array,int countPerGroup) {
        int groupCount = array.length/countPerGroup;
        String finalPrint = "+"+header+"+\n";
        i("check","group count calculated",groupCount);
        for (int i = 0;i<groupCount;i++) {
            i("check","current i",i);
            //一次循环就是一行
            String currentLine = "|"+i+"(";
            for (int j = 0;j<countPerGroup;j++) {
                i("check","current j",j);
                i("check","currently iterating",i*groupCount+j);
                currentLine += array[i*groupCount+j]+(j==countPerGroup-1?")":",");
            }
            finalPrint += currentLine+"\n";
        }
        finalPrint += "+";
        i("check","getDMN",getDirectMethodName());
        Log.i(getDirectMethodName(),finalPrint);
    }

    //key
    static void i(String key) {
        Log.i(getDirectMethodName(),key);
    }

    //key,value
    static void i(String key,Object value) {
        Log.i(getDirectMethodName(),key+" : "+value);
    }

    //tag,key,value
    static void i(String tag,String key,Object value) {
        Log.i(tag,key+" : "+value);
    }

    static void d(String tag,Object value){
        Log.i("Draw",tag+" : "+value);
    }

    static void l(String tag,Object value){
        Log.i("DataLoader",tag+" : "+value);
    }

    static void start() {
        Log.i(getDirectMethodName(),getDirectMethodName()+">");
    }

    static void end() {
        Log.i(getDirectMethodName(),"<");
    }

    static boolean same(boolean a,boolean b) {
        return (a && b) || (!a && !b);
    }

    private static String getDirectMethodName() {
        return Thread.currentThread().getStackTrace()[4].getMethodName();
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

}
