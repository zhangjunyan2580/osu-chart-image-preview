package coutcincerrclog.osubeatmapviewer.drawer;

import coutcincerrclog.osubeatmapviewer.Settings;
import coutcincerrclog.osubeatmapviewer.util.Vec2D;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.TimingPoint;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.mania.ManiaHitCircle;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.mania.ManiaHold;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ManiaDrawer extends Drawer {

    public static final int LANE_BOTTOM_Y = 3100;
    public static final int LANE_TOP_Y = 90;

    public static final int SECTION_BOTTOM_Y = 3095;
    public static final int SECTION_TOP_Y = 95;

    public static final int COLUMN_WIDTH = 10;
    public static final int COLUMN_SPACING = 1;
    public static final int SECTION_SPACING = 60;

    public static final int NOTE_HEIGHT = 5;

    public static final int LEFT_PADDING = 10;
    public static final int RIGHT_PADDING = 10;

    public static final int MILLISECONDS_TO_PIXEL_DIV = 5;
    public static final int SECTION_TIME_SPAN = (SECTION_BOTTOM_Y - SECTION_TOP_Y) * MILLISECONDS_TO_PIXEL_DIV;

    public static final Color BAR_LINE_COLOR = new Color(0x969696);
    public static final Color COLUMN_LINE_COLOR = new Color(0x2B2B2B);
    public static final Color BACKGROUND_COLOR = new Color(0x000000);

    public static final int[] SNAP_INVERTED = { 48, 24, 16, 12, 8, 6, 4, 3, 1 };
    public static final Color[] SNAP_COLOR = {
            new Color(0xED2928),
            new Color(0x3C8AD7),
            new Color(0x7E2EFA),
            new Color(0xFCEB2C),
            new Color(0xE829E7),
            new Color(0xEC8F2C),
            new Color(0x2AE0E3),
            new Color(0x2CE930),
            new Color(0x9A9A9A)
    };

    public static final Color[] SNAP_COLOR_EDGE = {
            new Color(0xF69494),
            new Color(0x9EC5EB),
            new Color(0xBF97FD),
            new Color(0xFEF596),
            new Color(0xF494F3),
            new Color(0xF6C796),
            new Color(0x95F0F1),
            new Color(0x96F498),
            new Color(0xCDCDCD)
    };

    public static final Color W = new Color(0xC2C2C2);
    public static final Color B = new Color(0x2AE0E3);
    public static final Color Y = new Color(0xFCEB2C);

    public static final Color WL = new Color(0xE1E1E1);
    public static final Color BL = new Color(0x95F0F1);
    public static final Color YL = new Color(0xFEF596);

    public static final Color[][] COLUMN_COLOR = {
            { Y },
            { W, W },
            { W, Y, W },
            { W, B, B, W },
            { W, B, Y, B, W },
            { W, B, W, W, B, W },
            { W, B, W, Y, W, B, W },
            { W, B, W, B, W, B, W, Y },
            { W, B, W, B, Y, B, W, B, W },
            { W, B, W, B, W, W, B, W, B, W },
            { W, B, W, B, W, Y, W, B, W, B, W },
            { W, B, W, W, B, W, W, B, W, W, B, W },
            { W, B, W, W, B, W, Y, W, B, W, W, B, W },
            { W, B, W, Y, W, B, W, W, B, W, Y, W, B, W },
            { W, B, W, B, W, B, W, Y, W, B, W, B, W, B, W },
            { W, B, W, B, B, W, B, W, W, B, W, B, B, W, B, W },
            { W, B, W, B, B, W, B, W, Y, W, B, W, B, B, W, B, W },
            { W, B, W, B, Y, B, W, B, W, W, B, W, B, Y, B, W, B, W }
    };

    public static final Color[][] COLUMN_COLOR_EDGE = {
            { YL },
            { WL, WL },
            { WL, YL, WL },
            { WL, BL, BL, WL },
            { WL, BL, YL, BL, WL },
            { WL, BL, WL, WL, BL, WL },
            { WL, BL, WL, YL, WL, BL, WL },
            { WL, BL, WL, BL, WL, BL, WL, YL },
            { WL, BL, WL, BL, YL, BL, WL, BL, WL },
            { WL, BL, WL, BL, WL, WL, BL, WL, BL, WL },
            { WL, BL, WL, BL, WL, YL, WL, BL, WL, BL, WL },
            { WL, BL, WL, WL, BL, WL, WL, BL, WL, WL, BL, WL },
            { WL, BL, WL, WL, BL, WL, YL, WL, BL, WL, WL, BL, WL },
            { WL, BL, WL, YL, WL, BL, WL, WL, BL, WL, YL, WL, BL, WL },
            { WL, BL, WL, BL, WL, BL, WL, YL, WL, BL, WL, BL, WL, BL, WL },
            { WL, BL, WL, BL, BL, WL, BL, WL, WL, BL, WL, BL, BL, WL, BL, WL },
            { WL, BL, WL, BL, BL, WL, BL, WL, YL, WL, BL, WL, BL, BL, WL, BL, WL },
            { WL, BL, WL, BL, YL, BL, WL, BL, WL, WL, BL, WL, BL, YL, BL, WL, BL, WL }
    };

    @Override
    public Dimension getPreferredSize(Beatmap beatmap, Settings settings) {
        if (beatmap.processedHitObjects.isEmpty())
            return new Dimension(1, 1);

        TimingPoint firstTimingPoint = null;
        for (TimingPoint timingPoint : beatmap.timingPoints) {
            if (timingPoint.uninherited) {
                firstTimingPoint = timingPoint;
                break;
            }
        }
        if (firstTimingPoint == null)
            firstTimingPoint = new TimingPoint(0, 60000.0 / 150.0, 4, 0, 0, 100, true, 0);

        HitObject startObject = beatmap.processedHitObjects.get(0);
        int paddingBeats = startObject.time >= firstTimingPoint.time ? 0 :
                (int) Math.ceil((firstTimingPoint.time - startObject.time - 1) / firstTimingPoint.beatLength);
        int startTime = (int) Math.round(firstTimingPoint.time - paddingBeats * firstTimingPoint.beatLength);
        int endTime = beatmap.processedHitObjects.stream().mapToInt(hitObject -> {
            if (hitObject instanceof ManiaHold)
                return Math.max(hitObject.time, ((ManiaHold) hitObject).endTime);
            return hitObject.time;
        }).max().getAsInt();

        int sectionCount = (endTime - startTime) / SECTION_TIME_SPAN + 1;
        int columnCount = Math.round(beatmap.circleSize);
        return new Dimension(LEFT_PADDING + (columnCount * (COLUMN_SPACING + COLUMN_WIDTH) + COLUMN_SPACING + SECTION_SPACING) * sectionCount - SECTION_SPACING + RIGHT_PADDING, LANE_BOTTOM_Y + 1);
    }

    @Override
    public void draw(Graphics2D g, Beatmap beatmap, Settings settings) {
        if (beatmap.processedHitObjects.isEmpty())
            return;

        int[] nextTimingPointIndices = new int[beatmap.timingPoints.size()];
        {
            int start = 0;
            for (int i = 0; i < beatmap.timingPoints.size(); ++i) {
                TimingPoint timingPoint = beatmap.timingPoints.get(i);
                if (timingPoint.uninherited) {
                    for (int j = start; j < i; ++j)
                        nextTimingPointIndices[j] = i;
                    start = i;
                }
            }
            for (int j = start; j < nextTimingPointIndices.length; ++j)
                nextTimingPointIndices[j] = -1;
        }

        int firstTimingPointIndex = beatmap.timingPoints.get(0).uninherited ? 0 : nextTimingPointIndices[0];
        TimingPoint firstTimingPoint = firstTimingPointIndex == -1 ?
                new TimingPoint(0, 60000.0 / 150.0, 4, 0, 0, 100, true, 0) :
                beatmap.timingPoints.get(firstTimingPointIndex);

        HitObject startObject = beatmap.processedHitObjects.get(0);
        int paddingBeats = startObject.time >= firstTimingPoint.time ? 0 :
                (int) Math.ceil((firstTimingPoint.time - startObject.time - 1) / firstTimingPoint.beatLength);
        int startTime = (int) Math.round(firstTimingPoint.time - paddingBeats * firstTimingPoint.beatLength);
        int endTime = beatmap.processedHitObjects.stream().mapToInt(hitObject -> {
            if (hitObject instanceof ManiaHold)
                return Math.max(hitObject.time, ((ManiaHold) hitObject).endTime);
            return hitObject.time;
        }).max().getAsInt();

        int sectionCount = (endTime - startTime) / SECTION_TIME_SPAN + 1;
        int columnCount = Math.round(beatmap.circleSize);

        {
            int totalWidth = LEFT_PADDING + (columnCount * (COLUMN_SPACING + COLUMN_WIDTH) + COLUMN_SPACING + SECTION_SPACING) * sectionCount - SECTION_SPACING + RIGHT_PADDING;
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, totalWidth, LANE_BOTTOM_Y + 1);
        }

        g.setColor(COLUMN_LINE_COLOR);
        for (int sectionIndex = 0; sectionIndex < sectionCount; ++sectionIndex) {
            for (int i = 0; i <= columnCount; ++i) {
                int x = getColumnStartX(sectionIndex, i, columnCount);
                g.drawLine(x, LANE_TOP_Y, x, LANE_BOTTOM_Y);
            }
        }

        if (firstTimingPointIndex != -1) {
            g.setColor(BAR_LINE_COLOR);

            double currentTime = startTime;
            int nextTimingPointIndex = nextTimingPointIndices[firstTimingPointIndex];
            TimingPoint currentTimingPoint = beatmap.timingPoints.get(firstTimingPointIndex);
            while (true) {
                for (Vec2D coord : getDrawCoordinates(startTime, currentTime)) {
                    int sectionIndex = coord.x, y = coord.y;
                    int x = getSectionStartX(sectionIndex, columnCount);
                    g.drawLine(x + COLUMN_SPACING, y, x + (COLUMN_WIDTH + COLUMN_SPACING) * columnCount - COLUMN_SPACING, y);
                }
                if (currentTime >= endTime)
                    break;
                currentTime += currentTimingPoint.beatLength * Math.ceil(5 / currentTimingPoint.beatLength);
                if (nextTimingPointIndex != -1 && beatmap.timingPoints.get(nextTimingPointIndex).time <= currentTime + 1) {
                    currentTimingPoint = beatmap.timingPoints.get(nextTimingPointIndex);
                    nextTimingPointIndex = nextTimingPointIndices[nextTimingPointIndex];
                }
            }
        }

        {
            int nextTimingPointIndex = firstTimingPointIndex == -1 ? -1 : nextTimingPointIndices[firstTimingPointIndex];
            TimingPoint currentTimingPoint = firstTimingPointIndex == -1 ? null : beatmap.timingPoints.get(firstTimingPointIndex);
            for (HitObject hitObject : beatmap.processedHitObjects) {
                if (!(hitObject instanceof ManiaHitCircle) && ! (hitObject instanceof ManiaHold))
                    continue;

                while (nextTimingPointIndex != -1 && beatmap.timingPoints.get(nextTimingPointIndex).time <= hitObject.time) {
                    currentTimingPoint = beatmap.timingPoints.get(nextTimingPointIndex);
                    nextTimingPointIndex = nextTimingPointIndices[nextTimingPointIndex];
                }

                int snapIndex = getSnapIndex(currentTimingPoint, hitObject.time);
                int column = hitObject instanceof ManiaHitCircle ? ((ManiaHitCircle) hitObject).column : ((ManiaHold) hitObject).column;

                Color interiorColor = settings.maniaColorBySnap ? SNAP_COLOR[snapIndex] : COLUMN_COLOR[columnCount - 1][column];
                Color edgeColor = settings.maniaColorBySnap ? SNAP_COLOR_EDGE[snapIndex] : COLUMN_COLOR_EDGE[columnCount - 1][column];
                if (hitObject instanceof ManiaHitCircle) {
                    for (Vec2D coord : getDrawCoordinates(startTime, hitObject.time)) {
                        int sectionIndex = coord.x, y = coord.y;
                        int x = getColumnStartX(sectionIndex, column, columnCount);
                        drawNote(g, x, y, interiorColor, edgeColor);
                    }
                } else {
                    Vec2D coordStart = getPrimaryCoordinate(startTime, hitObject.time);
                    Vec2D coordEnd = getPrimaryCoordinate(startTime, ((ManiaHold) hitObject).endTime);
                    int x = getColumnStartX(coordStart.x, column, columnCount);
                    drawNote(g, x, coordStart.y, interiorColor, edgeColor);
                    if (hitObject.time <= ((ManiaHold) hitObject).endTime) {
                        if (coordStart.x == coordEnd.x) {
                            g.setColor(edgeColor);
                            g.drawRect(x + 1, coordEnd.y + 1, COLUMN_WIDTH - 1, coordStart.y - coordEnd.y - 2);
                            g.setColor(interiorColor);
                            g.fillRect(x + 2, coordEnd.y + 2, COLUMN_WIDTH - 2, coordStart.y - coordEnd.y - 1 - NOTE_HEIGHT);
                        } else {
                            g.setColor(edgeColor);
                            g.drawLine(x + 1, LANE_TOP_Y, x + 1, coordStart.y - NOTE_HEIGHT);
                            g.drawLine(x + COLUMN_WIDTH, LANE_TOP_Y, x + COLUMN_WIDTH, coordStart.y - NOTE_HEIGHT);
                            x = getColumnStartX(coordEnd.x, column, columnCount);
                            g.drawLine(x + 1, coordEnd.y + 1, x + 1, LANE_BOTTOM_Y);
                            g.drawLine(x + COLUMN_WIDTH, coordEnd.y + 1, x + COLUMN_WIDTH, LANE_BOTTOM_Y);
                            g.drawLine(x + 1, coordEnd.y + 1, x + COLUMN_WIDTH, coordEnd.y + 1);
                            for (int i = coordStart.x + 1; i < coordEnd.x; ++i) {
                                x = getColumnStartX(i, column, columnCount);
                                g.drawLine(x + 1, LANE_TOP_Y, x + 1, LANE_BOTTOM_Y);
                                g.drawLine(x + COLUMN_WIDTH, LANE_TOP_Y, x + COLUMN_WIDTH, LANE_BOTTOM_Y);
                            }

                            x = getColumnStartX(coordStart.x, column, columnCount);
                            g.setColor(interiorColor);
                            g.fillRect(x + 2, LANE_TOP_Y, COLUMN_WIDTH - 2, coordStart.y - LANE_TOP_Y - NOTE_HEIGHT + 1);
                            x = getColumnStartX(coordEnd.x, column, columnCount);
                            g.fillRect(x + 2, coordEnd.y + 2, COLUMN_WIDTH - 2, LANE_BOTTOM_Y - coordEnd.y - 1);
                            for (int i = coordStart.x + 1; i < coordEnd.x; ++i) {
                                x = getColumnStartX(i, column, columnCount);
                                g.fillRect(x + 1, LANE_TOP_Y, COLUMN_WIDTH - 2, LANE_BOTTOM_Y - LANE_TOP_Y + 1);
                            }
                        }
                    }
                }
            }
        }
    }

    public static int getSectionStartX(int sectionIndex, int columns) {
        return LEFT_PADDING + sectionIndex * (SECTION_SPACING + COLUMN_SPACING + (COLUMN_WIDTH + COLUMN_SPACING) * columns);
    }

    public static int getColumnStartX(int sectionIndex, int column, int columns) {
        return LEFT_PADDING + sectionIndex * (SECTION_SPACING + COLUMN_SPACING + (COLUMN_WIDTH + COLUMN_SPACING) * columns) + column * (COLUMN_WIDTH + COLUMN_SPACING);
    }

    public static Collection<Vec2D> getDrawCoordinates(int startTime, double time) {
        double delta = time - startTime;
        int pixelHeight = (int) Math.round(delta / MILLISECONDS_TO_PIXEL_DIV);
        int y = SECTION_BOTTOM_Y - (pixelHeight % (SECTION_BOTTOM_Y - SECTION_TOP_Y));
        int sectionIndex = pixelHeight / (SECTION_BOTTOM_Y - SECTION_TOP_Y);
        if (y - SECTION_TOP_Y < 3)
            return Arrays.asList(new Vec2D(sectionIndex, y), new Vec2D(sectionIndex + 1, y + (SECTION_BOTTOM_Y - SECTION_TOP_Y)));
        if (SECTION_BOTTOM_Y - y < 3 && sectionIndex > 0)
            return Arrays.asList(new Vec2D(sectionIndex - 1, y - (SECTION_BOTTOM_Y - SECTION_TOP_Y)), new Vec2D(sectionIndex, y));
        return Collections.singletonList(new Vec2D(sectionIndex, y));
    }

    public static Vec2D getPrimaryCoordinate(int startTime, double time) {
        double delta = time - startTime;
        int pixelHeight = (int) Math.round(delta / MILLISECONDS_TO_PIXEL_DIV);
        int y = SECTION_BOTTOM_Y - (pixelHeight % (SECTION_BOTTOM_Y - SECTION_TOP_Y));
        int sectionIndex = pixelHeight / (SECTION_BOTTOM_Y - SECTION_TOP_Y);
        return new Vec2D(sectionIndex, y);
    }

    public static int getSnapIndex(TimingPoint timingPoint, int time) {
        if (timingPoint == null)
            return SNAP_COLOR.length - 1;
        int index = (int) Math.round(Math.abs(time - timingPoint.time) / timingPoint.beatLength * 48);
        for (int i = 0; i < SNAP_COLOR.length; ++i)
            if (index % SNAP_INVERTED[i] == 0)
                return i;
        return SNAP_COLOR.length - 1;
    }

    public static void drawNote(Graphics2D g, int x, int y, Color interior, Color edge) {
        g.setColor(interior);
        g.fillRect(x + 2, y - NOTE_HEIGHT + 2, COLUMN_WIDTH - 2, NOTE_HEIGHT - 2);
        g.setColor(edge);
        g.drawRect(x + 1, y - NOTE_HEIGHT + 1, COLUMN_WIDTH - 1, NOTE_HEIGHT - 1);
    }

}
