package com.conjaura;

class Init{
    public static Init instance;

    private static ConjauraSetup config;
    private static ColourConf colourSetup;
    private static Segment segments;
    private static IO dataIO;
    public static DataHandler dataHandler;

    public static void main( String[] args )
    {
        instance = new Init();
    }

    Init(){
        //dataIO = new IO();
        config = new ConjauraSetup(16);
        config.setColourMode(ColourModes.TRUE_COLOUR);
        config.setBam(BamBitSize.BAM_8BIT);
        config.setScan(ScanLines.SCAN_LINES8);

        for(int i=0;i< config.panelCount;i++){
            Panel thisPanel = new Panel((byte)16,(byte)16);
            //config.setPanel(thisPanel, i);


            thisPanel.enableLeds();
            if(i==0){
                thisPanel.setTouch(true,(byte)16,TouchSensitivity.DATA_8BIT);
            }
            if(i==0 || i==1){
                thisPanel.setEdge(true, EdgeLedThrottle.NONE, EdgeLedDensity.THREE_PER_EIGHT);
            }
        }
        //System.out.println("TC "+config.panels[0].dataLength);
        colourSetup = new ColourConf();
        colourSetup.dummyGamma();
        segments = new Segment();
        segments.createSegments();
        System.out.println("SEG "+segments.getSegmentLength(0));
        System.out.println("SEG "+segments.getSegmentLength(1));


        segments.createSegmentData();
        System.out.println("SEG "+segments.getSegmentData(0)+", "+segments.getSegmentLength(0));
        System.out.println("SEG "+segments.getSegmentData(1)+", "+segments.getSegmentLength(1));

        dataHandler.buildConfig();

        byte test = (byte)(254&0xFF);
        System.out.println("TEST "+(test));

    }

    public static ConjauraSetup getGlobalConfig(){
        return config;
    }

}
