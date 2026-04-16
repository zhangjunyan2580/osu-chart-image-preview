package coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;

public class TaikoSpinner extends HitObject {

    public int endTime;
    public int spinCount;

    public TaikoSpinner(int time, int endTime) {
        this.time = time;
        this.endTime = endTime;
    }

}
