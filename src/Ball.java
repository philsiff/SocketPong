import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Vector2f;

import java.io.Serializable;

/**
 * Created by Phillip on 5/12/2015.
 */
public class Ball implements Serializable{
    float x;
    float y;
    Vector2f movement;
    float radius;
    float magnitude;
    float angle;

    public Ball(Vector2f movement, int y, int x) {
        this.movement = movement;
        this.y = y;
        this.x = x;
        this.radius = 10;
        this.magnitude = (float) (Math.sqrt(Math.pow(movement.getX(), 2) + Math.pow(movement.getY(), 2)));
        this.angle = (float) movement.getTheta();
    }

    public void update(GameContainer gc, int i){
        x += movement.getX();
        y += movement.getY();
    }

    public void render(GameContainer gc, Graphics g){
        g.draw(new Circle(x + radius, y + radius, radius));
    }

    public void setMovementAngle(float angle){
        movement.setTheta(angle);
    }
}
