package com.conjaura;

import java.util.ArrayList;
import java.util.Collections;

public class Panel {
    static int index = 0;
    public static ArrayList<Panel> panels = new ArrayList<Panel>();

    int id;
    byte width;
    byte height;
    ScanLines scanLines;
    PanelOrientation orientation;
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
    byte[] ledData;
    byte[] edgeData;
    ArrayList<Byte> touchData;
    ArrayList<Byte> peripheralData;

    public Panel(byte w, byte h){
        id = index;
        orientation = PanelOrientation.UP;
        throttle = PanelLedThrottle.NONE;
        peripheralType = PeripheralTypes.NONE;
        scanLines = ScanLines.SCAN_LINES8;
        edgeActive = false;
        ledActive = false;
        touchEnabled = false;
        touchChannelsFlag = 0;
        touchChannels = 0;
        dataLength = 0;
        setSize(w,h);
        panels.add(this);
        index++;
    }

    public static Panel getPanel(int id){
        return panels.get(id);
    }


    public void setSize(byte w, byte h){
        width = w;
        height = h;
        System.out.println(w+" "+h);
        calcDataSize();
    }

    public void disableLeds(){ledActive = false;}

    public void enableLeds(){ledActive = true;}

    public void setOrientation(PanelOrientation orient){orientation = orient;}

    public void setScanLines(ScanLines scan){scanLines = scan;}

    public void setThrottle(PanelLedThrottle throt){throttle = throt;}

    public void setTouch(boolean enable, byte channels, TouchSensitivity sensitivity){
        touchEnabled = enable;
        if(channels == 16){
            touchChannelsFlag = 0;
        }
        touchChannels = channels;       //0=w*h/4 - only option for now
        touchDataSize = sensitivity;
    }

    public void setEdge(boolean enable, EdgeLedThrottle throttle, EdgeLedDensity density){
        edgeActive = enable;
        edgeThrottle = throttle;
        edgeDensity = density;
        calcDataSize();
    }

    public void disableEdge(){edgeActive = false;}

    public void setPeripheral(PeripheralTypes type, byte settings, byte size){
        peripheralType = type;
        peripheralSettings = settings;
        peripheralReturnSize = size;
    }

    private void calcDataSize(){
        byte pixelDataSize;
        int dataSize;
        int edgeSize = 0;

        if(ColourConf.getColourMode()==ColourModes.TRUE_COLOUR){
            pixelDataSize=3;
        }
        else if(ColourConf.getColourMode()==ColourModes.HIGH_COLOUR){
            pixelDataSize=2;
        }
        else{
            pixelDataSize = 1;
        }

        dataSize = (width*height)*pixelDataSize;
        ledData = new byte[dataSize];//new ArrayList<Byte>((Collections.nCopies(dataSize,(byte)0)));
        if(edgeActive==true){
            int multiple = 0;
            if(edgeDensity==EdgeLedDensity.THREE_PER_EIGHT){
                multiple = 3;
            }
            else if(edgeDensity==EdgeLedDensity.THREE_PER_EIGHT){
                multiple = 6;
            }
            edgeSize = ((((width * 2) + (height *2)) / 8) * multiple)*pixelDataSize;
            //edgeData = new ArrayList<Byte>((Collections.nCopies(edgeSize,(byte)0)));
            edgeData = new byte[edgeSize];
        }
        dataLength = dataSize+edgeSize;
        //System.out.println("PANEL "+id+" Size:"+dataLength);
    }

    public static void setData(byte[] data, int id){panels.get(id).ledData = data;}

}
