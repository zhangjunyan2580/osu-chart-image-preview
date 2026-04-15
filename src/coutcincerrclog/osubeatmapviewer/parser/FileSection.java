package coutcincerrclog.osubeatmapviewer.parser;

public enum FileSection {
    UNKNOWN,
    GENERAL,
    COLOURS,
    EDITOR,
    METADATA,
    TIMING_POINTS,
    EVENTS,
    HIT_OBJECTS,
    DIFFICULTY,
    VARIABLES;

    public static FileSection getFileSection(String sectionString) {
        if (sectionString.charAt(0) == '[' && sectionString.charAt(sectionString.length() - 1) == ']')
            sectionString = sectionString.substring(1, sectionString.length() - 1);
        sectionString = sectionString.toLowerCase();
        switch (sectionString) {
            case "unknown":
                return UNKNOWN;
            case "general":
                return GENERAL;
            case "colours":
                return COLOURS;
            case "editor":
                return EDITOR;
            case "metadata":
                return METADATA;
            case "timingpoints":
                return TIMING_POINTS;
            case "events":
                return EVENTS;
            case "hitobjects":
                return HIT_OBJECTS;
            case "difficulty":
                return DIFFICULTY;
            case "variables":
                return VARIABLES;
        }
        return UNKNOWN;
    }

}
