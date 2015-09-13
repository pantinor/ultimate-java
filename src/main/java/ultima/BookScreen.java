package ultima;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

public class BookScreen extends InputAdapter implements Screen, Constants {

    private final Stage stage;
    private final Ultima4 mainGame;
    private final BaseScreen returnScreen;
    private final java.util.List<String> lines = new ArrayList<>();
    
    public BookScreen(Ultima4 mainGame, BaseScreen returnScreen, Skin skin) {
        this.returnScreen = returnScreen;
        this.mainGame = mainGame;
        this.stage = new Stage();
        
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/lindberg.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        
        parameter.size = 24;
        BitmapFont fontLarger = generator.generateFont(parameter);
        
        generator.dispose();
        
        Label.LabelStyle labs = new Label.LabelStyle(skin.get("default", Label.LabelStyle.class));
        labs.font = fontLarger;
        
        Label text = new Label("", labs);
        text.setWrap(true);
        text.setAlignment(Align.topLeft, Align.left);
        
        try {
            StringBuilder sb = new StringBuilder(FileUtils.readFileToString(Gdx.files.internal("assets/data/commands.txt").file()));
            sb.append("\n\n").append(FileUtils.readFileToString(Gdx.files.internal("assets/data/book.txt").file()));
            text.setText(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
                
        ScrollPane sp1 = new ScrollPane(text, skin);
        sp1.setWidth(750);
        sp1.setHeight(650);
        sp1.setX(150);
        sp1.setY(60);

        stage.addActor(new Image(new Texture(Gdx.files.internal("assets/graphics/scroll.png"))));
        stage.addActor(sp1);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0);
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
    
    @Override
    public boolean keyUp(int i) {
        if (mainGame != null) {
            mainGame.setScreen(returnScreen);
            dispose();
        }
        return false;
    }

}
