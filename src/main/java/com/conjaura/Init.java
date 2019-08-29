package com.conjaura;

import java.util.ArrayList;
import java.util.Collections;

class Init{
    public static Init instance;
    public static ConjauraSetup config;

    public static void main( String[] args )
    {
        instance = new Init();
    }

    Init(){
        config = new ConjauraSetup();
        new TestStream();
    }
}
