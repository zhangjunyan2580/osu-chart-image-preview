package coutcincerrclog.osubeatmapviewer.parser.hitobjects.fruits;

import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Slider;
import coutcincerrclog.osubeatmapviewer.util.LegacyRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CatchJuiceStream extends HitObject {

    private LegacyRandom random;

    public Slider base;
    public int combo;

    public List<CatchHitObject> fruits;

    public CatchJuiceStream(Slider raw, LegacyRandom random, int combo) {
        this.base = raw;
        this.random = random;
        this.combo = combo;
        this.fruits = new ArrayList<>();
    }

    public void updateCalculation(Beatmap beatmap, boolean flip) {
        base.updateCalculation(beatmap, flip);

        CatchHitCircle f1 = new CatchHitCircle(base.time, base.pos.x, combo);
        fruits.add(f1);

        int lastTime = base.time;
        for (int i = 0; i < base.sliderScoreTimingPoints.size(); i++) {
            int time = base.sliderScoreTimingPoints.get(i);
            if (time - lastTime > 80) {
                float var = time - lastTime;
                while (var > 100)
                    var /= 2;
                for (float j = lastTime + var; j < time; j += var)
                    fruits.add(new CatchTinyDroplet((int) j, base.positionAtTime((int) j).x + random.next(-20, 20), combo));
            }

            lastTime = time;
            if (i < base.sliderScoreTimingPoints.size() - 1) {
                int repeatLocation = Collections.binarySearch(base.sliderRepeatPoints, time);
                if (repeatLocation >= 0) {
                    CatchHitCircle f = new CatchHitCircle(time, repeatLocation % 2 == 1 ? base.pos.x : base.pos2.x, combo);
                    fruits.add(f);
                } else {
                    CatchDroplet f = new CatchDroplet(time, base.positionAtTime(time).x, combo);
                    random.next();
                    fruits.add(f);
                }
            }
        }

        CatchHitCircle f2 = new CatchHitCircle(base.endTime, base.endPos.x, combo);
        fruits.add(f2);
    }

}
