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
    private Paddle paddle1;
    private Paddle paddle2;
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
                        if(message instanceof ClientInfo){
                            if(((ClientInfo) message).clientNumber == 1){
                                serverInfo.paddle1 = ((ClientInfo) message).paddle;
                            }else if(((ClientInfo) message).clientNumber == 2){
                                serverInfo.paddle2 = ((ClientInfo) message).paddle;
                            }
                        }
                        if(message instanceof String) {
                            System.out.println(message);
                        }
                    }
                    catch(InterruptedException e){ }
                }
            }
        };

        //messageHandling.setDaemon(true);
        messageHandling.start();
    }

    public void init(GameContainer gc){
        ball = new Ball(new Vector2f((float)1, (float)1), 400, 300);
        paddle1 = new Paddle(30, gc.getHeight()/2 - 30);
        paddle2 = new Paddle(gc.getWidth()*2 - 50, gc.getHeight()/2 - 30);
        serverInfo = new ServerInfo(ball, paddle1, paddle2);

    }

    public void update(GameContainer gc, int i){
        serverInfo.ball.update(gc, i);
        if (collision(serverInfo.paddle1, serverInfo.ball)) {
            double collisionAngle = calculateAngle(serverInfo.paddle1, serverInfo.ball);
            if(serverInfo.ball.movement.getY() > 0){
                if(collisionAngle < 0){
                    serverInfo.ball.setMovementAngle(-collisionAngle);
                }else{
                    serverInfo.ball.setMovementAngle(collisionAngle);
                }
            }else{
                if(collisionAngle < 0){
                    serverInfo.ball.setMovementAngle(collisionAngle);
                }else{
                    serverInfo.ball.setMovementAngle(-collisionAngle);
                }
            }
            if(serverInfo.ball.magnitude < 5) {
                serverInfo.ball.setMovementMagnitude((float) (ball.magnitude + .05));
            }
        }
        if (collision(serverInfo.paddle2, serverInfo.ball)){
            double collisionAngle = calculateAngle(serverInfo.paddle2, serverInfo.ball);
            if(serverInfo.ball.movement.getY() > 0){
                if(collisionAngle < 0){
                    serverInfo.ball.setMovementAngle(-collisionAngle);
                }else{
                    serverInfo.ball.setMovementAngle(collisionAngle);
                }
            }else{
                if(collisionAngle < 0){
                    serverInfo.ball.setMovementAngle(collisionAngle);
                }else{
                    serverInfo.ball.setMovementAngle(-collisionAngle);
                }
            }
            if(serverInfo.ball.magnitude < 5) {
                serverInfo.ball.setMovementMagnitude((float) (ball.magnitude + .05));
            }
        }
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
                out.writeObject((Object) obj);
                out.reset();
            }
            catch(IOException e){ e.printStackTrace(); }
        }
    }

    public void sendToOne(int index, Object message)throws IndexOutOfBoundsException {
        clientList.get(index).write(message);
    }

    public void sendToAll(Object message){
        for(ConnectionToClient client : clientList) {
            client.write(message);
        }
    }

    public boolean collision(Paddle paddle, Ball ball){
        float ballCX = ball.x + ball.radius;
        float ballCY = ball.y + ball.radius;
        if(paddle.x <= ballCX && ballCX <= paddle.x + paddle.width){ //Top Side
            if(Math.abs(paddle.y - ballCY) <= ball.radius){
                return true;
            }
        }
        if(Math.pow(paddle.x - ballCX, 2) + Math.pow(paddle.y - ballCY, 2) <= Math.pow(ball.radius, 2) ||
                Math.pow((paddle.x + paddle.width) - ballCX, 2) + Math.pow(paddle.y - ballCY, 2) <= Math.pow(ball.radius, 2)){
            return true;
        }

        if(paddle.y <= ballCY && ballCY <= paddle.y + paddle.height){ //Right Side
            if(Math.abs((paddle.x + paddle.width) - ballCX) <= ball.radius){
                return true;
            }
        }

        if(Math.pow((paddle.y - ballCY),2) + Math.pow((paddle.x + paddle.width) - ballCX,2) <= Math.pow(ball.radius, 2) ||
                Math.pow((paddle.y + paddle.height) - ballCY,2) + Math.pow((paddle.x + paddle.width) - ballCX,2) <= Math.pow(ball.radius, 2)){
            return true;
        }

        if(paddle.x <= ballCX && ballCX <= paddle.x + paddle.width){ //Bottom Side
            if(Math.abs((paddle.y + paddle.height) - ballCY) <= ball.radius){
                return true;
            }
        }

        if(Math.pow(paddle.x - ballCX, 2) + Math.pow(paddle.y + paddle.height - ballCY, 2) <= Math.pow(ball.radius, 2) ||
                Math.pow((paddle.x + paddle.width) - ballCX, 2) + Math.pow(paddle.y + paddle.height - ballCY, 2) <= Math.pow(ball.radius, 2)){
            return true;
        }

        if(paddle.y <= ballCY && ballCY <= paddle.y + paddle.height){ //Left Side
            if(Math.abs((paddle.x) - ballCX) <= ball.radius){
                return true;
            }
        }

        if(Math.pow((paddle.y - ballCY),2) + Math.pow((paddle.x) - ballCX,2) <= Math.pow(ball.radius, 2) ||
                Math.pow((paddle.y + paddle.height) - ballCY,2) + Math.pow((paddle.x) - ballCX,2) <= Math.pow(ball.radius, 2)){
            return true;
        }

        return false;
    }

    public double calculateAngle(Paddle paddle, Ball ball){
        return Math.toDegrees(Math.atan2((ball.y + ball.radius) - (paddle.y + (paddle.width / 2)), (ball.x + ball.radius) - (paddle.x + (paddle.width / 2))));
    }

}