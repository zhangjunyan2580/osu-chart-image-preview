package coutcincerrclog.osubeatmapviewer.drawer;

import coutcincerrclog.osubeatmapviewer.Settings;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.TimingPoint;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.fruits.*;
import coutcincerrclog.osubeatmapviewer.util.MathUtil;
import coutcincerrclog.osubeatmapviewer.util.Vec2D;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.stream.Collectors;

public class CatchDrawer extends Drawer {

    public static final int TOP_PADDING = 100;
    public static final int TOP_SPACE = 20;
    public static final int BOTTOM_SPACE = 20;
    public static final int BOTTOM_PADDING = 10;

    public static final int SECTION_SPACE = 70;
    public static final int SECTION_WIDTH = 192;
    public static final int SECTION_BORDER_WIDTH = 1;
    public static final int MEASURE_LINE_HEIGHT = 1;
    public static final int TIMING_LENGTH = 50;

    public static final int LEFT_PADDING = 30;
    public static final int RIGHT_PADDING = 30;

    public static final int CATCHER_Y = 440;
    public static final int GAME_FIELD_WIDTH = 512;
    public static final int WINDOW_WIDTH = 640;

    public static final double FRUIT_BASE_SIZE = 64;

    public static final Color BACKGROUND_COLOR = Color.BLACK;
    public static final Color SECTION_BORDER_COLOR = Color.WHITE;
    public static final Color MEASURE_LINE_COLOR = Color.WHITE;
    public static final Color UNINHERITED_TIMING_COLOR = new Color(0xE13030);

    public static final Color DEFAULT_FRUIT_BORDER_COLOR = Color.WHITE;
    public static final Color BANANA_COLOR = new Color(0xFEF743);
    public static final Color HYPER_DASH_COLOR = Color.RED;

    public static final Area FRUIT_BASE_SHAPE;
    public static final Area HYPER_DASH_BASE_SHAPE;

    public static final Font NUMBER_FONT = new Font("Consolas", Font.PLAIN, 12);

    static {
        Ellipse2D FRUIT_OUTER = new Ellipse2D.Double(-0.5, -0.5, 1, 1);
        Ellipse2D FRUIT_INNER = new Ellipse2D.Double(-0.4, -0.4, 0.8, 0.8);
        FRUIT_BASE_SHAPE = new Area(FRUIT_OUTER);
        FRUIT_BASE_SHAPE.subtract(new Area(FRUIT_INNER));

        Ellipse2D HYPER_DASH_OUTER = new Ellipse2D.Double(-0.55, -0.55, 1.1, 1.1);
        HYPER_DASH_BASE_SHAPE = new Area(HYPER_DASH_OUTER);
        HYPER_DASH_BASE_SHAPE.subtract(new Area(FRUIT_OUTER));
    }

    public static int getApproachTime(float approachingRate, int mod) {
        return (int) MathUtil.mapDifficultyRange(approachingRate, 1800, 1200, 450, mod);
    }

    @Override
    public Dimension getPreferredSize(Beatmap beatmap, Settings settings) {
        double bpm = beatmap.getPrimaryBPM();
        if (bpm == 0)
            bpm = 150;
        while (bpm < 30)
            bpm *= 2;
        while (bpm > 480)
            bpm /= 2;

        TimingPoint firstTimingPoint = null;
        for (TimingPoint timingPoint : beatmap.timingPoints) {
            if (timingPoint.uninherited) {
                firstTimingPoint = timingPoint;
                break;
            }
        }
        if (firstTimingPoint == null)
            firstTimingPoint = new TimingPoint(0, 60000.0 / 150.0, 4, 0, 0, 100, true, 0);

        int approachTime = getApproachTime(beatmap.approachRate, settings.modEZHR);
        double timeForSection = 8 * 60000. * firstTimingPoint.meter / bpm;
        double totalY = timeForSection / approachTime * CATCHER_Y;
        int sectionY = (int) Math.ceil(totalY / WINDOW_WIDTH * SECTION_WIDTH);

        if (beatmap.processedHitObjects.isEmpty())
            return new Dimension(1, 1);

        HitObject startObject = beatmap.processedHitObjects.get(0);
        int paddingBeats = startObject.time >= firstTimingPoint.time ? 0 :
                (int) Math.ceil((firstTimingPoint.time - startObject.time - 1) / firstTimingPoint.beatLength);
        int startTime = (int) Math.round(firstTimingPoint.time - paddingBeats * firstTimingPoint.beatLength);
        int endTime = beatmap.processedHitObjects.get(beatmap.processedHitObjects.size() - 1).time;
        int section = (int) Math.ceil((endTime - startTime) / timeForSection);
        return new Dimension(LEFT_PADDING + RIGHT_PADDING + (SECTION_SPACE + SECTION_WIDTH + 2 * SECTION_BORDER_WIDTH) * section - SECTION_SPACE, sectionY + TOP_PADDING + TOP_SPACE + BOTTOM_SPACE + BOTTOM_PADDING);
    }

