package ultima;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import java.io.IOException;
import java.util.ArrayList;
import objects.Party;
import org.apache.commons.io.IOUtils;

public class MixtureScreen implements Screen, Constants {

    private final Stage stage;
    private final Ultima4 mainGame;
    private final BaseScreen returnScreen;
    private final Party party;
    private final Skin skin;
    
    ReagentCount[] owned;
    ReagentCount[] mixing;
    MixtureCount[] mixtures;

    List<ReagentCount> ownedList;
    List<ReagentCount> mixedList;
    List<MixtureCount> mixtureList;

    TextButton add;
    TextButton clear;
    TextButton mix;
    TextButton exit;

    SelectBox<Spell> spellSelect;

    private final Table internalTable;
    private final Table buttonTable;
    
    private final java.util.List<String> spellDescs = new ArrayList<>();
    private final java.util.List<String> reagDescs = new ArrayList<>();
    
    private final Label reagLabel;
    private final Label spellLabel;

    public MixtureScreen(Ultima4 mainGame, BaseScreen returnScreen, Skin skin, Party party) {
        this.returnScreen = returnScreen;
        this.mainGame = mainGame;
        this.stage = new Stage();
        this.party = party;
        this.skin = skin;
        
        spellDescs.addAll(IOUtils.readLines(Gdx.files.classpath("assets/data/spells.txt").read()));
        reagDescs.addAll(IOUtils.readLines(Gdx.files.classpath("assets/data/reagents.txt").read()));

        this.internalTable = new Table(skin);
        this.internalTable.defaults().pad(5);

        spellSelect = new SelectBox<>(skin);
        spellSelect.setItems(Spell.values());
        spellSelect.setSelected(Spell.AWAKEN);
        spellSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Spell s = spellSelect.getSelected();
                spellLabel.setText(spellDescs.get(s.ordinal()));
            }
        });

        internalTable.add();
        internalTable.add(new Label("Spell:", skin)).align(Align.right);
        internalTable.add(spellSelect).align(Align.top).minWidth(200);

        internalTable.add();
        internalTable.row();

        internalTable.add(new Label("Reagents", skin)).align(Align.left);
        internalTable.add();
        internalTable.add(new Label("Current Batch", skin)).align(Align.left);
        internalTable.add(new Label("Mixtures", skin)).align(Align.left);
        internalTable.row();

        ArrayList<ReagentCount> tmp = new ArrayList<>();
        for (Reagent r : Reagent.values()) {
            if (party.getSaveGame().reagents[r.ordinal()] > 0) {
                tmp.add(new ReagentCount(r, party.getSaveGame().reagents[r.ordinal()]));
            }
        }
        owned = (ReagentCount[]) tmp.toArray(new ReagentCount[tmp.size()]);

        ArrayList<MixtureCount> tmp2 = new ArrayList<>();
        for (int i = 0; i < Spell.values().length; i++) {
            int count = party.getSaveGame().mixtures[i];
            if (count > 0) {
                tmp2.add(new MixtureCount(Spell.get(i), count));
            }
        }
        mixtures = (MixtureCount[]) tmp2.toArray(new MixtureCount[tmp2.size()]);

        ownedList = new List<>(skin);
        ownedList.setItems(owned);
        ownedList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ReagentCount rc = ownedList.getSelected();
                reagLabel.setText(reagDescs.get(rc.rgnt.ordinal()));
            }
        });
                
        mixedList = new List<>(skin);

        mixtureList = new List<>(skin);
        mixtureList.setItems(mixtures);

        buttonTable = new Table(skin);
        buttonTable.defaults().padLeft(20).padRight(20).padTop(5);

        add = new TextButton("Add", skin, "wood");
        add.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                ReagentCount sel = ownedList.getSelected();
                if (sel == null || sel.count == 0) {
                    return;
                }
                ReagentCount rc = new ReagentCount(sel.rgnt, 1);
                if (mixing != null) {
                    for (int i = 0; i < mixing.length; i++) {
                        if (mixing[i] != null && mixing[i].rgnt == sel.rgnt) {
                            mixing[i].count++;
                            rc = mixing[i];
                            break;
                        }
                    }
                }

                sel.count--;

                ArrayList<ReagentCount> tmp = new ArrayList<>();
                tmp.add(rc);
                if (mixing != null) {
                    for (int i = 0; i < mixing.length; i++) {
                        if (mixing[i] != null && mixing[i].rgnt != rc.rgnt) {
                            tmp.add(mixing[i]);
                        }
                    }
                }

                mixing = (ReagentCount[]) tmp.toArray(new ReagentCount[tmp.size()]);
                mixedList.setItems(mixing);

            }
        });
        buttonTable.add(add).expandX().left().width(100);

        clear = new TextButton("Clear", skin, "wood");
        clear.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                mixedList.clearItems();
                mixing = null;

                ArrayList<ReagentCount> tmp = new ArrayList<>();
                for (Reagent r : Reagent.values()) {
                    if (party.getSaveGame().reagents[r.ordinal()] > 0) {
                        tmp.add(new ReagentCount(r, party.getSaveGame().reagents[r.ordinal()]));
                    }
                }
                owned = (ReagentCount[]) tmp.toArray(new ReagentCount[tmp.size()]);
                ownedList.setItems(owned);
            }
        });
        buttonTable.row();
        buttonTable.add(clear).expandX().left().width(100).padBottom(30);

        mix = new TextButton("Mix", skin, "wood");
        mix.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                if (mixing == null) {
                    animateText("Nothing is mixed!", Color.WHITE, 300, 180, 500, 180);
                    return;
                }

                int mask = 0;
                for (ReagentCount rc : mixing) {
                    if (rc == null) {
                        continue;
                    }
                    mask = mask | rc.rgnt.getMask();
                }

                Spell sp = spellSelect.getSelected();
                if (sp == null) {
                    return;
                }
                if (mask == sp.getMask()) {
                    Sounds.play(Sound.POSITIVE_EFFECT);
                    animateText("Success!", Color.GREEN, 300, 180, 500, 180);

                    party.getSaveGame().mixtures[sp.ordinal()]++;

                    ArrayList<MixtureCount> tmp2 = new ArrayList<>();
                    for (int i = 0; i < Spell.values().length; i++) {
                        int count = party.getSaveGame().mixtures[i];
                        if (count > 0) {
                            tmp2.add(new MixtureCount(Spell.get(i), count));
                        }
                    }
                    mixtures = (MixtureCount[]) tmp2.toArray(new MixtureCount[tmp2.size()]);
                    mixtureList.setItems(mixtures);

                } else {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    animateText("It Fizzles!", Color.RED, 300, 180, 500, 180);
                }

                for (ReagentCount x : owned) {
                    party.getSaveGame().reagents[x.rgnt.ordinal()] = x.count;
                }

                mixedList.clearItems();
                mixing = null;

            }
        });
        buttonTable.row();
        buttonTable.add(mix).expandX().left().width(100);

        exit = new TextButton("Quit", skin, "wood");
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (MixtureScreen.this.mainGame != null) {
                    MixtureScreen.this.mainGame.setScreen(MixtureScreen.this.returnScreen);
                    MixtureScreen.this.dispose();
                }
            }
        });
        
        buttonTable.row();
        buttonTable.add(exit).expandX().left().width(100);

        ScrollPane sp1 = new ScrollPane(ownedList, skin);
        ScrollPane sp2 = new ScrollPane(mixedList, skin);
        ScrollPane sp3 = new ScrollPane(mixtureList, skin);

        internalTable.add(sp1).align(Align.top).minWidth(200).minHeight(300);
        internalTable.add(buttonTable).align(Align.top);
        internalTable.add(sp2).align(Align.top).padRight(10).minWidth(200).minHeight(300);
        internalTable.add(sp3).align(Align.top).minWidth(200).minHeight(300);
        internalTable.row();

        internalTable.setX(500);
        internalTable.setY(575);
        
        reagLabel = new Label("", skin);
        reagLabel.setWrap(true);
        reagLabel.setAlignment(Align.topLeft, Align.left);
        reagLabel.setWidth(465);
        reagLabel.setHeight(375);
        reagLabel.setX(20);
        reagLabel.setY(0);
        
        spellLabel = new Label(spellDescs.get(0), skin);
        spellLabel.setWrap(true);
        spellLabel.setAlignment(Align.topLeft, Align.left);
        spellLabel.setWidth(465);
        spellLabel.setHeight(375);
        spellLabel.setX(515);
        spellLabel.setY(0);
        
        stage.addActor(new Image(new Texture(Gdx.files.classpath("assets/graphics/alchemy.png"))));
        stage.addActor(reagLabel);
        stage.addActor(spellLabel);
        stage.addActor(internalTable);

    }

    private void animateText(String text, Color color, float sx, float sy, float dx, float dy) {
        Label label = new Label(text, this.skin);
        label.setPosition(sx, sy);
        label.setColor(color);
        stage.addActor(label);
        label.addAction(sequence(Actions.moveTo(dx, dy, 2f), Actions.fadeOut(1f), Actions.removeActor(label)));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    private class ReagentCount {

        Reagent rgnt;
        int count;

        ReagentCount(Reagent rt, int count) {
            this.rgnt = rt;
            this.count = count;
        }

        @Override
        public String toString() {
            return rgnt.getDesc() + " (" + count + ")";
        }
    }

    private class MixtureCount {

        Spell spell;
        int count;
        
        MixtureCount(Spell sp, int count) {
            this.spell = sp;
            this.count = count;
        }

        @Override
        public String toString() {
            return spell.getDesc() + " (" + count + ")";
        }
    }

}
