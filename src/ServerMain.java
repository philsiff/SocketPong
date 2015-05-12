import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import java.io.IOException;

public class ServerMain {
	public static void main(String[] args) throws IOException{
		Server server = new Server(7777);
        AppGameContainer appgc;
        try {
            appgc = new AppGameContainer(server);
            appgc.setDisplayMode(800, 600, false);
            appgc.setTargetFrameRate(60);
            appgc.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
	}
}
