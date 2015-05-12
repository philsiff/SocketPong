import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Vector2f;

public class Server extends BasicGame{
    private ArrayList<ConnectionToClient> clientList;
    private LinkedBlockingQueue<Object> messages;
    private ServerSocket serverSocket;
    private Ball ball;
    private ServerInfo serverInfo;

    public Server(int port) throws IOException {
        super("Pong Server");
        clientList = new ArrayList<ConnectionToClient>();
        messages = new LinkedBlockingQueue<Object>();
        serverSocket = new ServerSocket(port);

        Thread accept = new Thread() {
            public void run(){
                while(true){
                    try{
                        Socket s = serverSocket.accept();
                        System.out.println("User Connected");
                        clientList.add(new ConnectionToClient(s));
                        sendToOne(clientList.size()-1, "HczGkfodKL" + clientList.size());
                    }
                    catch(IOException e){ e.printStackTrace(); }
                }
            }
        };

        //accept.setDaemon(true);
        accept.start();

        Thread messageHandling = new Thread() {
            public void run(){
                while(true){
                    try{
                        Object message = messages.take();
                        // Do some handling here...
                        System.out.println(message);
                        sendToAll(message);
                    }
                    catch(InterruptedException e){ }
                }
            }
        };

        //messageHandling.setDaemon(true);
        messageHandling.start();
    }

    public void init(GameContainer gc){
        ball = new Ball(new Vector2f(1, 1), 400, 300);
        serverInfo = new ServerInfo(ball);
    }

    public void update(GameContainer gc, int i){
        serverInfo.ball.update(gc, i);
        sendToAll(serverInfo);
    }

    public void render(GameContainer gc, Graphics g){

    }

    private class ConnectionToClient {
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket socket;

        ConnectionToClient(final Socket socket) throws IOException {
            this.socket = socket;
            out = new ObjectOutputStream(socket.getOutputStream());

            Thread read = new Thread(){
                public void run(){
                    while(true){
                        try{

                            if(in == null){in = new ObjectInputStream(socket.getInputStream());}
                            Object obj = in.readObject();
                            messages.put(obj);
                        }
                        catch(IOException e){ e.printStackTrace(); } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            //read.setDaemon(true); // terminate when main ends
            read.start();
        }

        public void write(Object obj) {
            try{
                if(obj instanceof ServerInfo){
                    System.out.println(((ServerInfo) obj).ball.x);
                }

                out.writeObject((Object) obj);
            }
            catch(IOException e){ e.printStackTrace(); }
        }
    }

    public void sendToOne(int index, Object message)throws IndexOutOfBoundsException {
        clientList.get(index).write(message);
    }

    public void sendToAll(Object message){
        for(ConnectionToClient client : clientList)
            client.write(message);
    }

}