package coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;

public class Spinner extends HitObject {

    public int endTime;
    public int soundType;
    public int sampleSet;
    public int additionSet;
    public int customSample;
    public int volume;
    public String customSampleFile;

    public Spinner(int time, int endTime, int soundType, int sampleSet, int additionSet, int customSample, int volume, String customSampleFile) {
        this.time = time;
        this.endTime = endTime;
        this.soundType = soundType;
        this.sampleSet = sampleSet;
        this.additionSet = additionSet;
        this.customSample = customSample;
        this.volume = volume;
        this.customSampleFile = customSampleFile;
    }

}
