#include <ArduinoBLE.h>

/*
     For timer interruption
*/
#define TIMER_INTERRUPT_DEBUG         0
#define _TIMERINTERRUPT_LOGLEVEL_     0
#include "NRF52_MBED_TimerInterrupt.h"
NRF52_MBED_Timer ITimer(NRF_TIMER_3);

/*
    For multi-task
*/
#include <Scheduler.h>

/*
    For EMS
*/
#include "pinNames.h"
#include "WEMS_PulseControl.h"
EMSControl* ems;

int drum_note;
int drum_stim_flag;

/*
    For BLE
*/
BLEDevice central;
BLEService myService("19b10000-e9f3-537e-4f6c-d104768a1214");
//BLEStringCharacteristic intensityCharacteristic("19b10001-e9f3-537e-4f6c-d104768a1214", BLEWrite | BLERead, 20);
//BLEStringCharacteristic pulseWidthCharacteristic("19b10002-e9f3-537e-4f6c-d104768a1214", BLEWrite | BLERead, 20);
//BLEStringCharacteristic frequencyCharacteristic("19b10003-e9f3-537e-4f6c-d104768a1214", BLEWrite | BLERead, 20);
BLEStringCharacteristic startCharacteristic("19b10005-e9f3-537e-4f6c-d104768a1214", BLEWrite | BLERead, 200);

void led_blink(void);
void bat_read(void);
float vol_bat;

int thumbChannel = 0;
int indexChannel = 0;
int middleChannel = 0;
int wristChannel = 0;

int thumbIntensity = 0;
int indexIntensity = 0;
int middleIntensity = 0;
int wristIntensity = 0;

int currentChannel = 0;
int currentIntensity = 0;

/*  --------------------------------------------------------------------
    Command list

    commands is supposed to be received via Serial or BLE
    --------------------------------------------------------------------  */