    @Override
    public void draw(Graphics2D g, Dimension dimension, Beatmap beatmap, Settings settings) {
        if (beatmap.processedHitObjects.isEmpty())
            return;

        double bpm = beatmap.getPrimaryBPM();
        if (bpm == 0)
            bpm = 150;
        while (bpm < 30)
            bpm *= 2;
        while (bpm > 480)
            bpm /= 2;

        List<TimingPoint> uninheritedPoints = beatmap.timingPoints.stream().filter(t -> t.uninherited).collect(Collectors.toList());
        TimingPoint firstTimingPoint = uninheritedPoints.isEmpty() ? new TimingPoint(0, 60000.0 / 150.0, 4, 0, 0, 100, true, 0) : uninheritedPoints.get(0);

        int approachTime = getApproachTime(beatmap.approachRate, settings.modEZHR);
        double timeForSection = 8 * 60000. * firstTimingPoint.meter / bpm;
        double totalY = timeForSection / approachTime * CATCHER_Y;
        int sectionY = (int) Math.ceil(totalY / WINDOW_WIDTH * SECTION_WIDTH);

        HitObject startObject = beatmap.processedHitObjects.get(0);
        int paddingBeats = startObject.time >= firstTimingPoint.time ? 0 :
                (int) Math.ceil((firstTimingPoint.time - startObject.time - 1) / firstTimingPoint.beatLength);
        int startTime = (int) Math.round(firstTimingPoint.time - paddingBeats * firstTimingPoint.beatLength);
        int endTime = beatmap.processedHitObjects.get(beatmap.processedHitObjects.size() - 1).time;
        int section = (int) Math.ceil((endTime - startTime) / timeForSection);

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, LEFT_PADDING + RIGHT_PADDING + (SECTION_SPACE + SECTION_WIDTH + 2 * SECTION_BORDER_WIDTH) * section - SECTION_SPACE, sectionY + TOP_PADDING + TOP_SPACE + BOTTOM_SPACE + BOTTOM_PADDING);

        g.setColor(SECTION_BORDER_COLOR);
        for (int i = 0; i < section; ++i) {
            int x = getSectionStartX(i);
            g.fillRect(x, TOP_PADDING, SECTION_BORDER_WIDTH, sectionY + TOP_SPACE + BOTTOM_SPACE);
            g.fillRect(x + SECTION_WIDTH + SECTION_BORDER_WIDTH, TOP_PADDING, SECTION_BORDER_WIDTH, sectionY + TOP_SPACE + BOTTOM_SPACE);
        }

        if (!uninheritedPoints.isEmpty()) {
            g.setColor(MEASURE_LINE_COLOR);
            for (int i = 0; i < uninheritedPoints.size(); ++i) {
                TimingPoint currentTimingPoint = uninheritedPoints.get(i);
                double sectionStart = i == 0 ? startTime : currentTimingPoint.time;
                double sectionEnd = i == uninheritedPoints.size() - 1 ? endTime : uninheritedPoints.get(i + 1).time;
                double step = currentTimingPoint.meter * currentTimingPoint.beatLength;
                step = step * Math.ceil(5 / step);
                for (double time = sectionStart; time < sectionEnd; time += step) {
                    Vec2D coord = getDrawCoordinates(time, startTime, timeForSection, approachTime);
                    int x = getSectionStartX(coord.x);
                    g.fillRect(x + SECTION_BORDER_WIDTH, coord.y, SECTION_WIDTH, MEASURE_LINE_HEIGHT);
                }
            }

            g.setColor(UNINHERITED_TIMING_COLOR);
            g.setFont(NUMBER_FONT);
            Vec2D lastCoord = null;
            for (int i = uninheritedPoints.size() - 1; i >= 0; --i) {
                TimingPoint currentTimingPoint = uninheritedPoints.get(i);
                Vec2D coord = getDrawCoordinates(currentTimingPoint.time, startTime, timeForSection, approachTime);
                int x = getSectionStartX(coord.x);
                g.fillRect(x + SECTION_BORDER_WIDTH, coord.y, SECTION_WIDTH + SECTION_BORDER_WIDTH + TIMING_LENGTH, MEASURE_LINE_HEIGHT);
                if (lastCoord == null || lastCoord.x > coord.x || lastCoord.y < coord.y - 10) {
                    double curBPM = Math.abs(60000 / currentTimingPoint.beatLength);
                    String BPMString = curBPM >= 10000 ? "inf" :
                            curBPM < 0.1 ? "0" :
                            String.format("%.5g", curBPM);
                    g.drawString(BPMString, x + SECTION_BORDER_WIDTH * 2 + SECTION_WIDTH + 1, coord.y - 1);
                }
                lastCoord = coord;
            }
        }

