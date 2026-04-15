package coutcincerrclog.osubeatmapviewer.parser.hitobjects.mania;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;

public class ManiaHitCircle extends HitObject {

    public int column;

    public ManiaHitCircle(int time, int column) {
        this.time = time;
        this.column = column;
    }

}
