package com.conjaura;
import java.io.*;

import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

public class IO {
    static final int SPI_SPEED = 16000000;

    final GpioController gpio = GpioFactory.getInstance();
    public static SpiDevice spi = null;

    private static GpioPinDigitalOutput ledRed;
    private static GpioPinDigitalOutput ledGreen;
    private static GpioPinDigitalOutput ledBlue;

    private static GpioPinDigitalOutput pwr;
    private static GpioPinDigitalOutput fan;
    private static GpioPinDigitalOutput rst;

    private static GpioPinDigitalOutput sigOut;
    private static GpioPinDigitalInput sigIn;

    public IO(){
        try {
            spi = SpiFactory.getInstance(SpiChannel.CS0, SPI_SPEED, SpiMode.MODE_3);
        }
        catch(IOException e){

        }

        ledRed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05,"Red", PinState.HIGH);
        ledGreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02,"Green", PinState.HIGH);
        ledBlue = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00,"Blue", PinState.HIGH);

        pwr = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03,"Power", PinState.LOW);
        fan = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07,"Fan", PinState.LOW);
        rst = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22,"Reset", PinState.HIGH);

        sigOut = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25,"SigOut", PinState.LOW);
        sigIn = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29,"SigIn", PinPullResistance.PULL_DOWN);

        setPanelPower("off");
        setFan("off");
        setLed("off");
    }

    public static void setPanelPower(String state){
        if(state.equals("on")){
            pwr.setState(PinState.HIGH);
        }
        else{
            pwr.setState(PinState.LOW);
        }
    }

    public static void setFan(String state){
        if(state.equals("on")){
            fan.setState(PinState.HIGH);
        }
        else{
            fan.setState(PinState.LOW);
        }
    }

    public static void resetMCU(){
        try {
            rst.setState(PinState.LOW);
            Thread.sleep(500);
            rst.setState(PinState.HIGH);
        }
        catch(InterruptedException e){

        }
    }

    public static void pingMCU(){
        sigOut.setState(PinState.HIGH);
        //Thread.sleep(500);
        sigOut.setState(PinState.LOW);
    }

    public static void haltTilReady(){
        while(!sigIn.isHigh()){
            setLed("red");
        }
        setLed("off");
    }

    public static void setLed(String colour){
        ledRed.setState(PinState.HIGH);
        ledBlue.setState(PinState.HIGH);
        ledGreen.setState(PinState.HIGH);

        if(colour.equalsIgnoreCase("red")) {
            ledRed.setState(PinState.LOW);
        }
        else if(colour.equalsIgnoreCase("green")) {
            ledGreen.setState(PinState.LOW);
        }
        else if(colour.equalsIgnoreCase("blue")) {
            ledBlue.setState(PinState.LOW);
        }
        else if(colour.equalsIgnoreCase("yellow")) {
            ledGreen.setState(PinState.LOW);
            ledRed.setState(PinState.LOW);
        }
        else if(colour.equalsIgnoreCase("magenta")) {
            ledRed.setState(PinState.LOW);
            ledBlue.setState(PinState.LOW);
        }
        else if(colour.equalsIgnoreCase("white")) {
            ledRed.setState(PinState.LOW);
            ledGreen.setState(PinState.LOW);
            ledBlue.setState(PinState.LOW);
        }
    }

    public static byte[] spiTransfer(byte data[]){
        try {
            byte[] result = spi.write(data);
            return result;
        }
        catch(IOException e){
            return data;
        }
    }


}
