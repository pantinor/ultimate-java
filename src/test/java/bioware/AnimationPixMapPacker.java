package bioware;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap.Keys;
import com.badlogic.gdx.utils.OrderedMap;

public class AnimationPixMapPacker implements Disposable {

    static final class Node {

        public Node leftChild;
        public Node rightChild;
        public Rectangle rect;
        public String leaveName;

        public Node(int x, int y, int width, int height, Node leftChild, Node rightChild, String leaveName) {
            this.rect = new Rectangle(x, y, width, height);
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.leaveName = leaveName;
        }

        public Node() {
            rect = new Rectangle();
        }
    }

    public class Page {

        public Node root;
        public OrderedMap<String, Rectangle> rects;
        public Pixmap image;
        public Texture texture;
        final public Array<String> addedRects = new Array();

        public Pixmap getPixmap() {
            return image;
        }

        public OrderedMap<String, Rectangle> getRects() {
            return rects;
        }
    }

    final int pageWidth;
    final int pageHeight;
    final Format pageFormat;
    final int padding;
    final public Array<Page> pages = new Array();
    Page currPage;
    boolean disposed;

    /**
     * <
     * p>
     * Creates a new ImagePacker which will insert all supplied images into a
     * <code>width</code> by <code>height</code> image. <code>padding</code>
     * specifies the minimum number of pixels to insert between images.
     * <code>border</code> will duplicate the border pixels of the inserted
     * images to avoid seams when rendering with bi-linear filtering on.
     * </p>
     *
     * @param width the width of the output image
     * @param height the height of the output image
     * @param padding the number of padding pixels
     */
    public AnimationPixMapPacker(int width, int height, Format format, int padding) {
        this.pageWidth = width;
        this.pageHeight = height;
        this.pageFormat = format;
        this.padding = padding;
        newPage();
    }

    /**
     * <
     * p>
     * Inserts the given {@link Pixmap}. You can later on retrieve the images
     * position in the output image via the supplied name and the method
     * {@link #getRect(String)}.
     * </p>
     *
     * @param name the name of the image
     * @param image the image
     * @return Rectangle describing the area the pixmap was rendered to or null.
     * @throws RuntimeException in case the image did not fit due to the page
     * size being to small or providing a duplicate name
     */
    public synchronized Rectangle pack(String name, Pixmap image) {
        if (disposed) {
            return null;
        }
        if (getRect(name) != null) {
            throw new RuntimeException("Key with name '" + name + "' is already in map");
        }
        int borderPixels = padding;
        borderPixels <<= 1;

        Rectangle rect = new Rectangle(0, 0, image.getWidth() + borderPixels, image.getHeight() + borderPixels);
        if (rect.getWidth() > pageWidth || rect.getHeight() > pageHeight) {
            throw new GdxRuntimeException("page size for '" + name + "' to small");
        }

        Node node = insert(currPage.root, rect);

        if (node == null) {
            newPage();
            return pack(name, image);
        }

        node.leaveName = name;
        rect = new Rectangle(node.rect);
        rect.width -= borderPixels;
        rect.height -= borderPixels;
        borderPixels >>= 1;
        rect.x += borderPixels;
        rect.y += borderPixels;
        currPage.rects.put(name, rect);

        Blending blending = image.getBlending();
        image.setBlending(Blending.None);
        this.currPage.image.drawPixmap(image, (int) rect.x, (int) rect.y);
        image.setBlending(blending);

        currPage.addedRects.add(name);
        return rect;
    }

    private void newPage() {
        Page page = new Page();
        page.image = new Pixmap(pageWidth, pageHeight, pageFormat);
        page.root = new Node(0, 0, pageWidth, pageHeight, null, null, null);
        page.rects = new OrderedMap<>();
        pages.add(page);
        currPage = page;
    }

    private Node insert(Node node, Rectangle rect) {
        if (node.leaveName == null && node.leftChild != null && node.rightChild != null) {
            Node newNode = null;

            newNode = insert(node.leftChild, rect);
            if (newNode == null) {
                newNode = insert(node.rightChild, rect);
            }

            return newNode;
        } else {
            if (node.leaveName != null) {
                return null;
            }

            if (node.rect.width == rect.width && node.rect.height == rect.height) {
                return node;
            }

            if (node.rect.width < rect.width || node.rect.height < rect.height) {
                return null;
            }

            node.leftChild = new Node();
            node.rightChild = new Node();

            int deltaWidth = (int) node.rect.width - (int) rect.width;
            int deltaHeight = (int) node.rect.height - (int) rect.height;

            if (deltaWidth > deltaHeight) {
                node.leftChild.rect.x = node.rect.x;
                node.leftChild.rect.y = node.rect.y;
                node.leftChild.rect.width = rect.width;
                node.leftChild.rect.height = node.rect.height;

                node.rightChild.rect.x = node.rect.x + rect.width;
                node.rightChild.rect.y = node.rect.y;
                node.rightChild.rect.width = node.rect.width - rect.width;
                node.rightChild.rect.height = node.rect.height;
            } else {
                node.leftChild.rect.x = node.rect.x;
                node.leftChild.rect.y = node.rect.y;
                node.leftChild.rect.width = node.rect.width;
                node.leftChild.rect.height = rect.height;

                node.rightChild.rect.x = node.rect.x;
                node.rightChild.rect.y = node.rect.y + rect.height;
                node.rightChild.rect.width = node.rect.width;
                node.rightChild.rect.height = node.rect.height - rect.height;
            }

            return insert(node.leftChild, rect);
        }
    }