void execCommand(String command)
{
    /*
        Start/Stop the stimulation with infinit duration
    */
    if (command == "a" && ems->is_stimulating == 0) {
        ems->setStimDuration_inf();
        ems->start();
    } else if (command == "a" && ems->is_stimulating == 1) {
        ems->stop();
    }
    /*
        Start the stimulation with infinit duration. 
        This is the same fuction as "a" when stimulation is not started.
        This will not stop stimulation as "a". 
    */
    else if (command.startsWith("start")) {
        ems->setStimDuration_inf();
        ems->start();
    }
    /*
        Start the stimulation.
           If an integer argument is followed, it is used to define the stimulation duration in the same way as the command "dur millis"
           If no argument is followed, the stimulation duration is set at 0, which means stimulation lasts until the commands "stop"/"a" are sent.

           ex1. stim1000 is sent, stimulation lasts for 1 second (1000 ms)
           ex2. stim (or stim0) are sent, the last duration is kept.
    */
    else if (command.startsWith("stim")) {
        //set stimulation duration [ms]
        int tmp = command.substring(4).toInt();
        ems->setStimDuration_us(tmp * 1000);
        ems->start();
    }

    else if (command.startsWith("thumbSet")) {
        //set stimulation duration [ms]

        if (command.length()>8){

          String tmp = command.substring(8);

          int loc = tmp.indexOf(",");
          thumbChannel = tmp.substring(0,loc).toInt();
          thumbIntensity = tmp.substring(loc+1).toInt();
          Serial.print("tch: "+thumbChannel);
        }else{
          thumbChannel = currentChannel;
          thumbIntensity  = currentIntensity;
        }
        // ems->setStimDuration_us(tmp * 1000);
        // ems->start();
    }
    else if (command.startsWith("indSet")) {
        if (command.length()>6){

          String tmp = command.substring(6);

          int loc = tmp.indexOf(",");
          indexChannel = tmp.substring(0,loc).toInt();
          indexIntensity = tmp.substring(loc+1).toInt();
        }else{
          indexChannel = currentChannel;
          indexIntensity  = currentIntensity;
        }
    }
    else if (command.startsWith("middleSet")) {
        if (command.length()>9){
          String tmp = command.substring(9);
          int loc = tmp.indexOf(",");
          middleChannel = tmp.substring(0,loc).toInt();
          middleIntensity = tmp.substring(loc+1).toInt();
        }else{
          middleChannel = currentChannel;
          middleIntensity  = currentIntensity;
        }
    }
    else if (command.startsWith("wristSet")) {
        if (command.length()>8){
          String tmp = command.substring(8);
          int loc = tmp.indexOf(",");
          wristChannel = tmp.substring(0,loc).toInt();
          wristIntensity = tmp.substring(loc+1).toInt();
        }else{
          wristChannel = currentChannel;
          wristIntensity = currentIntensity;
        }
    }
    /*
        Stop the stimulation.
    */
    else if (command.startsWith("stop")) {
        ems->stop();
    }
    /*
        Define the intensity. Float is acceptable. the Unit is [mA]. The maximum is "15"
        ex1, if "int15" is sent, the intensity will be 15mA (100% of the max)
        ex2, if "int5" is sent, the intensity will be 5 mA (5/15 * 100% of the max)
    */
    else if (command.startsWith("int")) {//**int**ensity 0~15[mA]
        float tmp  = command.substring(3).toFloat();
        ems->setIntensity(tmp);
        currentIntensity = tmp;
    }
    /*
        Define the frequency.
        Due to the loop is slow, the frequency is limited to around 1~400 Hz
    */
    else if (command.startsWith("freq")) {
        int tmp  = command.substring(4).toInt();
        ems->setFrequency(tmp);
        ITimer.setInterval(ems->getPeriod(), ems->pulse_rise_timing);
    }
    /*
        Define the pulse width. This can be changed by 50 us
        150 ~ 500 us
    */
    else if (command.startsWith("wid")) {
        int tmp = command.substring(3).toInt();
        ems->setPulseWidth(tmp);
    }
    /*
        set stimulation duration (milli sec [ms]) in the same as the function "stim" without starting stimulation
        0 ~ 10000 ms
    */
    else if (command.startsWith("dur ms")) {
        int tmp = command.substring(6).toInt();
        ems->setStimDuration_us(tmp * 1000);
    }
    /*
        set stimulation duration (sec [s])
        0 ~ 10 s
    */
    else if (command.startsWith("dur s")) {
        float tmp = command.substring(5).toFloat();
        ems->setStimDuration_us(tmp * 1000000);
    }
    /*
        set channel from which the stimulation current is output
        1 ~ 12
    */
    else if (command.startsWith("setch")) {
        int tmp = command.substring(5).toInt();
        currentChannel = tmp;
        ems->swboard->setch(tmp);
        if (Serial) {
            Serial.print("channel: ");
            Serial.println(ems->swboard->getch());
            Serial.print("testCh: ");
            Serial.println(currentChannel);
        }
    }

    else if (command.startsWith("thumb")) {
        Serial.print("tch: "+thumbChannel);
        Serial.print("tin: "+thumbIntensity);

        ems->swboard->setch(thumbChannel);
        currentChannel = thumbChannel;

        ems->setIntensity(thumbIntensity);
        currentIntensity = thumbIntensity;

        if (Serial) {
            Serial.print("channel: ");
            Serial.println(ems->swboard->getch());
        }
        ems->setStimDuration_us(2000000);
        ems->start();

    }



    else if (command.startsWith("middle")) {

        ems->swboard->setch(middleChannel);
        currentChannel = middleChannel;
        ems->setIntensity(middleIntensity);
        currentIntensity = middleIntensity;

        if (Serial) {
            Serial.print("channel: ");
            Serial.println(ems->swboard->getch());
        }
        ems->setStimDuration_us(2000000);
        ems->start();

    }


    else if (command.startsWith("index")) {

        ems->swboard->setch(indexChannel);
        currentChannel = indexChannel;
        ems->setIntensity(indexIntensity);
        currentIntensity = indexIntensity;

        if (Serial) {
            Serial.print("channel: ");
            Serial.println(ems->swboard->getch());
        }
        ems->setStimDuration_us(2000000);
        ems->start();

    }


    else if (command.startsWith("wrist")) {

        ems->swboard->setch(wristChannel);
        currentChannel = wristChannel;
        ems->setIntensity(wristIntensity);
        currentIntensity = wristIntensity;

        if (Serial) {
            Serial.print("channel: ");
            Serial.println(ems->swboard->getch());
        }
        ems->setStimDuration_us(1000000);
        ems->start();

    }


    /*
     *  Show the parameters now applied in JSON style via BLE and Serial.
     *  
     *  Without any argument, i.e., if receiving "param", then it dumps all parameter. 
     *  With an argument, it return only the parameter requested.
     *      e.g., "param channel" being received, this return {"channel": 1}
     *      e.g., "param voltage" being received, this return {"voltage": 32}
     *  the means of parameters are:
     *      - "channel":    Returns the currently selected channel.
     *      - "intensity":  Returns the currently selected intensity (hight of a pulse) [mA].
     *      - "voltage":    Returns the last pulse's voltage, which varies by the intensity selected and the impedance of human body.
     *      - "p_width":    Returns the currently selected pulse widht [us]. 
     *                      The pulse width is the width of one phase of the biphasic pulse. 
     *                      Both phase has the same width and the interval bw thethem is around 130 us, so the overall pulse widh will be 2 * p_width + 130 us
     *                      (e.g.) if p_width = 200 us, then overall pulse width should be 530 us
     *      - "frequency":  Returns the currently selected frequency.
     *      - "period":     Returns the currently selected period, which is calculated from the frequency.
     *                      (e.g.) if frequency = 200 Hz, then the period should be 1000000/200 = 50000 us.
     *      - "battery":    Returns the current battery voltage. The valtage, when it fully charged, is around 4.2 V.
     *                      Note that the battery voltage decreases non-linearly. 
     */
    else if (command.startsWith("param")) {
        String tmp = command.substring(5);
        tmp.trim();
        String retval = "{\n";
        if (tmp == "" || tmp == "channel")  retval += String("  \"channel\":   ") + ems->swboard->getch() + String(",\n");
        if (tmp == "" || tmp == "intensity")retval += String("  \"intensity\": ") + ems->getIntensity()   + String(" [mA],\n");
        if (tmp == "" || tmp == "voltage")  retval += String("  \"voltage\":   ") + ems->getVoltage()     + String(" [V],\n");
        if (tmp == "" || tmp == "p_width")  retval += String("  \"p_width\":   ") + ems->getPulseWidth()  + String(" [us],\n");
        if (tmp == "" || tmp == "frequency")retval += String("  \"frequency\": ") + ems->getFrequency()   + String(" [Hz],\n");
        if (tmp == "" || tmp == "period")   retval += String("  \"period\":    ") + ems->getPeriod()      + String(" [us],\n");
        if (tmp == "" || tmp == "battery")  retval += String("  \"battery\":   ") + vol_bat               + String(" [V],\n"); 
        retval += String("}\n");
        if (central && central.connected() && startCharacteristic) {
            startCharacteristic.writeValue(retval);
        }
        if (Serial) {
            Serial.println(retval);
        }
    }
    /*
        Start/stop drum pattern.
    */
    else if (command == "drum") {
        drum_stim_flag = !drum_stim_flag;
    }
    else if (command == "fingerSettings"){
        String tmp = command.substring(14);
        tmp.trim();

        String retval = "{\n";
        if (tmp == "" || tmp == "tCh")  retval += String("  \"Thumb Channel\":   ") + thumbChannel + String("\n");
        if (tmp == "" || tmp == "tInt") retval += String("  \"Thumb Intensity\": ") + thumbIntensity   + String("\n");
        if (tmp == "" || tmp == "iCh")  retval += String("  \"Index Channel\":   ") + indexChannel     + String("\n");
        if (tmp == "" || tmp == "iInt")  retval += String("  \"Index Intensity\":   ") + indexIntensity  + String("\n");
        if (tmp == "" || tmp == "mCh") retval += String("  \"Middle Channel\": ") + middleChannel   + String("\n");
        if (tmp == "" || tmp == "mInt")   retval += String("  \"Middle Intensity\":    ") + middleIntensity      + String("\n");
        if (tmp == "" || tmp == "wCh")  retval += String("  \"Wrist Channel\":   ") + wristChannel              + String("\n"); 
        if (tmp == "" || tmp == "wInt")  retval += String("  \"Wrist Intensity\":   ") + wristIntensity              + String("\n"); 
        if (tmp == "" || tmp == "curCh")  retval += String("  \"Current Channel\":   ") + currentChannel              + String("\n"); 
        if (tmp == "" || tmp == "curInt")  retval += String("  \"Current Intensity\":   ") + currentIntensity              + String("\n"); 

        retval += String("}\n");
        if (central && central.connected() && startCharacteristic) {
            startCharacteristic.writeValue(retval);
        }
        if (Serial) {
            Serial.println(retval);
        }

    }
}

