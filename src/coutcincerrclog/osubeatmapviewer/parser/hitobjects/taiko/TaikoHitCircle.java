package coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;

public class TaikoHitCircle extends HitObject {

    public boolean colorKat;
    public boolean finisher;

    public TaikoHitCircle(int time, boolean colorKat, boolean finisher) {
        this.time = time;
        this.colorKat = colorKat;
        this.finisher = finisher;
    }

}
