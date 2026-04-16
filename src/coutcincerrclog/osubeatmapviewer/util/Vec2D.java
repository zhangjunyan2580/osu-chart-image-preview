package coutcincerrclog.osubeatmapviewer.util;

import java.util.Objects;

public class Vec2D implements Comparable<Vec2D> {

    public int x;
    public int y;

    public Vec2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vec2D)) return false;
        Vec2D vec2D = (Vec2D) o;
        return x == vec2D.x && y == vec2D.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(Vec2D o) {
        if (x < o.x)
            return -1;
        else if (x == o.x)
            return Integer.compare(y, o.y);
        else
            return 1;
    }

}
