package ultima;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import objects.BaseMap;
import objects.Creature;
import objects.CreatureSet;
import objects.Party;
import objects.Party.PartyMember;
import objects.ProjectileActor;
import objects.Tile;

import org.apache.commons.lang3.StringUtils;

import ultima.DungeonScreen.DungeonRoom;
import ultima.DungeonScreen.Trigger;
import util.UltimaTiledMapLoader;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import objects.Person;
import util.PartyDeathException;
import util.XORShiftRandom;

public class CombatScreen extends BaseScreen {

    public static int AREA_CREATURES = 16;
    public static int AREA_PLAYERS = 8;

    private CreatureType[] crSlots;

    private CursorActor cursor;

    private Maps contextMap;
    public BaseMap combatMap;
    private CreatureType crType;
    private CreatureSet creatureSet;

    public Party party;

    private TiledMap tmap;
    private OrthogonalTiledMapRenderer renderer;
    private SpriteBatch batch;
    private SecondaryInputProcessor sip;

    private Viewport mapViewPort;

    private boolean wounded;

    public CombatScreen(BaseScreen returnScreen, Context context, Maps contextMap,
            BaseMap combatMap, TiledMap tmap, CreatureType cr, CreatureSet cs, TextureAtlas a1) {

        scType = ScreenType.COMBAT;

        this.returnScreen = returnScreen;
        this.contextMap = contextMap;
        this.combatMap = combatMap;
        this.combatMap.clearCreatures();

        this.crType = cr;

        this.context = context;
        this.party = context.getParty();
        this.creatureSet = cs;

        this.tmap = tmap;
        renderer = new OrthogonalTiledMapRenderer(tmap, 1f);

        MapProperties prop = tmap.getProperties();
        mapPixelHeight = prop.get("height", Integer.class) * tilePixelHeight;

        camera = new OrthographicCamera(11 * tilePixelWidth, 11 * tilePixelHeight);

        mapViewPort = new ScreenViewport(camera);

        stage = new Stage();
        stage.setViewport(mapViewPort);

        cursor = new CursorActor();
        stage.addActor(cursor);
        cursor.addAction(forever(sequence(fadeOut(1), fadeIn(1))));

        batch = new SpriteBatch();

        sip = new SecondaryInputProcessor(this, stage);

        crSlots = new CreatureType[AREA_CREATURES];

        if (crType != null) {
            fillCreatureTable(crType);
        }

        MapLayer mLayer = tmap.getLayers().get("Monster Positions");
        Iterator<MapObject> iter = mLayer.getObjects().iterator();
        while (iter.hasNext()) {
            MapObject obj = iter.next();
            int index = (Integer) obj.getProperties().get("index");
            int startX = (Integer) obj.getProperties().get("startX");
            int startY = (Integer) obj.getProperties().get("startY");

            if (crSlots[index] == null) {
                continue;
            }

            Creature c = creatureSet.getInstance(crSlots[index], a1);

            c.currentX = startX;
            c.currentY = startY;
            c.currentPos = getMapPixelCoords(startX, startY);

            combatMap.addCreature(c);
        }

        MapLayer pLayer = tmap.getLayers().get("Player Positions");
        iter = pLayer.getObjects().iterator();
        while (iter.hasNext()) {
            MapObject obj = iter.next();
            int index = (Integer) obj.getProperties().get("index");
            int startX = (Integer) obj.getProperties().get("startX");
            int startY = (Integer) obj.getProperties().get("startY");

            if (index + 1 > party.getSaveGame().members) {
                continue;
            }

            Creature c = creatureSet.getInstance(CreatureType.get(party.getMember(index).getPlayer().klass.toString().toLowerCase()), a1);
            c.currentX = startX;
            c.currentY = startY;
            c.currentPos = getMapPixelCoords(startX, startY);

            party.getMember(index).combatCr = c;

            if (index == 0) {
                cursor.setPos(c.currentPos);
            }
        }

        combatMap.setCombatPlayers(party.getMembers());

        newMapPixelCoords = getMapPixelCoords(5, 5);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        party.addObserver(this);
    }

    @Override
    public void hide() {
        party.deleteObserver(this);
    }

    @Override
    public void dispose() {
        stage.dispose();
        renderer.dispose();
        batch.dispose();
    }

    private void fillCreatureTable(CreatureType ct) {

        if (ct == null) {
            return;
        }

        int numCreatures = getNumberOfCreatures(ct);

        CreatureType baseType = ct;
        if (baseType == CreatureType.pirate_ship) {
            baseType = CreatureType.rogue;
        }

        for (int i = 0; i < numCreatures; i++) {
            CreatureType current = baseType;

            /* find a free spot in the creature table */
            int j = 0;
            do {
                j = rand.nextInt(AREA_CREATURES);
            } while (crSlots[j] != null);

            /* see if creature is a leader or leader's leader */
            if (baseType.getCreature().getLeader() != 0) {
                if (CreatureType.get(baseType.getCreature().getLeader()) != baseType.getCreature().getTile() && i != (numCreatures - 1)) {
                    if (rand.nextInt(32) == 0) { // leader's leader
                        CreatureType t1 = CreatureType.get(baseType.getCreature().getLeader());
                        CreatureType t2 = CreatureType.get(t1.getCreature().getLeader());
                        current = t2;
                    } else if (rand.nextInt(8) == 0) { // leader
                        current = CreatureType.get(baseType.getCreature().getLeader());
                    }
                }
            }

            /* place this creature in the creature table */
            crSlots[j] = current;
        }

    }

