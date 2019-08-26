package com.conjaura;

public class ConjauraSetup {

    static int panelCount;
    ColourModes colourMode;
    HighColourBias hcBias;
    short paletteSize;
    short gammaSize;
    BamBitSize bamBits;
    ScanLines scanLines;

    byte totalSegment;
    byte currentSegment;
    int currentSegmentSize;
    byte lastSegmentStartPanel;
    byte lastSegmentEndPanel;
    String lastError;

    public ConjauraSetup(int panelCnt){
        panelCount = panelCnt;
    }

    public void getPanel(int id){
        //return panels[id];
    }



    public void setColourMode(ColourModes mode){
        colourMode = mode;
    }

    public void setColourMode(ColourModes mode, HighColourBias bias){
        colourMode = mode;
        if(colourMode == ColourModes.HIGH_COLOUR){
            setBiasMode(bias);
        }
    }

    public void setBiasMode(HighColourBias bias){
        boolean statusOK = true;
        if(bamBits.ordinal() >= 0 && bamBits.ordinal()<4){
            hcBias = bias;
        }
        else{
            statusOK = false;
            lastError = "Invalid HC BIAS";
        }
    }

    public void setBam(BamBitSize bam){
        boolean statusOK = true;
        if(bam.ordinal() >= 0 && bam.ordinal()<4){
            bamBits = bam;
            lastError = "";
        }
        else{
            statusOK = false;
            lastError = "Invalid BAM Rate";
        }
    }

    public void setScan(ScanLines scan){
        boolean statusOK = true;
        if(scan.ordinal() >= 0 && scan.ordinal()<2){
            scanLines = scan;
            lastError = "";
        }
        else{
            statusOK = false;
            lastError = "Invalid SCANLINES";
        }
    }

    public void setPalette(short size, byte[] data){
        boolean statusOK = true;
        if((size+1) % 8 == 0){
            paletteSize = size;
            if(data.length>0){
                if(data.length/3 == (size+1)){
                    ColourConf.paletteData = data;
                    //colourSetup.
                }
                else{
                    statusOK = false;
                    lastError = "Invalid palette data size";
                }
            }

        }
        else{
            statusOK = false;
            lastError = "Invalid palette size";
        }
    }


}
