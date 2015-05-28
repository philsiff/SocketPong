import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import java.io.IOException;
import java.util.Scanner;

public class ClientMain {
	public static void main(String[] args) throws IOException{
        Scanner scanner = new Scanner(System.in);
        System.out.print("What Ip: ");
        String ip = scanner.next();
        System.out.print("What Port: ");
        int port = Integer.parseInt(scanner.next());
		Client client = new Client(ip, port);
        AppGameContainer appgc;
        try {
            appgc = new AppGameContainer(client);
            appgc.setDisplayMode(800, 600, true);
            appgc.setTargetFrameRate(60);
            appgc.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