    private int getNumberOfCreatures(CreatureType ct) {
        int ncreatures = 0;

        if (contextMap == Maps.WORLD || contextMap.getMap().getType() == MapType.dungeon) {

            ncreatures = rand.nextInt(8) + 1;

            if (ncreatures == 1) {
                if (ct != null && ct.getCreature().getEncounterSize() > 0) {
                    ncreatures = rand.nextInt(ct.getCreature().getEncounterSize()) + ct.getCreature().getEncounterSize() + 1;
                } else {
                    ncreatures = 8;
                }
            }

            while (ncreatures > 2 * party.getSaveGame().members) {
                ncreatures = rand.nextInt(16) + 1;
            }

        } else {
            if (ct != null && ct.getCreature().getTile() == CreatureType.guard) {
                ncreatures = party.getSaveGame().members * 2;
            } else {
                ncreatures = 1;
            }
        }

        return ncreatures;
    }

    public void setAmbushingMonsters(BaseScreen returnScreen, int x, int y, TextureAtlas a1) {

        CreatureType ct = Ultima4.creatures.getRandomAmbushing();
        fillCreatureTable(ct);

        MapLayer mLayer = tmap.getLayers().get("Monster Positions");
        Iterator<MapObject> iter = mLayer.getObjects().iterator();
        while (iter.hasNext()) {
            MapObject obj = iter.next();
            int index = (Integer) obj.getProperties().get("index");
            int startX = (Integer) obj.getProperties().get("startX");
            int startY = (Integer) obj.getProperties().get("startY");

            if (crSlots[index] == null) {
                continue;
            }

            Creature c = creatureSet.getInstance(crSlots[index], a1);

            c.currentX = startX;
            c.currentY = startY;
            c.currentPos = getMapPixelCoords(startX, startY);

            combatMap.addCreature(c);
        }

        //for the chest when returning after combat
        returnScreen.currentEncounter = creatureSet.getInstance(ct, a1);
        returnScreen.currentEncounter.currentX = x;
        returnScreen.currentEncounter.currentY = y;
        returnScreen.currentEncounter.currentPos = returnScreen.getMapPixelCoords(x, y);

    }

    @Override
    public Vector3 getMapPixelCoords(int x, int y) {
        Vector3 v = new Vector3(x * tilePixelWidth, mapPixelHeight - y * tilePixelHeight - tilePixelHeight, 0);
        return v;
    }

    @Override
    public Vector3 getCurrentMapCoords() {
        return null;
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(newMapPixelCoords.x + 5 * tilePixelWidth, newMapPixelCoords.y, 0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - tilePixelWidth * 10, //this is voodoo
                camera.position.y - tilePixelHeight * 10,
                Ultima4.MAP_WIDTH,
                Ultima4.MAP_HEIGHT);

        renderer.render();

        renderer.getBatch().begin();
        for (Creature cr : combatMap.getCreatures()) {
            if (cr.currentPos == null || !cr.getVisible()) {
                continue;
            }
            renderer.getBatch().draw(cr.getAnim().getKeyFrame(time, true), cr.currentPos.x, cr.currentPos.y);
        }

        for (PartyMember p : party.getMembers()) {
            if (p.combatCr == null || p.combatCr.currentPos == null || p.fled) {
                continue;
            }

            if (p.getPlayer().status != StatusType.DEAD && p.getPlayer().status != StatusType.SLEEPING) {
                renderer.getBatch().draw(p.combatCr.getAnim().getKeyFrame(time, true), p.combatCr.currentPos.x, p.combatCr.currentPos.y);
            } else {
                renderer.getBatch().draw(Ultima4.corpse, p.combatCr.currentPos.x, p.combatCr.currentPos.y);
            }
        }

        renderer.getBatch().end();

        batch.begin();
        batch.draw(Ultima4.backGround, 0, 0);

        Ultima4.hud.render(batch, party);

        Ultima4.font.setColor(Color.WHITE);
        if (showZstats > 0) {
            party.getSaveGame().renderZstats(showZstats, Ultima4.font, batch, Ultima4.SCREEN_HEIGHT);
        }

        if (context.getAura().getType() != AuraType.NONE) {
            Ultima4.font.draw(batch, context.getAura().getType().toString(), 430, Ultima4.SCREEN_HEIGHT - 7);
        }

        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        mapViewPort.update(width, height, false);
    }

