package com.example.chenpeiqi.kells;

import android.os.Bundle;
import android.util.Log;

/**
 * Created on 2017/3/12.
 */
class L {

    private final static String tag = "P";

    //float array
    static String a(float[] array) {
        String arrayInString = "";
        for (float anArray : array) {
            arrayInString += anArray+"/";
        }
        return arrayInString;
    }

//    static String ta(float[] array) {
//        String result = "";
//        int length = array.length/2;
//        for (int i = 0;i<length;i++) {
//            result += L.kv(array[i]+"",array[i+1]);
//        }
//        return result;
//    }

    static String a(int[] array) {
        String arrayInString = "";
        for (int anArray : array) {
            arrayInString += anArray+"/";
        }
        return arrayInString;
    }

    static String a(String[] array) {
        String arrayInString = "";
        for (String anArray : array) {
            arrayInString += anArray+"/";
        }
        return arrayInString;
    }

    static String a(boolean[] array) {
        String arrayInString = "";
        for (boolean anArray : array) {
            arrayInString += anArray;
        }
        return arrayInString;
    }

    //打印keyValue对
//    static String kv(String key,Object value) {
//        String left = "|"+L.s(25-key.length())+key+L.s(2);
//        String right = L.s(2)+value+L.s(30-(""+value).length())+"|\n";
//        return left+":"+right;
//    }

    static String line() {
        String temp = "+";
        for (int i = 0;i<60;i++) {
            temp += "-";
        }
        temp += "+\n";
        return temp;
    }

    static void bd(String string,Bundle bundle) {
        Log.i(string,bundle.toString());
    }

    static void t(String string) {
        Log.i("drawText",string);
    }

//    static void i(String string) {
//        StackTraceElement[] check = Thread.currentThread().getStackTrace();
//        Log.i(check[3].getMethodName(),string);
//    }

    static void s(String tips) {
        Log.i(Thread.currentThread().getStackTrace()[3].getMethodName(),tips);
    }

    static void i(String key1,Object value1,String key2,Object value2,String key3,Object value3,String key4,Object value4,String key5,Object value5) {
        String[] keys = new String[]{key1,key2,key3,key4,key5};
        String[] values = new String[]{value1.toString(),value2.toString(),value3.toString(),value4.toString(),value5.toString()};
        output(keys,values);
    }

    static void i(String key1,Object value1,String key2,Object value2,String key3,Object value3,String key4,Object value4) {
        String[] keys = new String[]{key1,key2,key3,key4};
        String[] values = new String[]{value1.toString(),value2.toString(),value3.toString(),value4.toString()};
        output(keys,values);
    }

    static void i(String key1,Object value1,String key2,Object value2,String key3,Object value3) {
        String[] keys = new String[]{key1,key2,key3};
        String[] values = new String[]{value1.toString(),value2.toString(),value3.toString()};
        output(keys,values);
    }

    static void i(String key1,Object value1,String key2,Object value2) {
        String[] keys = new String[]{key1,key2};
        String[] values = new String[]{value1.toString(),value2.toString()};
        output(keys,values);
    }

    static void i(String key1,Object value1) {
        String[] keys = new String[]{key1};
        String[] values = new String[]{value1.toString()};
        output(keys,values);
    }

    static void output(String[] keys,String[] values) {
        Log.i(Thread.currentThread().getStackTrace()[4].getMethodName(),
                kv2String(keys,values));
    }

    static String kv2String(String[] keys,String[] values) {
        int key_maxLength = 0, value_maxLength = 0;
        //遍历所有的key和value,得出各自的最大长度
        for (int i = 0;i<keys.length;i++) {
            key_maxLength = Math.max(keys[i].length(),key_maxLength);
            value_maxLength = Math.max(values[i].length(),value_maxLength);
        }
        key_maxLength += 2; value_maxLength += 2;
        //首尾两端装饰用

        String header = "+";
        for (int i = 0;i<key_maxLength+value_maxLength+3;i++) {
            header += "-";
        }
        header += "+\n";
        //插入空格,拼接,返回
        String result = header;
        for (int i = 0;i<keys.length;i++) {
            result += "|"+insertEmptySpace(keys[i].length(),key_maxLength)+keys[i]
                    +" : "
                    +values[i]+insertEmptySpace(values[i].length(),value_maxLength)
                    +"|\n";
        }
        //再加入一条header作为结尾
        result += header;
        return result;
    }

    static String insertEmptySpace(int current_length,int max_length) {
        String spaces = "";
        for (int i = 0;i<max_length-current_length;i++) {
            spaces += " ";
        }
        return spaces;
    }


}
