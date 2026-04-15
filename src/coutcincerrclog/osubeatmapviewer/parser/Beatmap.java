package coutcincerrclog.osubeatmapviewer.parser;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;

import java.util.ArrayList;

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

}
