package coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Slider;

import java.util.ArrayList;
import java.util.List;

public class TaikoDrumroll extends HitObject {

    public int endTime;
    public boolean finisher;
    public List<Integer> tickTimes;
    public Slider base;

    public TaikoDrumroll(int time, int endTime, boolean finisher, Slider slider) {
        this.time = time;
        this.endTime = endTime;
        this.finisher = finisher;
        this.tickTimes = new ArrayList<>();
        this.base = slider;
    }

}
