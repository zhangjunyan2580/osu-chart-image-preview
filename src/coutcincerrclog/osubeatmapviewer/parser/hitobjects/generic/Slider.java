package coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.CurveType;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.util.Vec2F;

import java.util.ArrayList;

public class Slider extends HitObject {

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

}
