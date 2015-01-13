package util;


import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

import java.util.List;

import objects.BaseMap;
import objects.BaseMap.DoorStatus;
import objects.Creature;
import objects.Person;
import ultima.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;

public class UltimaMapRenderer extends BatchTiledMapRenderer implements Constants {
	
	private BaseMap bm;
	private ShadowFOV fov = new ShadowFOV();
	float stateTime = 0;
	
	TextureRegion door;
	TextureRegion brick_floor;
	TextureRegion locked_door;

	public UltimaMapRenderer(TextureAtlas atlas, BaseMap bm, TiledMap map, float unitScale) {
		super(map, unitScale);
		this.bm = bm;
		
		if (atlas != null) {
		
			door = atlas.findRegion("door");
			brick_floor = atlas.findRegion("brick_floor");
			locked_door = atlas.findRegion("locked_door");
			
			door.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
			brick_floor.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
			locked_door.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		}

	}

	@Override
	public void renderTileLayer(TiledMapTileLayer layer) {
				
		stateTime += Gdx.graphics.getDeltaTime();

		Color batchColor = batch.getColor();
		float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

		int layerWidth = layer.getWidth();
		int layerHeight = layer.getHeight();

		int layerTileWidth = (int) (layer.getTileWidth() * unitScale);
		int layerTileHeight = (int) (layer.getTileHeight() * unitScale);
		
		float[] vertices = this.vertices;

		int col1 = Math.max(0, (int) (viewBounds.x / layerTileWidth));
		int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));
		int row1 = Math.max(0, (int) (viewBounds.y / layerTileHeight));
		int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));
	
		if (bm.getBorderbehavior() == MapBorderBehavior.wrap) {
			col1 = (int) (viewBounds.x / layerTileWidth);
			col2 = (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth);
			row1 = (int) (viewBounds.y / layerTileHeight);
			row2 = (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight);
		}

		float y = row2 * layerTileHeight;
		float startX = col1 * layerTileWidth;
		
		//float[][] lightMap = null;
		//if (bm.getShadownMap() != null) {
		//	lightMap = fov.calculateFOV(bm.getShadownMap(), startx, starty, 15);	
		//}

		for (int row = row2; row >= row1; row--) {
			
			float x = startX;
			for (int col = col1; col < col2; col++) {
				
				TiledMapTileLayer.Cell cell = null;
				
				if (bm.getBorderbehavior() == MapBorderBehavior.wrap) {

					int cx = col;
					if (col < 0) {
						cx = layerWidth + col;
					} else if(col>=layerWidth) {
						cx = col - layerWidth;
					}
					int cy = row;
					if (row < 0) {
						cy = layerHeight + row;
					} else if(row>=layerHeight) {
						cy = row - layerHeight;
					}
					
					cell = layer.getCell(cx, cy);

				} else {
					cell = layer.getCell(col, row);
				}
				
								
				if (cell == null) { // || (lightMap != null && lightMap[col][row] <= 0)) {
					x += layerTileWidth;
					continue;
				}
				
				TiledMapTile tile = cell.getTile();
				if (tile != null) {

					TextureRegion region = tile.getTextureRegion();
									
					DoorStatus ds = bm.getDoor(col, layerHeight - row - 1);
					if (ds != null) {
						region = door;
						if (ds.locked) {
							region = locked_door;
						}
						if (bm.isDoorOpen(ds)) {
							region = brick_floor;
						}
					}

					float x1 = x + tile.getOffsetX() * unitScale;
					float y1 = y + tile.getOffsetY() * unitScale;
					float x2 = x1 + region.getRegionWidth() * unitScale;
					float y2 = y1 + region.getRegionHeight() * unitScale;

					float u1 = region.getU();
					float v1 = region.getV2();
					float u2 = region.getU2();
					float v2 = region.getV();

					vertices[X1] = x1;
					vertices[Y1] = y1;
					vertices[C1] = color;
					vertices[U1] = u1;
					vertices[V1] = v1;

					vertices[X2] = x1;
					vertices[Y2] = y2;
					vertices[C2] = color;
					vertices[U2] = u1;
					vertices[V2] = v2;

					vertices[X3] = x2;
					vertices[Y3] = y2;
					vertices[C3] = color;
					vertices[U3] = u2;
					vertices[V3] = v2;

					vertices[X4] = x2;
					vertices[Y4] = y1;
					vertices[C4] = color;
					vertices[U4] = u2;
					vertices[V4] = v1;

					batch.draw(region.getTexture(), vertices, 0, 20);
				}
				x += layerTileWidth;
			}
			y -= layerTileHeight;
		}
		
		
		//render person objects on map
		if (bm.getCity() != null) {
			for(Person p : bm.getCity().getPeople()) {
				if (p == null || p.isRemovedFromMap()) {
					continue;
				}
				
//				if (p.getConversation() != null && GameScreen.context.getParty().isJoinedInParty(p.getConversation().getName())) {
//					continue;
//				}
				
				//see if person is in shadow
//				int px = Math.round(p.getCurrentPos().x / tilePixelWidth);
//				int py = Math.round(p.getCurrentPos().y / tilePixelHeight);
//				if (lightMap != null && lightMap[px][py] <= 0) {
//					continue;
//				}
				
				batch.draw(p.getTextureRegion(), p.getCurrentPos().x, p.getCurrentPos().y, tilePixelWidth, tilePixelHeight);
				//batch.draw(p.getAnim().getKeyFrame(stateTime, true), p.getCurrentPos().x, p.getCurrentPos().y, tilePixelWidth, tilePixelHeight);
			}
			
		}
		
		List<Creature> crs = bm.getCreatures();
		if (crs.size() > 0) {
			for (Creature cr : crs) {
				if (cr.currentPos == null  || !cr.getVisible()) {
					continue;
				}
				
				//see if in shadow
//				int px = Math.round(cr.currentX / tilePixelWidth);
//				int py = Math.round(cr.currentY / tilePixelHeight);
//				if (lightMap != null && lightMap[px][py] <= 0) {
//					continue;
//				}

//				System.out.println(cr);
				
				if (cr.getTile() == CreatureType.pirate_ship) {
					TextureRegion tr = cr.getAnim().getKeyFrames()[cr.sailDir.getVal()-1];
					batch.draw(tr, cr.currentPos.x, cr.currentPos.y, tilePixelWidth, tilePixelHeight);
				} else {
					batch.draw(cr.getAnim().getKeyFrame(stateTime, true), cr.currentPos.x, cr.currentPos.y, tilePixelWidth, tilePixelHeight);
				}
				

			}
		}
	}
}
