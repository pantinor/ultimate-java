package ultima;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import objects.JournalEntries;
import util.JournalList;

public class JournalScreen implements Screen, InputProcessor, Constants {

    private final Stage stage;
    private final Ultima4 mainGame;
    private final BaseScreen returnScreen;
    private final JournalEntries entries;
    private final JournalList list;

    public JournalScreen(Ultima4 mainGame, BaseScreen returnScreen, Skin skin, JournalEntries entries) {
        this.returnScreen = returnScreen;
        this.mainGame = mainGame;
        this.entries = entries;
        this.stage = new Stage();

        Label filterLabel = new Label("Filter:", skin);
        filterLabel.setX(16);
        filterLabel.setY(Gdx.graphics.getHeight() - 32);

        TextField filterField = new TextField("", skin);
        filterField.setX(56);
        filterField.setY(Gdx.graphics.getHeight() - 32);

        CheckBox cb = new CheckBox("Show Active", skin);
        cb.setX(256);
        cb.setY(Gdx.graphics.getHeight() - 32);

        list = new JournalList(skin, filterField, cb, this.entries.toArray(skin));

        ScrollPane sp = new ScrollPane(list, skin);
        sp.setX(16);
        sp.setY(16);
        sp.setWidth(Gdx.graphics.getWidth() - 32);
        sp.setHeight(Gdx.graphics.getHeight() - 64);

        TextButton exit = new TextButton("Exit", skin, "wood");
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (JournalScreen.this.mainGame != null) {
                    JournalScreen.this.entries.fromArray(JournalScreen.this.list.getItems());
                    JournalScreen.this.mainGame.setScreen(JournalScreen.this.returnScreen);
                    JournalScreen.this.dispose();
                }
            }
        });
        exit.setX(Gdx.graphics.getWidth() - 100);
        exit.setY(Gdx.graphics.getHeight() - 36);
        exit.setWidth(75);
        exit.setHeight(25);

        stage.addActor(filterLabel);
        stage.addActor(filterField);
        stage.addActor(cb);
        stage.addActor(exit);
        stage.addActor(sp);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        if (mainGame != null) {
            this.entries.fromArray(this.list.getItems());
            mainGame.setScreen(returnScreen);
            dispose();
        }
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }

}
