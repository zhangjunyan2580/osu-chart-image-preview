package coutcincerrclog.osubeatmapviewer;

import java.io.*;

public class Settings {

    public static final int MOD_EZ = 0;
    public static final int MOD_NM = 1;
    public static final int MOD_HR = 2;
    public static final int MOD_HT = 0;
    public static final int MOD_DT = 2;

    public int convertMode = 2;
    public boolean maniaColorBySnap = true;
    public boolean catchOpaqueFruits = false;
    public boolean catchColorByCombo = true;
    public int modEZHR = 1;
    public int modHTDT = 1;
    public int maniaConvertKeys = 0;

    public String lastChosenFolder = "%appdata%\\..\\Local\\osu!\\Songs";

    public void readFromFile(File file) {
        // Mod data is intentionally not persisted
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                String[] split = line.split(":", 2);
                if (split.length == 2) {
                    String key = split[0], val = split[1];
                    try {
                        switch (key) {
                            case "convertMode":
                                convertMode = Integer.parseInt(val);
                                break;

                            case "maniaColorBySnap":
                                maniaColorBySnap = Boolean.parseBoolean(val);
                                break;

                            case "catchOpaqueFruits":
                                catchOpaqueFruits = Boolean.parseBoolean(val);
                                break;

                            case "catchColorByCombo":
                                catchColorByCombo = Boolean.parseBoolean(val);
                                break;

                            case "lastChosenFolder":
                                lastChosenFolder = val;
                                break;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            reader.close();
        } catch (IOException ignored) {}
    }

    public void writeToFile(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("convertMode:" + convertMode + "\n");
            writer.write("maniaColorBySnap:" + maniaColorBySnap + "\n");
            writer.write("catchOpaqueFruits:" + catchOpaqueFruits + "\n");
            writer.write("catchColorByCombo:" + catchColorByCombo + "\n");
            writer.write("lastChosenFolder:" + lastChosenFolder + "\n");
            writer.close();
        } catch (IOException ignored) {}
    }

}
