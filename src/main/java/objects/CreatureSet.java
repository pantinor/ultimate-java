package objects;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ultima.Constants.CreatureType;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.utils.Array;

@XmlRootElement(name = "creatures")
public class CreatureSet {
	
	private List<Creature> creatures;
	
	@XmlElement(name="creature")
	public List<Creature> getCreatures() {
		return creatures;
	}

	public void setCreatures(List<Creature> creatures) {
		this.creatures = creatures;
	}
	
	public void init() {
		
		for(Creature cr : creatures ) {
			CreatureType ct = CreatureType.get(cr.getId());
			if (ct != null) ct.setCreature(cr);
		}
	}
	
	public Creature getInstance(CreatureType type, TextureAtlas atlas1, TextureAtlas atlas2) {
		for (Creature cr : creatures) {
			if (cr.getTile() == type) {
				
				Creature newCr = new Creature(cr);
				
				Array<AtlasRegion> tr = atlas1.findRegions(cr.getTile().toString());
				if (tr == null || tr.size == 0) {
					tr = atlas2.findRegions(cr.getTile().toString());
				}
				
				int frameRate = ThreadLocalRandom.current().nextInt(1,3);
				
				int fr = ThreadLocalRandom.current().nextInt(0,tr.size);
				TextureRegion reg = tr.get(fr);
				
				newCr.setAnim(new Animation(frameRate, reg));
				
				Decal d = Decal.newDecal(reg, true);
				d.setScale(.018f);
				newCr.setDecal(d);
				
				if (type == CreatureType.pirate_ship) {
					newCr.setAnim(new Animation(frameRate, tr));
				}
				
				return newCr;
			}
		}
		
		System.err.println(type + " not found.");

		return null;
	}
	
	
	public CreatureType getRandomAmbushing() {
		
	    int numAmbushingCreatures = 0, randCreature = 0;
	    for (Creature cr : creatures) {
	        if (cr.getAmbushes())
	            numAmbushingCreatures++;
	    }
	    
	    if (numAmbushingCreatures > 0) {
	    	
	        randCreature = new Random().nextInt(numAmbushingCreatures);
	        numAmbushingCreatures = 0;
	        
		    for (Creature cr : creatures) {
		    	
		        if (cr.getAmbushes()) {
		        	
	                if (numAmbushingCreatures == randCreature) {
	                	return cr.getTile();
	                } else {
	                	numAmbushingCreatures++;
	                }
	            }
	        }
	    }

	    return null;
	}

}
