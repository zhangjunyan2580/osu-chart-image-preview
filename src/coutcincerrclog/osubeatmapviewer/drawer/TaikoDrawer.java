package coutcincerrclog.osubeatmapviewer.drawer;

import coutcincerrclog.osubeatmapviewer.Settings;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.TimingPoint;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko.TaikoDrumroll;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko.TaikoSpinner;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class TaikoDrawer extends Drawer {

    public static final int WIDTH = 1600;
    public static final int LEFT_PADDING = 50;
    public static final int RIGHT_PADDING = 50;

    public static final int ROW_START = 100;
    public static final int ROW_BOUNDARY_HEIGHT = 1;
    public static final int ROW_HEIGHT = 16;
    public static final int ROW_SPACING = 20;
    public static final int BOTTOM_PADDING = 10;

    public static final int FINISHER_RADIUS = 16;
    public static final int FINISHER_INTERIOR_RADIUS = 13;
    public static final int DRUM_HIT_RADIUS = 11;
    public static final int DRUM_HIT_INTERIOR_RADIUS = 8;
    public static final Color BORDER_COLOR = Color.WHITE;

    public static final Color BACKGROUND_COLOR = Color.BLACK;
    public static final Color ROW_COLOR = Color.GRAY;
    public static final Color ROW_BORDER_COLOR = Color.LIGHT_GRAY;
    public static final Color MEASURE_LINE_COLOR = Color.WHITE;
    public static final Color BEAT_LINE_COLOR = Color.LIGHT_GRAY;

    public static final Color DON_COLOR = new Color(0xEB462D);
    public static final Color KAT_COLOR = new Color(0x448FAE);
    public static final Color ROLL_COLOR = new Color(0xFBB909);

    @Override
    public Dimension getPreferredSize(Beatmap beatmap, Settings settings) {
        if (beatmap.processedHitObjects.isEmpty())
            return new Dimension(1, 1);

        List<TimingPoint> uninheritedPoints = beatmap.timingPoints.stream().filter(t -> t.uninherited).collect(Collectors.toList());
        TimingPoint firstTimingPoint = uninheritedPoints.isEmpty() ?
                new TimingPoint(0, 60000.0 / 150.0, 4, 0, 0, 100, true, 0) :
                uninheritedPoints.get(0);

        HitObject startObject = beatmap.processedHitObjects.get(0);
        int paddingMeasures = startObject.time >= firstTimingPoint.time ? 0 :
                (int) Math.ceil((firstTimingPoint.time - startObject.time - 1) / firstTimingPoint.beatLength);
        int startTime = (int) Math.round(firstTimingPoint.time - paddingMeasures * firstTimingPoint.beatLength);
        int endTime = beatmap.processedHitObjects.stream().mapToInt(hitObject -> {
            if (hitObject instanceof TaikoDrumroll)
                return Math.max(hitObject.time, ((TaikoDrumroll) hitObject).endTime);
            else if (hitObject instanceof TaikoSpinner)
                return Math.max(hitObject.time, ((TaikoSpinner) hitObject).endTime);
            return hitObject.time;
        }).max().getAsInt();

        int bpm = beatmap.getPrimaryBPM();
        double virtualBeats = 0;
        if (uninheritedPoints.size() <= 1)
            virtualBeats = (endTime - startTime) / firstTimingPoint.beatLength;
        else {
            for (int i = 0; i < uninheritedPoints.size(); ++i) {
                double sectionStart = i == 0 ? startTime : uninheritedPoints.get(i).time;
                double sectionEnd = i == uninheritedPoints.size() - 1 ? endTime : Math.min(uninheritedPoints.get(i + 1).time, endTime);
                double beatLength = uninheritedPoints.get(i).beatLength;
                while (beatLength < 50)
                    beatLength *= 2;
                while (beatLength > 3200)
                    beatLength /= 2;
                virtualBeats += (sectionEnd - sectionStart) / uninheritedPoints.get(i).beatLength;
            }
        }
        int rows = (int) Math.ceil(virtualBeats / 16);
        return new Dimension(WIDTH, ROW_START + rows * (ROW_HEIGHT + 2 * ROW_BOUNDARY_HEIGHT + ROW_SPACING) - ROW_SPACING + BOTTOM_PADDING);
    }

    @Override
    public void draw(Graphics2D g, Beatmap beatmap, Settings settings) {
    }

}
