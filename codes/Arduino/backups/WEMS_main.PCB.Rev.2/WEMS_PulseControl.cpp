#include "WEMS_PulseControl.h"

EMSControl* EMSControl::self = NULL;

EMSControl::EMSControl(float arg_intensity, unsigned long arg_pulsewidth, unsigned long arg_frequency)
{
    /*
        DAC device code is 0b1100
        Address of the DAC (MCP4725A0T-E/CH) is 000
        Therefore the full SPI address is 1100000(0x60)
        see p.43 of MCP4725's data sheet for details
    */
    this->DAC_MCP = new MCP4725(0x60);
    this->swboard = new SWBoard();

    for (int i = 0; i < 12; i++){
        this->val_intensity[i] = arg_intensity;
        this->intensity_voltage[i] = 0;
    }
    this->val_pulseWidth = arg_pulsewidth;
    this->val_frequency = arg_frequency;
    this->val_period = 1000000 / this->val_frequency;

    this->stim_duration = 1000000; //1sec
    this->previousMicros_stim_duration = micros();
    this->pulse_rise = 0;
    this->is_stimulating = 0;


    digitalWrite(PIN_5V_EN, HIGH);
    Wire.begin();
    Wire.setClock(400000);
    this->swboard->resetch();
    this->DAC_MCP->setPercentage(0);
    digitalWrite(PIN_5V_EN, LOW);

    this->self = this;
}


void EMSControl::pulse_rise_timing(void)
{
    if (EMSControl::self == NULL) return;
    self->pulse_rise = 1;
}

void EMSControl::task_pulse(void)
{
    static unsigned long currentMicros = 0;

    if (EMSControl::self == NULL) return;
    if (self->is_stimulating != 1) return;

    currentMicros = micros();

    if (self->pulse_rise == 1) {
        //anodic
        self->swboard->setpol(0);
        self->pulse();

        //cathodic
        self->swboard->setpol(1);
        self->pulse();

        self->pulse_rise = 0;
    }

    /*
        If stimulation duration is set >0, stop the stimulation after the duration has elapsed.
    */
    if (self->stim_duration > 0 && currentMicros - self->previousMicros_stim_duration >= self->stim_duration) {
        self->DAC_MCP->setPercentage(0);
        self->is_stimulating = 0;
        self->swboard->resetch();
        self->previousMicros_stim_duration = micros();
        digitalWrite(PIN_5V_EN, LOW);

        if (Serial && WEMS_SERIAL_PRINT_DEBUG) {
            Serial.print("stop by time out (");
            Serial.print(self->stim_duration);
            Serial.println(" [us])");
        }
    }
}


void EMSControl::pulse(void) {
    static int times_delay;
    if (EMSControl::self == NULL) return;
    digitalWrite(P1_14, HIGH);
    /*
        Raise a pulse
        The rise time is about 2.5us
    */
    self->DAC_MCP->setPercentage(100 * self->val_intensity[self->swboard->getch() - 1] / 15);
    /*
        The five digitalWrite()s noted with <*> makes 25 us delay
    */
    digitalWrite(P1_14, LOW);   //<*>
    digitalWrite(P1_14, HIGH);  //<*>

    /*
        Wait for (times_delay * 50) us = (val_pulseWidth - 150) us

        The 150us is the sum of the delays by digitalWrite (5*5 us) and setPercentage(0) (125us)
        Pulse width can be changed in increments of 50μs
    */
    if (self->val_pulseWidth >= 150)
        times_delay = (self->val_pulseWidth - 150) / 50;
    else
        times_delay = 0;
    while (times_delay > 0) {
        // Following procedures takes 50us
        digitalWrite(P1_14, LOW);    //takes 5us for each
        digitalWrite(P1_14, HIGH);
        digitalWrite(P1_14, LOW);
        digitalWrite(P1_14, HIGH);
        self->intensity_voltage[self->swboard->getch() - 1] = analogRead(PIN_VOL_CUR);  //takse 30us, the stimulation voltage is stored in the intensity_voltage.
        times_delay --;
    }
    digitalWrite(P1_14, LOW);   //<*>
    digitalWrite(P1_14, HIGH);  //<*>
    digitalWrite(P1_14, LOW);   //<*>

    /*
        Lets fallen the pulse.

        The rise time is about 2.5us.
        It takes around 125us for the pulse to fall after the following function execution,
        so that the 125us is subtracted from the pulse width above.
    */
    self->DAC_MCP->setPercentage(0);//takes around 125us until the pulse falls
    //  if I2C communication failed, try again
    while (analogRead(PIN_VOL_CUR) * self->RATE_DAC2VOL > self->intensity_voltage[self->swboard->getch() - 1] / 2)
        self->DAC_MCP->setPercentage(0);
}
