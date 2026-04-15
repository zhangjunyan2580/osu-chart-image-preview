package coutcincerrclog.osubeatmapviewer.parser;

import coutcincerrclog.osubeatmapviewer.parser.hitobjects.CurveType;
import coutcincerrclog.osubeatmapviewer.util.Vec2F;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.HitCircle;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Hold;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Slider;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.generic.Spinner;

import java.io.*;
import java.util.ArrayList;

public class BeatmapParser {

    public static Beatmap parse(BufferedReader reader) throws IOException {
        FileSection currentSection = FileSection.UNKNOWN;
        String line;

        Beatmap beatmap = new Beatmap();

        boolean forceNewCombo = true;

        while (true) {
            line = reader.readLine();
            if (line == null)
                break;
            if (line.isEmpty() || line.startsWith(" ") || line.startsWith("_") || line.startsWith("//"))
                continue;

            if (line.charAt(0) == '[') {
                FileSection newSection = FileSection.getFileSection(line);
                if (newSection != FileSection.UNKNOWN)
                    currentSection = newSection;
                continue;
            }

            String[] split = line.trim().split(",");
            String[] var = line.trim().split(":");
            String key = "", val = "";
            if (var.length > 1) {
                key = var[0].trim();
                val = var[1].trim();
            }

            switch (currentSection) {
                case GENERAL:
                    switch (key) {
                        case "Mode":
                            beatmap.mode = Integer.parseInt(val);
                            break;
                    }
                    break;

                case EDITOR:
                    // We are previewing the beatmap, so we won't bother read this section
                    break;

                case COLOURS:
                    // TODO
                    break;

                case METADATA:
                    switch (key) {
                        case "Title":
                            beatmap.title = val;
                            break;

                        case "Artist":
                            beatmap.artist = val;
                            break;

                        case "Creator":
                            beatmap.creator = val;
                            break;

                        case "Version":
                            beatmap.version = val;
                            break;
                    }
                    break;

                case VARIABLES:
                    // We are previewing the beatmap, so we won't bother read this section
                    break;

                case EVENTS:
                    // We are previewing the beatmap, so we won't bother read this section
                    break;

                case TIMING_POINTS:
                    beatmap.timingPoints.add(new TimingPoint(line));
                    break;

                case HIT_OBJECTS:
                    int type = Integer.parseInt(split[3]) & ~112;
                    int soundType = Integer.parseInt(split[4]);
                    int x = (int) Math.max(0, Math.min(512, Float.parseFloat(split[0])));
                    int y = (int) Math.max(0, Math.min(512, Float.parseFloat(split[1])));
                    int time = (int) Double.parseDouble(split[2]);

                    int comboOffset = (Integer.parseInt(split[3]) >> 4) & 7;
                    boolean newCombo = (type & 4) != 0;

                    int sampleSet = 0, additionSet = 0, customSample = 0, volume = 0;
                    String customSampleFile = "";

                    if ((type & 1) != 0) {
                        if (split.length > 5 && !split[5].isEmpty()) {
                            String[] sample = split[5].split(":");
                            sampleSet = Integer.parseInt(sample[0]);
                            additionSet = Integer.parseInt(sample[1]);
                            customSample = sample.length > 2 ? Integer.parseInt(sample[2]) : 0;
                            volume = sample.length > 3 ? Math.max(0, Math.min(100, Integer.parseInt(sample[3]))) : 0;
                            customSampleFile = sample.length > 4 ? sample[4] : "";
                        }
                        beatmap.rawHitObjects.add(new HitCircle(new Vec2F(x, y), time, newCombo || forceNewCombo, soundType, newCombo ? comboOffset : 0, sampleSet, additionSet, customSample, volume, customSampleFile));
                        forceNewCombo = false;
                    } else if ((type & 2) != 0) {
                        CurveType curveType = CurveType.CATMULL;
                        int repeatCount;
                        double length = 0;
                        ArrayList<Vec2F> points = new ArrayList<>();
                        ArrayList<Integer> sounds = null;

                        String[] pointSplit = split[5].split("\\|");
                        for (String point : pointSplit) {
                            if (point.length() == 1) {
                                switch (point) {
                                    case "C":
                                        curveType = CurveType.CATMULL;
                                        break;
                                    case "B":
                                        curveType = CurveType.BEZIER;
                                        break;
                                    case "L":
                                        curveType = CurveType.LINEAR;
                                        break;
                                    case "P":
                                        curveType = CurveType.PERFECT_CURVE;
                                        break;
                                }
                                continue;
                            }

                            String[] anchorPos = point.split(":");
                            Vec2F anchorPoint = new Vec2F((int) Double.parseDouble(anchorPos[0]), (int) Double.parseDouble(anchorPos[1]));
                            points.add(anchorPoint);
                        }

                        repeatCount = Integer.parseInt(split[6]);
                        if (repeatCount > 9000)
                            throw new InvalidBeatmapException("Too many repeats in slider");

                        if (split.length > 7)
                            length = Double.parseDouble(split[7]);

                        if (split.length > 8 && !split[8].isEmpty()) {
                            String[] additionSets = split[8].split("\\|");
                            if (additionSets.length > 0) {
                                sounds = new ArrayList<>();
                                int additionSetsLength = Math.min(additionSets.length, repeatCount + 1);
                                int i = 0;
                                for (; i < additionSetsLength; ++i) {
                                    try {
                                        sounds.add(Integer.parseInt(additionSets[i]));
                                    } catch (NumberFormatException e) {
                                        sounds.add(0);
                                    }
                                }
                                for (; i < repeatCount + 1; ++i) {
                                    sounds.add(soundType);
                                }
                            }
                        }

                        ArrayList<Integer> ss = new ArrayList<>();
                        ArrayList<Integer> ssa = new ArrayList<>();
                        if (split.length > 9 && !split[9].isEmpty()) {
                            String[] sets = split[9].split("\\|");
                            for (String t : sets) {
                                String[] splitSet = t.split(":");
                                ss.add(Integer.parseInt(splitSet[0]));
                                ssa.add(Integer.parseInt(splitSet[1]));
                            }
                        }

                        if (sounds != null) {
                            if (ss.size() > repeatCount + 1) {
                                ss.subList(repeatCount + 1, ss.size()).clear();
                            } else {
                                for (int i = ss.size(); i < repeatCount + 1; ++i)
                                    ss.add(0);
                            }
                            if (ssa.size() > repeatCount + 1) {
                                ssa.subList(repeatCount + 1, ssa.size()).clear();
                            } else {
                                for (int i = ssa.size(); i < repeatCount + 1; ++i)
                                    ssa.add(0);
                            }
                        }

                        if (split.length > 10) {
                            String[] sample = split[10].split(":");
                            sampleSet = Integer.parseInt(sample[0]);
                            additionSet = Integer.parseInt(sample[1]);
                            customSample = sample.length > 2 ? Integer.parseInt(sample[2]) : 0;
                            volume = sample.length > 3 ? Math.max(0, Math.min(100, Integer.parseInt(sample[3]))) : 0;
                            customSampleFile = sample.length > 4 ? sample[4] : "";
                        }
                        beatmap.rawHitObjects.add(new Slider(new Vec2F(x, y), time, forceNewCombo || newCombo, soundType, curveType, repeatCount, length, points, sounds, newCombo ? comboOffset : 0, sampleSet, additionSet, ss, ssa, customSample, volume, customSampleFile));
                        forceNewCombo = false;
                    } else if ((type & 8) != 0) {
                        if (split.length > 6) {
                            String[] sample = split[6].split(":");
                            sampleSet = Integer.parseInt(sample[0]);
                            additionSet = Integer.parseInt(sample[1]);
                            customSample = sample.length > 2 ? Integer.parseInt(sample[2]) : 0;
                            volume = sample.length > 3 ? Math.max(0, Math.min(100, Integer.parseInt(sample[3]))) : 0;
                            customSampleFile = sample.length > 4 ? sample[4] : "";
                        }

                        beatmap.rawHitObjects.add(new Spinner(time, Integer.parseInt(split[5]), soundType, sampleSet, additionSet, customSample, volume, customSampleFile));
                        forceNewCombo = true;
                    } else if ((type & 128) != 0) {
                        int end = time;
                        if (split.length > 5 && !split[5].isEmpty()) {
                            String[] ss = split[5].split(":");
                            end = (int) Double.parseDouble(ss[0]);
                            if (ss.length > 1) {
                                sampleSet = Integer.parseInt(ss[1]);
                                additionSet = Integer.parseInt(ss[2]);
                            }
                            if (ss.length > 3)
                                customSample = Integer.parseInt(ss[3]);
                            volume = ss.length > 4 ? Math.max(0, Math.min(100, Integer.parseInt(ss[4]))) : 0;
                            customSampleFile = ss.length > 5 ? ss[5] : "";
                        }

                        beatmap.rawHitObjects.add(new Hold(new Vec2F(x, y), time, end, forceNewCombo || newCombo, soundType, newCombo ? comboOffset : 0, sampleSet, additionSet, customSample, volume, customSampleFile));
                        forceNewCombo = false;
                    }
                    break;

                case DIFFICULTY:
                    switch (key) {
                        case "HPDrainRate":
                            beatmap.HPDrainRate = Math.max(0, Math.min(10, Float.parseFloat(val)));
                            break;

                        case "CircleSize":
                            beatmap.circleSize = Math.max(0, Math.min(10, Float.parseFloat(val)));
                            break;

                        case "OverallDifficulty":
                            beatmap.overallDifficulty = Math.max(0, Math.min(10, Float.parseFloat(val)));
                            break;

                        case "ApproachRate":
                            beatmap.approachRate = Math.max(0, Math.min(10, Float.parseFloat(val)));
                            break;

                        case "SliderMultiplier":
                            beatmap.sliderMultiplier = Double.parseDouble(val);
                            break;

                        case "SliderTickRate":
                            beatmap.sliderTickRate = Double.parseDouble(val);
                            break;
                    }
                    break;
            }
        }

        return beatmap;
    }

    public static Beatmap parse(String beatmapString) {
        try {
            return parse(new BufferedReader(new StringReader(beatmapString)));
        } catch (IOException ignored) {}
        return null;
    }

    public static Beatmap parse(File beatmapFile) {
        BufferedReader reader = null;
        Beatmap beatmap;
        try {
            reader = new BufferedReader(new FileReader(beatmapFile));
            beatmap = parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException ignored) {}
        }
        return beatmap;
    }

}
