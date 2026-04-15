package coutcincerrclog.osubeatmapviewer.util;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ImageTransferable implements Transferable {

    public static final DataFlavor[] DATA_FLAVORS = {DataFlavor.imageFlavor};
    public Image image;

    public ImageTransferable(Image image) {
        this.image = image;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return DATA_FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.imageFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(DataFlavor.imageFlavor))
            return image;
        throw new UnsupportedFlavorException(flavor);
    }

}
