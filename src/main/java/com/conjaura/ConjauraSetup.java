package com.conjaura;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class ConjauraSetup {

    ColourConf colourSetup;
    DataHandler dataHandler;
    private static int maxFPS;
    private static BamBitSize bamBits;

    ConjauraSetup(){
        colourSetup = new ColourConf();
        dataHandler = new DataHandler(this);

        JSONParser jsonParser = new JSONParser();
        try (FileReader fileContents = new FileReader("config.json"))
        {
            System.out.println("Loaded Config");
            Object obj = jsonParser.parse(fileContents);
            JSONObject jsonFile = (JSONObject) obj;
            JSONObject setup = (JSONObject) jsonFile.get("conjauraSetup");
            JSONObject colourSetup = (JSONObject) jsonFile.get("colourSetup");
            JSONObject config = (JSONObject) setup.get("config");
            JSONArray panels = (JSONArray) setup.get("panels");

            //SET FPS AND BAM MODES
            confParseConfigSection(config);
            //SET COLOUR MODE, GAMMA LOOKUP, PALETTE LOOKUP AND HCBIAS
            confParseColourSection(colourSetup);
            //CREATE PANELS AND CONFIGURE LED STATES, EDGE STATES, TOUCH AND PERIPH STATES
            confParsePanelsMain(panels);
            //CREATE OUR DATA HANDLER AND PREP OUR SEGMENTS FROM OUR PANEL DATA
            dataHandler.createSegments();

        } catch (FileNotFoundException e) {
            System.out.println("File not found error");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO 1Exception error");
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Exception error");
            e.printStackTrace();
        }
    }

    BamBitSize getBamBits(){return bamBits;}

    private void confParseConfigSection(JSONObject configJSON){
        //SET FPS LIMITER
        maxFPS = (int)(long)configJSON.get("maxFPS");

        //SET BAM MODE:
        if((long)configJSON.get("bamBits") == 5){
            bamBits = BamBitSize.BAM_5BIT;
        }
        else if((long)configJSON.get("bamBits") == 6){
            bamBits = BamBitSize.BAM_6BIT;
        }
        else if((long)configJSON.get("bamBits") == 7){
            bamBits = BamBitSize.BAM_7BIT;
        }
        else{
            bamBits = BamBitSize.BAM_8BIT;
        }
    }

    private void confParseColourSection(JSONObject colourJSON){
        //CONFIGURE OUR COLOUR MODES
        if(colourJSON.get("colourMode").equals("TRUECOLOUR")) {
            colourSetup.setColourMode(ColourModes.TRUE_COLOUR);
        }
        else if(colourJSON.get("colourMode").equals("HIGHCOLOUR")) {
            colourSetup.setColourMode(ColourModes.HIGH_COLOUR);
            if(colourJSON.get("hcBias").equals("EVEN")) {
                colourSetup.setHcBias(HighColourBias.EVEN);
            }
            else if(colourJSON.get("hcBias").equals("GREEN")) {
                colourSetup.setHcBias(HighColourBias.BLUE_BIAS);
            }
            else if(colourJSON.get("hcBias").equals("RED")) {
                colourSetup.setHcBias(HighColourBias.RED_BIAS);
            }
            else{
                colourSetup.setHcBias(HighColourBias.GREEN_BIAS);
            }
        }
        else if(colourJSON.get("colourMode").equals("PALETTECOLOUR")){
            //CREATE PALETTE ARRAY IF IN PALETTE MODE
            colourSetup.setColourMode(ColourModes.PALETTE_COLOUR);
            JSONArray jsonPalArray = (JSONArray)colourJSON.get("palette");
            try {
                colourSetup.setPalette(jsonPalArray);
            }
            catch(IllegalArgumentException ex){
                System.out.println(ex.getMessage());
            }
        }
        //CONFIGURE/CREATE OUR GAMMA ARRAYS
        try {
            colourSetup.setGamma((JSONObject) colourJSON.get("gamma"));
        }
        catch(IllegalArgumentException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void confParsePanelsMain(JSONArray panelJSON){
        for(int i=0;i<panelJSON.size();i++){
            JSONObject panelInfo = (JSONObject)panelJSON.get(i);
            JSONObject ledInfo = (JSONObject)panelInfo.get("ledState");
            JSONObject touchInfo = (JSONObject)panelInfo.get("touchState");
            JSONObject edgeInfo = (JSONObject)panelInfo.get("edgeState");
            JSONObject peripheralInfo = (JSONObject)panelInfo.get("peripheralState");

            //CREATE PANEL OBJECT WITH DIMENSIONS AND COLOUR MODE
            Long w = (Long)panelInfo.get("width");
            Long h = (Long)panelInfo.get("height");
            Panel thisPanel = new Panel(w.byteValue(),h.byteValue(),colourSetup.getColourMode());

            //SET ORIENTATION
            if(panelInfo.get("orientation").equals("DOWN")){
                thisPanel.setOrientation(PanelOrientation.DOWN);
            }
            else if(panelInfo.get("orientation").equals("LEFT")){
                thisPanel.setOrientation(PanelOrientation.LEFT);
            }
            else if(panelInfo.get("orientation").equals("RIGHT")){
                thisPanel.setOrientation(PanelOrientation.RIGHT);
            }
            else{
                thisPanel.setOrientation(PanelOrientation.UP);
            }

            //SET SCANLINES
            if((long)panelInfo.get("scanLines")!=8) {
                thisPanel.setScanLines(ScanLines.SCAN_LINES16);
            }
            else{
                thisPanel.setScanLines(ScanLines.SCAN_LINES8);
            }

            //SET LED STATES
            if((boolean)ledInfo.get("active")){
                thisPanel.enableLeds();
                if(ledInfo.get("throttle").equals("50PERCENT")){
                    thisPanel.setThrottle(PanelLedThrottle.FIFTY_PERCENT);
                }
                else if(ledInfo.get("throttle").equals("25PERCENT")){
                    thisPanel.setThrottle(PanelLedThrottle.TWENTY_FIVE_PERCENT);
                }
                else{
                    thisPanel.setThrottle(PanelLedThrottle.NONE);
                }
            }
            else{
                thisPanel.disableLeds();
            }

            //SET EDGE STATES
            if((boolean)edgeInfo.get("active")){
                EdgeLedThrottle throttle;
                EdgeLedDensity density;
                if(edgeInfo.get("throttle").equals("50PERCENT")){
                    throttle = EdgeLedThrottle.FIFTY_PERCENT;
                }
                else{
                    throttle = EdgeLedThrottle.NONE;
                }
                if(edgeInfo.get("density").equals("6PER8")){
                    density = EdgeLedDensity.SIX_PER_EIGHT;
                }
                else{
                    density = EdgeLedDensity.THREE_PER_EIGHT;
                }
                thisPanel.setEdge(throttle, density);
            }
            else{
                thisPanel.disableEdge();
            }

            //SET TOUCH STATES
            if((boolean)touchInfo.get("active")){
                TouchSensitivity touchSens;
                byte channels = (byte)(long)touchInfo.get("channels");
                if(touchInfo.get("sensitivity").equals("8BIT")){
                    touchSens = TouchSensitivity.DATA_8BIT;
                }
                else{
                    touchSens = TouchSensitivity.DATA_4BIT;
                }
                thisPanel.setTouch(channels,touchSens);
            }
            else{
                thisPanel.disableTouch();
            }

            //SET PERIPHERAL STATES

            PeripheralTypes type = PeripheralTypes.NONE;
            if(peripheralInfo.get("type").equals("MIC")){
                type = PeripheralTypes.MICROPHONE;
            }
            else if(peripheralInfo.get("type").equals("LIGHT")){
                type = PeripheralTypes.LIGHTSENSOR;
            }
            thisPanel.setPeripheral(type, (byte)(long)peripheralInfo.get("settings"),
                    (byte)(long)peripheralInfo.get("returnSize"));


            //ADD PANEL TO OUR LIST
            dataHandler.panels.add(thisPanel);

        }
    }
}
