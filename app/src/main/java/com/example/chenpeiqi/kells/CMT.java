package com.example.chenpeiqi.kells;

import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created on 16/3/25.
 */
class CMT implements Callable<Bundle> {

    private final static String tag = "CMT";
    private Bundle bundle;

    CMT(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Bundle call() {
        Bundle bundle = new Bundle();
        Socket socket;
        try {
//        socket = new Socket("192.168.1.110",20000);
//        socket = new Socket("192.168.43.182",20000);
            socket = new Socket("10.0.3.2",20000);

            bundle = request(socket);//genymotion
        } catch(Exception e) {
            e.printStackTrace();
        }
        return bundle;
    }

    private Bundle request(Socket socket) throws Exception {
        //发送数据部分
        JSONObject requestJson = new JSONObject();
        String requestType = bundle.getString("requestType");
        String email = "aaa";
        requestJson.put("email",email).put("requestType",requestType);
        //非必选部分,在构造期间加入con_i以表明使用哪个构造器
        //再用switch-case语句用con_i指明哪些东西需要放入requestJson,
        //从而用con_i作为中间变量在总体上实现构造器-请求结构的闭环
        switch (requestType) {
        case "login":
            requestJson.put("password",bundle.get("password"));
            break;
        case "register":
            requestJson.put("password",bundle.get("password"))
                    .put("nickname",bundle.get("nickname"))
                    .put("self_intro",bundle.get("selfIntro"));
            break;
        case "requestCanvas":
            requestJson.put("email",bundle.getString("email"))
                    .put("si",bundle.getInt("si"));
            break;
        case "initCanvas":
            JSONArray ja_path = new JSONArray(), ja_fp = new JSONArray();
            for (float path_item : bundle.getFloatArray("path"))
                ja_path.put(path_item);
            for (float fp_item : bundle.getFloatArray("posTan")) {
                ja_fp.put(fp_item);
            }
            requestJson.put("si",bundle.getInt("si")).put("path",ja_path)
                    .put("posTan",ja_fp);
            break;
        case "requestContent":
            requestJson.put("si",bundle.getInt("s_i")).put("pi",bundle.getInt("pi"));
            break;
        case "initContent":
            requestJson.put("si",bundle.getInt("si")).put("pi",bundle.getInt("pi"))
                    .put("content",bundle.getString("content"))
                    .put("province",bundle.getString("province"))
                    .put("city",bundle.getString("city"));
            break;
        case "checkWidthHeight":
            requestJson.put("width",bundle.getInt("width"))
                    .put("height",bundle.getInt("height"));
            break;
        }
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
        Log.i(tag,"——>requestJson:"+requestJson);
        bw.write(requestJson+"");
        bw.flush();

        //读取回复部分
        Bundle bundle = new Bundle();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        String temp = br.readLine();
        JSONObject rJOJ = new JSONObject(temp);
        String rsp = rJOJ.getString("respondType"),
                rsr = rJOJ.has("result")? rJOJ.getString("result"): "null";
        if (temp != null) {
            Log.i(tag,"<——receiving JSON:"+rsp+":"+rsr);
            JSONObject resJS = new JSONObject(temp);
            String respondType = resJS.getString("respondType");
            bundle.putString("respondType",respondType);
            switch (respondType) {
            case "check": case "login": case "register": case "record":
            case "checkWidthHeight":
                bundle.putBoolean("result",resJS.getBoolean("result"));
                break;
            case "requestContent":
                boolean con_existed = resJS.getBoolean("result");
                bundle.putBoolean("result",con_existed);
                if (con_existed) {
                    String result = resJS.getString("content");
                    String[] stripCon = result.split("#");
                    bundle.putString("province",stripCon[0]);
                    bundle.putString("city",stripCon[1]);
                    bundle.putString("content",stripCon[2]);
                }
                break;
            case "requestCanvas":
                boolean existed = resJS.getBoolean("existed");
                bundle.putBoolean("result",existed);
                if (existed) {
                    //path
                    JSONArray jaPath = resJS.getJSONArray("path");
                    float[] arPath = new float[6];
                    for (int i = 0;i<6;i++) {
                        arPath[i] = (float) jaPath.getDouble(i);
                    }
                    bundle.putFloatArray("path",arPath);
                    //posTan
                    JSONArray jaPosTan = resJS.getJSONArray("fps");
                    float[] arPosTan = new float[jaPosTan.length()];
                    for (int i = 0;i<jaPosTan.length();i++) {
                        arPosTan[i] = (float) jaPosTan.getDouble(i);
                    }
                    bundle.putFloatArray("posTan",arPosTan);
                    //date
                    JSONArray date = resJS.getJSONArray("date");
                    int[] arDate = new int[]{(int) date.get(0),(int) date.get(1)};
                    bundle.putIntArray("date",arDate);
                }
            }
        }
        return bundle;
    }

}