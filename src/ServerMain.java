import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;
import java.util.Scanner;
import java.io.IOException;

public class ServerMain {
	public static void main(String[] args) throws IOException{
        Scanner scanner = new Scanner(System.in);
        System.out.print("What port do you want to host it on?: ");
        int port = Integer.parseInt(scanner.next());
		Server server = new Server(port);
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
