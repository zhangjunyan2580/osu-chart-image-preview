package coutcincerrclog.osubeatmapviewer.drawer;

import coutcincerrclog.osubeatmapviewer.Settings;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;

import java.awt.*;

public abstract class Drawer {

    public abstract Dimension getPreferredSize(Beatmap beatmap, Settings settings);
    public abstract void draw(Graphics2D g, Beatmap beatmap, Settings settings);

}
