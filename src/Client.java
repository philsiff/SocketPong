import org.newdawn.slick.BasicGame;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;


public class Client extends BasicGame {
    private ConnectionToServer server;
    private LinkedBlockingQueue<Object> messages;
    private Socket socket;
    private int clientNumber;
    private Ball ball;


    public Client(String IPAddress, int port) throws IOException{
        super("Pong Client");

        socket = new Socket(IPAddress, port);
        messages = new LinkedBlockingQueue<Object>();
        server = new ConnectionToServer(socket);

        Thread messageHandling = new Thread() {
            public void run(){
                while(true){
                    try{
                        Object message = messages.take();
                        // Do some handling here...
                        if(message instanceof ServerInfo){
                            ball = ((ServerInfo) message).ball;
                        }

                        if(message instanceof String){
                            if(((String) message).substring(0, 10).equals("HczGkfodKL")){
                                clientNumber = Integer.parseInt(((String) message).substring(10, 11));
                                System.out.println(clientNumber);
                            }
                        }
                    }
                    catch(InterruptedException e){ }
                }
            }
        };

        messageHandling.start();
    }

    public void init(GameContainer gc) throws SlickException{
        gc.setAlwaysRender(true);
    }

    public void update(GameContainer gc, int i) throws SlickException{

    }

    public void render(GameContainer gc, Graphics g){
        if(ball != null){
            ball.render(gc, g);
        }
    }

    private class ConnectionToServer {
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket socket;

        ConnectionToServer(final Socket socket) throws IOException {
            this.socket = socket;

            out = new ObjectOutputStream(socket.getOutputStream());

            Thread read = new Thread(){
                public void run(){
                    while(true){
                        try{
                            if(in==null){in = new ObjectInputStream(socket.getInputStream());}
                            Object obj = ((Object) in.readObject());
                            messages.put((Object)obj);
                        }
                        catch(IOException e){ e.printStackTrace(); } catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
                }
            };

            //read.setDaemon(true);
            read.start();
        }

        private void write(Object obj) {
            try{
                out.writeObject(obj);
            }
            catch(IOException e){ e.printStackTrace(); }
        }


    }

    public void send(Object obj) {
        server.write(obj);
    }
}