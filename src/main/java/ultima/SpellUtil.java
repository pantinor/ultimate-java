package ultima;

import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Moongate;
import objects.Party;
import objects.Party.PartyMember;
import objects.Tile;
import util.DungeonTileModelInstance;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class SpellUtil implements Constants {

    public static boolean spellCast(final BaseScreen screen, final Context context, final Spell spell,
            final PartyMember caster, final PartyMember subject, final Direction dir, final int phase) {

        if (caster == null || spell == null || screen == null) {
            return false;
        }

        switch (spell) {
            case AWAKEN:
            case CURE:
            case HEAL:
            case RESURRECT:
                if (subject == null) {
                    Ultima4.hud.add("Thou must indicate a target to cast the spell!");
                    return false;
                }
                break;

            case DISPEL:
            case ICEBALL:
            case KILL:
            case SLEEP:
            case MAGICMISSILE:
            case FIREBALL:
            case ENERGY:
            case WINDS:
            case BLINK:
                if (dir == null) {
                    Ultima4.hud.add("Thou must indicate a direction to cast the spell!");
                    return false;
                }
                break;

            default:
                break;

        }

        if (caster.getPlayer().mp < spell.getMp()) {
            Ultima4.hud.add("Thou dost not have enough magic points!");
            return false;
        }

        Party party = context.getParty();

        if (party.getSaveGame().mixtures[spell.ordinal()] < 1) {
            Ultima4.hud.add("Thou dost not have the mixture prepared!");
            return false;
        }

        // subtract the mixture for even trying to cast the spell
        party.getSaveGame().mixtures[spell.ordinal()]--;

        if (context.getAura().getType() == AuraType.NEGATE) {
            Ultima4.hud.add("Spell is negated!");
            return false;
        }

        caster.adjustMagic(spell.getMp());

        SequenceAction seq = Actions.action(SequenceAction.class);
        seq.addAction(Actions.run(new PlaySoundAction(spell.getSound())));
        seq.addAction(Actions.delay(0.5f));
        seq.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                switch (spell) {

                    case AWAKEN:
                        spellAwaken(subject);
                        break;
                    case CURE:
                        spellCure(subject);
                        break;
                    case HEAL:
                        spellHeal(subject);
                        break;
                    case RESURRECT:
                        spellRez(subject);
                        break;
                    case DISPEL:
                        spellDispel(screen, context, caster, dir);
                        break;
                    case ICEBALL:
                        spellIceball(screen, caster, dir);
                        break;
                    case KILL:
                        spellKill(screen, caster, dir);
                        break;
                    case MAGICMISSILE:
                        spellMMissle(screen, caster, dir);
                        break;
                    case FIREBALL:
                        spellFireball(screen, caster, dir);
                        break;
                    case ENERGY:
                        spellEnergyField(dir);
                        break;
                    case WINDS:
                        spellWinds(screen, dir);
                        break;
                    case BLINK:
                        spellBlink(screen, dir);
                        break;
                    case GATE:
                        spellGate(screen, phase);
                        break;
                    case JINX:
                        spellJinx(context);
                        break;
                    case LIGHT:
                        spellLight();
                        break;
                    case NEGATE:
                        spellNegate(context);
                        break;
                    case OPEN:
                        spellOpen();
                        break;
                    case PROTECTION:
                        spellProtect(context);
                        break;
                    case QUICKNESS:
                        spellQuick(context);
                        break;
                    case SLEEP:
                        spellSleep(screen, caster, dir);
                        break;
                    case TREMOR:
                        spellTremor(screen, caster);
                        break;
                    case UNDEAD:
                        spellUndead(screen, caster);
                        break;
                    case VIEW:
                        spellView(screen, caster);
                        break;
                    case XIT:
                        spellXit(screen, caster);
                        break;
                    case YUP:
                        spellYup(screen, caster);
                        break;
                    case ZDOWN:
                        spellZdown(screen, caster);
                        break;
                    default:
                        break;
                }
            }
        }));
        
        screen.getStage().addAction(seq);

        return true;
    }

    private static void spellMagicAttack(CombatScreen screen, PartyMember caster, Spell spell, Direction dir, int minDamage, int maxDamage) {

        int x = caster.combatCr.currentX;
        int y = caster.combatCr.currentY;
        if (dir == Direction.NORTH) {
            y--;
        }
        if (dir == Direction.SOUTH) {
            y++;
        }
        if (dir == Direction.EAST) {
            x++;
        }
        if (dir == Direction.WEST) {
            x--;
        }

        Utils.animateMagicAttack(screen.getStage(), screen, caster, dir, x, y, spell, minDamage, maxDamage);

    }

    public static void spellAwaken(PartyMember subject) {
        subject.wakeUp();
    }

    public static void spellBlink(BaseScreen screen, Direction dir) {

        if (screen.scType == ScreenType.MAIN) {

            GameScreen gameScreen = (GameScreen) screen;
            BaseMap bm = screen.context.getCurrentMap();

            Vector3 v = gameScreen.getCurrentMapCoords();
            int x = (int) v.x;
            int y = (int) v.y;

            /*
             * Blink doesn't work near the mouth of the abyss
             * Note: This means you can teleport to Hythloth from the top of the
             * map, and that you can teleport to the abyss from the left edge of
             * the map, Unfortunately, this matches the bugs in the game.
             */
            if (bm.getId() != Maps.WORLD.getId() || (x >= 192 && y >= 192)) {
                return;
            }

            int distance = 0;
            int diff = 0;
            Direction reverseDir = Direction.reverse(dir);

            /* figure out what numbers we're working with */
            int var = (dir.getMask() & (Direction.WEST.getMask() | Direction.EAST.getMask())) > 0 ? x : y;

            /* find the distance we are going to move */
            distance = (var) % 0x10;
            if (dir == Direction.EAST || dir == Direction.SOUTH) {
                distance = 0x10 - distance;
            }

            /* see if we move another 16 spaces over */
            diff = 0x10 - distance;
            if ((diff > 0) && (Utils.rand.nextInt(diff * diff) > distance)) {
                distance += 0x10;
            }

            /* test our distance, and see if it works */
            for (int i = 0; i < distance; i++) {
                if (dir == Direction.NORTH) {
                    y--;
                }
                if (dir == Direction.SOUTH) {
                    y++;
                }
                if (dir == Direction.WEST) {
                    x--;
                }
                if (dir == Direction.EAST) {
                    x++;
                }
            }

            int i = distance;
            /* begin walking backward until you find a valid spot */
            while ((i-- > 0) && bm.getTile(x, y) != null && bm.getTile(x, y).getRule().has(TileAttrib.unwalkable)) {
                if (reverseDir == Direction.NORTH) {
                    y--;
                }
                if (reverseDir == Direction.SOUTH) {
                    y++;
                }
                if (reverseDir == Direction.WEST) {
                    x--;
                }
                if (reverseDir == Direction.EAST) {
                    x++;
                }
            }

            if (bm.getTile(x, y) != null && !bm.getTile(x, y).getRule().has(TileAttrib.unwalkable)) {

                /* we didn't move! */
                if (x == (int) v.x && y == (int) v.y) {
                    screen.log("Failed to blink!");
                }

                gameScreen.newMapPixelCoords = gameScreen.getMapPixelCoords(x, y);
                gameScreen.recalcFOV(bm, x, y);

            } else {
                screen.log("Failed to blink!");
            }
        } else {
            screen.log("Outdoors only!");
        }

    }

    public static void spellCure(PartyMember subject) {
        subject.heal(HealType.CURE);
    }

    public static void spellDispel(BaseScreen screen, Context context, PartyMember caster, Direction dir) {

        if (screen.scType == ScreenType.MAIN) {

            GameScreen gameScreen = (GameScreen) screen;
            Vector3 v = gameScreen.getCurrentMapCoords();
            int x = (int) v.x;
            int y = (int) v.y;
            if (dir == Direction.NORTH) {
                y--;
            }
            if (dir == Direction.SOUTH) {
                y++;
            }
            if (dir == Direction.EAST) {
                x++;
            }
            if (dir == Direction.WEST) {
                x--;
            }
            Tile dispellable = context.getCurrentMap().getTile(x, y);
            if (dispellable.getRule() == null || !dispellable.getRule().has(TileAttrib.dispelable)) {
                return;
            }
            gameScreen.replaceTile("grass", x, y);

        } else if (screen.scType == ScreenType.COMBAT) {

            CombatScreen combatScreen = (CombatScreen) screen;
            int x = caster.combatCr.currentX;
            int y = caster.combatCr.currentY;
            if (dir == Direction.NORTH) {
                y--;
            }
            if (dir == Direction.SOUTH) {
                y++;
            }
            if (dir == Direction.EAST) {
                x++;
            }
            if (dir == Direction.WEST) {
                x--;
            }
            Tile dispellable = combatScreen.combatMap.getTile(x, y);
            if (dispellable.getRule() == null || !dispellable.getRule().has(TileAttrib.dispelable)) {
                return;
            }
            combatScreen.replaceTile("dungeon_floor", x, y);

        } else if (screen.scType == ScreenType.DUNGEON) {

            DungeonScreen dngScreen = (DungeonScreen) screen;
            int x = (Math.round(dngScreen.currentPos.x) - 1);
            int y = (Math.round(dngScreen.currentPos.z) - 1);

            if (dngScreen.currentDir == Direction.NORTH) {
                y = y - 1 < 0 ? DungeonScreen.DUNGEON_MAP - 1 : y - 1;
            }
            if (dngScreen.currentDir == Direction.SOUTH) {
                y = y + 1 >= DungeonScreen.DUNGEON_MAP ? 0 : y + 1;
            }
            if (dngScreen.currentDir == Direction.EAST) {
                x = x + 1 >= DungeonScreen.DUNGEON_MAP ? 0 : x + 1;
            }
            if (dngScreen.currentDir == Direction.WEST) {
                x = x - 1 < 0 ? DungeonScreen.DUNGEON_MAP - 1 : x - 1;
            }

            DungeonTileModelInstance dispellable = null;
            for (DungeonTileModelInstance dmi : dngScreen.modelInstances) {
                if (dmi.getTile().getValue() >= DungeonTile.FIELD_POISON.getValue()
                        && dmi.getTile().getValue() <= DungeonTile.FIELD_SLEEP.getValue()) {
                    if (dmi.x == x && dmi.y == y && dmi.getLevel() == dngScreen.currentLevel) {
                        dispellable = dmi;
                        break;
                    }
                }
            }

            if (dispellable != null) {
                dngScreen.modelInstances.remove(dispellable);
                dngScreen.dungeonTiles[dngScreen.currentLevel][x][y] = DungeonTile.NOTHING;
                dngScreen.createMiniMap();
            }
        }

    }

    public static void spellEnergyField(Direction dir) {
        //TODO
    }

    public static void spellFireball(BaseScreen screen, PartyMember caster, Direction dir) {
        if (screen.scType == ScreenType.COMBAT) {
            spellMagicAttack((CombatScreen) screen, caster, Spell.FIREBALL, dir, 24, 128);
        }
    }

    public static void spellGate(BaseScreen screen, int phase) {
        if (screen.scType == ScreenType.MAIN) {
            GameScreen gsc = (GameScreen) screen;
            for (Moongate g : gsc.context.getCurrentMap().getMoongates()) {
                if (g.getPhase() == phase) {
                    Vector3 dest = new Vector3(g.getX(), g.getY(), 0);
                    gsc.newMapPixelCoords = gsc.getMapPixelCoords((int) dest.x, (int) dest.y);
                    BaseMap bm = gsc.context.getCurrentMap();
                    gsc.recalcFOV(bm, (int) dest.x, (int) dest.y);
                    break;
                }
            }
        }
    }

    public static void spellHeal(PartyMember subject) {
        subject.heal(HealType.HEAL);
    }

    public static void spellIceball(BaseScreen screen, PartyMember caster, Direction dir) {
        if (screen.scType == ScreenType.COMBAT) {
            spellMagicAttack((CombatScreen) screen, caster, Spell.ICEBALL, dir, 32, 224);
        }
    }

    public static void spellJinx(Context context) {
        context.getAura().set(AuraType.JINX, 10);
    }

    public static void spellKill(BaseScreen screen, PartyMember caster, Direction dir) {
        if (screen.scType == ScreenType.COMBAT) {
            spellMagicAttack((CombatScreen) screen, caster, Spell.KILL, dir, -1, 232);
        }
    }

    public static void spellLight() {
        //TODO
    }

    public static void spellMMissle(BaseScreen screen, PartyMember caster, Direction dir) {
        if (screen.scType == ScreenType.COMBAT) {
            spellMagicAttack((CombatScreen) screen, caster, Spell.MAGICMISSILE, dir, 64, 16);
        }
    }

    public static void spellNegate(Context context) {
        context.getAura().set(AuraType.NEGATE, 10);
    }

    public static void spellOpen() {
        //TODO
    }

    public static void spellProtect(Context context) {
        //TODO
        context.getAura().set(AuraType.PROTECTION, 10);
    }

    public static void spellRez(PartyMember subject) {
        subject.heal(HealType.RESURRECT);
    }

    public static void spellQuick(Context context) {
        context.getAura().set(AuraType.QUICKNESS, 10);
    }

    public static void spellSleep(BaseScreen screen, PartyMember caster, Direction dir) {
        if (screen.scType == ScreenType.COMBAT) {
            spellMagicAttack((CombatScreen) screen, caster, Spell.SLEEP, dir, 0, 0);
        }
    }

    public static void spellTremor(BaseScreen screen, PartyMember caster) {

        if (screen.scType == ScreenType.COMBAT) {

            final CombatScreen combatScreen = (CombatScreen) screen;

            SequenceAction seq = Actions.action(SequenceAction.class);

            for (Creature cr : combatScreen.combatMap.getCreatures()) {

                /* creatures with over 192 hp are unaffected */
                if (cr.getHP() > 192) {
                    seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                    continue;
                } else {

                    if (Utils.rand.nextInt(2) == 0) {
                        /* Deal maximum damage to creature */
                        Utils.dealDamage(caster, cr, 0xFF);

                        Tile tile = Ultima4.baseTileSet.getTileByName("hit_flash");
                        Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Ultima4.standardAtlas);
                        d.setX(cr.currentPos.x);
                        d.setY(cr.currentPos.y);
                        d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                        seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                        seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                    } else if (Utils.rand.nextInt(2) == 0) {
                        /* Deal enough damage to creature to make it flee */
                        if (cr.getHP() > 23) {
                            Utils.dealDamage(caster, cr, cr.getHP() - 23);

                            Tile tile = Ultima4.baseTileSet.getTileByName("hit_flash");
                            Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Ultima4.standardAtlas);
                            d.setX(cr.currentPos.x);
                            d.setY(cr.currentPos.y);
                            d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                            seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                            seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                        }
                    } else {
                        seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                    }
                }

                if (cr.getDamageStatus() == CreatureStatus.DEAD) {
                    seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
                }

            }

            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    combatScreen.finishPlayerTurn();
                }
            }));

            combatScreen.getStage().addAction(seq);

        }

    }

    public static void spellUndead(BaseScreen screen, PartyMember caster) {

        if (screen.scType == ScreenType.COMBAT) {

            SequenceAction seq = Actions.action(SequenceAction.class);

            final CombatScreen combatScreen = (CombatScreen) screen;
            
            
            int level = caster.getPlayer().getLevel();
            //from the AD&D dungeon masters guide page 75 :)
            boolean turn = Utils.rand.nextInt(100) >= 50;
            if (level > 1) {
                turn = Utils.rand.nextInt(100) >= 35;
            }
            if (level > 2) {
                turn = Utils.rand.nextInt(100) >= 20;
            }
            if (level > 3) {
                turn = true;
            }
            
            for (Creature cr : combatScreen.combatMap.getCreatures()) {
                if (cr.getUndead() && turn) {

                    Tile tile = Ultima4.baseTileSet.getTileByName("hit_flash");
                    Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Ultima4.standardAtlas);
                    d.setX(cr.currentPos.x);
                    d.setY(cr.currentPos.y);

                    d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                    seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                    seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                    Utils.dealDamage(caster, cr, 23);
                } else {
                    seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                }

                if (cr.getDamageStatus() == CreatureStatus.DEAD) {
                    seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
                }
            }

            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    combatScreen.finishPlayerTurn();
                }
            }));

            combatScreen.getStage().addAction(seq);

        }
    }

    public static void spellView(BaseScreen screen, PartyMember caster) {
        InputProcessor ip = screen.getPeerGemInputProcessor();
        if (ip != null) {
            Gdx.input.setInputProcessor(ip);
        }
    }

    public static void spellWinds(BaseScreen screen, Direction fromdir) {
        screen.context.setWindDirection(fromdir);
    }

    public static void spellXit(BaseScreen screen, PartyMember caster) {
        if (screen.scType == ScreenType.DUNGEON) {
            DungeonScreen dngScreen = (DungeonScreen) screen;
            screen.log("Leaving " + dngScreen.dngMap.getLabel());
            if (dngScreen.dngMap == Maps.HYTHLOTH) {
                dngScreen.gameScreen.addBalloonActor(233, 242);
                screen.context.getParty().getSaveGame().balloonx = 233;
                screen.context.getParty().getSaveGame().balloony = 242;
                screen.context.getParty().getSaveGame().balloonfound = 1;
                dngScreen.gameScreen.newMapPixelCoords = dngScreen.gameScreen.getMapPixelCoords(239, 240);
            }
            if (DungeonScreen.mainGame != null) {
                DungeonScreen.mainGame.setScreen(dngScreen.gameScreen);
            }
        }
    }

    public static void spellYup(BaseScreen screen, PartyMember caster) {
        if (screen.scType == ScreenType.DUNGEON) {
            DungeonScreen dngScreen = (DungeonScreen) screen;

            if (dngScreen.dngMap == Maps.ABYSS) {
                return;
            }

            dngScreen.currentLevel--;

            if (dngScreen.currentLevel < 0) {
                dngScreen.currentLevel = 0;
                if (dngScreen.mainGame != null) {
                    dngScreen.mainGame.setScreen(dngScreen.gameScreen);
                }
            } else {

                for (int i = 0; i < 32; i++) {
                    int x = Utils.rand.nextInt(8);
                    int y = Utils.rand.nextInt(8);
                    if (dngScreen.validTeleportLocation(x, y, dngScreen.currentLevel)) {
                        dngScreen.currentPos = new Vector3(x + .5f, .5f, y + .5f);
                        dngScreen.camera.position.set(dngScreen.currentPos);
                        if (dngScreen.currentDir == Direction.EAST) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x + 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
                        } else if (dngScreen.currentDir == Direction.WEST) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x - 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
                        } else if (dngScreen.currentDir == Direction.NORTH) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z - 1);
                        } else if (dngScreen.currentDir == Direction.SOUTH) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z + 1);
                        }
                        dngScreen.moveMiniMapIcon();
                        break;
                    }
                }

                dngScreen.createMiniMap();
            }

        }
    }

    public static void spellZdown(BaseScreen screen, PartyMember caster) {
        if (screen.scType == ScreenType.DUNGEON) {
            DungeonScreen dngScreen = (DungeonScreen) screen;

            if (dngScreen.dngMap == Maps.ABYSS) {
                return;
            }

            dngScreen.currentLevel++;

            if (dngScreen.currentLevel > DungeonScreen.DUNGEON_MAP) {

                dngScreen.currentLevel = DungeonScreen.DUNGEON_MAP;

            } else {

                for (int i = 0; i < 32; i++) {
                    int x = Utils.rand.nextInt(8);
                    int y = Utils.rand.nextInt(8);
                    if (dngScreen.validTeleportLocation(x, y, dngScreen.currentLevel)) {
                        dngScreen.currentPos = new Vector3(x + .5f, .5f, y + .5f);
                        dngScreen.camera.position.set(dngScreen.currentPos);
                        if (dngScreen.currentDir == Direction.EAST) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x + 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
                        } else if (dngScreen.currentDir == Direction.WEST) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x - 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
                        } else if (dngScreen.currentDir == Direction.NORTH) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z - 1);
                        } else if (dngScreen.currentDir == Direction.SOUTH) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z + 1);
                        }
                        dngScreen.moveMiniMapIcon();
                        break;
                    }
                }

                dngScreen.createMiniMap();
            }

        }
    }

    /**
     * Used with skull of mondain on world map
     */
    public static void destoryAllCreatures(BaseScreen screen, PartyMember caster) {

        if (screen.scType == ScreenType.MAIN) {

            final GameScreen gameScreen = (GameScreen) screen;

            SequenceAction seq = Actions.action(SequenceAction.class);

            for (final Creature cr : screen.context.getCurrentMap().getCreatures()) {

                /* Deal maximum damage to creature */
                Utils.dealDamage(caster, cr, 0xFF);

                Tile tile = Ultima4.baseTileSet.getTileByName("hit_flash");
                Drawable d = new Drawable(screen.context.getCurrentMap(), cr.currentX, cr.currentY, tile, Ultima4.standardAtlas);
                d.setX(cr.currentPos.x);
                d.setY(cr.currentPos.y);
                d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                seq.addAction(Actions.run(new AddActorAction(gameScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                seq.addAction(Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        screen.context.getCurrentMap().getCreatures().remove(cr);
                    }
                }));

            }

            gameScreen.getStage().addAction(seq);

        }

    }
    
    public static void useMaskOfMinax(BaseScreen screen, PartyMember caster) {

        if (screen.scType == ScreenType.COMBAT) {

            final CombatScreen combatScreen = (CombatScreen) screen;

            final SequenceAction seq = Actions.action(SequenceAction.class);

            for (Creature cr : combatScreen.combatMap.getCreatures()) {

                if (Utils.rand.nextInt(3) == 0) {
                    /* Deal maximum damage to creature */
                    Utils.dealDamage(caster, cr, 0xFF);
                } else {
                    if (cr.getHP() > 23) {
                        Utils.dealDamage(caster, cr, cr.getHP() * (3/4));
                    } else {
                        Utils.dealDamage(caster, cr, 15);
                    }
                }

                Actor d = new CloudDrawable();
                d.setX(cr.currentPos.x - 16);
                d.setY(cr.currentPos.y - 16);
                d.addAction(Actions.sequence(Actions.delay(2f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.SPIRITS)));
                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                if (cr.getDamageStatus() == CreatureStatus.DEAD) {
                    seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
                }

            }

            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    combatScreen.finishPlayerTurn();
                }
            }));

            combatScreen.getStage().addAction(seq);

        } else {
            Sounds.play(Sound.ERROR);
        }

    }

    public static void useRageOfGod(BaseScreen screen, PartyMember caster) {

        if (screen.scType == ScreenType.COMBAT) {

            final CombatScreen combatScreen = (CombatScreen) screen;

            final SequenceAction seq = Actions.action(SequenceAction.class);

            for (Creature cr : combatScreen.combatMap.getCreatures()) {

                if (Utils.rand.nextInt(2) == 0) {
                    /* Deal maximum damage to creature */
                    Utils.dealDamage(caster, cr, 0xFF);
                } else if (Utils.rand.nextInt(2) == 0) {
                    /* Deal enough damage to creature to make it flee */
                    if (cr.getHP() > 23) {
                        Utils.dealDamage(caster, cr, cr.getHP() - 23);
                    }
                } else {
                    //deal damage of half its hit points
                    Utils.dealDamage(caster, cr, cr.getHP() / 2);
                }

                Actor d = new ExplosionLargeDrawable();
                d.setX(cr.currentPos.x - 32*3 + 16);
                d.setY(cr.currentPos.y - 32*3 + 16);
                d.addAction(Actions.sequence(Actions.delay(2f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.RAGE)));
                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                if (cr.getDamageStatus() == CreatureStatus.DEAD) {
                    seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
                }

            }

            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    combatScreen.finishPlayerTurn();
                }
            }));

            combatScreen.getStage().addAction(seq);

        } else {
            Sounds.play(Sound.ERROR);
        }

    }
    
    private static class CloudDrawable extends Actor {
        float stateTime;
        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Ultima4.cloud.getKeyFrame(stateTime, false), getX(), getY(), 64, 64);
        }
    }
    
    private static class ExplosionDrawable extends Actor {
        float stateTime;
        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Ultima4.explosion.getKeyFrame(stateTime, false), getX(), getY(), 64, 64);
        }
    }
    
    private static class ExplosionLargeDrawable extends Actor {
        float stateTime;
        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Ultima4.explosionLarge.getKeyFrame(stateTime, false), getX(), getY(), 192, 192);
        }
    }

}
