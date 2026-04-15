package coutcincerrclog.osubeatmapviewer.util;

import java.awt.*;
import java.awt.datatransfer.Clipboard;

public class ClipboardUtil {

    public static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void copyImage(Image image) {
        ImageTransferable imageTransferable = new ImageTransferable(image);
        clipboard.setContents(imageTransferable, null);
    }

}
