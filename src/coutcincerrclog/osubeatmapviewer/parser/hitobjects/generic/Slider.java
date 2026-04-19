package coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic;

import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.CurveType;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.util.Line;
import coutcincerrclog.osubeatmapviewer.util.MathUtil;
import coutcincerrclog.osubeatmapviewer.util.Vec2F;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Slider extends HitObject {

    public static final int SUB_SEGMENT_COUNT = 50;
    public static final double MIN_SEGMENT_LENGTH = 0.0001;

    public Vec2F pos;
    public boolean newCombo;
    public int soundType;
    public CurveType curveType;
    public int repeatCount;
    public double length;
    public ArrayList<Vec2F> points;
    public ArrayList<Integer> sounds;
    public int comboOffset;
    public int sampleSet;
    public int additionSet;
    public ArrayList<Integer> repeatSampleSet;
    public ArrayList<Integer> repeatAdditionSet;
    public int customSample;
    public int volume;
    public String customSampleFile;

    public int endTime;
    public Vec2F pos2;
    public Vec2F endPos;
    public List<Integer> sliderScoreTimingPoints;
    public List<Integer> sliderRepeatPoints;
    public List<Line> sliderCurveSmoothLines;
    public List<Double> cumulativeLengths;
    public double velocity;
    public double sliderScoringPointDistance;
    public double curveLength;

    public Slider(Vec2F pos, int time, boolean newCombo, int soundType, CurveType curveType, int repeatCount, double length, ArrayList<Vec2F> points, ArrayList<Integer> sounds, int comboOffset, int sampleSet, int additionSet, ArrayList<Integer> repeatSampleSet, ArrayList<Integer> repeatAdditionSet, int customSample, int volume, String customSampleFile) {
        this.pos = pos;
        this.time = time;
        this.newCombo = newCombo;
        this.soundType = soundType;
        this.curveType = curveType;
        this.repeatCount = repeatCount;
        this.length = length;
        this.points = points;
        this.sounds = sounds;
        this.comboOffset = comboOffset;
        this.sampleSet = sampleSet;
        this.additionSet = additionSet;
        this.repeatSampleSet = repeatSampleSet;
        this.repeatAdditionSet = repeatAdditionSet;
        this.customSample = customSample;
        this.volume = volume;
        this.customSampleFile = customSampleFile;
    }

    public void updateCalculation(Beatmap beatmap, boolean flip) {
        this.sliderRepeatPoints = new ArrayList<>();
        this.sliderScoreTimingPoints = new ArrayList<>();

        sliderScoringPointDistance = (100 * beatmap.sliderMultiplier) / beatmap.sliderTickRate;
        velocity = beatmap.sliderVelocityAt(time);
        List<Line> path = new ArrayList<>();
        List<Vec2F> points = new ArrayList<>();
        if (this.points != null && !this.points.isEmpty() && !this.points.get(0).equals(pos))
            points.add(pos);
        if (this.points != null)
            points.addAll(this.points);
        if (flip)
            points.replaceAll(vec2F -> new Vec2F(vec2F.x, 384 - vec2F.y));
        switch (curveType) {
            case CATMULL:
                createCatmullPath(path, points);
                break;

            case BEZIER:
                createBezierPath(path, points);
                break;

            case PERFECT_CURVE:
                createCurvePath(path, points);
                break;

            case LINEAR:
                createLinearPath(path, points);
                break;
        }

        sliderCurveSmoothLines = path;

        curveLength = 0;
        for (Line line : path)
            curveLength += line.length();

        double tickDistance = sliderScoringPointDistance / beatmap.bpmMultiplierAt(time);
        if (curveLength > 0) {
            if (length == 0)
                length = curveLength;
            if (tickDistance > length)
                tickDistance = length;
            double cutLength = curveLength - length;
            while (!path.isEmpty()) {
                Line lastLine = path.get(path.size() - 1);
                float lastLineLength = Vec2F.distance(lastLine.p1, lastLine.p2);
                if (lastLineLength > cutLength + MIN_SEGMENT_LENGTH) {
                    if (!lastLine.p2.equals(lastLine.p1))
                        lastLine.p2 = lastLine.p1.add(Vec2F.normalize(lastLine.p2.sub(lastLine.p1)).mul(lastLine.length() - (float) cutLength));
                    break;
                }
                path.remove(path.size() - 1);
                cutLength -= lastLineLength;
            }
        }

        if (!path.isEmpty()) {
            if (cumulativeLengths == null) cumulativeLengths = new ArrayList<>(path.size());
            else cumulativeLengths.clear();

            double totalLength = 0.0;

            for (Line l : path) {
                totalLength += l.length();
                cumulativeLengths.add(totalLength);
            }
        }

        if (path.isEmpty())
            return;

        {
            double scoringLengthTotal = 0;
            double currentTime = time;

            Vec2F p2 = new Vec2F(0, 0);

            double scoringDistance = 0;

            pos2 = path.get(path.size() - 1).p2;

            for (int i = 0; i < repeatCount; i++) {
                double distanceRemain = cumulativeLengths.get(cumulativeLengths.size() - 1);
                boolean skipTick = false;

                double minTickDistanceFromEnd = 0.01 * velocity;
                boolean reverse = (i % 2) == 1;
                int start = reverse ? path.size() - 1 : 0;
                int end = reverse ? -1 : path.size();
                int direction = reverse ? -1 : 1;

                for (int j = start; j != end; j += direction) {
                    Line l = path.get(j);
                    float distance = (float)(cumulativeLengths.get(j) - (j == 0 ? 0 : cumulativeLengths.get(j - 1)));

                    p2 = reverse ? l.p1 : l.p2;

                    double duration = 1000F * distance / velocity;

                    currentTime += duration;
                    scoringDistance += distance;

                    while (scoringDistance >= tickDistance && !skipTick)
                    {
                        scoringLengthTotal += tickDistance;
                        scoringDistance -= tickDistance;
                        distanceRemain -= tickDistance;

                        skipTick = distanceRemain <= minTickDistanceFromEnd;
                        if (skipTick)
                            break;

                        int scoreTime = timeAtLength((float)scoringLengthTotal);
                        sliderScoreTimingPoints.add(scoreTime);
                    }
                }

                scoringLengthTotal += scoringDistance;
                sliderScoreTimingPoints.add(timeAtLength((float)scoringLengthTotal));

                if (skipTick)
                    scoringDistance = 0;
                else {
                    scoringLengthTotal -= tickDistance - scoringDistance;
                    scoringDistance = tickDistance - scoringDistance;
                }
            }

            endPos = p2;
            endTime = (int) currentTime;

            if (!sliderScoreTimingPoints.isEmpty())
                sliderScoreTimingPoints.set(sliderScoreTimingPoints.size() - 1, Math.max(time + (endTime - time) / 2, sliderScoreTimingPoints.get(sliderScoreTimingPoints.size() - 1) - 36));

            sliderRepeatPoints.clear();
            int timingPointsPerSegment = sliderScoreTimingPoints.size() / repeatCount;
            if (timingPointsPerSegment > 0)
                for (int i = 0; i < sliderScoreTimingPoints.size() - 1; i++)
                    if ((i + 1) % timingPointsPerSegment == 0)
                        sliderRepeatPoints.add(sliderScoreTimingPoints.get(i));
        }
    }

    private int timeAtLength(float length) {
        return (int) (time + (length / velocity) * 1000);
    }

    public Vec2F positionAtTime(int time) {
        if (time < this.time || time > this.endTime) return pos;
        float pos = (time - this.time) / ((float) (this.endTime - this.time) / repeatCount);
        if (pos % 2 > 1)
            pos = 1 - (pos % 1);
        else
            pos = (pos % 1);
        float lengthRequired = (float)(length * pos);
        return positionAtLength(lengthRequired);
    }

    private Vec2F positionAtLength(float length) {
        if (sliderCurveSmoothLines.isEmpty() || cumulativeLengths.isEmpty())
            return pos;

        if (length == 0)
            return sliderCurveSmoothLines.get(0).p1;

        double end = cumulativeLengths.get(cumulativeLengths.size() - 1);
        if (length >= end)
            return sliderCurveSmoothLines.get(sliderCurveSmoothLines.size() - 1).p2;

        int i = Collections.binarySearch(cumulativeLengths, (double) length);
        if (i < 0)
            i = Math.min(~i, cumulativeLengths.size() - 1);

        double lengthNext = cumulativeLengths.get(i);
        double lengthPrevious = i == 0 ? 0 : cumulativeLengths.get(i - 1);

        Vec2F res = sliderCurveSmoothLines.get(i).p1;

        if (lengthNext != lengthPrevious)
            res = res.add(sliderCurveSmoothLines.get(i).p2.sub(sliderCurveSmoothLines.get(i).p1).mul((float)((length - lengthPrevious) / (lengthNext - lengthPrevious))));

        return res;
    }

    private void createBezierPath(List<Line> path, List<Vec2F> points) {
        int lastIndex = 0;

        for (int i = 0; i < points.size(); i++) {
            boolean lastPointInCurrentPart = i < points.size() - 2 && points.get(i).equals(points.get(i + 1));
            if (lastPointInCurrentPart || i == points.size() - 1) {
                List<Vec2F> currentPartControlPoints = points.subList(lastIndex, i + 1);
                if (currentPartControlPoints.size() == 2)
                    path.add(new Line(currentPartControlPoints.get(0), currentPartControlPoints.get(1)));
                else {
                    List<Vec2F> bezierPoints = MathUtil.createBezier(currentPartControlPoints);
                    for (int j = 1; j < bezierPoints.size(); j++)
                        path.add(new Line(bezierPoints.get(j - 1), bezierPoints.get(j)));
                }
                if (lastPointInCurrentPart) i++;
                lastIndex = i;
            }
        }
    }

    private void createLinearPath(List<Line> path, List<Vec2F> points) {
        for (int i = 1; i < points.size(); i++)
            path.add(new Line(points.get(i - 1), points.get(i)));
    }

    private void createCurvePath(List<Line> path, List<Vec2F> points) {
        if (points.size() < 3) {
            createLinearPath(path, points);
            return;
        }
        if (points.size() > 3) {
            createBezierPath(path, points);
            return;
        }

        Vec2F p1 = points.get(0);
        Vec2F p2 = points.get(1);
        Vec2F p3 = points.get(2);

        if (MathUtil.isStraightLine(p1, p2, p3)) {
            createLinearPath(path, points);
            return;
        }

        MathUtil.CircleThroughPointResult result = MathUtil.circleThroughPoints(p1, p2, p3);
        Vec2F center = result.center;
        float radius = result.radius;
        double startAngle = result.startAngle, endAngle = result.endAngle;

        double length = Math.abs((endAngle - startAngle) * radius);
        int segments = (int) (length * 0.125f);

        Vec2F lastPoint = p1;
        for (int i = 1; i < segments; i++) {
            double progress = (double) i / (double) segments;
            double t = endAngle * progress + startAngle * (1 - progress);
            Vec2F newPoint = MathUtil.circlePoint(center, radius, t);
            path.add(new Line(lastPoint, newPoint));
            lastPoint = newPoint;
        }

        path.add(new Line(lastPoint, p3));
    }

    private void createCatmullPath(List<Line> path, List<Vec2F> points) {
        for (int j = 0; j < points.size() - 1; j++) {
            Vec2F v1 = j - 1 >= 0 ? points.get(j - 1) : points.get(j);
            Vec2F v2 = points.get(j);
            Vec2F v3 = j + 1 < points.size() ? points.get(j + 1) : v2.add(v2.sub(v1));
            Vec2F v4 = j + 2 < points.size() ? points.get(j + 2) : v3.add(v3.sub(v2));

            for (int k = 0; k < SUB_SEGMENT_COUNT; k++)
                path.add(new Line(
                        MathUtil.catmullRom(v1, v2, v3, v4, (float)k / SUB_SEGMENT_COUNT),
                        MathUtil.catmullRom(v1, v2, v3, v4, (float)(k + 1) / SUB_SEGMENT_COUNT)
                ));
        }
    }

}
