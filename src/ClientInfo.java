import java.io.Serializable;

/**
 * Created by block7 on 5/11/15.
 */
public class ClientInfo implements Serializable{
    int clientNumber;
    int paddleY;

    public ClientInfo(int clientNumber, int paddleY) {
        this.clientNumber = clientNumber;
        this.paddleY = paddleY;
    }
}
