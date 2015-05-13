import org.newdawn.slick.geom.Vector2f;

import java.io.Serializable;

/**
 * Created by Phillip on 5/12/2015.
 */
public class ServerInfo implements Serializable{
    Ball ball;
    Paddle paddle1;
    Paddle paddle2;

    public ServerInfo(Ball ball, Paddle paddle1, Paddle paddle2) {
        this.ball = ball;
        this.paddle1 = paddle1;
        this.paddle2 = paddle2;
    }
}
