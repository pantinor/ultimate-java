package dungeon;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class TorchObject {

   private ShaderProgram shader;
   private Mesh mesh;
   protected Texture texture;

   private Vector3 pos = new Vector3(0.0f, 0.0f, 0.0f);
   private Vector3 rot = new Vector3(0.0f, 0.0f, 0.0f);
   private Vector3 scale = new Vector3(1.0f, 1.0f, 1.0f);

   private boolean useLighting = true;

   private int type;

   public TorchObject(ShaderProgram shader, Mesh mesh, int type) {
      this(shader, mesh, type, null);
   }

   public TorchObject(ShaderProgram shader, Mesh mesh, int type, Texture texture) {
      this.shader = shader;
      this.mesh = mesh;
      this.type = type;
      this.texture = texture;
      if (texture != null) {
         texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
      }
   }

   private Matrix4 getMatrix() {
      return new Matrix4().mul(new Matrix4().translate(pos).rotate(1, 0, 0, rot.x).rotate(0, 1, 0, rot.y).rotate(0, 0, 1, rot.z).scale(scale.x, scale.y, scale.z));
   }

   public void render() {
      if (texture != null) {
         texture.bind();
         shader.setUniformi("uUseTextures", 1);
      } else {
         shader.setUniformi("uUseTextures", 0);
      }

      shader.setUniformi("uUseLighting", useLighting ? 1 : 0);

      Matrix4 matrix = getMatrix();
      shader.setUniformMatrix("uMVMatrix", matrix);
      shader.setUniformMatrix("uNMatrix", new Matrix3().set(matrix));

      mesh.render(shader, type);
   }

   public Vector3 getPos() {
      return pos;
   }

   public void setPos(Vector3 pos) {
      this.pos = pos;
   }

   public Vector3 getRot() {
      return rot;
   }

   public void setRot(Vector3 rot) {
      this.rot = rot;
   }

   public Vector3 getScale() {
      return scale;
   }

   public void setScale(Vector3 scale) {
      this.scale = scale;
   }

   public boolean useLighting() {
      return useLighting;
   }

   public void setUseLighting(boolean useLighting) {
      this.useLighting = useLighting;
   }
}