        double adjustedCircleSize = settings.modEZHR == Settings.MOD_EZ ? beatmap.circleSize / 2 :
                settings.modEZHR == Settings.MOD_NM ? beatmap.circleSize : beatmap.circleSize * 1.3;
        adjustedCircleSize = Math.max(0, Math.min(10, adjustedCircleSize));
        double cs2 = (adjustedCircleSize - 5) / 5;
        float fruitSize = (float) (FRUIT_BASE_SIZE * (1f - 0.7f * cs2) / WINDOW_WIDTH * SECTION_WIDTH);
        float dropletSize = fruitSize * 0.8f;
        float tinyDropletSize = fruitSize * 0.4f;

        Area fruitShape = FRUIT_BASE_SHAPE.createTransformedArea(AffineTransform.getScaleInstance(fruitSize, fruitSize));
        Area dropletShape = FRUIT_BASE_SHAPE.createTransformedArea(AffineTransform.getScaleInstance(dropletSize, dropletSize));
        Area tinyDropletShape = FRUIT_BASE_SHAPE.createTransformedArea(AffineTransform.getScaleInstance(tinyDropletSize, tinyDropletSize));

        Area fruitHyperDashShape = HYPER_DASH_BASE_SHAPE.createTransformedArea(AffineTransform.getScaleInstance(fruitSize, fruitSize));
        Area dropletHyperDashShape = HYPER_DASH_BASE_SHAPE.createTransformedArea(AffineTransform.getScaleInstance(dropletSize, dropletSize));

        for (int i = beatmap.processedHitObjects.size() - 1; i >= 0; --i) {
            HitObject hitObject = beatmap.processedHitObjects.get(i);
            if (hitObject instanceof CatchHitObject) {
                CatchHitObject catchHitObject = (CatchHitObject) hitObject;
                Vec2D coord = getDrawCoordinates(hitObject.time, startTime, timeForSection, approachTime);
                int x = getSectionStartX(coord.x) + getOffsetX(catchHitObject.x);
                AffineTransform translate = AffineTransform.getTranslateInstance(x, coord.y);
                if (hitObject instanceof CatchHitCircle) {
                    Area translated = fruitShape.createTransformedArea(translate);
                    g.setColor(DEFAULT_FRUIT_BORDER_COLOR);
                    g.fill(translated);
                    if (catchHitObject.hyperDash) {
                        translated = fruitHyperDashShape.createTransformedArea(translate);
                        g.setColor(HYPER_DASH_COLOR);
                        g.fill(translated);
                    }
                } else if (hitObject instanceof CatchDroplet) {
                    Area translated = dropletShape.createTransformedArea(translate);
                    g.setColor(DEFAULT_FRUIT_BORDER_COLOR);
                    g.fill(translated);
                    if (catchHitObject.hyperDash) {
                        translated = dropletHyperDashShape.createTransformedArea(translate);
                        g.setColor(HYPER_DASH_COLOR);
                        g.fill(translated);
                    }
                } else if (hitObject instanceof CatchTinyDroplet) {
                    Area translated = tinyDropletShape.createTransformedArea(translate);
                    g.setColor(DEFAULT_FRUIT_BORDER_COLOR);
                    g.fill(translated);
                } else if (hitObject instanceof CatchBanana) {
                    Area translated = fruitShape.createTransformedArea(translate);
                    g.setColor(BANANA_COLOR);
                    g.fill(translated);
                }
            }
        }
    }

    public static int getSectionStartX(int section) {
        return LEFT_PADDING + section * (SECTION_SPACE + SECTION_WIDTH + SECTION_BORDER_WIDTH * 2);
    }

    public static Vec2D getDrawCoordinates(double time, double startTime, double timeForSection, int approachTime) {
        double totalSection = (time - startTime) / timeForSection;
        int section = (int) Math.floor(totalSection);
        double remain = (1 - (totalSection - section)) * timeForSection;
        int ySection = (int) Math.round(remain / approachTime * CATCHER_Y * SECTION_WIDTH / WINDOW_WIDTH);
        if (ySection <= 1) {
            ySection = (int) Math.round(timeForSection * CATCHER_Y * SECTION_WIDTH / (WINDOW_WIDTH * approachTime));
            ++section;
        }
        return new Vec2D(section, ySection + TOP_PADDING);
    }

    public static int getOffsetX(double x) {
        return (int) Math.round((x - (GAME_FIELD_WIDTH - WINDOW_WIDTH) / 2.0) * SECTION_WIDTH / WINDOW_WIDTH);
    }

}
