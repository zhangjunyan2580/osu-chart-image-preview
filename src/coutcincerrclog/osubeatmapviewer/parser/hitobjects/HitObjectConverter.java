package coutcincerrclog.osubeatmapviewer.parser.hitobjects;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.HitCircle;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Hold;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Slider;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Spinner;

import java.util.Collection;
import java.util.Collections;

public abstract class HitObjectConverter {

    public Collection<HitObject> convert(HitObject raw) {
        if (raw instanceof HitCircle)
            return convertCircle((HitCircle) raw);
        else if (raw instanceof Slider)
            return convertSlider((Slider) raw);
        else if (raw instanceof Spinner)
            return convertSpinner((Spinner) raw);
        else if (raw instanceof Hold)
            return convertHold((Hold) raw);
        return Collections.singletonList(raw);
    }

    public abstract Collection<HitObject> convertCircle(HitCircle raw);
    public abstract Collection<HitObject> convertSlider(Slider raw);
    public abstract Collection<HitObject> convertSpinner(Spinner raw);
    public abstract Collection<HitObject> convertHold(Hold raw);

    public abstract void postProcessing();

}
