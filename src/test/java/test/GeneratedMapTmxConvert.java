package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import test.StaticGeneratedDungeon.Key;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;

public class GeneratedMapTmxConvert {

    private String tilesetName;
    private String imageSource;
    private int mapWidth;
    private int mapHeight;
    private int tileWidth;
    private int tileHeight;

    private String layer1;
    private String layer2;
    private String layer3;

    private String objects1;
    private String objects2;
    private String objects3;

    public static void main(String[] args) throws Exception {

        int TILE_SIZE = 16;

        FileHandle f = new FileHandle("assets/tilemaps/tiles-vga-atlas.txt");
        TextureAtlasData atlas = new TextureAtlasData(f, f.parent(), false);
        String[] mapTileIds = new String[atlas.getRegions().size + 1];
        for (Region r : atlas.getRegions()) {
            int x = r.left / r.width;
            int y = r.top / r.height;
            int i = y * TILE_SIZE + x + 1;
            mapTileIds[i] = r.name;
        }

        List<StaticGeneratedDungeon> dungeons = new ArrayList<StaticGeneratedDungeon>();

        dungeons.add(new StaticGeneratedDungeon("The Dark Pit of Emes the Fallen 01 (tsv).txt"));
        dungeons.add(new StaticGeneratedDungeon("The Dark Pit of Emes the Fallen 10 (tsv).txt"));
        dungeons.add(new StaticGeneratedDungeon("The Dark Pit of Emes the Fallen 20 (tsv).txt"));

        List<String> layers = new ArrayList<String>();

        for (StaticGeneratedDungeon sd : dungeons) {

            StringBuffer data = new StringBuffer();

            for (int y = 0; y < StaticGeneratedDungeon.DIM; y++) {
                for (int x = 0; x < StaticGeneratedDungeon.DIM; x++) {
                    Key val = sd.getCell(x, y);
                    if (val == null) {
                        val = StaticGeneratedDungeon.Key.NULL;
                    }
                    data.append(findTileId(mapTileIds, val.getName()) + ",");
                }
                data.append("\n");
            }

            String dl = data.toString();
            dl = dl.substring(0, dl.length() - 2);

            layers.add(dl);

        }

        GeneratedMapTmxConvert c = new GeneratedMapTmxConvert("delve", "tiles-vga.png",
                StaticGeneratedDungeon.DIM, StaticGeneratedDungeon.DIM,
                TILE_SIZE, TILE_SIZE,
                layers.get(0), layers.get(1), layers.get(2),
                "", "", "");

        FileUtils.writeStringToFile(new File("assets/tilemaps/generatedDungeon.tmx"), c.toString());
    }

    private static int findTileId(String[] tiles, String name) {
        for (int i = 1; i < tiles.length; i++) {
            if (tiles[i] == null) {
                continue;
            }
            if (StringUtils.equals(tiles[i], name)) {
                return i;
            }
        }
        return 0;
    }

    private GeneratedMapTmxConvert(String tilesetName, String imageSource,
            int mapWidth, int mapHeight,
            int tileWidth, int tileHeight,
            String l1, String l2, String l3,
            String o1, String o2, String o3) {

        this.tilesetName = tilesetName;
        this.imageSource = imageSource;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        this.layer1 = l1;
        this.layer2 = l2;
        this.layer3 = l3;

        this.objects1 = o1;
        this.objects2 = o2;
        this.objects3 = o3;

    }

    @Override
    public String toString() {

        String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<map version=\"1.0\" orientation=\"orthogonal\" width=\"%s\" height=\"%s\" tilewidth=\"%s\" tileheight=\"%s\" renderorder=\"right-down\">\n"
                + "<tileset firstgid=\"1\" name=\"%s\" tilewidth=\"%s\" tileheight=\"%s\">\n"
                + "<image source=\"%s\" width=\"%s\" height=\"%s\"/>\n</tileset>\n"
                + "<layer name=\"Level 1\" width=\"%s\" height=\"%s\">\n"
                + "<data encoding=\"csv\">\n%s\n</data>\n</layer>\n"
                + "<layer name=\"Level 2\" width=\"%s\" height=\"%s\">\n"
                + "<data encoding=\"csv\">\n%s\n</data>\n</layer>\n"
                + "<layer name=\"Level 3\" width=\"%s\" height=\"%s\">\n"
                + "<data encoding=\"csv\">\n%s\n</data>\n</layer>\n"
                + "<objectgroup name=\"Level 1 Properties\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n"
                + "<objectgroup name=\"Level 2 Properties\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n"
                + "<objectgroup name=\"Level 3 Properties\" width=\"%s\" height=\"%s\">\n%s\n</objectgroup>\n"
                + "</map>";

        return String.format(template,
                mapWidth, mapHeight, tileWidth, tileHeight,
                tilesetName, tileWidth, tileHeight,
                imageSource, 256, 256,
                mapWidth, mapHeight, layer1,
                mapWidth, mapHeight, layer2,
                mapWidth, mapHeight, layer3,
                mapWidth, mapHeight, objects1,
                mapWidth, mapHeight, objects2,
                mapWidth, mapHeight, objects3
        );

    }

}
