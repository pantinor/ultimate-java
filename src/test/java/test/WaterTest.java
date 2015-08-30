package test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WaterTest extends Game {

    float time = 0;
    Batch batch2;
    
    int dim = 200;
    float[][] heightMapPrev = new float[dim+1][dim+1];
    float[][] heightMapCurr = new float[dim+1][dim+1];
    float damping = 0.4f;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "test";
        cfg.width = 800;
        cfg.height = 600;
        new LwjglApplication(new WaterTest(), cfg);
    }

    @Override
    public void create() {

        heightMapCurr[30][40] = 5f; // changing one value to start a water wave

        batch2 = new SpriteBatch();

    }

    private void processWater() {

        // Loop through all the vertices and update their vertical position values 
        // according to their surrounding vertices' vertical positions
        for (int i = 1; i < dim; i++) {
            for (int j = 1; j < dim; j++) {
                // Count new vertical position of each vertex
                heightMapCurr[i][j] = (heightMapPrev[i + 1][j]
                        + heightMapPrev[i - 1][j]
                        + heightMapPrev[i][j + 1]
                        + heightMapPrev[i][j - 1]) / 2.0f
                        - heightMapCurr[i][j];

                // Damp ripples to make them loose energy                    
                heightMapCurr[i][j] -= heightMapCurr[i][j] * damping;
            }
        }
    }

    private Texture getTexture() {

        Pixmap p = new Pixmap(dim, dim, Format.RGBA8888);
        p.setColor(Color.BLUE);
        p.fillRectangle(0, 0, dim, dim);

        for (int x = 1; x < dim; x++) {
            for (int y = 1; y < dim; y++) {
                float xoff = heightMapCurr[x - 1][y] - heightMapCurr[x + 1][y];
                float yoff = heightMapCurr[x][y - 1] - heightMapCurr[x][y + 1];
                int shading = (int)xoff;
                if (shading != 0) {
                    System.out.println(shading);
                }
                int rgb = Color.BLUE.toIntBits() + shading;
                p.drawPixel(x, y, shading);
            }
        }

        Texture t = new Texture(p, Format.RGB888, false);
        p.dispose();

        return t;
    }    

    @Override
    public void render() {
        time += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        processWater();

        batch2.begin();

        batch2.draw(getTexture(), dim, dim);

        batch2.end();
        
        //now swap values on the buffers
        for (int i = 0; i < dim+1; i++) {
            for (int j = 0; j < dim+1; j++) {
                float temp = heightMapPrev[i][j];
                heightMapPrev[i][j] = heightMapCurr[i][j];
                heightMapCurr[i][j] = temp;
            }
        }

    }

}