void setup() {

    pinMode(LEDR, OUTPUT);
    pinMode(LEDG, OUTPUT);
    pinMode(LEDB, OUTPUT);
    pinMode(PIN_VOL_CUR, INPUT);
    pinMode(PIN_VOL_BAT, INPUT);

    // init Serial
    Serial.begin(115200);

    // init BLE
    while (!BLE.begin()) delay (100);
    BLE.setLocalName("wristEMS");
    BLE.setAdvertisedService(myService);
    myService.addCharacteristic(startCharacteristic);
    BLE.addService(myService);
    BLE.advertise();


    // init EMS
    ems = new EMSControl();
    //set stimulation frequency
    ITimer.attachInterruptInterval(ems->getPeriod(), ems->pulse_rise_timing);
    //attach a task that render stimulation pulses
    Scheduler.startLoop(ems->task_pulse);

    analogReadResolution(12);

    digitalWrite(LEDG, LOW);
    delay (1000);
}

void loop() {
    /*
        Used in common for Serial and BLE
    */
    static String command = "";

    /*  -----------------------
        Serial communication
        ----------------------- */
    static int const buff_size = 64;
    static char buff[buff_size];
    static int idx = 0;
    static bool lastconnection = false;
    if (Serial && Serial.available() > 0 ) {
        while (idx < buff_size) {
            buff[idx] = Serial.read();
            if (buff[idx] == '\n') break;
            idx++;
        }
        buff[idx] = '\0';
        command = String(buff);
        Serial.print("input: ");
        Serial.println(command);
        execCommand(command);
        idx = 0;
    }

    /*  -----------------------
        Bluetooth communication
        ----------------------- */
    /*
        Check if a BLE central (phone or watch) is connected
    */
    if (!central) {
        //  if not connected, try to connect.
        central = BLE.central();
    }
    else if (central.connected() && startCharacteristic.written()) {
        if(!lastconnection){
            Serial.println("connected:");
            lastconnection = true;
        }
        //  if connected, read a buffer in string and parse the command.
        command = startCharacteristic.value();
        if (Serial) {
            Serial.print("input: ");
            Serial.println(command);
        }
        execCommand(command);
    }
    else if(!central.connected() && lastconnection){
        lastconnection = false;
        central.disconnect();
        if(Serial) Serial.println("disconnected"+ (!central));
    }
    

    led_blink();
    bat_read();

    if (drum_stim_flag == 1) {
        if (ems->is_stimulating == 0) {
            if (drum_note == 0) {
                drum_note++;
            } else if (drum_note == 1) {
                delay(300);
                drum_note++;
            } else if (drum_note == 2) {
                delay(100);
                drum_note++;
            } else if (drum_note == 3) {
                delay(400);
                drum_note = 0;
            }
            
            execCommand("stim 100000");
        }
    }
}

/*
    LED blink
*/
void led_blink()
{
    static unsigned long currentMicros = 0;
    static unsigned long previousMicros = 0;
    static unsigned long const interval = 1000000;
    static int ledState = LOW;

    currentMicros = micros();
    if (currentMicros - previousMicros >= interval) {
        previousMicros = currentMicros;
        ledState = !ledState;
        digitalWrite(LEDB, ledState);
        digitalWrite(LEDG, !ledState);
    }
}

/*
    Battery's voltage check
*/
void bat_read()
{
    static unsigned long currentMicros = 0;
    static unsigned long previousMicros = 0;
    static unsigned long const interval = 5000000;
    static float const rate_DAC2BAT = (1.0 / 4095.0) * 3.3 * 2;

    currentMicros = micros();
    if (currentMicros - previousMicros >= interval) {
        previousMicros = currentMicros;
        vol_bat = analogRead(PIN_VOL_BAT) * rate_DAC2BAT;
    }
}
