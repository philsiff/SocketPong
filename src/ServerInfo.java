import org.newdawn.slick.geom.Vector2f;

import java.io.Serializable;

/**
 * Created by Phillip on 5/12/2015.
 */
public class ServerInfo implements Serializable{
    Ball ball;

    public ServerInfo(Ball ball) {
        this.ball = ball;
    }
}
