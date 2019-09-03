package com.conjaura;

import java.util.ArrayList;

class Panel {


    byte width;
    byte height;
    ScanLines scanLines;
    PanelOrientation orientation;
    boolean ledActive;
    private ColourModes panelColourMode;
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

    Panel(byte w, byte h, ColourModes cMode){
        orientation = PanelOrientation.UP;
        throttle = PanelLedThrottle.NONE;
        peripheralType = PeripheralTypes.NONE;
        scanLines = ScanLines.SCAN_LINES8;
        edgeActive = false;
        ledActive = false;
        touchEnabled = false;
        dataLength = 0;
        panelColourMode = cMode;
        setSize(w,h);
        //panels.add(this);
    }

    private void setSize(byte w, byte h){
        width = w;
        height = h;
        calcDataSize();
    }

    void disableLeds(){ledActive = false;}

    void enableLeds(){ledActive = true;}

    void setOrientation(PanelOrientation orient){orientation = orient;}

    void setScanLines(ScanLines scan){scanLines = scan;}

    void setThrottle(PanelLedThrottle throt){throttle = throt;}

    void setTouch(byte channels, TouchSensitivity sensitivity){
        touchEnabled = true;
        if(channels == 16){
            touchChannelsFlag = 0;
        }
        touchChannels = channels;       //0=w*h/4 - only option for now
        touchDataSize = sensitivity;
    }

    void disableTouch(){touchEnabled = false;}

    void setEdge(EdgeLedThrottle throttle, EdgeLedDensity density){
        edgeActive = true;
        edgeThrottle = throttle;
        edgeDensity = density;
        calcDataSize();
    }

    void disableEdge(){edgeActive = false;}

    void setPeripheral(PeripheralTypes type, byte settings, byte size){
        peripheralType = type;
        peripheralSettings = settings;
        peripheralReturnSize = size;
    }

    private void calcDataSize(){
        byte pixelDataSize;
        int dataSize;
        int edgeSize = 0;

        if(panelColourMode==ColourModes.TRUE_COLOUR){
            pixelDataSize=3;
        }
        else if(panelColourMode==ColourModes.HIGH_COLOUR){
            pixelDataSize=2;
        }
        else{
            pixelDataSize = 1;
        }
        dataSize = (width*height)*pixelDataSize;
        ledData = new byte[dataSize];
        if(edgeActive){
            int multiple = 0;
            if(edgeDensity==EdgeLedDensity.THREE_PER_EIGHT){
                multiple = 3;
            }
            else if(edgeDensity==EdgeLedDensity.SIX_PER_EIGHT){
                multiple = 6;
            }
            edgeSize = ((((width * 2) + (height *2)) / 8) * multiple)*pixelDataSize;
            edgeData = new byte[edgeSize];
        }
        dataLength = dataSize+edgeSize;
    }

    void setData(byte[] data){ledData = data;}

}
