package com.conjaura;


import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;

public class ConjauraServer extends WebSocketServer {
    SocketPermission p1,p2;
    TestStream testStream = new TestStream();
    public ConjauraServer( int port ) throws IOException {
        super( new InetSocketAddress( port ) );

        System.out.println(InetAddress.getLocalHost().getHostName());
        p1 = new SocketPermission(InetAddress.getLocalHost().getHostName()+":"+port, "connect,accept,listen,resolve");
        p2 = new SocketPermission(InetAddress.getLocalHost().getHostName()+":80", "connect,accept,listen,resolve");
        ServerSocket serverSocket = null;
        try {
            System.out.println(port);
            serverSocket = new ServerSocket(); // <-- create an unbound socket first
            serverSocket.bind(new InetSocketAddress("localhost",port));
            //serverSocket.bind(port);
            // Do your server stuff
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("Fail");
            }
        }

    }

    public ConjauraServer( InetSocketAddress address ) {
        super( address );
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake ) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        broadcast( conn + " has left the room!" );
        System.out.println( conn + " has left the room!" );
    }

    @Override
    public void onMessage( WebSocket conn, String message ){
        broadcast( message );
        System.out.println( conn + ": " + message );
        if( message.equals( "exit" ) ) {
            System.out.println( "Exit");
            try{
                Init.s.stop(1000);
            }
            catch(InterruptedException e){}
        }
        if( message.equals( "run" ) ) {
            //CREATE OUR DATA HANDLER OBJECT AND BEGIN OUR INIT ROUTINE
            System.out.println( "Run routine");
            testStream.run();
        }
    }
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        broadcast( message.array() );
        System.out.println( conn + ": " + message );
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

}