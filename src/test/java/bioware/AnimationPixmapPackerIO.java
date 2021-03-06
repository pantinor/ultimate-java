package bioware;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;

import bioware.AnimationPixMapPacker.Page;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.Rectangle;

public class AnimationPixmapPackerIO {

    /**
     * Image formats which can be used when saving a PixmapPacker.
     */
    public static enum ImageFormat {

        /**
         * A simple compressed image format which is libgdx specific.
         */
        CIM(".cim"),
        /**
         * A standard compressed image format which is not libgdx specific.
         */
        PNG(".png");

        private final String extension;

        /**
         * Returns the file extension for the image format.
         */
        public String getExtension() {
            return extension;
        }

        ImageFormat(String extension) {
            this.extension = extension;
        }
    }

    /**
     * Additional parameters which will be used when writing a PixmapPacker.
     */
    public static class SaveParameters {

        ImageFormat format = ImageFormat.PNG;
        TextureFilter minFilter = TextureFilter.Nearest;
        TextureFilter magFilter = TextureFilter.Nearest;
    }

    /**
     * Saves the provided PixmapPacker to the provided file. The resulting file
     * will use the standard TextureAtlas file format and can be loaded by
     * TextureAtlas as if it had been created using TexturePacker. Default
     * {@link SaveParameters} will be used.
     *
     * @param file the file to which the atlas descriptor will be written,
     * images will be written as siblings
     * @param packer the PixmapPacker to be written
     * @throws IOException if the atlas file can not be written
     */
    public void save(FileHandle file, AnimationPixMapPacker packer) throws IOException {
        save(file, packer, new SaveParameters());
    }

    /**
     * Saves the provided PixmapPacker to the provided file. The resulting file
     * will use the standard TextureAtlas file format and can be loaded by
     * TextureAtlas as if it had been created using TexturePacker.
     *
     * @param file the file to which the atlas descriptor will be written,
     * images will be written as siblings
     * @param packer the PixmapPacker to be written
     * @param parameters the SaveParameters specifying how to save the
     * PixmapPacker
     * @throws IOException if the atlas file can not be written
     */
    public void save(FileHandle file, AnimationPixMapPacker packer, SaveParameters parameters) throws IOException {
        Writer writer = file.writer(false);
        int index = 0;
        for (Page page : packer.pages) {
            if (page.rects.size > 0) {
                FileHandle pageFile = file.sibling(file.nameWithoutExtension() + "_" + (++index) + parameters.format.getExtension());
                switch (parameters.format) {
                    case CIM: {
                        PixmapIO.writeCIM(pageFile, page.image);
                        break;
                    }
                    case PNG: {
                        PixmapIO.writePNG(pageFile, page.image);
                        break;
                    }
                }
                writer.write("\n");
                writer.write(pageFile.name() + "\n");
                writer.write("size: " + page.image.getWidth() + "," + page.image.getHeight() + "\n");
                writer.write("format: " + packer.pageFormat.name() + "\n");
                writer.write("filter: " + parameters.minFilter.name() + "," + parameters.magFilter.name() + "\n");
                writer.write("repeat: none" + "\n");
                int count = 0;
                String prevName = "";
                for (String name : page.rects.keys()) {
                    String[] sp = name.split("-");

                    if (StringUtils.equals(prevName, sp[0])) {
                        count++;
                    } else {
                        count = 0;
                    }

                    writer.write(sp[0] + "\n");
                    Rectangle rect = page.rects.get(name);
                    writer.write("  rotate: false" + "\n");
                    writer.write("  xy: " + (int) rect.x + "," + (int) rect.y + "\n");
                    writer.write("  size: " + (int) rect.width + "," + (int) rect.height + "\n");
                    writer.write("  orig: " + (int) rect.width/2 + "," + (int) rect.height/2 + "\n");
                    writer.write("  offset: 0, 0" + "\n");
                    writer.write("  index: " + count + "\n");

                    prevName = sp[0];
                }
            }
        }
        writer.close();
    }

}
