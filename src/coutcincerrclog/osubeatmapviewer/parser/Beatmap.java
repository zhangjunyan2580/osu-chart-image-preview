package coutcincerrclog.osubeatmapviewer.parser;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.HitCircle;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Hold;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Slider;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Spinner;

import java.util.*;

public class Beatmap {

    public int mode;

    public String title;
    public String artist;
    public String creator;
    public String version;

    public ArrayList<TimingPoint> timingPoints = new ArrayList<>();
    public ArrayList<HitObject> rawHitObjects = new ArrayList<>();
    public ArrayList<HitObject> processedHitObjects = new ArrayList<>();

    public float HPDrainRate;
    public float circleSize;
    public float overallDifficulty;
    public float approachRate;
    public double sliderMultiplier;
    public double sliderTickRate;

    public int getPrimaryBPM() {
        if (timingPoints == null || timingPoints.isEmpty())
            return 0;

        double lastTime = 0;
        for (HitObject hitObject : rawHitObjects) {
            if (hitObject instanceof HitCircle)
                lastTime = hitObject.time;
            else if (hitObject instanceof Slider)
                lastTime = hitObject.time;
            else if (hitObject instanceof Spinner)
                lastTime = ((Spinner) hitObject).endTime;
            else if (hitObject instanceof Hold)
                lastTime = ((Hold) hitObject).endTime;
        }

        // Original implementation uses a hashtable there
        // so we can't ensure original order here
        Map<Double, Integer> beatLengthDuration = new HashMap<>();
        double currentBeatLength = 0;
        for (int i = timingPoints.size() - 1; i >= 0; --i) {
            TimingPoint timingPoint = timingPoints.get(i);
            if (timingPoint.uninherited)
                currentBeatLength = timingPoint.beatLength;

            if (currentBeatLength == 0 || timingPoint.time > lastTime || (!timingPoint.uninherited && i > 0))
                continue; // Not sure why they wrote this

            int duration = (int) (lastTime - (i == 0 ? 0 : timingPoint.time));
            beatLengthDuration.merge(currentBeatLength, duration, Integer::sum);
            lastTime = timingPoint.time;
        }
        return beatLengthDuration.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey).map(e -> (int) Math.round(60000 / e)).orElse(0);
        // Yes they returned the *rounded* integer BPM
        // and used it to scale mania scroll speed instead of the exact one
    }

    public double beatLengthAt(double time, boolean allowMultiplier) {
        if (timingPoints == null || timingPoints.isEmpty())
            return 0;
        int point = 0, samplePoint = 0;
        for (int i = 0; i < timingPoints.size(); ++i) {
            if (timingPoints.get(i).time <= time) {
                if (timingPoints.get(i).uninherited)
                    point = i;
                else
                    samplePoint = i;
            }
        }
        if (allowMultiplier && samplePoint > point && timingPoints.get(samplePoint).beatLength < 0)
            return timingPoints.get(point).beatLength * timingPoints.get(samplePoint).BPMMultiplier();
        return timingPoints.get(point).beatLength;
    }

    public double beatLengthAt(double time) {
        return beatLengthAt(time, true);
    }

    public void initIndex() {
        for (int i = 0; i < processedHitObjects.size(); ++i)
            processedHitObjects.get(i).index = i;
    }

    public double sliderVelocityAt(int time) {
        double beatLength = beatLengthAt(time);
        if (beatLength > 0)
            return ((100 * sliderMultiplier) / sliderTickRate) * sliderTickRate * (1000F / beatLength);
        return ((100 * sliderMultiplier) / sliderTickRate) * sliderTickRate;
    }

    public float bpmMultiplierAt(double time) {
        if (timingPoints == null || timingPoints.isEmpty())
            return 1;
        TimingPoint timingPoint = null;
        for (TimingPoint point : timingPoints)
            if (point.time <= time)
                timingPoint = point;
        if (timingPoint == null)
            timingPoint = timingPoints.get(0);
        if (timingPoint.uninherited)
            return 1;
        else
            return Math.max(10, Math.min(1000, (float) -timingPoint.beatLength)) / 100f;
    }

}
