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

public class ProjectileActor extends Actor {

    public static final Texture blueBall;
    public static final Texture redBall;
    public static final Texture greenBall;
    public static final Texture purpleBall;
    public static final Texture whiteBall;
    public static final Texture tealBall;

    static {
        redBall = getBall(Color.RED);
        blueBall = getBall(Color.BLUE);
        greenBall = getBall(Color.GREEN);
        purpleBall = getBall(Color.PURPLE);
        whiteBall = getBall(Color.WHITE);
        tealBall = getBall(Color.TEAL);
    }

    public static Texture getBall(Color color) {
        Pixmap red = new Pixmap(32, 32, Format.RGBA8888);
        red.setColor(color);
        red.fillCircle(16, 16, 3);
        Texture t = new Texture(red);
        red.dispose();
        return t;
    }

    public Texture texture;
    public TextureRegion resultTexture;
    public AttackResult res;

    public ProjectileActor(BaseScreen screen, Color color, int x, int y, AttackResult res) {
        this.res = res;

        if (color == Color.RED) {
            texture = redBall;
        }
        if (color == Color.BLUE) {
            texture = blueBall;
        }
        if (color == Color.WHITE) {
            texture = whiteBall;
        }
        if (color == Color.GREEN) {
            texture = greenBall;
        }
        if (color == Color.TEAL) {
            texture = tealBall;
        }
        if (color == Color.PURPLE) {
            texture = purpleBall;
        }

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
