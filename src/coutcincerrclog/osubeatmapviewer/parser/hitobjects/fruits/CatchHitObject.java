package coutcincerrclog.osubeatmapviewer.parser.hitobjects.fruits;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;

public abstract class CatchHitObject extends HitObject {

    public float x;
    public int combo;

    public boolean hyperDash;
    public float distanceToHyperDash;

    public CatchHitObject(int time, float x, int combo) {
        this.time = time;
        this.x = x;
        this.combo = combo;
    }

}
