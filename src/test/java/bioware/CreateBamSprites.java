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

public class CreateBamSprites  extends InputAdapter implements ApplicationListener {
	
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
		
		HashMap<String, String> animNamesMap = new HashMap<String, String>();
//		animNamesMap.put("MIMP", "STAND2-1");
//		animNamesMap.put("MDRO", "STAND2-1");
//		animNamesMap.put("MEAS", "STAND2-1");
//		animNamesMap.put("MAIS", "STAND2-1");
//		animNamesMap.put("MBES", "STAND2-1");
//		animNamesMap.put("MDJL", "STAND2-1");
//		animNamesMap.put("MDLI", "STAND2-1");
//		animNamesMap.put("MDOP", "STAND1-9");
//		animNamesMap.put("MDRO", "STAND2-1");
//		animNamesMap.put("MEYE", "STAND1-1");
//		animNamesMap.put("MFDR", "STAND2-1");
//		animNamesMap.put("MFIS", "STAND2-1");
//		animNamesMap.put("MGHL", "STAND1-14");
//		animNamesMap.put("MGIB", "STAND1-9");
//		animNamesMap.put("MGIT", "STAND2-1");
//		animNamesMap.put("MGLC", "STAND2-1");
//		animNamesMap.put("MGO1", "STAND1-1");
//		animNamesMap.put("MGO2", "STAND1-1");
//		animNamesMap.put("MGO3", "STAND1-1");
//		animNamesMap.put("MGO4", "STAND1-1");
//		animNamesMap.put("MGWE", "STAND1-9");
//		animNamesMap.put("MLER", "STAND2-1");
//		animNamesMap.put("MLIC", "STAND2-1");
//		animNamesMap.put("MLIZ", "STAND2-1");
//		animNamesMap.put("MMEL", "STAND1-19");
//		animNamesMap.put("MMIN", "STAND2-1");
		animNamesMap.put("MMIS", "STAND2-1");
		animNamesMap.put("MMST", "STAND1-1");
		animNamesMap.put("MMUM", "STAND1-1");
		animNamesMap.put("MMY2", "STAND2-1");
		animNamesMap.put("MMYC", "STAND2-1");
		animNamesMap.put("MNO1", "STAND2-1");
		animNamesMap.put("MNO2", "STAND2-1");
		animNamesMap.put("MNO3", "STAND2-1");
		animNamesMap.put("MOR1", "STAND2-1");
		animNamesMap.put("MOR3", "STAND2-1");
		animNamesMap.put("MOR5", "STAND2-1");
		//animNamesMap.put("MOTY", "STAND2-1");
		//animNamesMap.put("MRAK", "STAND2-1");
		animNamesMap.put("MSA2", "STAND2-1");
		animNamesMap.put("MSAH", "STAND2-1");
		animNamesMap.put("MSAL", "STAND2-1");
		animNamesMap.put("MSHD", "STAND2-1");
		animNamesMap.put("MSLI", "STAND1-1");
		animNamesMap.put("MSLY", "STAND2-1");
		animNamesMap.put("MSPI", "STAND1-9");
		animNamesMap.put("MSPS", "STAND2-1");
		//animNamesMap.put("MTRO", "STAND2-1");
		//animNamesMap.put("MUMB", "STAND2-1");
		animNamesMap.put("MVAF", "STAND1-1");
		animNamesMap.put("MWER", "STAND1-9");
		animNamesMap.put("MWFM", "STAND2-1");

		Map<String, Animation> imgMap = new HashMap<String, Animation>();
		
		for (String bamName : animNamesMap.keySet()) {
			
			BamAnimationStore store = new BamAnimationStore(BAMDIR, bamName);
			store.init();
			
			System.out.println("animations size="+store.gdxAnimations.size());
			Iterator<String> iter = store.gdxAnimations.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				if (!key.startsWith("STAND")) {
					iter.remove();
					continue;
				}
			}
			
			imgMap.put(bamName, store.gdxAnimations.get(animNamesMap.get(bamName)));
		}
		
		AnimationPixMapPacker packer = new AnimationPixMapPacker(1024, 1024, Format.RGBA8888, 0, false);
		for (String bamName : animNamesMap.keySet()) {
			TextureRegion[] texts = imgMap.get(bamName).getKeyFrames();
			int count = 0;
			for (TextureRegion tr : texts) {
				packer.pack(bamName + "-" +count, tr.getTexture().getTextureData().consumePixmap());
				count ++;
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
