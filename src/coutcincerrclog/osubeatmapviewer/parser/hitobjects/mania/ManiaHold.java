package coutcincerrclog.osubeatmapviewer.parser.hitobjects.mania;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;

public class ManiaHold extends HitObject {

    public int endTime;
    public int column;

    public ManiaHold(int time, int endTime, int column) {
        this.time = time;
        this.endTime = endTime;
        this.column = column;
    }

}
