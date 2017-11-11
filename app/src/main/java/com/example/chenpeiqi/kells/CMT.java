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

import static com.example.chenpeiqi.kells.Tool.i;

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
            socket = new Socket("10.0.3.2",20000);
//            socket = new Socket("192.168.1.110",20000);
//            socket = new Socket("192.168.43.182",20000);
            bundle = request(socket);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return bundle;
    }

    private Bundle request(Socket socket) throws Exception {
        //发送数据部分
        JSONObject requestJson = new JSONObject();
        String requestType = bundle.getString("requestType");
        i("bundle",bundle);
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
//                Thread.sleep(1000);
                requestJson.put("email",bundle.getString("email"))
                        .put("si",bundle.getInt("si")).put("pi",bundle.getInt("pi"));
                break;
            case "initCanvas":
                JSONArray ja_path = new JSONArray(), ja_fp = new JSONArray();
                for (float path_item : bundle.getFloatArray("path")) {
                    ja_path.put(path_item);
                }
                for (float fp_item : bundle.getFloatArray("posTan")) {
                    ja_fp.put(fp_item);
                }
                requestJson.put("si",bundle.getInt("si"))
                        .put("path",ja_path).put("posTan",ja_fp);
                break;
            case "requestContent":
                requestJson
                        .put("si",bundle.getInt("s_i")).put("pi",bundle.getInt("pi"));
                break;
            case "initContent":
                requestJson.put("content",bundle.getString("content"))
                        .put("si",bundle.getInt("si")).put("pi",bundle.getInt("pi"))
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
        JSONObject resJOJ = new JSONObject(temp);
        i("resJOJ",resJOJ);
        String rsp = resJOJ.getString("respondType"),
                rsr = resJOJ.has("result")? resJOJ.getString("result"): "null";
        if (temp!=null) {
            Log.i(tag,"<——receiving JSON:"+resJOJ);
            JSONObject resJS = new JSONObject(temp);
            String respondType = resJS.getString("respondType");
            bundle.putString("respondType",respondType);
            switch (respondType) {
                case "check": case "login": case "register": case "record":
                case "checkWidthHeight":
                    bundle.putBoolean("result",resJS.getBoolean("result"));
                    break;
                case "requestCanvas":
                    boolean canvas_exist = resJS.getBoolean("canvas_exist");
                    bundle.putBoolean("canvas_exist",canvas_exist);
                    if (canvas_exist) {
                        i("canvas exist");
                        //path
                        JSONArray jaPath = resJS.getJSONArray("path");
                        int jal = jaPath.length();
                        int len = jaPath.getDouble(jal-2)==0 &&
                                jaPath.getDouble(jal-1)==0? jal-2: jal;
                        float[] arPath = new float[len];
                        for (int i = 0;i<len;i++) {
                            arPath[i] = (float) jaPath.getDouble(i);
                        }
                        bundle.putFloatArray("path",arPath);
                        //posTan
                        JSONArray jaPosTan = resJS.getJSONArray("posTan");
                        float[] arPosTan = new float[jaPosTan.length()];
                        for (int i = 0;i<jaPosTan.length();i++) {
                            arPosTan[i] = (float) jaPosTan.getDouble(i);
                        }
                        bundle.putFloatArray("posTan",arPosTan);
                        //date
                        JSONArray date = resJS.getJSONArray("date");
                        int[] arDate = new int[]{(int) date.get(0),(int) date.get(1)};
                        bundle.putIntArray("date",arDate);
                    } else {
                        i("canvas not exist,about to break");
                        //如果!canvas_exist，那么就没有必要requestContent
                        break;
                    }
                case "requestContent":
                    //todo:首先获取查询数据库的结果
                    boolean content_exist = resJS.getBoolean("content_exist");
                    bundle.putBoolean("content_exist",content_exist);
                    bundle.putInt("si",resJS.getInt("si"));
                    bundle.putInt("pi",resJS.getInt("pi"));
                    i("content_exist",content_exist);
                    i("canvas_exist",resJOJ.getBoolean("canvas_exist"));
                    if (content_exist) {
                        int year = resJS.getInt("year"), month = resJS.getInt("month");
                        bundle.putInt("year",year); bundle.putInt("month",month);
                    }
                    String result = resJS.getString("content");
                    i("content",result);
                    String[] stripCon = result.split("#");
//                        bundle.putString("province",stripCon[0]);
//                        bundle.putString("city",stripCon[1]);
                    i("pure content",stripCon[2]);
                    bundle.putString("content",stripCon[2]);
            }
        }
        return bundle;
    }
}