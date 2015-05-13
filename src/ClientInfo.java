import java.io.Serializable;

/**
 * Created by block7 on 5/11/15.
 */
public class ClientInfo implements Serializable{
    int clientNumber;
    Paddle paddle;

    public ClientInfo(int clientNumber, Paddle paddle) {
        this.clientNumber = clientNumber;
        this.paddle = paddle;
    }
}
