package coutcincerrclog.osubeatmapviewer.parser.hitobjects.mania;

import coutcincerrclog.osubeatmapviewer.Settings;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObjectConverter;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.HitCircle;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Hold;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Slider;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Spinner;

import java.util.Collection;
import java.util.Collections;

public class ManiaConverter extends HitObjectConverter {

    private int columnCount;
    private boolean convert;

    public ManiaConverter(Beatmap beatmap, Settings settings) {
        convert = beatmap.mode != 3;
        columnCount = convert ? settings.maniaConvertKeys : Math.round(beatmap.circleSize);
    }

    @Override
    public Collection<HitObject> convertCircle(HitCircle raw) {
        if (!convert) {
            return Collections.singleton(new ManiaHitCircle(raw.time, (int) Math.min(Math.floor(raw.pos.x * columnCount / 512), columnCount - 1)));
        }
        // TODO: Add converts
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<HitObject> convertSlider(Slider raw) {
        return Collections.emptyList();
    }

    @Override
    public Collection<HitObject> convertSpinner(Spinner raw) {
        return Collections.emptyList();
    }

    @Override
    public Collection<HitObject> convertHold(Hold raw) {
        if (!convert) {
            return Collections.singleton(new ManiaHold(raw.time, raw.endTime, Math.min((int) Math.floor(raw.pos.x * columnCount / 512), columnCount - 1)));
        }
        // TODO: Add converts
        throw new UnsupportedOperationException();
    }

    @Override
    public void postProcessing() {}

}
