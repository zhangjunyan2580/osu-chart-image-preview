package coutcincerrclog.osubeatmapviewer;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    public static void main(String[] args) {
        JFrame frame = new JFrame("osu! beatmap preview");
        MainWindow mainWindow = new MainWindow();
        frame.setContentPane(mainWindow.contentPanel);
        frame.pack();

        mainWindow.initSettings();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                mainWindow.saveSettings();
            }
        });
    }

}
