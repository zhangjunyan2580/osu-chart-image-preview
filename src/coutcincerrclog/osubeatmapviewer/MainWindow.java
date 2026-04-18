package coutcincerrclog.osubeatmapviewer;

import coutcincerrclog.osubeatmapviewer.drawer.Drawer;
import coutcincerrclog.osubeatmapviewer.drawer.ManiaDrawer;
import coutcincerrclog.osubeatmapviewer.drawer.TaikoDrawer;
import coutcincerrclog.osubeatmapviewer.network.BeatmapDownload;
import coutcincerrclog.osubeatmapviewer.parser.Beatmap;
import coutcincerrclog.osubeatmapviewer.parser.BeatmapParser;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObject;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.HitObjectConverter;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.mania.ManiaConverter;
import coutcincerrclog.osubeatmapviewer.parser.hitobjects.taiko.TaikoConverter;
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
    public JPanel contentPanel;
    private JLabel imageLabel;
    private JCheckBox catchColorFruitComboCheckBox;
    private JButton selectBIDButton;
    private JRadioButton modEZRadioButton;
    private JRadioButton modHRRadioButton;
    private JRadioButton modHTRadioButton;
    private JRadioButton modDTRadioButton;
    private JRadioButton modNMRadioButtonEZHR;
    private JRadioButton modNMRadioButtonHTDT;
    private JSpinner convertKeysSpinner;
    private JButton clearCacheButton;
    private JSpinner BIDSpinner;

    public Settings settings = new Settings();
    public Beatmap beatmap = null;

    public static final File BEATMAP_CACHE_DIR = new File("." + File.separator + "cache" + File.separator);

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

            new Thread(() -> {
                try {
                    beatmap = BeatmapParser.parse(chosenFile);
                    updateImage();
                } catch (RuntimeException ex) {
                    setImageLabelContent(ex.getMessage());
                }
            }, "Processing").start();
        });

        selectBIDButton.addActionListener(e -> {
            if (!BEATMAP_CACHE_DIR.exists()) {
                if (!BEATMAP_CACHE_DIR.mkdirs()) {
                    setImageLabelContent("Error downloading beatmap: failed to make cache directory");
                    return;
                }
            }

            int bid = (int) BIDSpinner.getModel().getValue();
            File beatmapFile = new File(BEATMAP_CACHE_DIR, bid + ".osu");
            new Thread(() -> {
                try {
                    if (!beatmapFile.exists())
                        BeatmapDownload.downloadBeatmap(bid, beatmapFile);
                    new Thread(() -> {
                        try {
                            beatmap = BeatmapParser.parse(beatmapFile);
                            updateImage();
                        } catch (RuntimeException ex) {
                            setImageLabelContent(ex.getMessage());
                        }
                    }, "Processing").start();
                } catch (RuntimeException ex) {
                    setImageLabelContent(ex.getMessage());
                }
            }, "Downloading").start();
        });

        convertTaikoRadioButton.addActionListener(e -> {
            settings.convertMode = 1;
            updateImage();
        });
        convertCatchRadioButton.addActionListener(e -> {
            settings.convertMode = 2;
            updateImage();
        });
        convertManiaRadioButton.addActionListener(e -> {
            settings.convertMode = 3;
            updateImage();
        });

        maniaColorNoteSnapRadioButton.addActionListener(e -> {
            settings.maniaColorBySnap = true;
            updateImage();
        });
        maniaColorNoteColumnRadioButton.addActionListener(e -> {
            settings.maniaColorBySnap = false;
            updateImage();
        });

        catchOpaqueFruitsCheckBox.addActionListener(e -> {
            settings.catchOpaqueFruits = catchOpaqueFruitsCheckBox.isSelected();
            updateImage();
        });
        catchColorFruitComboCheckBox.addActionListener(e -> {
            settings.catchColorByCombo = catchColorFruitComboCheckBox.isSelected();
            updateImage();
        });

        modNMRadioButtonEZHR.setSelected(true);
        modEZRadioButton.addActionListener(e -> {
            settings.modEZHR = Settings.MOD_EZ;
            updateImage();
        });
        modNMRadioButtonEZHR.addActionListener(e -> {
            settings.modEZHR = Settings.MOD_NM;
            updateImage();
        });
        modHRRadioButton.addActionListener(e -> {
            settings.modEZHR = Settings.MOD_HR;
            updateImage();
        });

        modNMRadioButtonHTDT.setSelected(true);
        modHTRadioButton.addActionListener(e -> {
            settings.modHTDT = Settings.MOD_HT;
            updateImage();
        });
        modNMRadioButtonHTDT.addActionListener(e -> {
            settings.modHTDT = Settings.MOD_NM;
            updateImage();
        });
        modDTRadioButton.addActionListener(e -> {
            settings.modHTDT = Settings.MOD_DT;
            updateImage();
        });

        convertKeysSpinner.setModel(new SpinnerNumberModel(7, 1, 9, 1));
        convertKeysSpinner.addChangeListener(e -> {
            settings.maniaConvertKeys = (int) convertKeysSpinner.getModel().getValue();
            updateImage();
        });

        BIDSpinner.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        BIDSpinner.setEditor(new JSpinner.NumberEditor(BIDSpinner, "#"));
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
        File configFile = new File("." + File.separator + "config.txt");
        settings.writeToFile(configFile);
    }

    public void updateImage() {
        if (beatmap == null)
            return;

        int showMode = beatmap.mode == 0 ? settings.convertMode : beatmap.mode;
        HitObjectConverter converter;
        switch (showMode) {
            case 1:
                converter = new TaikoConverter(beatmap, settings);
                break;

            case 2:
                throw new UnsupportedOperationException();

            case 3:
                converter = new ManiaConverter(beatmap, settings);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        beatmap.processedHitObjects.clear();
        for (HitObject hitObject : beatmap.rawHitObjects)
            for (HitObject converted : converter.convert(hitObject)) {
                int index = Collections.binarySearch(beatmap.processedHitObjects, converted, Comparator.comparingInt(h -> h.time));
                if (index < 0)
                    index = ~index;
                beatmap.processedHitObjects.add(index, converted);
            }
        beatmap.initIndex();
        converter.postProcessing();

        Drawer drawer;
        switch (showMode) {
            case 1:
                drawer = new TaikoDrawer();
                break;

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
            try {
                Graphics2D g = image.createGraphics();
                drawer.draw(g, dimensions, beatmap, settings);
                g.dispose();

                ClipboardUtil.copyImage(image);
            } catch (Exception e) {
                setImageLabelContent(e.getMessage());
            }
            setImageLabelContent(new ImageIcon(image));
        }, "Drawing");
        thread.start();
    }

    private void setImageLabelContent(String text) {
        SwingUtilities.invokeLater(() -> {
            imageLabel.setIcon(null);
            imageLabel.setText(text);
        });
    }

    private void setImageLabelContent(Icon icon) {
        SwingUtilities.invokeLater(() -> {
            imageLabel.setText(null);
            imageLabel.setIcon(icon);
        });
    }

}
