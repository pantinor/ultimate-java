package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.testng.annotations.Test;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PackImagesUtil {

    public static File dir = new File("D:\\xu4-source\\u4\\graphics\\png");

    public static List<File> listFileTree(File dir) {
        List<File> fileTree = new ArrayList<File>();
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) {
                fileTree.add(entry);
            } else {
                fileTree.addAll(listFileTree(entry));
            }
        }
        return fileTree;
    }

    public static void main1(String[] argv) throws Exception {

        List<File> files = listFileTree(new File("C:\\Users\\Paul\\Desktop\\crawl-tiles Oct-5-2010\\dc-dngn"));
        Collections.sort(files);

        Map<String, BufferedImage> imgMap = new TreeMap<String, BufferedImage>();

        for (File file : files) {
            if (file.isDirectory() || !file.getName().endsWith("png")) {
                continue;
            }
            String name = file.getName();
            BufferedImage fr = ImageIO.read(file);
            imgMap.put(name, fr);
        }

        Settings settings = new Settings();
        settings.maxWidth = 768;
        settings.maxHeight = 768;
        settings.paddingX = 0;
        settings.paddingY = 0;
        settings.fast = true;
        settings.pot = false;
        settings.grid = true;

        TexturePacker tp = new TexturePacker(settings);

        for (String name : imgMap.keySet()) {
            BufferedImage image = imgMap.get(name);
            tp.addImage(image, name);
        }

        System.out.println("Writing: number of res: " + imgMap.size());

        tp.pack(new File("."), "utumno-dngn");

        System.out.println("done");

    }

    public static void main2(String[] argv) throws Exception {

        List<File> files = listFileTree(new File("C:\\Users\\Paul\\Desktop\\water\\worlds\\greenland"));
        Collections.sort(files);

        List<File> files2 = listFileTree(new File("C:\\Users\\Paul\\Desktop\\water\\worlds\\desert"));
        Collections.sort(files2);

        List<File> files3 = listFileTree(new File("C:\\Users\\Paul\\Desktop\\water\\worlds\\winterland"));
        Collections.sort(files3);

        Map<String, BufferedImage> imgMap = new TreeMap<>();

        for (File file : files) {
            if (file.isDirectory() || !file.getName().endsWith("png")) {
                continue;
            }
            String name = "greenland_" + file.getName();
            BufferedImage fr = ImageIO.read(file);
            //BufferedImage sub = fr.getSubimage(11, 11, 32, 32);
            imgMap.put(name, fr);
        }

        for (File file : files2) {
            if (file.isDirectory() || !file.getName().endsWith("png")) {
                continue;
            }
            String name = "desert_" + file.getName();
            BufferedImage fr = ImageIO.read(file);
            imgMap.put(name, fr);
        }

        for (File file : files3) {
            if (file.isDirectory() || !file.getName().endsWith("png")) {
                continue;
            }
            String name = "winterland_" + file.getName();
            BufferedImage fr = ImageIO.read(file);
            imgMap.put(name, fr);
        }

        System.out.println("Writing: number of images: " + imgMap.size());

        Settings settings = new Settings();
        settings.maxWidth = 32 * 12;
        settings.maxHeight = 32 * 32;
        settings.paddingX = 0;
        settings.paddingY = 0;
        settings.fast = true;
        settings.pot = false;
        settings.grid = true;

        TexturePacker tp = new TexturePacker(settings);
        for (String name : imgMap.keySet()) {
            BufferedImage image = imgMap.get(name);
            tp.addImage(image, name);
        }
        tp.pack(new File("C:\\Users\\Paul\\Desktop\\water\\"), "water_anims");

        //java.awt.Color[] col = {java.awt.Color.MAGENTA};
        //ImageTransparency.convert("monsters.png", "monsters-trans.png", col);
        System.out.println("done");

    }

    public static void main(String[] argv) throws Exception {
        
        Settings settings = new Settings();
        settings.maxWidth = 192*8;
        settings.maxHeight = 192*8;
        settings.paddingX = 0;
        settings.paddingY = 0;
        settings.fast = true;
        settings.pot = false;
        settings.grid = true;

        TexturePacker tp = new TexturePacker(settings);

        File file = new File("C:\\Users\\Paul\\Desktop\\water\\Exp_type_B.png");

        BufferedImage fr = ImageIO.read(file);
        int count = 0;
        for (int x=0;x<fr.getWidth();x+=192) {
            count++;
            BufferedImage sub = fr.getSubimage(x, 0, 192, 192);
            tp.addImage(sub, "expl"+count);
        }

        tp.pack(new File("."), "Exp_type_B");

        System.out.println("done");
    }

    public static void main4(String[] argv) throws Exception {
        
        ImageFrame[] frames = readGif(new File("C:\\Users\\Paul\\Desktop\\water\\lava.gif"));

        Map<String, BufferedImage> imgMap = new TreeMap<>();

        for (int i = 0, count = frames.length; i < count; i++) {
            BufferedImage fr = frames[i].image;
            String name = "w_" + i;
            imgMap.put(name, fr);
        }

        System.out.println("Writing: number of images: " + imgMap.size());

        Settings settings = new Settings();
        settings.maxWidth = 128 * 3;
        settings.maxHeight = 128 * 6;
        settings.paddingX = 0;
        settings.paddingY = 0;
        settings.fast = true;
        settings.pot = false;
        settings.grid = true;

        TexturePacker tp = new TexturePacker(settings);
        for (String name : imgMap.keySet()) {
            BufferedImage image = imgMap.get(name);
            tp.addImage(image, name);
        }
        tp.pack(new File("C:\\Users\\Paul\\Desktop\\water\\"), "lava");

        System.out.println("done");

    }

    private static ImageFrame[] readGif(File file) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList<>(2);

        ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(file));

        int lastx = 0;
        int lasty = 0;

        int width = -1;
        int height = -1;

        IIOMetadata metadata = reader.getStreamMetadata();

        Color backgroundColor = null;

        if (metadata != null) {
            IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

            NodeList globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
            NodeList globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

            if (globalScreeDescriptor != null && globalScreeDescriptor.getLength() > 0) {
                IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreeDescriptor.item(0);

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }

            if (globalColorTable != null && globalColorTable.getLength() > 0) {
                IIOMetadataNode colorTable = (IIOMetadataNode) globalColorTable.item(0);

                if (colorTable != null) {
                    String bgIndex = colorTable.getAttribute("backgroundColorIndex");

                    IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
                    while (colorEntry != null) {
                        if (colorEntry.getAttribute("index").equals(bgIndex)) {
                            int red = Integer.parseInt(colorEntry.getAttribute("red"));
                            int green = Integer.parseInt(colorEntry.getAttribute("green"));
                            int blue = Integer.parseInt(colorEntry.getAttribute("blue"));

                            backgroundColor = new Color(red, green, blue);
                            break;
                        }

                        colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
                    }
                }
            }
        }

        BufferedImage master = null;
        boolean hasBackround = false;

        for (int frameIndex = 0;; frameIndex++) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException io) {
                break;
            }

            if (width == -1 || height == -1) {
                width = image.getWidth();
                height = image.getHeight();
            }

            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            NodeList children = root.getChildNodes();

            int delay = Integer.valueOf(gce.getAttribute("delayTime"));

            String disposal = gce.getAttribute("disposalMethod");

            if (master == null) {
                master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                master.createGraphics().setColor(backgroundColor);
                master.createGraphics().fillRect(0, 0, master.getWidth(), master.getHeight());

                hasBackround = image.getWidth() == width && image.getHeight() == height;

                master.createGraphics().drawImage(image, 0, 0, null);
            } else {
                int x = 0;
                int y = 0;

                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
                    Node nodeItem = children.item(nodeIndex);

                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();

                        x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }

                if (disposal.equals("restoreToPrevious")) {
                    BufferedImage from = null;
                    for (int i = frameIndex - 1; i >= 0; i--) {
                        if (!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0) {
                            from = frames.get(i).getImage();
                            break;
                        }
                    }

                    {
                        ColorModel model = from.getColorModel();
                        boolean alpha = from.isAlphaPremultiplied();
                        WritableRaster raster = from.copyData(null);
                        master = new BufferedImage(model, raster, alpha, null);
                    }
                } else if (disposal.equals("restoreToBackgroundColor") && backgroundColor != null) {
                    if (!hasBackround || frameIndex > 1) {
                        master.createGraphics().fillRect(lastx, lasty, frames.get(frameIndex - 1).getWidth(), frames.get(frameIndex - 1).getHeight());
                    }
                }
                master.createGraphics().drawImage(image, x, y, null);

                lastx = x;
                lasty = y;
            }

            {
                BufferedImage copy;

                {
                    ColorModel model = master.getColorModel();
                    boolean alpha = master.isAlphaPremultiplied();
                    WritableRaster raster = master.copyData(null);
                    copy = new BufferedImage(model, raster, alpha, null);
                }
                frames.add(new ImageFrame(copy, delay, disposal, image.getWidth(), image.getHeight()));
            }

            master.flush();
        }
        reader.dispose();

        return frames.toArray(new ImageFrame[frames.size()]);
    }

    private static class ImageFrame {

        private final int delay;
        private final BufferedImage image;
        private final String disposal;
        private final int width, height;

        public ImageFrame(BufferedImage image, int delay, String disposal, int width, int height) {
            this.image = image;
            this.delay = delay;
            this.disposal = disposal;
            this.width = width;
            this.height = height;
        }

        public ImageFrame(BufferedImage image) {
            this.image = image;
            this.delay = -1;
            this.disposal = null;
            this.width = -1;
            this.height = -1;
        }

        public BufferedImage getImage() {
            return image;
        }

        public int getDelay() {
            return delay;
        }

        public String getDisposal() {
            return disposal;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
    
            }
