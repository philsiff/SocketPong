import org.newdawn.slick.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.Font;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.geom.Vector2f;


public class Client extends BasicGame {
    private ConnectionToServer server;
    private LinkedBlockingQueue<Object> messages;
    private Socket socket;
    private int clientNumber;
    private Ball ball;
    private Paddle paddle1;
    private Paddle paddle2;
    private int score1 = 0;
    private int score2 = 0;
    private ClientInfo clientInfo;
    private Input input;
    private int numClients;
    private TrueTypeFont font;


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
                            paddle1 = ((ServerInfo) message).paddle1;
                            paddle2 = ((ServerInfo) message).paddle2;
                            score1 = ((ServerInfo) message).score1;
                            score2 = ((ServerInfo) message).score2;
                        }

                        if(message instanceof String){
                            if(((String) message).substring(0, 10).equals("HczGkfodKL")){
                                clientNumber = Integer.parseInt(((String) message).substring(10, 11));
                                System.out.println(clientNumber);
                                System.out.println("NumClients: " + numClients);
                            }
                            if(((String) message).substring(0, 10).equals("evDWphmwFh")){
                                numClients = Integer.parseInt(((String) message).substring(10,11));
                                System.out.println("NumClients: " + numClients);
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
        this.clientInfo = new ClientInfo(clientNumber, paddle1);
        this.font = new TrueTypeFont(new java.awt.Font("ubuntu", Font.PLAIN, 32), false);
    }

    public void update(GameContainer gc, int i) throws SlickException{
        input = gc.getInput();
        if(input.isKeyDown(Input.KEY_UP)){
            if(clientNumber == 1 && paddle1.y >= 5){
                paddle1.y -= 5;
                clientInfo.paddle = paddle1;
                send((Object)clientInfo);
            }else if(clientNumber == numClients && paddle2.y >= 5){
                paddle2.y -= 5;
                clientInfo.paddle = paddle2;
                send((Object)clientInfo);
            }
        }
        if(input.isKeyDown(Input.KEY_DOWN)){
            if(clientNumber == 1 && paddle1.y <= gc.getHeight() - paddle1.height - 5){
               paddle1.y += 5;
                clientInfo.paddle = paddle1;
                send((Object)clientInfo);
            }else if(clientNumber == numClients && paddle2.y <= gc.getHeight() - paddle2.height - 5){
                paddle2.y += 5;
                clientInfo.paddle = paddle2;
                send((Object)clientInfo);
            }
        }
    }

    public void render(GameContainer gc, Graphics g){
        if(paddle2 != null && paddle1 != null && ball != null){
            if(clientNumber == 1) {
                paddle1.render(gc, g);
            }
            else if(clientNumber == numClients) {
                paddle2.x -= gc.getWidth() * (numClients - 1);
                ball.x -= gc.getWidth() * (numClients - 1);
                paddle2.render(gc, g);
            }else{
                ball.x -= gc.getWidth() * (clientNumber - 1);
            }
            ball.render(gc, g);
            renderScore(gc, g);
        }


    }

    private void renderScore(GameContainer gc, Graphics g){
        if(clientNumber == 1){
            font.drawString(gc.getWidth()/2 - 20, 60, "" + score1);
        }else if(clientNumber == numClients){
            font.drawString(gc.getWidth()/2 - 20, 60, "" + score2);
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
                out.writeObject((Object) obj);
                out.reset();
            }
            catch(IOException e){ e.printStackTrace(); }
        }


    }

    public void send(Object obj) {
        server.write(obj);
    }
}