package coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.util.Vec2F;

public class HitCircle extends HitObject {

    public Vec2F pos;
    public boolean newCombo;
    public int soundType;
    public int comboOffset;
    public int sampleSet;
    public int additionSet;
    public int customSample;
    public int volume;
    public String customSampleFile;

    public HitCircle(Vec2F pos, int time, boolean newCombo, int soundType, int comboOffset, int sampleSet, int additionSet, int customSample, int volume, String customSampleFile) {
        this.pos = pos;
        this.time = time;
        this.newCombo = newCombo;
        this.soundType = soundType;
        this.comboOffset = comboOffset;
        this.sampleSet = sampleSet;
        this.additionSet = additionSet;
        this.customSample = customSample;
        this.volume = volume;
        this.customSampleFile = customSampleFile;
    }

}
