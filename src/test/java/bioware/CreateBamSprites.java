package bioware;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CreateBamSprites extends InputAdapter implements ApplicationListener {

    public static final String BAMDIR = "C:\\Users\\Paul\\Desktop\\BAMS";

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "CreateBamSprites";
        cfg.width = 800;
        cfg.height = 600;
        new LwjglApplication(new CreateBamSprites(), cfg);
    }

    @Override
    public void create() {
        pack();
    }

    public void pack() {

        HashMap<String, String> animNamesMap = new HashMap<>();
        animNamesMap.put("MGO1", "STAND1-1");
        animNamesMap.put("MGO2", "STAND1-1");
        animNamesMap.put("MGO3", "STAND1-1");
        animNamesMap.put("MGO4", "STAND1-1");
        animNamesMap.put("NSHD", "STAND1-1");
        animNamesMap.put("NSOL", "STAND1-1");
        animNamesMap.put("MMIS", "STAND1-1");
        animNamesMap.put("MBESG", "STAND1-1");
        animNamesMap.put("MMUM", "STAND1-1");
        animNamesMap.put("MMY2", "STAND1-1");
        animNamesMap.put("MMYC", "STAND1-1");
        animNamesMap.put("MNO1", "STAND1-1");
        animNamesMap.put("MNO2", "STAND1-1");
        animNamesMap.put("MNO3", "STAND1-1");
        animNamesMap.put("MOR1", "STAND1-1");
        animNamesMap.put("MOR3", "STAND1-1");
        animNamesMap.put("MOR5", "STAND1-1");
        animNamesMap.put("MOTY", "STAND1-1");
        animNamesMap.put("NPIR", "STAND1-1");
        animNamesMap.put("MLIC", "STAND1-1");
        animNamesMap.put("MSAH", "STAND1-1");
        animNamesMap.put("MSHD", "STAND1-1");
        animNamesMap.put("NSAI", "STAND1-1");
        animNamesMap.put("MSLY", "STAND1-1");
        animNamesMap.put("MSPI", "STAND1-1");
        animNamesMap.put("MSPS", "STAND1-1");
//        animNamesMap.put("NIRO", "STAND1-1");
//        animNamesMap.put("MUMB", "STAND1-1");
//        animNamesMap.put("MVAF", "STAND1-1");
//        animNamesMap.put("MWER", "STAND1-1");
//        animNamesMap.put("MWFM", "STAND1-1");

        Map<String, Animation> imgMap = new HashMap<>();

        for (String bamName : animNamesMap.keySet()) {

            BamAnimationStore store = new BamAnimationStore(BAMDIR, bamName);
            store.init();

            System.out.println("animations size=" + store.gdxAnimations.size());
            Iterator<String> iter = store.gdxAnimations.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                if (!key.startsWith("STAND")) {
                    iter.remove();
                }
            }

            imgMap.put(bamName, store.gdxAnimations.get(animNamesMap.get(bamName)));
        }

        AnimationPixMapPacker packer = new AnimationPixMapPacker(2000, 1500, Format.RGBA8888, 0);
        for (String bamName : animNamesMap.keySet()) {
            TextureRegion[] texts = imgMap.get(bamName).getKeyFrames();
            int count = 0;
            for (TextureRegion tr : texts) {
                packer.pack(bamName + "-" + count, tr.getTexture().getTextureData().consumePixmap());
                count++;
            }
        }

        try {
            AnimationPixmapPackerIO pp = new AnimationPixmapPackerIO();
            pp.save(new FileHandle(new File("bioware-sprites-2")), packer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("done");

    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void render() {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

}