    @Override
    public boolean keyUp(int keycode) {

        PartyMember ap = party.getActivePartyMember();
        Creature active = ap.combatCr;

        try {
            if (keycode == Keys.SPACE || ap.isDisabled()) {
                log("Pass");
            } else if (keycode == Keys.UP) {
                if (preMove(active, Direction.NORTH)) {
                    active.currentY--;
                    active.currentPos = getMapPixelCoords(active.currentX, active.currentY);
                    checkTileAffects(ap, active.currentX, active.currentY);
                }
            } else if (keycode == Keys.DOWN) {
                if (preMove(active, Direction.SOUTH)) {
                    active.currentY++;
                    active.currentPos = getMapPixelCoords(active.currentX, active.currentY);
                    checkTileAffects(ap, active.currentX, active.currentY);
                }
            } else if (keycode == Keys.RIGHT) {
                if (preMove(active, Direction.EAST)) {
                    active.currentX++;
                    active.currentPos = getMapPixelCoords(active.currentX, active.currentY);
                    checkTileAffects(ap, active.currentX, active.currentY);
                }
            } else if (keycode == Keys.LEFT) {
                if (preMove(active, Direction.WEST)) {
                    active.currentX--;
                    active.currentPos = getMapPixelCoords(active.currentX, active.currentY);
                    checkTileAffects(ap, active.currentX, active.currentY);
                }
            } else if (keycode == Keys.A) {
                log("Attack: ");
                Gdx.input.setInputProcessor(sip);
                sip.setinitialKeyCode(keycode, combatMap, active.currentX, active.currentY);
                return false;
            } else if (keycode == Keys.C) {
                log("Cast Spell (A-Z): ");
                Gdx.input.setInputProcessor(new SpellInputProcessor(this, context, stage, active.currentX, active.currentY, ap));
                return false;
            } else if (keycode == Keys.U) {
                Tile tile = combatMap.getTile(active.currentX, active.currentY);
                if (tile.getIndex() == 74 //altar
                        || (party.getSaveGame().items & Item.RAGE_GOD.getLoc()) > 0
                        || (party.getSaveGame().items & Item.MASK_MINAX.getLoc()) > 0) { //altar or rage of god
                    log("Use which item: ");
                    log("");
                    Gdx.input.setInputProcessor(sip);
                    sip.setinitialKeyCode(keycode, combatMap, active.currentX, active.currentY);
                    return false;
                } else {
                    log("Nothing to use!");
                }
            } else if (keycode == Keys.R) {
                Gdx.input.setInputProcessor(new ReadyWearInputAdapter(ap, true));
                return false;

            } else if (keycode == Keys.W) {
                Gdx.input.setInputProcessor(new ReadyWearInputAdapter(ap, false));
                return false;

            } else if (keycode == Keys.G) {
                getChest(party.getActivePlayer(), active.currentX, active.currentY);
                return false;

            } else if (keycode == Keys.Z) {
                showZstats = showZstats + 1;
                if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER8) {
                    if (showZstats > party.getMembers().size()) {
                        showZstats = STATS_WEAPONS;
                    }
                }
                if (showZstats > STATS_SPELLS) {
                    showZstats = STATS_NONE;
                }

                return false;
            }

            finishPlayerTurn();
        } catch (PartyDeathException e) {
            this.returnScreen.partyDeath();
        }

