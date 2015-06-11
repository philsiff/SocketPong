import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Vector2f;

public class Server extends BasicGame{ //extends BasicGame so we get that update and render loops easy
    //Server sends its information to its clients by sending a ServerInfo object
    //Clients sends its information to the server by sending a ClientInfo object
    //All updates are done modifying the ServerInfo ojbect and then sending it out to clients so they can update their information

    //Various Bloated Instanced Variables
    private ArrayList<ConnectionToClient> clientList;
    private LinkedBlockingQueue<Object> messages;
    private ServerSocket serverSocket;
    private Ball ball;
    private Paddle paddle1;
    private Paddle paddle2;
    private int paddleHeight = 100;
    private int paddleWidth = 20;
    private ServerInfo serverInfo;
    private boolean paddle1Hit = false;
    private boolean paddle2Hit = false;
    private int gameContainerHeight;
    private int gameContainerWidth;
    private String state = "playing";

    public Server(int port) throws IOException {
        //Sets up serversockt which clients connect to and list of clients as well as queue of messages
        super("Pong Server");
        clientList = new ArrayList<ConnectionToClient>();
        messages = new LinkedBlockingQueue<Object>();
        serverSocket = new ServerSocket(port);

        Thread accept = new Thread() {
            public void run(){ //sets up thread to continually check for new connections to server
                while(true){
                    try{
                        Socket s = serverSocket.accept();//if the client connects then it sends a message to server what number it is
                        System.out.println("User Connected");
                        clientList.add(new ConnectionToClient(s)); //Connection to client holds all info of client and can get and send message from/to client
                        sendToOne(clientList.size()-1, "HczGkfodKL" + clientList.size());
                        if(clientList.size() >= 1){
                            sendToAll("evDWphmwFh" + clientList.size());
                        }
                        serverInfo.paddle2.x = gameContainerWidth*clientList.size() - 10 - paddleWidth; //updates paddle x to be in farthest monitor


                        serverInfo.score1 = 0;
                        serverInfo.score2 = 0;
                        paddle1Hit = false;
                        paddle2Hit = false;
                    }
                    catch(IOException e){ e.printStackTrace(); }
                }
            }
        };

        //accept.setDaemon(true);
        accept.start();

        Thread messageHandling = new Thread() {
            public void run(){ //Continually checks for messages that are added to the queue by various ConnectionToClient objects
                while(true){
                    try{
                        Object message = messages.take();
                        // Do some handling here...
                        if(message instanceof ClientInfo){ //updates y of paddle depending on which client. Client only sends updates of their paddle movement
                            if(((ClientInfo) message).clientNumber == 1){
                                serverInfo.paddle1 = ((ClientInfo) message).paddle;
                            }else if(((ClientInfo) message).clientNumber == clientList.size()){
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
        //Initialize various things
        ball = new Ball(new Vector2f((float)3, (float) 0), (gc.getWidth() * clientList.size())/2, gc.getHeight()/2);
        paddle1 = new Paddle(10 + paddleWidth, gc.getHeight()/2 - (paddleHeight/2), paddleHeight, paddleWidth);
        paddle2 = new Paddle(gc.getWidth()*clientList.size() - 10 - paddleWidth, gc.getHeight()/2 - (paddleHeight/2), paddleHeight, paddleWidth);
        serverInfo = new ServerInfo(ball, paddle1, paddle2);
        gameContainerHeight = gc.getHeight();
        gameContainerWidth = gc.getWidth();
    }

    public void update(GameContainer gc, int i){
        serverInfo.paddle2.x = gameContainerWidth*clientList.size() - 10 - paddleWidth;
        if(state.equals("playing")) {
            ///////Check if the Ball hits the sides;
            if (serverInfo.ball.x < 0 && clientList.size() > 0) {
                serverInfo.ball.movement.set(-1 * serverInfo.ball.movement.getX(), serverInfo.ball.movement.getY());
                serverInfo.score2++;
                state = "countdown";
            }
            if (serverInfo.ball.x + (2 * serverInfo.ball.radius) > gc.getWidth() * (clientList.size()) && clientList.size() > 0) {
                serverInfo.ball.movement.set(-1 * serverInfo.ball.movement.getX(), serverInfo.ball.movement.getY());
                serverInfo.score1++;
                state = "countdown";
            }

            ///////Check if ball hits ceiling or floor and change balls vertical movement

            if (serverInfo.ball.y < 0) {
                serverInfo.ball.movement.set(serverInfo.ball.movement.getX(), -1 * serverInfo.ball.movement.getY());
            }
            if (serverInfo.ball.y + (2 * serverInfo.ball.radius) > gc.getHeight()) {
                serverInfo.ball.movement.set(serverInfo.ball.movement.getX(), -1 * serverInfo.ball.movement.getY());
            }

            ////////Update Ball Position based on its movement vectors

            serverInfo.ball.update(gc, i);

            //////////////Check Collision and Change Ball Direction Angle Depending on where it hits the paddle

            if (collision(serverInfo.paddle1, serverInfo.ball) && !paddle1Hit) {
                float collisionAngle = calculateAngle(serverInfo.paddle1, serverInfo.ball);
                if (serverInfo.ball.movement.getY() > 0) {
                    if (collisionAngle < 0) {
                        collisionAngle = Math.max(-60, collisionAngle);
                        serverInfo.ball.setMovementAngle(-collisionAngle);
                    } else {
                        collisionAngle = Math.min(60, collisionAngle);
                        serverInfo.ball.setMovementAngle(collisionAngle);
                    }
                } else {
                    if (collisionAngle < 0) {
                        collisionAngle = Math.max(-60, collisionAngle);
                        serverInfo.ball.setMovementAngle(collisionAngle);
                    } else {
                        collisionAngle = Math.min(60, collisionAngle);
                        serverInfo.ball.setMovementAngle(-collisionAngle);
                    }
                }
                /////Increase Ball speed
                if (serverInfo.ball.magnitude < 10) {
                    serverInfo.ball.movement.scale((float) 1.1);
                }
                /////Deactive paddle hit and reactivate other paddle. Makes sure ball doesn't get stuck in paddle
                paddle1Hit = true;
                paddle2Hit = false;
            }

            if (collision(serverInfo.paddle2, serverInfo.ball) && !paddle2Hit) {
                float collisionAngle = calculateAngle(serverInfo.paddle2, serverInfo.ball);
                if (serverInfo.ball.movement.getY() > 0) {
                    if (collisionAngle < 0) {
                        collisionAngle = Math.min(-120, collisionAngle);
                        serverInfo.ball.setMovementAngle(-collisionAngle);
                    } else {
                        collisionAngle = Math.max(120, collisionAngle);
                        serverInfo.ball.setMovementAngle(collisionAngle);
                    }
                } else {
                    if (collisionAngle < 0) {
                        collisionAngle = Math.min(-120, collisionAngle);
                        serverInfo.ball.setMovementAngle(collisionAngle);
                    } else {
                        collisionAngle = Math.max(120, collisionAngle);
                        serverInfo.ball.setMovementAngle(-collisionAngle);
                    }
                }
                //////////Increase ball speed;
                if (serverInfo.ball.magnitude < 5) {
                    serverInfo.ball.movement.scale((float) 1.1);
                }
                /////////Deactivate hit paddle and reactivate other paddle. Makes sure ball doesn't get stuck in paddle
                paddle1Hit = false;
                paddle2Hit = true;
            }
            if(serverInfo.score1 >= 5 && serverInfo.score1 - serverInfo.score2 >= 2){
                state = "end";
                serverInfo.winner = "1";
            }else if(serverInfo.score2 >= 5 && serverInfo.score2 - serverInfo.score1 >= 2){
                state = "end";
                serverInfo.winner = "" + clientList.size();
            }
        }else if(state.equals("countdown")){
            //resets ball position
            state = "playing";
            serverInfo.ball.x = (gc.getWidth() * (clientList.size()))/2;
            serverInfo.ball.y = gc.getHeight() / 2;
            serverInfo.ball.movement = new Vector2f(2,0);
            paddle1Hit = false;
            paddle2Hit = false;
        }else if (state.equals("end")){
                serverInfo.ball.x = (gc.getWidth() * (clientList.size()))/2;
                serverInfo.ball.y = gc.getHeight() / 2;
                serverInfo.ball.movement = new Vector2f(0,0);
        }

        ///////Send Info to Client
        serverInfo.paddle2.x = gameContainerWidth*clientList.size() - 10 - paddleWidth;
        sendToAll(serverInfo);
    }

    public void render(GameContainer gc, Graphics g){

    }

    private class ConnectionToClient {
        ObjectInputStream in; //get stuff from Client
        ObjectOutputStream out; //send stuff to Client
        Socket socket; //Connection between Client and Server

        ConnectionToClient(final Socket socket) throws IOException {
            this.socket = socket;
            out = new ObjectOutputStream(socket.getOutputStream());

            Thread read = new Thread(){
                public void run(){ //keep checking if client has written something to inputstream
                    while(true){
                        try{
                            if(in == null){in = new ObjectInputStream(socket.getInputStream());}
                            Object obj = in.readObject();
                            messages.put(obj);
                        }
                        catch(IOException e){
                            e.printStackTrace();
                            sendToAll("terminate");
                            System.exit(0);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            sendToAll("terminate");
                            System.exit(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            sendToAll("terminate");
                            System.exit(0);
                        }
                    }
                }
            };

            //read.setDaemon(true); // terminate when main ends
            read.start();
        }

        //Send message to Client
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

    //Checks for collisions between axis alligned rectangle and circle
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

    public float calculateAngle(Paddle paddle, Ball ball){
        float paddleCX = paddle.x + (paddle.width/2);
        float paddleCY = paddle.y + (paddle.height/2);
        float ballCX = ball.x + ball.radius;
        float ballCY = ball.y + ball.radius;
        return (float) Math.toDegrees((double)Math.atan2(ballCY - paddleCY, ballCX - paddleCX));
    }

}