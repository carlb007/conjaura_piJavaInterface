package com.conjaura;

import java.io.IOException;

class Init{
    //public static Init instance;
    //private static ConjauraSetup config;
    static ConjauraServer s;

    public static void main( String[] args ) throws IOException
    {
        //instance = new Init();
        ConjauraSetup config = new ConjauraSetup();

        int port = 8887; // 843 flash policy port
        s = new ConjauraServer( port, config );
        s.start();
        System.out.println( "ChatServer started on port: " + s.getPort() );
/*
        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
            //s.broadcast( in );
            if( in.equals( "exit" ) ) {
                System.out.println( "Exit2");
                s.stop(1000);
                break;
            }
            if( in.equals( "run" ) ) {
                //CREATE OUR DATA HANDLER OBJECT AND BEGIN OUR INIT ROUTINE
                System.out.println( "Run routine2");
                ///dataHandler = new DataHandler();
                //new TestStream();
            }
        }
        */
    }

    //Init() throws InterruptedException , IOException {


   // }
}
