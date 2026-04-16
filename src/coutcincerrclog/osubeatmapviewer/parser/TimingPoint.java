package coutcincerrclog.osubeatmapviewer.parser;

public class TimingPoint {

    public double time;
    public double beatLength;
    public int meter;
    public int sampleSet;
    public int sampleIndex;
    public int volume;
    public boolean uninherited;
    public int effects;

    public TimingPoint(double time, double beatLength, int meter, int sampleSet, int sampleIndex, int volume, boolean uninherited, int effects) {
        this.time = time;
        this.beatLength = beatLength;
        this.meter = meter;
        this.sampleSet = sampleSet;
        this.sampleIndex = sampleIndex;
        this.volume = volume;
        this.uninherited = uninherited;
        this.effects = effects;
    }

    public TimingPoint(String timingString) {
        this(timingString, 0, 100);
    }

    public TimingPoint(String timingString, int offset, int defaultVolume) {
        try {
            String[] split = timingString.split(",");
            this.time = Double.parseDouble(split[0].trim()) + offset;
            this.beatLength = Double.parseDouble(split[1].trim());
            this.meter = Integer.parseInt(split[2]);
            this.sampleSet = Integer.parseInt(split[3]);
            this.sampleIndex = split.length > 4 ? Integer.parseInt(split[4]) : 0;
            this.volume = split.length > 5 ? Integer.parseInt(split[5]) : defaultVolume;
            this.uninherited = split.length > 6 ? "1".equals(split[6]) : true;
            this.effects = split.length > 7 ? Integer.parseInt(split[7]) : 0;
        } catch (RuntimeException e) {
            throw new InvalidBeatmapException(e);
        }
    }

    public float BPMMultiplier() {
        return Math.max(10, Math.min(10000, (float) -beatLength)) / 100f;
    }

}
