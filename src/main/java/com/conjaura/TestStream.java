package com.conjaura;


class TestStream {
    private ConjauraSetup parentConfig;
    TestStream(ConjauraSetup config){
        parentConfig = config;
    }

    void run(){
        /*START OF STREAM*/
        /*
        try {
            Thread.sleep(500);
        }
        catch(InterruptedException e){
            System.out.println( e.getMessage());
        }
        */
        parentConfig.dataHandler.initDisplay();
        System.out.println( "Stream start");
        int loopsToRun = 2000;
        int rStart = 0;
        //int gStart = 767;
        int loopsForSpeed = 100;
        long start = System.currentTimeMillis();
        int tallyBytes = 0;
        byte[] panelData = new byte[768];//ArrayList<Byte>((Collections.nCopies(768,(byte)1)));
        for(int loops=0;loops<loopsToRun;loops++){

            for(int r=0;r<256;r++){
                //panelData.set(rStart,(byte)(r));
                panelData[rStart] = (byte)r;
                rStart+=3;
                if(rStart>765){
                    rStart=0;
                }
            }

            rStart+=3;
            if(rStart>765){
                rStart=0;
            }

            for(Panel thisPanel : parentConfig.dataHandler.panels){
                thisPanel.setData(panelData);
            }

            tallyBytes += parentConfig.dataHandler.transferFrame();
            loopsForSpeed--;
            if(loopsForSpeed==0){
                long end = System.currentTimeMillis();
                float timeLen = (end-start) / 1000F;
                int bytesPerSecond = (int)(tallyBytes/timeLen);
                System.out.println(tallyBytes+" Bytes in "+timeLen+"seconds ("+(bytesPerSecond/1000)+"KB per second)");
                loopsForSpeed = 100;
                tallyBytes = 0;
                start = System.currentTimeMillis();
            }
        }
    }
}
