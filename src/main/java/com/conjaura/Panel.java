package com.conjaura;

import java.util.ArrayList;
import java.util.Collections;

public class Panel {
    static int index = 0;
    private static ArrayList<Panel> panels = new ArrayList<Panel>();

    int id;
    byte width;
    byte height;
    PanelOrientation orientation;
    byte scanLines;
    boolean ledActive;
    PanelLedThrottle throttle;
    boolean touchEnabled;
    byte touchChannelsFlag;
    byte touchChannels;
    TouchSensitivity touchDataSize;
    boolean edgeActive;
    EdgeLedThrottle edgeThrottle;
    EdgeLedDensity edgeDensity;
    PeripheralTypes peripheralType;
    byte peripheralSettings;
    byte peripheralReturnSize;
    int dataLength;
    ArrayList<Byte> ledData;
    ArrayList<Byte> edgeData;
    ArrayList<Byte> touchData;
    ArrayList<Byte> peripheralData;

    static ConjauraSetup config = Init.getGlobalConfig();

    public Panel(byte w, byte h){
        id = index;
        orientation = PanelOrientation.UP;
        throttle = PanelLedThrottle.NONE;
        peripheralType = PeripheralTypes.NONE;
        scanLines = 8;
        dataLength = 0;
        setSize(w,h);
        enableLeds();
        panels.add(this);
        index++;
    }

    public static Panel getPanel(int id){
        return panels.get(id);
    }


    public void setSize(byte w, byte h){
        width = w;
        height = h;
        calcDataSize();
    }

    public void disableLeds(){
        ledActive = false;
    }

    public void enableLeds(){
        ledActive = true;
    }

    public void setTouch(boolean enable, byte channels, TouchSensitivity sensitivity){
        touchEnabled = enable;
        int ch = 0;
        touchChannelsFlag = channels;
        if(channels == 0){
            ch = ((width / 4) *(height / 4));
        }
        touchChannels = (byte)ch;       //0=w*h/4 - only option for now
        touchDataSize = sensitivity;
    }

    public void setEdge(boolean enable, EdgeLedThrottle throttle, EdgeLedDensity density){
        edgeActive = enable;
        edgeThrottle = throttle;
        edgeDensity = density;
        calcDataSize();
    }

    public void setPeripheral(PeripheralTypes type, byte settings, byte size){
        peripheralType = type;
        peripheralSettings = settings;
        peripheralReturnSize = size;
    }

    private void calcDataSize(){
        byte pixelDataSize;
        int dataSize;
        int edgeSize = 0;
        if(config.colourMode==ColourModes.TRUE_COLOUR){
            pixelDataSize=3;
        }
        else if(config.colourMode==ColourModes.HIGH_COLOUR){
            pixelDataSize=2;
        }
        else{
            pixelDataSize = 1;
        }
        dataSize = (width*height)*pixelDataSize;
        ledData = new ArrayList<Byte>((Collections.nCopies(dataSize,(byte)0)));
        System.out.println("LEDSIZE "+ledData.size());
        if(edgeActive==true){
            int multiple = 0;
            if(edgeDensity==EdgeLedDensity.THREE_PER_EIGHT){
                multiple = 3;
            }
            else if(edgeDensity==EdgeLedDensity.THREE_PER_EIGHT){
                multiple = 6;
            }
            edgeSize = ((((width * 2) + (height *2)) / 8) * multiple)*pixelDataSize;
            edgeData = new ArrayList<Byte>((Collections.nCopies(edgeSize,(byte)0)));
        }
        dataLength = dataSize+edgeSize;
        System.out.println("PANEL "+id+" Size:"+dataLength);
    }

}