    /**
     * @return the {@link Page} instances created so far. This method is not
     * thread safe!
     */
    public Array<Page> getPages() {
        return pages;
    }

    /**
     * @param name the name of the image
     * @return the rectangle for the image in the page it's stored in or null
     */
    public synchronized Rectangle getRect(String name) {
        for (Page page : pages) {
            Rectangle rect = page.rects.get(name);
            if (rect != null) {
                return rect;
            }
        }
        return null;
    }

    /**
     * @param name the name of the image
     * @return the page the image is stored in or null
     */
    public synchronized Page getPage(String name) {
        for (Page page : pages) {
            Rectangle rect = page.rects.get(name);
            if (rect != null) {
                return page;
            }
        }
        return null;
    }

    /**
     * Returns the index of the page containing the given packed rectangle.
     *
     * @param name the name of the image
     * @return the index of the page the image is stored in or -1
     */
    public synchronized int getPageIndex(String name) {
        for (int i = 0; i < pages.size; i++) {
            Rectangle rect = pages.get(i).rects.get(name);
            if (rect != null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Disposes all resources, including Pixmap instances for the pages created
     * so far. These page Pixmap instances are shared with any
     * {@link TextureAtlas} generated or updated by either
     * {@link #generateTextureAtlas(TextureFilter, TextureFilter, boolean)} or
     * {@link #updateTextureAtlas(TextureAtlas, TextureFilter, TextureFilter, boolean)}.
     * Do not call this method if you generated or updated a TextureAtlas,
     * instead dispose the TextureAtlas.
     */
    @Override
    public synchronized void dispose() {
        for (Page page : pages) {
            page.image.dispose();
        }
        disposed = true;
    }

    /**
     * Generates a new {@link TextureAtlas} from the {@link Pixmap} instances
     * inserted so far.
     *
     * @param minFilter
     * @param magFilter
     * @return the TextureAtlas
     */
    public synchronized TextureAtlas generateTextureAtlas(TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
        TextureAtlas atlas = new TextureAtlas();
        for (Page page : pages) {
            if (page.rects.size != 0) {
                Texture texture = new Texture(new PixmapTextureData(page.image, page.image.getFormat(), useMipMaps, false, true)) {
                    @Override
                    public void dispose() {
                        super.dispose();
                        getTextureData().consumePixmap().dispose();
                    }
                };
                texture.setFilter(minFilter, magFilter);

                Keys<String> names = page.rects.keys();
                for (String name : names) {
                    Rectangle rect = page.rects.get(name);
                    TextureRegion region = new TextureRegion(texture, (int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height);
                    atlas.addRegion(name, region);
                }
            }
        }
        return atlas;
    }

    /**
     * Updates the given {@link TextureAtlas}, adding any new {@link Pixmap}
     * instances packed since the last call to this method. This can be used to
     * insert Pixmap instances on a separate thread via
     * {@link #pack(String, Pixmap)} and update the TextureAtlas on the
     * rendering thread. This method must be called on the rendering thread.
     */
    public synchronized void updateTextureAtlas(TextureAtlas atlas, TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
        for (Page page : pages) {
            if (page.texture == null) {
                if (page.rects.size != 0 && page.addedRects.size > 0) {
                    page.texture = new Texture(new PixmapTextureData(page.image, page.image.getFormat(), useMipMaps, false, true)) {
                        @Override
                        public void dispose() {
                            super.dispose();
                            getTextureData().consumePixmap().dispose();
                        }
                    };
                    page.texture.setFilter(minFilter, magFilter);

                    for (String name : page.addedRects) {
                        Rectangle rect = page.rects.get(name);
                        TextureRegion region = new TextureRegion(page.texture, (int) rect.x, (int) rect.y, (int) rect.width,
                                (int) rect.height);
                        atlas.addRegion(name, region);
                    }
                    page.addedRects.clear();
                }
            } else {
                if (page.addedRects.size > 0) {
                    page.texture.load(page.texture.getTextureData());
                    for (String name : page.addedRects) {
                        Rectangle rect = page.rects.get(name);
                        TextureRegion region = new TextureRegion(page.texture, (int) rect.x, (int) rect.y, (int) rect.width,
                                (int) rect.height);
                        atlas.addRegion(name, region);
                    }
                    page.addedRects.clear();
                    return;
                }
            }
        }
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public int getPadding() {
        return padding;
    }

}
