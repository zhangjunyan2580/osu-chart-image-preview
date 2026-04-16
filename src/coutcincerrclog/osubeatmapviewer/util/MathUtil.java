package coutcincerrclog.osubeatmapviewer.util;

import coutcincerrclog.osubeatmapviewer.Settings;

public class MathUtil {

    public static double mapDifficultyRange(double difficulty, double min, double mid, double max, int modEZHR) {
        switch (modEZHR) {
            case Settings.MOD_EZ:
                difficulty = Math.max(0, difficulty / 2);
                break;

            case Settings.MOD_HR:
                difficulty = Math.min(10, difficulty * 1.4);
                break;
        }

        if (difficulty > 5)
            return mid + (max - mid) * (difficulty - 5) / 5;
        if (difficulty < 5)
            return mid - (mid - min) * (5 - difficulty) / 5;
        return mid;
    }

}
