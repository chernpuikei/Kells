package com.example.chenpeiqi.kells;

import android.util.Log;

/**
 * Created on 2017/3/12.
 */
class Tool {

    static void array(int[] array) {
        String arrayInString = "";
        for (int item : array) {
            arrayInString += item+"/";
        }
        Log.i(getDirectMethodName(),arrayInString);
    }

    static void array(float[] array) {
        String result = "";
        for (int i =0;i<array.length/2;i++){
            result += array[i*2]+"|"+array[i*2+1]+"\n";
        }
        Log.i(getDirectMethodName(),result);
    }

    static void array(String tag,String key,float[] array) {
        String result = "";
        for (int i =0;i<array.length/2;i++){
            result += array[i*2]+"|"+array[i*2+1]+"\n";
        }
        Log.i(tag,key+"\n"+result);
    }

    //key
    static void i(String key){
        Log.i(getDirectMethodName(),key);
    }

//    static void i(String tag,String hint){
//        Log.i(tag,hint);
//    }

    //key,value
    static void i(String key,Object value) {
        String space_k = "",space_v = "";
        for (int i = 0;i<15-key.length();i++) {
            space_k += " ";
        }
        String value_s = value.toString();
        for (int i =0;i<15-value_s.length();i++){
            space_v += " ";
        }
        Log.i(getDirectMethodName(),"  |  "+space_k+key+" : "+value+space_v+"  |");
    }

    //tag,key,value
    static void i(String tag,String key,Object value) {
        String space_k = "",space_v = "";
        for (int i = 0;i<15-key.length();i++) {
            space_k += " ";
        }
        String value_s = value.toString();
        for (int i =0;i<15-value_s.length();i++){
            space_v += " ";
        }
        Log.i(tag,"  |  "+space_k+key+" : "+value+space_v+"  |");
    }

    static void header() {
        Log.i(getDirectMethodName(),"  +------"+getDirectMethodName()+"------");
    }

    static void s(){
        String result = "  |";
        for (int i = 0;i<37;i++){
            result += "-";
        }
        Log.i(getDirectMethodName(),result+"|");
    }

    static boolean same(boolean a,boolean b) {
        return (a && b) || (!a && !b);
    }

    private static String getDirectMethodName() {
        return Thread.currentThread().getStackTrace()[4].getMethodName();
    }

}
