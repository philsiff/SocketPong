import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Vector2f;

import java.io.Serializable;

/**
 * Created by Phillip on 5/12/2015.
 */
public class Ball implements Serializable{
    int x;
    int y;
    Vector2f movement;
    int radius;

    public Ball(Vector2f movement, int y, int x) {
        this.movement = movement;
        this.y = y;
        this.x = x;
        this.radius = 20;
    }

    public void update(GameContainer gc, int i){
        if(x < 0){movement.set(-1 * movement.getX(), movement.getY());}
        if(x + (2 * radius) > gc.getWidth() * 2){movement.set(-1 * movement.getX(), movement.getY());}
        if(y < 0){movement.set(movement.getX(), -1 * movement.getY());}
        if(y + (2 * radius) > gc.getHeight()){movement.set(movement.getX(), -1 * movement.getY());}

        x += movement.getX();
        y += movement.getY();
    }

    public void render(GameContainer gc, Graphics g){
        g.draw(new Circle(x + 20, y + 20, 20));
    }

    public void setMovementAngle(double angle){

    }

    public void setMovementMagnitude(float magnitude){
        double angle = Math.atan2(movement.getY(), movement.getX());
        movement.set((float)(magnitude * Math.cos((double) angle)), (float) (magnitude * Math.sin((double) angle)));
    }
}