        return false;

    }

    private void checkTileAffects(PartyMember ap, int x, int y) throws PartyDeathException {

        Tile tile = combatMap.getTile(x, y);
        if (tile == null || tile.getRule() == null) {
            return;
        }

        TileEffect effect = tile.getRule().getEffect();
        context.getParty().applyEffect(ap, effect);
        if (effect == TileEffect.FIRE || effect == TileEffect.LAVA) {
            Sounds.play(Sound.FIREFIELD);
        } else if (effect == TileEffect.POISON || effect == TileEffect.POISONFIELD) {
            Sounds.play(Sound.POISON_EFFECT);
        } else if (effect == TileEffect.SLEEP) {
            Sounds.play(Sound.SLEEP);
        }

        DungeonRoom room = (DungeonRoom) tmap.getProperties().get("dungeonRoom");
        if (room != null) {
            for (int i = 0; i < 4; i++) {
                Trigger tr = room.triggers[i];
                if (tr.tile.getIndex() != 0 && tr.trigX == x && tr.trigY == y) {

                    Sounds.play(Sound.TRIGGER);

                    TileRule rule = tr.tile.getRule();

                    boolean nullplace1 = tr.t1X == 0 && tr.t1Y == 0;
                    boolean nullplace2 = tr.t2X == 0 && tr.t2Y == 0;

                    if (rule == TileRule.monster) {

                    } else {
                        if (!nullplace1) {
                            replaceTile(tr.tile.getName(), tr.t1X, tr.t1Y);
                        }
                        if (!nullplace2) {
                            replaceTile(tr.tile.getName(), tr.t2X, tr.t2Y);
                        }
                    }
                }
            }
        }
    }

    public void replaceTile(String name, int x, int y) {
        if (name == null) {
            return;
        }
        TextureRegion texture = Ultima4.standardAtlas.findRegion(name);
        TiledMapTileLayer layer = (TiledMapTileLayer) tmap.getLayers().get("Map Layer");
        Cell cell = layer.getCell(x, 11 - 1 - y);
        TiledMapTile tmt = new StaticTiledMapTile(texture);
        tmt.setId(y * 11 + x);
        cell.setTile(tmt);
        combatMap.setTile(Ultima4.baseTileSet.getTileByName(name), x, y);
    }

    private boolean preMove(Creature active, Direction dir) {

        int x = active.currentX;
        int y = active.currentY;

        AttackVector next = null;
        if (dir == Direction.NORTH) {
            next = new AttackVector(x, y - 1);
        }
        if (dir == Direction.SOUTH) {
            next = new AttackVector(x, y + 1);
        }
        if (dir == Direction.EAST) {
            next = new AttackVector(x + 1, y);
        }
        if (dir == Direction.WEST) {
            next = new AttackVector(x - 1, y);
        }

        if (next.x > combatMap.getWidth() - 1 || next.x < 0 || next.y > combatMap.getHeight() - 1 || next.y < 0) {

            if (combatMap.getType() == MapType.dungeon && !party.isOKtoExitDirection(dir)) {
                log("Cannot exit in that direction!");
                Sounds.play(Sound.BLOCKED);
                return false;
            } else {
                PartyMember ap = party.getActivePartyMember();
                ap.fled = true;
                ap.combatMapExitDirection = dir;
                Sounds.play(Sound.FLEE);

                if (party.getAbleCombatPlayers() == 0) {
                    end();
                    return false;
                } else {
                    int ni = party.getNextActiveIndex();
                    Creature nextActivePlayer = party.getMember(ni).combatCr;
                    cursor.setPos(nextActivePlayer.currentPos);
                }
            }

        } else {

            int mask = combatMap.getValidMovesMask(context, x, y);
            if (!Direction.isDirInMask(dir, mask)) {
                Sounds.play(Sound.BLOCKED);
                return false;
            }
        }

        return true;
    }

    public void finishPlayerTurn() {

        //remove dead creatures
        Iterator<Creature> iter = combatMap.getCreatures().iterator();
        while (iter.hasNext()) {
            Creature c = iter.next();
            if (c.getDamageStatus() == CreatureStatus.DEAD) {
                iter.remove();
            }
        }

        boolean roundIsDone = party.isRoundDone() || combatMap.getCreatures().isEmpty();

        PartyMember next = party.getAndSetNextActivePlayer();
        if (next != null) {
            cursor.setVisible(true);
            Creature nextActivePlayer = next.combatCr;
            cursor.setPos(nextActivePlayer.currentPos);
        } else {
            cursor.setVisible(false);
        }

        if (roundIsDone) {
            finishTurn(0, 0);
        }
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

        try {
            party.endTurn(combatMap.getType());

            context.getAura().passTurn();

            if (combatMap.getCreatures().isEmpty() && combatMap.getType() == MapType.combat) {
                end();
                return;
            }

            boolean quick = context.getAura().getType() == AuraType.QUICKNESS && (rand.nextInt(2) == 0);

            if (!quick) {
                SequenceAction seq = Actions.action(SequenceAction.class);
                for (Creature cr : combatMap.getCreatures()) {
                    seq.addAction(Actions.run(new CreatureActionsAction(cr)));
                    seq.addAction(Actions.delay(.04f));
                }
                seq.addAction(Actions.run(new FinishCreatureAction()));
                stage.addAction(seq);
            }
        } catch (PartyDeathException e) {
            this.returnScreen.partyDeath();
        }

    }

    public class CreatureActionsAction implements Runnable {

        private Creature cr;

        public CreatureActionsAction(Creature cr) {
            super();
            this.cr = cr;
        }

        @Override
        public void run() {
            try {
                if (!creatureAction(cr)) {
                    //remove creature from map
                    combatMap.getCreatures().remove(cr);
                }
            } catch (PartyDeathException e) {
                CombatScreen.this.returnScreen.partyDeath();
            }
        }
    }

    public class RemoveCreatureAction implements Runnable {

        private Creature cr;

        public RemoveCreatureAction(Creature cr) {
            this.cr = cr;
        }

        @Override
        public void run() {
            combatMap.getCreatures().remove(cr);
        }
    }

    public class FinishCreatureAction implements Runnable {

        @Override
        public void run() {
            //enable input again
            Gdx.input.setInputProcessor(CombatScreen.this);
            if (!party.isAnyoneAlive()) {
                end();
            }
        }
    }

    public void end() {

        if (returnScreen != null) {
            boolean isWon = combatMap.getCreatures().isEmpty();
            returnScreen.endCombat(isWon, combatMap, wounded);
        }

        combatMap.setCombatPlayers(null);
        party.reset();
    }

    private boolean rangedAttackAt(AttackVector target, Creature attacker) throws PartyDeathException {

        PartyMember defender = null;
        for (PartyMember p : party.getMembers()) {
            if (p.combatCr.currentX == target.x && p.combatCr.currentY == target.y) {
                defender = p;
                break;
            }
        }

        if (defender == null) {
            return false;
        }

        AttackResult res = Utils.attackHit(attacker, defender);

        TileEffect effect = TileEffect.NONE;
        Color col = Color.WHITE;

        if (attacker.rangedAttackIs("poison_field")) {
            effect = TileEffect.POISON;
            col = Color.GREEN;
        } else if (attacker.rangedAttackIs("magic_flash")) {
            col = Color.CYAN;
        } else if (attacker.rangedAttackIs("fire_field")) {
            effect = TileEffect.FIRE;
            col = Color.RED;
        } else if (attacker.rangedAttackIs("sleep_field")) {
            effect = TileEffect.SLEEP;
            col = Color.PURPLE;
        } else if (attacker.rangedAttackIs("energy_field")) {
            effect = TileEffect.ELECTRICITY;
            col = Color.BLUE;
        } else if (attacker.rangedAttackIs("rocks")) {
            col = Color.BROWN;
        }

        final ProjectileActor p = new ProjectileActor(this, col, attacker.currentX, attacker.currentY, res);
        Vector3 v = getMapPixelCoords(defender.combatCr.currentX, defender.combatCr.currentY);
        p.addAction(sequence(moveTo(v.x, v.y, .3f), new Action() {
            public boolean act(float delta) {
                switch (p.res) {
                    case HIT:
                        p.resultTexture = Ultima4.hitTile;
                        Sounds.play(Sound.NPC_MAGIC_STRUCK);
                        wounded = true;
                        break;
                    case MISS:
                        p.resultTexture = Ultima4.missTile;
                        Sounds.play(Sound.EVADE);
                        break;
                }
                return true;
            }
        }, fadeOut(.2f), removeActor(p)));

        stage.addActor(p);

        switch (effect) {
            case ELECTRICITY:
                Sounds.play(Sound.LIGHTNING);
                log("Electrified!");
                Utils.dealDamage(attacker, defender);
                break;
            case FIRE:
            case LAVA:
                Sounds.play(Sound.FIREBALL);
                log("Fiery Hit!");
                Utils.dealDamage(attacker, defender);
                break;
            case NONE:
                break;
            case POISON:
            case POISONFIELD:
                if (rand.nextInt(2) == 0 && defender.getPlayer().status != StatusType.POISONED) {
                    Sounds.play(Sound.POISON_EFFECT);
                    log("Poisoned!");
                    defender.getPlayer().status = StatusType.POISONED;
                }
                break;
            case SLEEP:
                if (context.getAura().getType() == AuraType.NEGATE) {
                    log("Sleep attack negated.");
                } else {
                    log("Sleep!");
                    Sounds.play(Sound.SLEEP);
                    if (rand.nextInt(2) == 0 && !defender.isDisabled()) {
                        defender.putToSleep();
                    }
                }
                break;
            default:
                break;

        }

        if (res == AttackResult.HIT) {
            Utils.dealDamage(attacker, defender);
        }

        return res == AttackResult.HIT;
    }

    @Override
    public void partyDeath() {
        //not used here
    }

    /**
     * Return false if to remove from map.
     */
    private boolean creatureAction(Creature creature) throws PartyDeathException {

        //accept no input starting now, re-enabled when creature actions are done
        Gdx.input.setInputProcessor(null);

        if (creature.getStatus() == StatusType.SLEEPING && rand.nextInt(16) == 0) {
            creature.setStatus(StatusType.GOOD);
        }

        if (creature.getStatus() == StatusType.SLEEPING) {
            return true;
        }

        if (creature.negates()) {
            context.setAura(AuraType.NEGATE, 2);
        }

        CombatAction action = null;

        if (creature.getTeleports() && rand.nextInt(100) <= 25) {
            action = CombatAction.TELEPORT;
        } else if (creature.getRanged() && rand.nextInt(100) <= 25) {
            action = CombatAction.RANGED;
        } else if (creature.castsSleep() && rand.nextInt(100) <= 25 && context.getAura().getType() != AuraType.NEGATE) {
            action = CombatAction.CAST_SLEEP;
        } else if (creature.getDamageStatus() == CreatureStatus.FLEEING) {
            action = CombatAction.FLEE;
        } else {
            action = CombatAction.ATTACK;
        }

        /* 
         * now find out who to do it to
         */
        DistanceWrapper dist = new DistanceWrapper(0);
        PartyMember target = nearestPartyMember(creature.currentX, creature.currentY, dist, action == CombatAction.RANGED);
        if (target == null) {
            return true;
        }

        if (action == CombatAction.ATTACK && dist.getVal() > 1) {
            action = CombatAction.ADVANCE;
        }

        if (creature.getCamouflage() && !hideOrShow(creature)) {
            return true;
        }

        switch (action) {
            case ATTACK: {
                Sounds.play(Sound.NPC_ATTACK);

                if (Utils.attackHit(creature, target) == AttackResult.HIT) {
                    Sounds.play(Sound.PC_STRUCK);
                    wounded = true;

                    if (!Utils.dealDamage(creature, target)) {
                        target = null;
                    }

                    if (target != null) {
                        if (creature.stealsFood() && rand.nextInt(8) == 0) {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                            party.adjustFood(-(rand.nextInt(10) * 1000));
                        }

                        if (creature.stealsGold() && rand.nextInt(8) == 0) {
                            Sounds.play(Sound.NEGATIVE_EFFECT);
                            party.adjustGold(-(rand.nextInt(40)));
                        }
                    }
                }
                break;
            }
            case CAST_SLEEP: {
                if (context.getAura().getType() == AuraType.NEGATE) {
                    log("Sleep spell negated.");
                } else {
                    log("Sleep!");
                    Sounds.play(Sound.SLEEP);
                    for (PartyMember p : party.getMembers()) {
                        if (rand.nextInt(2) == 0 && !p.isDisabled()) {
                            p.putToSleep();
                        }
                    }
                }
                break;
            }

            case TELEPORT: {//only wisp teleports
                boolean valid = false;
                int rx = 0, ry = 0, count = 0;
                while (!valid && count < 5) {
                    rx = rand.nextInt(combatMap.getWidth());
                    ry = rand.nextInt(combatMap.getHeight());
                    Tile t = combatMap.getTile(rx, ry);
                    if (t != null) {
                        valid = true;
                        TileRule rule = t.getRule();
                        if (rule != null && rule.has(TileAttrib.creatureunwalkable)) {
                            valid = false;
                        }
                    }
                    count++;
                }
                if (valid) {
                    moveCreature(action, creature, rx, ry);
                }
                break;
            }

            case RANGED: {

                // figure out which direction to fire the weapon
                int dirmask = Utils.getRelativeDirection(MapBorderBehavior.fixed, combatMap.getWidth(), combatMap.getHeight(), target.combatCr.currentX, target.combatCr.currentY, creature.currentX, creature.currentY);

                Sounds.play(Sound.NPC_ATTACK);

                List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dirmask, creature.currentX, creature.currentY, 1, 11, false, false, false);
                for (AttackVector v : path) {
                    if (rangedAttackAt(v, creature)) {
                        break;
                    }
                }

                break;
            }

            case FLEE:
            case ADVANCE: {

                if (StringUtils.equals("none", creature.getMovement())) {
                    return true; //reapers do not move
                }

                moveCreature(action, creature, target.combatCr.currentX, target.combatCr.currentY);

                //is map OOB
                if (creature.currentX > combatMap.getWidth() - 1 || creature.currentX < 0
                        || creature.currentY > combatMap.getHeight() - 1 || creature.currentY < 0) {
                    log(String.format("%s Flees!", creature.getName()));
                    Sounds.play(Sound.EVADE);
                    if (creature.getGood()) {
                        party.adjustKarma(KarmaAction.SPARED_GOOD);
                    }
                    return false;
                }

                break;
            }
        }

        return true;
    }

    private PartyMember nearestPartyMember(int fromX, int fromY, DistanceWrapper dist, boolean ranged) {
        PartyMember opponent = null;
        int d = 0;
        int leastDist = 0xFFFF;
        boolean jinx = context.getAura().getType() == AuraType.JINX;

        for (int i = 0; i < party.getMembers().size(); i++) {

            PartyMember pm = party.getMember(i);
            if (pm.fled || pm.getPlayer().status == StatusType.DEAD) {
                continue;
            }

            if (!jinx) {
                if (ranged) {
                    d = Utils.distance(MapBorderBehavior.fixed, combatMap.getWidth(), combatMap.getHeight(), fromX, fromY, pm.combatCr.currentX, pm.combatCr.currentY);
                } else {
                    d = Utils.movementDistance(MapBorderBehavior.fixed, combatMap.getWidth(), combatMap.getHeight(), fromX, fromY, pm.combatCr.currentX, pm.combatCr.currentY);
                }

                /* skip target 50% of time if same distance */
                if (d < leastDist || (d == leastDist && rand.nextInt(2) == 0)) {
                    opponent = pm;
                    leastDist = d;
                }
            }
        }

        if (opponent != null) {
            dist.setVal(leastDist);
        }

        return opponent;
    }

    class DistanceWrapper {

        private int val;

        public DistanceWrapper(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

        public void setVal(int val) {
            this.val = val;
        }
    }

    /**
     * Hides or shows a camouflaged creature, depending on its distance from the
     * nearest opponent
     */
    public boolean hideOrShow(Creature cr) {
        /* find the nearest opponent */
        DistanceWrapper dist = new DistanceWrapper(0);

        /* ok, now we've got the nearest party member.  Now, see if they're close enough */
        if (nearestPartyMember(cr.currentX, cr.currentY, dist, false) != null) {
            if ((dist.getVal() < 5) && !cr.getVisible()) {
                cr.setVisible(true);
                /* show yourself */

            } else if (dist.getVal() >= 5) {
                cr.setVisible(false);
                /* hide and take no action! */

            }
        }

        return cr.getVisible();
    }

    /**
     * Moves an object in combat according to its chosen combat action
     */
    public boolean moveCreature(CombatAction action, Creature cr, int targetX, int targetY) {

        int nx = cr.currentX;
        int ny = cr.currentY;

        int mask = combatMap.getValidMovesMask(context, nx, ny, cr, -1, -1);
        Direction dir;

        if (action == CombatAction.FLEE) {
            dir = Utils.getPath(MapBorderBehavior.fixed, combatMap.getWidth(), combatMap.getHeight(), targetX, targetY, mask, false, nx, ny);
            if (dir == null && (nx == 0 || ny == 0)) {
                //force a map exit
                cr.currentX = -1;
                cr.currentY = -1;
                return true;
            }
        } else {
            dir = Utils.getPath(MapBorderBehavior.fixed, combatMap.getWidth(), combatMap.getHeight(), targetX, targetY, mask, true, nx, ny);
        }

        Vector3 pixelPos = null;

        if (dir == null) {
            return false;
        } else {
            if (dir == Direction.NORTH) {
                ny--;
            }
            if (dir == Direction.SOUTH) {
                ny++;
            }
            if (dir == Direction.EAST) {
                nx++;
            }
            if (dir == Direction.WEST) {
                nx--;
            }
        }

        boolean slowed = false;
        SlowedType slowedType = SlowedType.SLOWED_BY_TILE;
        if (cr.getSwims() || cr.getSails()) {
            slowedType = SlowedType.SLOWED_BY_WIND;
        } else if (cr.getFlies()) {
            slowedType = SlowedType.SLOWED_BY_NOTHING;
        }

        switch (slowedType) {
            case SLOWED_BY_TILE:
                Tile t = combatMap.getTile(nx, ny);
                if (t != null) {
                    slowed = context.slowedByTile(t);
                }
                break;
            case SLOWED_BY_WIND:
                slowed = context.slowedByWind(dir);
                break;
            case SLOWED_BY_NOTHING:
            default:
                break;
        }

        if (!slowed) {
            pixelPos = getMapPixelCoords(nx, ny);
            cr.currentPos = pixelPos;
            cr.currentX = nx;
            cr.currentY = ny;
            return true;
        }

        return false;
    }

    private Texture getCursorTexture() {
        Pixmap pixmap = new Pixmap(tilePixelHeight, tilePixelHeight, Format.RGBA8888);
        pixmap.setColor(Color.YELLOW);
        int w = 4;
        pixmap.fillRectangle(0, 0, w, tilePixelHeight);
        pixmap.fillRectangle(tilePixelHeight - w, 0, w, tilePixelHeight);
        pixmap.fillRectangle(w, 0, tilePixelHeight - 2 * w, w);
        pixmap.fillRectangle(w, tilePixelHeight - w, tilePixelHeight - 2 * w, w);
        return new Texture(pixmap);
    }

    class CursorActor extends Actor {

        Texture texture;
        boolean visible = true;

        CursorActor() {
            texture = getCursorTexture();
        }

        void setPos(Vector3 v) {
            setX(v.x);
            setY(v.y);
        }

        @Override
        public void setVisible(boolean v) {
            this.visible = v;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {

            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

            if (visible) {
                batch.draw(texture, getX(), getY());
            }
        }

    }

    public void useStones(Stone c1, Stone c2, Stone c3, Stone c4) {

        DungeonRoom room = (DungeonRoom) tmap.getProperties().get("dungeonRoom");
        if (room != null) {

            int mask = c1.getLoc() | c2.getLoc() | c3.getLoc() | c4.getLoc();

            int TRUTH = Stone.WHITE.getLoc() | Stone.PURPLE.getLoc() | Stone.GREEN.getLoc() | Stone.BLUE.getLoc();
            int LOVE = Stone.WHITE.getLoc() | Stone.YELLOW.getLoc() | Stone.GREEN.getLoc() | Stone.ORANGE.getLoc();
            int COURAGE = Stone.WHITE.getLoc() | Stone.RED.getLoc() | Stone.PURPLE.getLoc() | Stone.ORANGE.getLoc();

            int attrib = 0;
            Item key = null;

            switch (room.altarRoomVirtue) {
                case COURAGE:
                    attrib = COURAGE;
                    key = Item.KEY_C;
                    break;
                case LOVE:
                    attrib = LOVE;
                    key = Item.KEY_L;
                    break;
                case TRUTH:
                    attrib = TRUTH;
                    key = Item.KEY_T;
                    break;
            }

            if (mask == attrib && (party.getSaveGame().items & key.getLoc()) > 0) {
                log("Thou doth find the " + key.getDesc());
                log("one third of the Three Part Key!");
                party.getSaveGame().items |= key.getLoc();
            } else {
                log("Hmm...No effect!");
            }

        }

        finishPlayerTurn();
    }

    //for dungeon room chests only
    public void getChest(int index, int x, int y) {
        try {
            Tile tile = combatMap.getTile(x, y);
            if (tile != null && tile.getIndex() == 60) {
                PartyMember pm = context.getParty().getMember(index);
                context.getChestTrapHandler(pm);
                log(String.format("The Chest Holds: %d Gold", context.getParty().getChestGold()));
                replaceTile("dungeon_floor", x, y);
            } else {
                log("Not Here!");
            }
        } catch (PartyDeathException e) {
            this.returnScreen.partyDeath();
        }
    }

    public class ReadyWearInputAdapter extends InputAdapter {

        boolean ready;
        PartyMember pm;

        public ReadyWearInputAdapter(PartyMember pm, boolean ready) {
            this.ready = ready;
            this.pm = pm;

            StringBuilder sb = new StringBuilder();
            if (ready) {
                for (char ch = 'a'; ch <= 'p'; ch++) {
                    if (pm.getParty().getSaveGame().weapons[ch - 'a'] > 0) {
                        sb.append(Character.toUpperCase(ch)).append(" - ").append(WeaponType.get(ch - 'a'));
                    }
                }
            } else {
                for (char ch = 'a'; ch <= 'h'; ch++) {
                    if (pm.getParty().getSaveGame().armor[ch - 'a'] > 0) {
                        sb.append(Character.toUpperCase(ch)).append(" - ").append(ArmorType.get(ch - 'a'));
                    }
                }
            }
            log(sb.length() > 0 ? sb.toString() : "Nothing owned.");
        }

        @Override
        public boolean keyUp(int keycode) {
            if (keycode >= Keys.A && keycode <= Keys.P) {
                boolean ret = false;
                if (ready) {
                    ret = pm.readyWeapon(keycode - 29);
                } else {
                    ret = pm.wearArmor(keycode - 29);
                }
                if (!ret) {
                    log("Failed!");
                } else {
                    log("Success!");
                }
            }
            Gdx.input.setInputProcessor(new InputMultiplexer(CombatScreen.this, stage));
            finishPlayerTurn();
            return false;
        }
    }

    public static void holeUp(Maps contextMap, final int x, final int y, final BaseScreen rs,
            final Context context, CreatureSet cs, final TextureAtlas sa, boolean inn) {

        Ultima4.hud.add("Hole up & Camp!");

        if (!inn && context.getCurrentMap().getCity() != null) {
            Ultima4.hud.add("Only outside or in the dungeon!");
            return;
        }

        if (context.getTransportContext() != TransportContext.FOOT) {
            Ultima4.hud.add("Only on foot!");
            return;
        }

        // make sure everyone's asleep
        for (int i = 0; i < context.getParty().getMembers().size(); i++) {
            context.getParty().getMember(i).putToSleep();
        }

        Ultima4.hud.add("Resting");

        final Maps campMap;
        TiledMap tmap;
        if (contextMap == Maps.WORLD) {
            campMap = Maps.CAMP_CON;
        } else if (inn) {
            campMap = Maps.INN_CON;
        } else {
            campMap = Maps.CAMP_DNG;
        }

        tmap = new UltimaTiledMapLoader(campMap, sa, campMap.getMap().getWidth(), campMap.getMap().getHeight(), tilePixelWidth, tilePixelHeight).load();

        context.setCurrentTiledMap(tmap);

        final CombatScreen sc = new CombatScreen(rs, context, contextMap, campMap.getMap(), tmap, null, cs, sa);

        mainGame.setScreen(sc);

        final int CAMP_HEAL_INTERVAL = 5;
        Random rand = new XORShiftRandom();

        SequenceAction seq = Actions.action(SequenceAction.class);
        for (int i = 0; i < CAMP_HEAL_INTERVAL; i++) {
            seq.addAction(Actions.delay(1f));
            seq.addAction(Actions.run(new Runnable() {
                public void run() {
                    Ultima4.hud.append(" .");
                }
            }));
        }

        if (!inn && rand.nextInt(8) == 0) {

            seq.addAction(Actions.run(new Runnable() {
                public void run() {
                    Ultima4.hud.add("Ambushed!");
                    sc.setAmbushingMonsters(rs, x, y, sa);
                }
            }));

        } else {

            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < context.getParty().getMembers().size(); i++) {
                        context.getParty().getMember(i).wakeUp();
                    }

                    /* Make sure we've waited long enough for camping to be effective */
                    boolean healed = false;
                    if (((context.getParty().getSaveGame().moves / CAMP_HEAL_INTERVAL) >= 0x10000)
                            || (((context.getParty().getSaveGame().moves / CAMP_HEAL_INTERVAL) & 0xffff) != context.getParty().getSaveGame().lastcamp)) {
                        for (int i = 0; i < context.getParty().getMembers().size(); i++) {
                            PartyMember m = context.getParty().getMember(i);
                            m.getPlayer().mp = m.getPlayer().getMaxMp();
                            if ((m.getPlayer().hp < m.getPlayer().hpMax) && m.heal(HealType.CAMPHEAL)) {
                                healed = true;
                            }
                        }
                    }

                    Ultima4.hud.add(healed ? "Party Healed!" : "No effect.");

                    context.getParty().getSaveGame().lastcamp = (context.getParty().getSaveGame().moves / 5) & 0xffff;

                    sc.end();

                }
            }));

            if (inn && contextMap == Maps.SKARABRAE && rand.nextInt(3) == 0) {
                //show isaac
                for (Person p : context.getCurrentMap().getCity().getPeople()) {
                    if (p.getId() == 10) {
                        p.setRemovedFromMap(false);
                        p.setStart_x(27);
                        p.setX(27);
                        p.setStart_y(12);
                        p.setY(12);
                        p.setMovement(ObjectMovementBehavior.WANDER);
                    }
                }
            }

        }

        sc.getStage().addAction(seq);

    }

}
