package coutcincerrclog.osubeatmapviewer.util;

import java.util.Objects;

public class Vec2F {

    public float x;
    public float y;

    public Vec2F(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static float distance(Vec2F value1, Vec2F value2) {
        float num = value1.x - value2.x;
        float num2 = value1.y - value2.y;
        float num3 = num * num + num2 * num2;
        return (float)Math.sqrt(num3);
    }

    public static Vec2F normalize(Vec2F value) {
        float num = value.x * value.x + value.y * value.y;
        float num2 = 1f / (float) Math.sqrt(num);
        return new Vec2F(value.x * num2, value.y * num2);
    }

    public Vec2F sub(Vec2F r) {
        return new Vec2F(x - r.x, y - r.y);
    }

    public Vec2F mul(float f) {
        return new Vec2F(x * f, y * f);
    }

    public Vec2F add(Vec2F r) {
        return new Vec2F(x + r.x, y + r.y);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vec2F)) return false;
        Vec2F vec2F = (Vec2F) o;
        return Float.compare(x, vec2F.x) == 0 && Float.compare(y, vec2F.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public Vec2F div(float v) {
        float num = 1f / v;
        return new Vec2F(x * num, y * num);
    }

    public float length() {
        float num = x * x + y * y;
        return (float) Math.sqrt(num);
    }

}
