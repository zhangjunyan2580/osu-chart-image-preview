package coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko;

import coutcincerrclog.osubeatmapviewer.Settings;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObjectConverter;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.HitCircle;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Hold;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Slider;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Spinner;
import coutcincerrclog.osubeatmapviewer.util.MathUtil;

import java.util.Collection;
import java.util.Collections;

public class TaikoConverter extends HitObjectConverter {

    private boolean convert;
    private Beatmap beatmap;
    private Settings settings;

    public TaikoConverter(Beatmap beatmap, Settings settings) {
        this.convert = beatmap.mode != 1;
        this.beatmap = beatmap;
        this.settings = settings;

        beatmap.sliderMultiplier *= 1.4;
    }

    @Override
    public Collection<HitObject> convertCircle(HitCircle raw) {
        return Collections.singletonList(new TaikoHitCircle(raw.time, (raw.soundType & ~5) != 0, (raw.soundType & 4) != 0));
    }

    @Override
    public Collection<HitObject> convertSlider(Slider raw) {
        raw.length *= 1.4;
        TaikoDrumroll drumroll = new TaikoDrumroll(raw.time, raw.time, (raw.soundType & 4) != 0, raw);

        double l = raw.length * raw.repeatCount;
        double v = 100 * beatmap.sliderMultiplier;
        double b = beatmap.beatLengthAt(raw.time);
        drumroll.endTime = drumroll.time + (int) (l / v * b);
        if (!convert)
            return Collections.singletonList(drumroll);
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<HitObject> convertSpinner(Spinner raw) {
        if (!convert) {
            TaikoSpinner spinner = new TaikoSpinner(raw.time, raw.endTime);
            double spinnerRotationRatio = MathUtil.mapDifficultyRange(beatmap.overallDifficulty, 3, 5, 7.5, settings.modEZHR);
            int rotationRequirement = (int) ((float) (raw.endTime - raw.time) / 1000 * spinnerRotationRatio);
            rotationRequirement = Math.max(1, (int) (rotationRequirement * 1.65f));
            switch (settings.modHTDT) {
                case Settings.MOD_DT:
                    rotationRequirement = Math.max(1, (int) (rotationRequirement * 0.75f));
                    break;

                case Settings.MOD_HT:
                    rotationRequirement = Math.max(1, (int) (rotationRequirement * 1.5f));
                    break;
            }
            spinner.spinCount = rotationRequirement;
            return Collections.singletonList(spinner);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<HitObject> convertHold(Hold raw) {
        return Collections.emptyList();
    }

    @Override
    public void postProcessing() {
        for (HitObject hitObject : beatmap.processedHitObjects) {
            if (hitObject instanceof TaikoDrumroll) {
                TaikoDrumroll drumroll = (TaikoDrumroll) hitObject;
                double length = drumroll.base.length * drumroll.base.repeatCount;
                double v = 100 * beatmap.sliderMultiplier;
                double b = beatmap.beatLengthAt(drumroll.time);
                drumroll.endTime = drumroll.time + (int) (length / v * b);

                double maxRate;
                if (beatmap.sliderTickRate == 3 || beatmap.sliderTickRate == 6 || beatmap.sliderTickRate == 1.5)
                    maxRate = beatmap.beatLengthAt(drumroll.time, false) / 6;
                else
                    maxRate = beatmap.beatLengthAt(drumroll.time, false) / 8;
                while (maxRate < 60)
                    maxRate *= 2;
                while (maxRate > 120)
                    maxRate /= 2;

                boolean endpointHittable =
                        hitObject.index == beatmap.processedHitObjects.size() - 1 ||
                        beatmap.processedHitObjects.get(hitObject.index + 1).time - (drumroll.endTime + (int) maxRate) > (int) maxRate;
                int hittableEndTime = endpointHittable ? drumroll.endTime + (int) maxRate : drumroll.endTime;
                for (double i = drumroll.time; i < hittableEndTime; i += maxRate)
                    drumroll.tickTimes.add((int) i);
            }
        }
    }

}
