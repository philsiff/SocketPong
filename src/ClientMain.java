import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import java.io.IOException;
import java.util.Scanner;

public class ClientMain {
	public static void main(String[] args) throws IOException{
		Client client = new Client("localhost", 7777);
        AppGameContainer appgc;
        try {
            appgc = new AppGameContainer(client);
            appgc.setDisplayMode(800, 600, false);
            appgc.setTargetFrameRate(60);
            appgc.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
