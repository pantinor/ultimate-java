package objects;

import ultima.BaseScreen;
import ultima.Constants.AttackResult;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import java.util.HashMap;
import java.util.Map;

public class ProjectileActor extends Actor {
    
    private static final Map<Color, Texture> BALLS = new HashMap<>();

    static {
        BALLS.put(Color.RED,getBall(Color.RED));
        BALLS.put(Color.GREEN,getBall(Color.GREEN));
        BALLS.put(Color.PURPLE,getBall(Color.PURPLE));
        BALLS.put(Color.CYAN,getBall(Color.CYAN));
        BALLS.put(Color.VIOLET,getBall(Color.VIOLET));
        BALLS.put(Color.BLUE,getBall(Color.BLUE));
        BALLS.put(Color.YELLOW,getBall(Color.YELLOW));
        BALLS.put(Color.WHITE,getBall(Color.WHITE));
        BALLS.put(Color.BROWN,getBall(Color.BROWN));
    }

    private static Texture getBall(Color color) {
        Pixmap red = new Pixmap(32, 32, Format.RGBA8888);
        red.setColor(color);
        red.fillCircle(16, 16, 4);
        Texture t = new Texture(red);
        red.dispose();
        return t;
    }

    private final Texture texture;
    
    public TextureRegion resultTexture;
    public AttackResult res;

    public ProjectileActor(BaseScreen screen, Color color, int x, int y, AttackResult res) {
        this.res = res;
        this.texture = BALLS.get(color);
        Vector3 v = screen.getMapPixelCoords(x, y);
        this.setX(v.x);
        this.setY(v.y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        if (resultTexture == null) {
            batch.draw(texture, getX(), getY());
        } else {
            batch.draw(resultTexture, getX(), getY(), 32, 32);
        }
    }

}
