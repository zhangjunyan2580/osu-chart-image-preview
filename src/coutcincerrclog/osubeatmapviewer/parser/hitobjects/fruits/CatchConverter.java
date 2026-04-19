package coutcincerrclog.osubeatmapviewer.parser.hitobjects.fruits;

import coutcincerrclog.osubeatmapviewer.Settings;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObjectConverter;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.HitCircle;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Hold;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Slider;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Spinner;
import coutcincerrclog.osubeatmapviewer.util.LegacyRandom;
import coutcincerrclog.osubeatmapviewer.util.Vec2F;

import java.util.Collection;
import java.util.Collections;

public class CatchConverter extends HitObjectConverter {

    private Beatmap beatmap;
    private Settings settings;

    private LegacyRandom random;

    private float lastStartX;
    private int lastStartTime;
    private int currentCombo;

    public static final double SIXTY_FRAME_TIME = 1000. / 60;

    public CatchConverter(Beatmap beatmap, Settings settings) {
        this.beatmap = beatmap;
        this.settings = settings;
        this.random = new LegacyRandom(1337);
        this.currentCombo = -1;
    }

    @Override
    public Collection<HitObject> convertCircle(HitCircle raw) {
        Vec2F newPos = new Vec2F(raw.pos.x, raw.pos.y);
        if (currentCombo == -1)
            currentCombo = 0;
        else if (raw.newCombo)
            currentCombo = (currentCombo + 1) % 4;

        if (settings.modEZHR == Settings.MOD_HR)
            makeHROffset(newPos, raw.time);
        return Collections.singletonList(new CatchHitCircle(raw.time, newPos.x, currentCombo));
    }

    @Override
    public Collection<HitObject> convertSlider(Slider raw) {
        if (currentCombo == -1)
            currentCombo = 0;
        else if (raw.newCombo)
            currentCombo = (currentCombo + 1) % 4;

        lastStartX = raw.points.get(raw.points.size() - 1).x;
        lastStartTime = raw.time;

        CatchJuiceStream juiceStream = new CatchJuiceStream(raw, random, currentCombo);
        juiceStream.updateCalculation(beatmap, settings.modEZHR == Settings.MOD_HR);
        return Collections.unmodifiableList(juiceStream.fruits);
    }

    @Override
    public Collection<HitObject> convertSpinner(Spinner raw) {
        CatchSpinner spinner = new CatchSpinner(raw, random);
        spinner.updateCalculation();
        return Collections.unmodifiableList(spinner.bananas);
    }

    @Override
    public Collection<HitObject> convertHold(Hold raw) {
        return Collections.emptyList();
    }

    @Override
    public void postProcessing() {
        double adjustedCS = settings.modEZHR == Settings.MOD_EZ ? beatmap.circleSize / 2 :
                settings.modEZHR == Settings.MOD_NM ? beatmap.circleSize : beatmap.circleSize * 1.4;
        adjustedCS = Math.max(0, Math.min(10, adjustedCS));
        float catcherWidth = (float) (106.75f * (1.7f - 0.14f * adjustedCS));
        float halfCatcherWidth = catcherWidth / 2;
        initHyperDash(halfCatcherWidth);
    }

    private void initHyperDash(float halfCatcherWidth) {
        int lastDirection = 0;
        float lastExcess = halfCatcherWidth;

        for (int i = 0; i < beatmap.processedHitObjects.size() - 1; ++i) {
            HitObject currentHitObject = beatmap.processedHitObjects.get(i);
            if (!(currentHitObject instanceof CatchHitObject))
                continue;

            CatchHitObject currentObject = (CatchHitObject) currentHitObject;
            if (currentObject instanceof CatchBanana || currentObject instanceof CatchTinyDroplet)
                continue;

            HitObject nextHitObject = beatmap.processedHitObjects.get(i + 1);
            while (!(nextHitObject instanceof CatchHitCircle || nextHitObject instanceof CatchDroplet)) {
                if (++i == beatmap.processedHitObjects.size() - 1) break;
                nextHitObject = beatmap.processedHitObjects.get(i + 1);
            }

            CatchHitObject nextObject = (CatchHitObject) nextHitObject;
            int thisDirection = nextObject.x > currentObject.x ? 1 : -1;
            float timeToNext = nextObject.time - currentObject.time - (float) (SIXTY_FRAME_TIME / 4);
            float distanceToNext = Math.abs(nextObject.x - currentObject.x) - (lastDirection == thisDirection ? lastExcess : halfCatcherWidth);

            if (timeToNext < distanceToNext) {
                currentObject.hyperDash = true;
                lastExcess = halfCatcherWidth;
            } else {
                currentObject.distanceToHyperDash = timeToNext - distanceToNext;
                lastExcess = Math.max(0, Math.min(halfCatcherWidth, timeToNext - distanceToNext));
            }

            lastDirection = thisDirection;
        }
    }

    private void makeHROffset(Vec2F pos, int time) {
        if (lastStartX == 0) {
            lastStartX = pos.x;
            lastStartTime = time;
            return;
        }

        float diff = lastStartX - pos.x;
        int timeDiff = time - lastStartTime;

        if (timeDiff > 1000) {
            lastStartX = pos.x;
            lastStartTime = time;
            return;
        }

        if (diff == 0) {
            boolean right = random.nextBoolean();
            float rand = Math.min(20, random.next(0, timeDiff / 4));
            if (right) {
                if (pos.x + rand <= 512)
                    pos.x += rand;
                else
                    pos.x -= rand;
            } else {
                if (pos.x - rand >= 0)
                    pos.x -= rand;
                else
                    pos.x += rand;
            }
            return;
        }

        // Intentionally this is integer division
        // noinspection IntegerDivisionInFloatingPointContext
        if (Math.abs(diff) < timeDiff / 3) {
            if (diff > 0) {
                if (pos.x - diff > 0)
                    pos.x -= diff;
            } else {
                if (pos.x - diff < 512)
                    pos.x -= diff;
            }
        }

        lastStartX = pos.x;
        lastStartTime = time;
    }

}
