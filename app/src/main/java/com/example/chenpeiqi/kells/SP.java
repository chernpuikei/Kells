package com.example.chenpeiqi.kells;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created on 2017/3/13.
 */
class SP {

    static SharedPreferences getSP(Context context) {
        return context.getSharedPreferences("status",Context.MODE_PRIVATE);

    }

}

