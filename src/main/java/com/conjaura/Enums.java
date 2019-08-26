package com.conjaura;

enum TouchSensitivity{
    DATA_4BIT,
    DATA_8BIT
}

enum EdgeLedThrottle{
    NONE,
    FIFTY_PERCENT
}

enum EdgeLedDensity{
    THREE_PER_EIGHT,
    SIX_PER_EIGHT
}

enum PanelLedThrottle{
    NONE,
    FIFTY_PERCENT,
    TWENTY_FIVE_PERCENT
}

enum PanelOrientation{
    UP,
    DOWN,
    LEFT,
    RIGHT
}

enum PeripheralTypes{
    NONE,
    MICROPHONE,
    LIGHTSENSOR
}

enum ColourModes{
    TRUE_COLOUR,    //24 BIT
    HIGH_COLOUR,    //15-16 BIT
    PALETTE_COLOUR  //8 BIT
}

enum HighColourBias{
    GREEN_BIAS,
    RED_BIAS,
    BLUE_BIAS,
    EVEN
}

enum BamBitSize{
    BAM_5BIT,
    BAM_6BIT,
    BAM_7BIT,
    BAM_8BIT
}

enum ScanLines{
    SCAN_LINES8,
    SCAN_LINES16
}