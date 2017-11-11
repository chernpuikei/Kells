package com.example.chenpeiqi.kells;

/**
 * Created on 2017/8/9.
 */
public class TaPiece {

    float[] ta;

    public TaPiece(float[] ta) {
        this.ta = ta;
    }

    float getLength() {
        return ta[1]-ta[0];
    }
}