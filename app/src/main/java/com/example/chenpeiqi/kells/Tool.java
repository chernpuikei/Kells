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

    static void array(String header,float[] array) {
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/2;
        for (int i = 0;i<counter;i++) {
            String currentLine = "|"+i+".(";
            for (int j = 0;j<2;j++) {
                currentLine += array[i*2+j]+(j==1? ")\n": ",");
            }
            finalPrint += currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(getDirectMethodName(),finalPrint);
    }

    static void array(String header,int[] array,int countPerGroup) {
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/countPerGroup;
        for (int i = 0;i<counter;i++) {
            String currentLine = "|"+i+".(";
            for (int j = 0;j<countPerGroup;j++) {
                currentLine += array[i*countPerGroup+j]+(j==1? ")\n": ",");
            }
            finalPrint += currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(getDirectMethodName(),finalPrint);
    }

    static void array(String header,String[] array,int countPerGroup) {
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/countPerGroup;
        for (int i = 0;i<counter;i++) {
            String currentLine = "|"+i+".(";
            for (int j = 0;j<countPerGroup;j++) {
                currentLine += array[i*countPerGroup+j]+(j==1? ")\n": ",");
            }
            finalPrint += currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(getDirectMethodName(),finalPrint);
    }

    static void array(String header,int[] array) {
        array(header,array,2);
    }

    static void array(String tag,String header,float[] array) {
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/2;
        for (int i = 0;i<counter;i++) {
            String currentLine = "|"+i+".(";
            for (int j = 0;j<2;j++) {
                currentLine += array[i*2+j]+(j==1? ")\n": ",");
            }
            finalPrint += currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(tag,finalPrint);
    }

    static void array(String tag,String header,String[] array) {
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/2;
        for (int i = 0;i<counter;i++) {
            String currentLine = "|"+i+".(";
            for (int j = 0;j<2;j++) {
                currentLine += array[i*2+j]+(j==1? ")\n": ",");
            }
            finalPrint += currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(tag,finalPrint);
    }

    static void array(String tag,String header,int[] array) {
        String finalPrint = "+——————"+header+"——————\n";
        int counter = array.length/2;
        for (int i = 0;i<counter;i++) {
            String currentLine = "|"+i+".(";
            for (int j = 0;j<2;j++) {
                currentLine += array[i*2+j]+(j==1? ")\n": ",");
            }
            finalPrint += currentLine;
        }
        String end = "+";
        for (int i = 0;i<header.length()+12;i++) {
            end += "—";
        }
        finalPrint += end;
        Log.i(tag,finalPrint);
    }

    static void array(String header,float[] array,int countPerGroup) {
        int groupCount = array.length/countPerGroup;
        String finalPrint = "+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            //一次循环就是一行
            String curLine = "|"+i+"(";
            for (int j = 0;j<countPerGroup;j++) {
                curLine += array[i*countPerGroup+j]+(j==countPerGroup-1? ")": ",");
            }
            finalPrint += curLine+"\n";
        }
        finalPrint += "+";
        Log.i(getDirectMethodName(),finalPrint);
    }

    static void arrayListX2(String description,ArrayList<ArrayList<float[]>> check) {
        String result = "+"+description+"\n";
        for (int i = 0;i<check.size();i++) {//每一个vc
            result += "|"+i+":";
            ArrayList<float[]> curLine = check.get(i);
            for (int j = 0;j<curLine.size();j++) {//单个VC内的每一片
                float[] curPiece = curLine.get(j);
                for (int k = 0;k<curPiece.length;k++) {//x and y
                    result += curPiece[k]+",";
                }
                result += "\n";//同一个vc可能会含有不同的piece
            }
            result += "\n";
        }
        Log.i(getDirectMethodName(),result);
    }

    static void arrayListX1(String description,ArrayList<float[]> al2bShown) {
        String result = "+"+description+"\n";
        for (int i = 0;i<al2bShown.size();i++) {
            float[] curPiece = al2bShown.get(i);
            result += "|"+i+":";
            for (int j = 0;j<curPiece.length;j++) {
                result += curPiece[j]+",";
            }
            result += "\n";
        }
        i("check","getDirect?",getDirectMethodName());
        Log.i(getDirectMethodName(),result);
    }

    static void array(String tag,String header,float[] array,int countPG) {
        int groupCount = array.length/countPG;
        String finalPrint = "+"+header+"+\n";
        for (int i = 0;i<groupCount;i++) {
            //一次循环就是一行
            String currentLine = "|"+i+"(";
            for (int j = 0;j<countPG;j++) {
                currentLine += array[i*countPG+j]+(j==countPG-1? ")": ",");
            }
            finalPrint += currentLine+"\n";
        }
        finalPrint += "+";
        Log.i(tag,finalPrint);
    }

    static void i(String message) {
        Log.i(getDirectMethodName(),message);
    }

    static void i(String key,Object value) {
        Log.i(getDirectMethodName(),key+" : "+value);
    }

    static void i(String tag,String message) {
        Log.i(tag,message);
    }

    static void i(String tag,String key,Object value) {
        Log.i(tag,key+" : "+value);
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

    static boolean sameHOV(float[] cor1,float[] cor2) {
        return cor1[0]==cor2[0] || cor1[1]==cor2[1];
    }

    static boolean randomBoolean() {
        return Math.random()>0.5;
    }

}
