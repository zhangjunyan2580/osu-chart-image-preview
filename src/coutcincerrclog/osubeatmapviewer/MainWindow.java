package coutcincerrclog.osubeatmapviewer;

import coutcincerrclog.osubeatmapviewer.drawer.Drawer;
import coutcincerrclog.osubeatmapviewer.drawer.ManiaDrawer;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.BeatmapParser;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObjectModeConverter;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.mania.ManiaConverter;
import coutcincerrclog.osubeatmapviewer.util.ClipboardUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;

public class MainWindow {
    private JRadioButton convertTaikoRadioButton;
    private JRadioButton convertCatchRadioButton;
    private JRadioButton maniaColorNoteSnapRadioButton;
    private JRadioButton maniaColorNoteColumnRadioButton;
    private JRadioButton convertManiaRadioButton;
    private JCheckBox catchOpaqueFruitsCheckBox;
    private JButton selectLocalBeatmapFileButton;
    private JScrollPane imagePanel;
    public JPanel contentPanel;
    private JLabel imageLabel;
    private JCheckBox catchColorFruitComboCheckBox;
    private JTextField beatmapIDTextField;
    private JButton selectBIDButton;

    public Settings settings = new Settings();

    public MainWindow() {
        selectLocalBeatmapFileButton.addActionListener(e -> {
            FileDialog dialog = new FileDialog((Frame) null);
            dialog.setMode(FileDialog.LOAD);
            dialog.setDirectory(settings.lastChosenFolder);
            dialog.setVisible(true);

            if (dialog.getDirectory() == null)
                return;
            settings.lastChosenFolder = dialog.getDirectory();
            File chosenFile = new File(settings.lastChosenFolder + "\\" + dialog.getFile());
            Beatmap beatmap = BeatmapParser.parse(chosenFile);

            int showMode = beatmap.mode == 0 ? settings.convertMode : beatmap.mode;
            HitObjectModeConverter converter;
            switch (showMode) {
                case 1:
                    throw new UnsupportedOperationException();

                case 2:
                    throw new UnsupportedOperationException();

                case 3:
                    converter = new ManiaConverter(beatmap);
                    break;

                default:
                    throw new UnsupportedOperationException();
            }

            for (HitObject hitObject : beatmap.rawHitObjects)
                for (HitObject converted : converter.convert(hitObject)) {
                    int index = Collections.binarySearch(beatmap.processedHitObjects, converted, Comparator.comparingInt(h -> h.time));
                    if (index < 0)
                        index = ~index;
                    beatmap.processedHitObjects.add(index, converted);
                }

            Drawer drawer;
            switch (showMode) {
                case 1:
                    throw new UnsupportedOperationException();

                case 2:
                    throw new UnsupportedOperationException();

                case 3:
                    drawer = new ManiaDrawer();
                    break;

                default:
                    throw new UnsupportedOperationException();
            }

            Dimension dimensions = drawer.getPreferredSize(beatmap, settings);
            BufferedImage image = new BufferedImage(dimensions.width, dimensions.height, BufferedImage.TYPE_INT_ARGB);
            imageLabel.setSize(dimensions);
            Thread thread = new Thread(() -> {
                Graphics2D g = image.createGraphics();
                drawer.draw(g, beatmap, settings);
                g.dispose();

                ClipboardUtil.copyImage(image);
                SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
            }, "Drawing");
            thread.start();
        });

        convertTaikoRadioButton.addActionListener(e -> settings.convertMode = 1);
        convertCatchRadioButton.addActionListener(e -> settings.convertMode = 2);
        convertManiaRadioButton.addActionListener(e -> settings.convertMode = 3);
        maniaColorNoteSnapRadioButton.addActionListener(e -> settings.maniaColorBySnap = true);
        maniaColorNoteColumnRadioButton.addActionListener(e -> settings.maniaColorBySnap = false);
        catchOpaqueFruitsCheckBox.addActionListener(e -> settings.catchOpaqueFruits = catchOpaqueFruitsCheckBox.isSelected());
        catchColorFruitComboCheckBox.addActionListener(e -> settings.catchColorByCombo = catchColorFruitComboCheckBox.isSelected());
    }

    public void initSettings() {
        File configFile = new File(".\\config.txt");
        settings.readFromFile(configFile);
        switch (settings.convertMode) {
            case 1:
                convertTaikoRadioButton.setSelected(true);
                break;

            case 2:
                convertCatchRadioButton.setSelected(true);
                break;

            case 3:
                convertManiaRadioButton.setSelected(true);
                break;
        }
        if (settings.maniaColorBySnap) {
            maniaColorNoteSnapRadioButton.setSelected(true);
        } else {
            maniaColorNoteColumnRadioButton.setSelected(true);
        }
        catchColorFruitComboCheckBox.setSelected(settings.catchColorByCombo);
        catchOpaqueFruitsCheckBox.setSelected(settings.catchOpaqueFruits);
    }

    public void saveSettings() {
        File configFile = new File(".\\config.txt");
        settings.writeToFile(configFile);
    }

}
