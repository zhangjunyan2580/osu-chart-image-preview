package coutcincerrclog.osubeatmapviewer.util;

import coutcincerrclog.osubeatmapviewer.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MathUtil {

    private static class BezierApproximator {

        private int count;
        private List<Vec2F> controlPoints;
        private Vec2F[] subdivisionBuffer1;
        private Vec2F[] subdivisionBuffer2;

        public static final float TOLERANCE = 0.5f;
        public static final float TOLERANCE_SQ = TOLERANCE * TOLERANCE;

        public BezierApproximator(List<Vec2F> controlPoints) {
            this.controlPoints = controlPoints;
            count = controlPoints.size();

            subdivisionBuffer1 = new Vec2F[count];
            subdivisionBuffer2 = new Vec2F[count * 2 - 1];
        }

        private static boolean isFlatEnough(Vec2F[] controlPoints) {
            for (int i = 1; i < controlPoints.length - 1; i++)
                if ((controlPoints[i - 1].sub(controlPoints[i].mul(2)).add(controlPoints[i + 1])).lengthSquared() > TOLERANCE_SQ)
                    return false;
            return true;
        }

        private void subdivide(Vec2F[] controlPoints, Vec2F[] l, Vec2F[] r) {
            Vec2F[] midpoints = subdivisionBuffer1;
            System.arraycopy(controlPoints, 0, midpoints, 0, count);
            for (int i = 0; i < count; i++) {
                l[i] = midpoints[0];
                r[count - i - 1] = midpoints[count - i - 1];
                for (int j = 0; j < count - i - 1; j++)
                    midpoints[j] = (midpoints[j].add(midpoints[j + 1])).div(2);
            }
        }

        private void Approximate(Vec2F[] controlPoints, List<Vec2F> output) {
            Vec2F[] l = subdivisionBuffer2;
            Vec2F[] r = subdivisionBuffer1;

            subdivide(controlPoints, l, r);

            if (count - 1 >= 0)
                System.arraycopy(r, 1, l, count, count - 1);

            output.add(controlPoints[0]);
            for (int i = 1; i < count - 1; ++i) {
                int index = 2 * i;
                Vec2F p = l[index - 1].add(l[index].mul(2)).add(l[index + 1]).mul(0.25f);
                output.add(p);
            }
        }

        public List<Vec2F> createBezier() {
            List<Vec2F> output = new ArrayList<>();

            if (count == 0)
                return output;

            Stack<Vec2F[]> toFlatten = new Stack<>();
            Stack<Vec2F[]> freeBuffers = new Stack<>();

            toFlatten.push(controlPoints.toArray(controlPoints.toArray(new Vec2F[0])));

            Vec2F[] leftChild = subdivisionBuffer2;

            while (!toFlatten.isEmpty()) {
                Vec2F[] parent = toFlatten.pop();
                if (isFlatEnough(parent)) {
                    Approximate(parent, output);
                    freeBuffers.push(parent);
                    continue;
                }

                Vec2F[] rightChild = freeBuffers.isEmpty() ? new Vec2F[count] : freeBuffers.pop();
                subdivide(parent, leftChild, rightChild);

                System.arraycopy(leftChild, 0, parent, 0, count);

                toFlatten.push(rightChild);
                toFlatten.push(parent);
            }

            output.add(controlPoints.get(count - 1));
            return output;
        }

    }

    public static final float Pi = 3.14159274f;

    public static double mapDifficultyRange(double difficulty, double min, double mid, double max, int modEZHR) {
        switch (modEZHR) {
            case Settings.MOD_EZ:
                difficulty = Math.max(0, difficulty / 2);
                break;

            case Settings.MOD_HR:
                difficulty = Math.min(10, difficulty * 1.4);
                break;
        }

        if (difficulty > 5)
            return mid + (max - mid) * (difficulty - 5) / 5;
        if (difficulty < 5)
            return mid - (mid - min) * (5 - difficulty) / 5;
        return mid;
    }

    public static boolean isStraightLine(Vec2F a, Vec2F b, Vec2F c) {
        return (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y) == 0.0f;
    }

    public static List<Vec2F> createBezier(List<Vec2F> input) {
        BezierApproximator b = new BezierApproximator(input);
        return b.createBezier();
    }

    public static class CircleThroughPointResult {

        public Vec2F center;
        public float radius;
        public double startAngle, endAngle;

        public CircleThroughPointResult(Vec2F center, float radius, double startAngle, double endAngle) {
            this.center = center;
            this.radius = radius;
            this.startAngle = startAngle;
            this.endAngle = endAngle;
        }

    }

    public static CircleThroughPointResult circleThroughPoints(Vec2F a, Vec2F b, Vec2F c) {
        float D = 2 * (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y));
        float AMagSq = a.lengthSquared();
        float BMagSq = b.lengthSquared();
        float CMagSq = c.lengthSquared();
        Vec2F center = new Vec2F(
                (AMagSq * (b.y - c.y) + BMagSq * (c.y - a.y) + CMagSq * (a.y - b.y)) / D,
                (AMagSq * (c.x - b.x) + BMagSq * (a.x - c.x) + CMagSq * (b.x - a.x)) / D);
        float radius = Vec2F.distance(center, a);

        double t_initial = circleTAt(a, center);
        double t_mid = circleTAt(b, center);
        double t_final = circleTAt(c, center);

        while (t_mid < t_initial) t_mid += 2 * Pi;
        while (t_final < t_initial) t_final += 2 * Pi;
        if (t_mid > t_final)
            t_final -= 2 * Pi;
        return new CircleThroughPointResult(center, radius, t_initial, t_final);
    }

    private static double circleTAt(Vec2F pt, Vec2F center) {
        return Math.atan2(pt.y - center.y, pt.x - center.x);
    }

    public static Vec2F circlePoint(Vec2F center, float radius, double t) {
        return new Vec2F((float) (Math.cos(t) * radius), (float) (Math.sin(t) * radius)).add(center);
    }

    public static Vec2F catmullRom(Vec2F value1, Vec2F value2, Vec2F value3, Vec2F value4, float amount) {
        float num = amount * amount;
        float num2 = amount * num;
        return new Vec2F(
            0.5f * (2f * value2.x + (-value1.x + value3.x) * amount + (2f * value1.x - 5f * value2.x + 4f * value3.x - value4.x) * num + (-value1.x + 3f * value2.x - 3f * value3.x + value4.x) * num2),
            0.5f * (2f * value2.y + (-value1.y + value3.y) * amount + (2f * value1.y - 5f * value2.y + 4f * value3.y - value4.y) * num + (-value1.y + 3f * value2.y - 3f * value3.y + value4.y) * num2)
        );
    }

}
