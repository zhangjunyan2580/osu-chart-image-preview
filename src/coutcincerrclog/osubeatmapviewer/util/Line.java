package coutcincerrclog.osubeatmapviewer.util;

public class Line {

    public Vec2F p1, p2;

    public Line(Vec2F p1, Vec2F p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public float length() {
        return p2.sub(p1).length();
    }

}
