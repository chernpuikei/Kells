package com.example.chenpeiqi.kells;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.chenpeiqi.kells.Tool.i;

public class Diary extends AppCompatActivity implements AMapLocationListener {

    private static EditText editText;
    static Handler handler;
    private String content;
    private static final int MP = Context.MODE_PRIVATE;
    private static final String tag = "Diary";
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;
    private String[] locations;
    private int si,pi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        editText = (EditText) findViewById(R.id.content_text);
        handler = new MaiHandler(new WeakReference<>(this));
        locations = new String[2];
        Intent intent = getIntent();
        this.content = intent.getStringExtra("content");
        this.si = intent.getIntExtra("si",0);this.pi = intent.getIntExtra("pi",-1);
        editText.setText(content);
        //下面要注释回来
        initLocate();
    }

    private void initLocate() {
        //此方法只是设置各种参数然后开始定位，真正初始化位置是在后续回调方法中
        locationClient = new AMapLocationClient(this);
        i("initLocate>>");
        locationOption = new AMapLocationClientOption();
        locationClient.setLocationListener(this);
        locationOption.setLocationMode(
                AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setOnceLocation(true);
        locationClient.setLocationOption(locationOption);
        locationClient.startLocation();
        i("location located",locationClient);
        i("<<");
        //startLocation()后系统会自动调用onLocationChanged方法从而初始化AMapLocation对象
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        i("aMapLocation",aMapLocation);
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                this.locations[0] = aMapLocation.getProvince();
                i("province?",locations[0]);
                this.locations[1] = aMapLocation.getCity();
                Log.i(tag,"province/city:"+locations[0]+"/"+locations[1]);
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("Error","location Error, ErrCode:"+aMapLocation.getErrorCode()
                        +", errInfo:"+aMapLocation.getErrorInfo());
            }
        }
    }

    private static class MaiHandler extends Handler {

        WeakReference<Diary> ref;

        MaiHandler(WeakReference<Diary> ref) {
            this.ref = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 10086:
                //从服务器获得数据并将其显示在EditText上
                Bundle bundle = msg.getData();
                String content = bundle.getString("content");
                editText.setText(content);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.day_record,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        i("onOptionsItemSelected>");
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_send:
            String content = editText.getText().toString();
            String email = getSharedPreferences("status",MP).getString("email","aaa");
            Bundle writeToServer = new Bundle();
            writeToServer.putString("email",email);
            content = content.equals("")? "default content": content;
            writeToServer.putString("content",content);
            //todo:模拟器限制，暂行直接传输参数作为获取地理位置的结果
            locations[0] = "广东";locations[1] = "广州";
            writeToServer.putString("province",locations[0]);
            writeToServer.putString("city",locations[1]);
            writeToServer.putString("requestType","initContent");
            writeToServer.putInt("si",si);
            writeToServer.putInt("pi",pi);
            i("writeToServer?",writeToServer);
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(new CMT(writeToServer));
            Intent intent = new Intent(Diary.this,Kells.class);
            intent.putExtra("content",content);
            setResult(0,intent);
            this.finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

}
