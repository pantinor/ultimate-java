package ultima;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class BookScreen extends InputAdapter implements Screen, Constants {

    private final Stage stage;
    private final Ultima4 mainGame;
    private final BaseScreen returnScreen;
    private final java.util.List<Label> labels = new ArrayList<>();
    private final java.util.Map<Integer, Page> pages = new HashMap<>();

    private int currentPage = 0;

    public BookScreen(Ultima4 mainGame, BaseScreen returnScreen, Skin skin) {
        this.returnScreen = returnScreen;
        this.mainGame = mainGame;
        this.stage = new Stage();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/lindberg.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 18;
        BitmapFont fontLarger = generator.generateFont(parameter);

        generator.dispose();

        Label.LabelStyle labs = new Label.LabelStyle(skin.get("default", Label.LabelStyle.class));
        labs.font = fontLarger;
        labs.background = null;

        addPages(fontLarger, 0, "/assets/data/commands.txt", labs);
        addPages(fontLarger, labels.size(), "/assets/data/book.txt", labs);

        if (labels.size() % 2 != 0) {
            labels.add(new Label("", labs));
        }

        int x = 0;
        for (Label l : labels) {
            l.setWrap(true);
            l.setAlignment(Align.topLeft, Align.left);
            l.setWidth(460);
            l.setHeight(600);
            l.setX(x % 2 == 0 ? 35 : 525);
            l.setY(145);
            x++;
        }

        int idx = 0;
        for (int i = 0; i < labels.size(); i += 2) {
            Page page = new Page(idx, labels.get(i), labels.get(i + 1));
            pages.put(idx, page);
            idx++;
        }

        Skin imgBtnSkin = new Skin(Gdx.files.classpath("assets/skin/imgBtn.json"));
        ImageButton left = new ImageButton(imgBtnSkin, "left");
        ImageButton right = new ImageButton(imgBtnSkin, "right");
        left.setX(512 - 64);
        left.setY(16);
        right.setX(512 + 64);
        right.setY(16);

        left.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                pages.get(currentPage).left.remove();
                pages.get(currentPage).right.remove();
                currentPage--;
                if (currentPage < 0) {
                    currentPage = 0;
                }
                stage.addActor(pages.get(currentPage).left);
                stage.addActor(pages.get(currentPage).right);
            }
        });

        right.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                pages.get(currentPage).left.remove();
                pages.get(currentPage).right.remove();
                currentPage++;
                if (currentPage >= pages.size()) {
                    currentPage = pages.size() - 1;
                }
                stage.addActor(pages.get(currentPage).left);
                stage.addActor(pages.get(currentPage).right);
            }
        });

        CheckBox.CheckBoxStyle cbs = new CheckBox.CheckBoxStyle(skin.get("default", CheckBox.CheckBoxStyle.class));
        cbs.font = fontLarger;
        cbs.fontColor = Color.BLUE;

        CheckBox cb = new CheckBox("Enable Music", cbs);
        cb.setX(32);
        cb.setY(16);
        cb.setChecked(Ultima4.playMusic);

        cb.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                CheckBox cb = (CheckBox) actor;
                if (cb.isChecked()) {
                    Ultima4.playMusic = true;
                    if (Ultima4.music != null) {
                        Ultima4.music.play();
                    }
                } else {
                    Ultima4.playMusic = false;
                    if (Ultima4.music != null) {
                        Ultima4.music.stop();
                    }
                }
            }
        });

        stage.addActor(new Image(new Texture(Gdx.files.classpath("assets/graphics/scroll.png"))));
        stage.addActor(pages.get(0).left);
        stage.addActor(pages.get(0).right);

        stage.addActor(left);
        stage.addActor(right);
        stage.addActor(cb);

    }

    private void addPages(BitmapFont font, int pageStart, String fn, Label.LabelStyle labs) {
        try {
            List<String> lines = IOUtils.readLines(BookScreen.class.getResourceAsStream(fn));
            int page = pageStart;
            GlyphLayout gl = new GlyphLayout(font, "");
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line).append("\n");
                gl.setText(font, sb.toString().trim(), Color.WHITE, 450, Align.left, true);
                if (gl.height > 600) {
                    labels.add(new Label(sb.toString().trim(), labs));
                    sb = new StringBuilder();
                    page++;
                }
            }
            labels.add(new Label(sb.toString().trim(), labs));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }

    @Override
    public boolean keyUp(int i) {
        if (mainGame != null) {
            mainGame.setScreen(returnScreen);
        }
        return false;
    }

    private class Page {

        int page;
        Label left;
        Label right;

        public Page(int page, Label left, Label right) {
            this.page = page;
            this.left = left;
            this.right = right;
        }

    }

}
