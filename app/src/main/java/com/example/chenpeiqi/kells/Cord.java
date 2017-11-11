package com.example.chenpeiqi.kells;

import java.util.ArrayList;

import static com.example.chenpeiqi.kells.Tool.*;

/**
 * Created on 2017/6/25.
 */
class Cord {

    private float x, y;
    private int width, height;

    Cord(int width,int height,float x,float y) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    Cord(int width,int height,Cord previous) {
        float px = previous.getX(), py = previous.getY();
        this.width = width; this.height = height;
        if (px==0 && py==0) {
            boolean hov = randomBoolean();
            this.x = hov? randomBoolean()? 0: width: randomCord(width);
            this.y = !hov? randomBoolean()? 0: height: randomCord(height);
        } else {
            boolean changeOnX = px==width || px==0;
            if (changeOnX) {
                this.x = px==width? 0: width; this.y = py;
            } else {
                this.x = px; this.y = py==height? 0: height;
            }
        }
    }

    Cord(Cord start,boolean crushOrNot,int curQuad,int width,int height) {
        //所有终点生成区间
        this.width = width; this.height = height;
        String[] AL = new String[2];//All Locations
        switch (curQuad) {
            case 0: case 1:
                AL[0] = crushOrNot? "l": "r"; break;
            default://here for case 2,case 3
                AL[0] = crushOrNot? "r": "l";
        }
        switch (curQuad) {
            case 0: case 3:
                AL[1] = crushOrNot? "t": "b"; break;
            default:
                AL[1] = crushOrNot? "b": "t";
        }
        Array("AL",AL,2);
        String selfLoc = start.getLocInQua(curQuad);
        ArrayList<String> asd = new ArrayList<>();//Possible After Self Deleted
        for (int i = 0;i<2;i++) {
            if (!AL[i].equals(selfLoc)) {
                asd.add(AL[i]);
            }
        }
        String finalLoc = asd.get(asd.size()==2? (int) (Math.random()/0.5): 0);
        //还没有生成坐标
        //loc X quad=>(x,y)
        float[] deltas = calDeltaFromQuad(curQuad);
        float dx = deltas[0], dy = deltas[1];
        switch (finalLoc) {
            case "l": this.x = dx; break;
            case "r": this.x = width/2+dx; break;
            default: this.x = (float) (Math.random()*width/4+width/8+dx);
        }
        switch (finalLoc) {
            case "t": this.y = dy; break;
            case "b": this.y = height/2+dy; break;
            default: this.y = (float) (Math.random()*height/4+height/8+dy);
        }
    }

    private float randomCord(float woh) {
        return (float) (woh/8+Math.random()*woh/4+(randomBoolean()? 0: woh/2));
    }

    boolean onEdge() {return x==width || x==0 || y==height || y==0;}

    int[] getSiblingQuad() {//这里的cord必然是坐标轴上的点
        String loc = this.getLocOnAxis();
        int[] result = new int[2];
        switch (loc) {
            case "t": case "l":
                result[0] = 0; break;
            default:
                result[0] = 2;
        }
        switch (loc) {
            case "l": case "b":
                result[1] = 1; break;
            default:
                result[1] = 3;
        }
        return result;
    }

    private String getLocInQua(int quadrant) {
        //确定坐标轴上的点相对于当前quad处在什么位置
        String l_c = getLocOnAxis();
        switch (l_c) {
            case "t":
                return quadrant==0? "r": "l";
            case "b":
                return quadrant==1? "r": "l";
            case "l":
                return quadrant==0? "b": "t";
            default:
                return quadrant==3? "b": "t";
        }
    }

    private String getLocOnAxis() {//确定在坐标轴上的点是那个
        boolean hov = y==height/2;
        return hov? x<width/2? "l": "r": y<height/2? "t": "b";
    }

    int getQuad() {
        boolean hor = x<width/2;
        return same(hor,y<height/2)? hor? 0: 2: hor? 1: 3;
    }

    private float[] calDeltaFromQuad(int quadrant) {
        int deltaX = 0, deltaY = 0;
        switch (quadrant) {
            case 1: deltaY = height/2; break;
            case 2: deltaX = width/2; deltaY = height/2; break;
            case 3: deltaX = width/2; break;
        }
        return new float[]{deltaX,deltaY};
    }

    float getX() { return this.x;}

    float getY() { return this.y;}
}
