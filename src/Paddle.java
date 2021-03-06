import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import java.io.Serializable;

/**
 * Created by Phillip on 5/12/2015.
 */
public class Paddle implements Serializable {
    int x;
    int y;
    int height;
    int width;

    public Paddle(int x, int y, int height, int width) {
        this.y = y;
        this.x = x;
        this.height = height;
        this.width = width;
    }

    //draw the rectangle in the gamecontainer
    public void render(GameContainer gc, Graphics g){
        g.draw(new Rectangle(x, y, width, height));
    }
}
