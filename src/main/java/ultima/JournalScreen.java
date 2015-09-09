package ultima;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import objects.JournalEntries;
import util.JournalList;

public class JournalScreen implements Screen, InputProcessor, Constants {

    private final Stage stage;
    private final Ultima4 mainGame;
    private final BaseScreen returnScreen;
    private final JournalEntries entries;
    private final JournalList list;

    public JournalScreen(Ultima4 mainGame, BaseScreen returnScreen, JournalEntries entries) {
        this.returnScreen = returnScreen;
        this.mainGame = mainGame;
        this.entries = entries;
        this.stage = new Stage();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/ultima.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 14;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        Skin skin = new Skin(Gdx.files.internal("assets/skin/uiskin.json"));

        LabelStyle ls = skin.get(LabelStyle.class);
        ls.font = font;
        Label filterLabel = new Label("Filter:", ls);
        filterLabel.setX(16);
        filterLabel.setY(Gdx.graphics.getHeight() - 32);

        TextFieldStyle tfs = skin.get(TextFieldStyle.class);
        tfs.font = font;
        TextField filterField = new TextField("", tfs);
        filterField.setX(56);
        filterField.setY(Gdx.graphics.getHeight() - 32);
        
        CheckBoxStyle cbs = skin.get("journal", CheckBoxStyle.class);
        cbs.font = font;
        CheckBox cb = new CheckBox("Show Active", cbs);
        cb.setX(256);
        cb.setY(Gdx.graphics.getHeight() - 32);
        
        list = new JournalList(skin, font, filterField, cb, this.entries.toArray(skin));

        ScrollPane sp = new ScrollPane(list, skin, "logs");
        sp.setX(16);
        sp.setY(16);
        sp.setWidth(Gdx.graphics.getWidth() - 16);
        sp.setHeight(Gdx.graphics.getHeight() - 64);

        stage.addActor(filterLabel);
        stage.addActor(filterField);
        stage.addActor(cb);
        stage.addActor(sp);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
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
