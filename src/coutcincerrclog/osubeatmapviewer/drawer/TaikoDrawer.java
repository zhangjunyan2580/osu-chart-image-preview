package coutcincerrclog.osubeatmapviewer.drawer;

import coutcincerrclog.osubeatmapviewer.Settings;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.TimingPoint;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko.TaikoDrumroll;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko.TaikoHitCircle;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko.TaikoSpinner;
import coutcincerrclog.osubeatmapviewer.util.Vec2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaikoDrawer extends Drawer {

    public static final int WIDTH = 1600;
    public static final int LEFT_PADDING = 50;
    public static final int RIGHT_PADDING = 50;
    public static final int BEATS_PER_ROW = 16;

    public static final int ROW_START = 100;
    public static final int ROW_BOUNDARY_HEIGHT = 1;
    public static final int ROW_HEIGHT = 33;
    public static final int ROW_SPACING = 40;
    public static final int BOTTOM_PADDING = 20;

    public static final int FINISHER_RADIUS = 15;
    public static final int FINISHER_INTERIOR_RADIUS = 12;
    public static final int DRUM_HIT_RADIUS = 10;
    public static final int DRUM_HIT_INTERIOR_RADIUS = 8;
    public static final int DRUMROLL_TICK_RADIUS = 3;
    public static final int DRUMROLL_TICK_INTERIOR_RADIUS = 2;
    public static final int SPINNER_RADIUS = 5;
    public static final int SPINNER_INTERIOR_RADIUS = 3;

    public static final Color BORDER_COLOR = Color.WHITE;

    public static final Color BACKGROUND_COLOR = Color.BLACK;
    public static final Color ROW_COLOR = Color.GRAY;
    public static final Color ROW_BORDER_COLOR = Color.LIGHT_GRAY;
    public static final Color MEASURE_LINE_COLOR = Color.WHITE;
    public static final Color BEAT_LINE_COLOR = Color.LIGHT_GRAY;

    public static final Color DON_COLOR = new Color(0xEB462D);
    public static final Color KAT_COLOR = new Color(0x448FAE);
    public static final Color DRUMROLL_COLOR = new Color(0xFBB909);
    public static final Color SPINNER_COLOR = Color.BLACK;

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
                double beatLength = getStandardizedBeatLength(uninheritedPoints.get(i).beatLength, 60000. / bpm);
                virtualBeats += (sectionEnd - sectionStart) / beatLength;
            }
        }
        int rows = getBeatCoordinates(virtualBeats).y + 1;
        return new Dimension(WIDTH, ROW_START + rows * (ROW_HEIGHT + 2 * ROW_BOUNDARY_HEIGHT + ROW_SPACING) - ROW_SPACING + BOTTOM_PADDING);
    }

    @Override
    public void draw(Graphics2D g, Dimension dimension, Beatmap beatmap, Settings settings) {
        if (beatmap.processedHitObjects.isEmpty())
            return;

        List<TimingPoint> uninheritedPoints = beatmap.timingPoints.stream().filter(t -> t.uninherited).collect(Collectors.toList());
        if (uninheritedPoints.isEmpty())
            uninheritedPoints.add(new TimingPoint(0, 60000.0 / 150.0, 4, 0, 0, 100, true, 0));

        TimingPoint firstTimingPoint = uninheritedPoints.get(0);
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
        if (bpm == 0)
            bpm = 150;
        double[] cumulativeBeats = new double[uninheritedPoints.size()];
        for (int i = 0; i < uninheritedPoints.size(); ++i) {
            double beatLength = getStandardizedBeatLength(uninheritedPoints.get(i == 0 ? 0 : i - 1).beatLength, 60000. / bpm);
            double lastTime = (i == 0 ? startTime : uninheritedPoints.get(i - 1).time);
            cumulativeBeats[i] = (i == 0 ? 0 : cumulativeBeats[i - 1]) + (uninheritedPoints.get(i).time - lastTime) / beatLength;
        }

        TimingPoint lastTimingPoint = uninheritedPoints.get(uninheritedPoints.size() - 1);
        double totalBeats = cumulativeBeats[uninheritedPoints.size() - 1] + (endTime - lastTimingPoint.time) / getStandardizedBeatLength(lastTimingPoint.beatLength, 60000. / bpm);
        int rows = getBeatCoordinates(totalBeats).y + 1;

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, dimension.width, dimension.height);

        for (int i = 0; i < rows; ++i) {
            g.setColor(ROW_BORDER_COLOR);
            int y = getRowStartY(i);
            g.drawLine(LEFT_PADDING, y, WIDTH - RIGHT_PADDING, y);
            g.drawLine(LEFT_PADDING, y + ROW_BOUNDARY_HEIGHT + ROW_HEIGHT, WIDTH - RIGHT_PADDING, y + ROW_BOUNDARY_HEIGHT + ROW_HEIGHT);
            g.setColor(ROW_COLOR);
            g.fillRect(LEFT_PADDING, y + 1, WIDTH - RIGHT_PADDING - LEFT_PADDING + 1, ROW_HEIGHT);
        }

        {
            Map<Integer, Double> timeToBeatsMapping = new HashMap<>();
            List<Integer> times = new ArrayList<>();
            for (HitObject hitObject : beatmap.processedHitObjects) {
                if (hitObject instanceof TaikoHitCircle || hitObject instanceof TaikoSpinner)
                    times.add(hitObject.time);
                else if (hitObject instanceof TaikoDrumroll) {
                    times.addAll(((TaikoDrumroll) hitObject).tickTimes);
                    times.add(((TaikoDrumroll) hitObject).endTime);
                }
            }
            times = times.stream().sorted().distinct().collect(Collectors.toList());
            int currentTimingPointIndex = 0;
            double currentBeatLength = getStandardizedBeatLength(uninheritedPoints.get(0).beatLength, 60000. / bpm);
            double currentTimingTime = uninheritedPoints.get(0).time;
            TimingPoint nextTimingPoint = uninheritedPoints.size() == 1 ? null : uninheritedPoints.get(1);
            for (int time : times) {
                while (nextTimingPoint != null && time >= nextTimingPoint.time) {
                    ++currentTimingPointIndex;
                    currentTimingTime = nextTimingPoint.time;
                    currentBeatLength = getStandardizedBeatLength(nextTimingPoint.beatLength, 60000. / bpm);
                    nextTimingPoint = currentTimingPointIndex == uninheritedPoints.size() - 1 ? null : uninheritedPoints.get(currentTimingPointIndex + 1);
                }
                timeToBeatsMapping.put(time, cumulativeBeats[currentTimingPointIndex] + (time - currentTimingTime) / currentBeatLength);
            }
            for (int i = beatmap.processedHitObjects.size() - 1; i >= 0; --i) {
                HitObject hitObject = beatmap.processedHitObjects.get(i);
                if (hitObject instanceof TaikoHitCircle) {
                    TaikoHitCircle hitCircle = (TaikoHitCircle) hitObject;
                    drawCircle(timeToBeatsMapping.get(hitObject.time), g,
                            hitCircle.finisher ? FINISHER_RADIUS : DRUM_HIT_RADIUS,
                            hitCircle.finisher ? FINISHER_INTERIOR_RADIUS : DRUM_HIT_INTERIOR_RADIUS,
                            hitCircle.colorKat ? KAT_COLOR : DON_COLOR);
                } else if (hitObject instanceof TaikoDrumroll) {
                    TaikoDrumroll drumroll = (TaikoDrumroll) hitObject;
                    double startBeats = timeToBeatsMapping.get(drumroll.time);
                    double endBeats = timeToBeatsMapping.get(drumroll.endTime);
                    Vec2D end = getBeatCoordinates(endBeats), start = getBeatCoordinates(startBeats);
                    int radius = drumroll.finisher ? FINISHER_RADIUS : DRUM_HIT_RADIUS;
                    int interiorRadius = drumroll.finisher ? FINISHER_INTERIOR_RADIUS : DRUM_HIT_INTERIOR_RADIUS;
                    drawCircle(endBeats, g, radius, interiorRadius, DRUMROLL_COLOR);
                    if (start.y == end.y)
                        drawRollRectangle(start.x, end.x, start.y, g, radius, interiorRadius);
                    else {
                        drawRollRectangle(start.x, WIDTH - RIGHT_PADDING, start.y, g, radius, interiorRadius);
                        drawRollRectangle(LEFT_PADDING, end.x, end.y, g, radius, interiorRadius);
                        for (int row = start.y; row < end.y; ++row)
                            drawRollRectangle(LEFT_PADDING, WIDTH - RIGHT_PADDING, row, g, radius, interiorRadius);
                    }
                    drawCircle(startBeats, g, radius, interiorRadius, DRUMROLL_COLOR);
                    for (int tickTime : drumroll.tickTimes)
                        if (tickTime != drumroll.time)
                            drawCircle(timeToBeatsMapping.get(tickTime), g,
                                    DRUMROLL_TICK_RADIUS, DRUMROLL_TICK_INTERIOR_RADIUS, DRUMROLL_COLOR);
                } else if (hitObject instanceof TaikoSpinner) {
                    double beats = timeToBeatsMapping.get(hitObject.time);
                    drawCircle(beats, g, DRUM_HIT_RADIUS, DRUM_HIT_INTERIOR_RADIUS, SPINNER_COLOR);
                    drawCircle(beats, g, SPINNER_RADIUS, SPINNER_INTERIOR_RADIUS, SPINNER_COLOR);
                }
            }
        }
    }

    public static double getStandardizedBeatLength(double beatLength, double fallback) {
        if (beatLength < 6e-2 || beatLength > 6e6)
            return fallback;
        while (beatLength < 50)
            beatLength *= 2;
        while (beatLength > 3200)
            beatLength /= 2;
        return beatLength;
    }

    public static int getRowStartY(int row) {
        return ROW_START + row * (ROW_HEIGHT + 2 * ROW_BOUNDARY_HEIGHT + ROW_SPACING);
    }

    public static Vec2D getBeatCoordinates(double beat) {
        double rowPos = beat / BEATS_PER_ROW;
        int row = (int) Math.floor(rowPos);
        int x = (int) Math.round((rowPos - row) * (WIDTH - LEFT_PADDING - RIGHT_PADDING) + LEFT_PADDING);
        if (x >= WIDTH - RIGHT_PADDING) {
            x = LEFT_PADDING;
            row += 1;
        }
        return new Vec2D(x, row);
    }

    public static void drawCircle(double beat, Graphics2D g, int radius, int interiorRadius, Color interior) {
        Vec2D coord = getBeatCoordinates(beat);
        int x = coord.x, y = getRowStartY(coord.y) + ROW_HEIGHT / 2 + ROW_BOUNDARY_HEIGHT;
        g.setColor(BORDER_COLOR);
        g.fillOval(x - radius - 1, y - radius - 1, radius * 2 + 2, radius * 2 + 2);
        g.setColor(interior);
        g.fillOval(x - interiorRadius - 1, y - interiorRadius - 1, interiorRadius * 2 + 2, interiorRadius * 2 + 2);
    }

    public static void drawRollRectangle(int startX, int endX, int row, Graphics2D g, int radius, int interiorRadius) {
        int y = getRowStartY(row) + ROW_HEIGHT / 2 + ROW_BOUNDARY_HEIGHT;
        g.setColor(BORDER_COLOR);
        g.fillRect(startX, y - radius, endX - startX + 1, radius - interiorRadius);
        g.fillRect(startX, y + interiorRadius + 1, endX - startX + 1, radius - interiorRadius);
        g.setColor(DRUMROLL_COLOR);
        g.fillRect(startX, y - interiorRadius, endX - startX + 1, 2 * interiorRadius + 1);
    }

}
