#include "WEMS_PulseControl.h"

EMSControl* EMSControl::self = NULL;

EMSControl::EMSControl(float arg_intensity, unsigned long arg_pulsewidth, unsigned long arg_frequency)
{
    pinMode(PIN_HiV_EN, OUTPUT);
    this->self = this;
    
    /*
     * DAC inir
     * In Ver.3, DAC is Texas Instruments' DAC8830, which is controlled via SPI. 
     * 16 bit, 50 MHz
    */
    SPI.begin();
    pinMode(PIN_DAC_nCS, OUTPUT);
    digitalWrite(PIN_DAC_nCS, HIGH);
    //mbed-enable library limit SPI speed 8MHz, while the other library limit it 32MHz
    //the DAC can be 50MHz at max, though
    SPI.beginTransaction(SPISettings(8000000, MSBFIRST, SPI_MODE0));
    nrf_gpio_pin_clear(PIN_DAC_nCS);
    SPI.transfer16(0);
    nrf_gpio_pin_set  (PIN_DAC_nCS);
    SPI.endTransaction();
    
    /*
     *  Init Switching Board
     */
    this->swboard = new SWBoard();
    this->swboard->resetch();

    /*
     *  Init stimulation parameters
     */
    for (int i = 0; i < 12; i++){
        this->val_intensity[i] = arg_intensity;
        this->intensity_voltage[i] = 0;
    }
    this->val_pulseWidth = arg_pulsewidth;
    this->val_frequency = arg_frequency;
    this->val_period = 1000000 / this->val_frequency;

    /*
     *  Init timing vars
     */
    this->stim_duration = 1000000; //1sec
    this->previousMicros_stim_duration = micros();
    this->pulse_rise = 0;
    this->is_stimulating = 0;

    pinMode(PIN_NC, OUTPUT);
}


void EMSControl::pulse_rise_timing(void)
{
    if (EMSControl::self == NULL) return;
    self->pulse_rise = 1;
}

void EMSControl::task_pulse(void)
{
    static unsigned long currentMicros = 0;
    static bool l_lock = false;
    

    if (EMSControl::self == NULL) return;
    if (self->is_stimulating != 1) return;
    if (l_lock) return;
    l_lock = true;
    
    currentMicros = micros();

    if (self->pulse_rise == 1) {
        self->pulse();  //anodic
        self->swboard->setpol(1); //set pol cathode for next pulse();
        //delayMicroseconds(10); // wait for the swboard being set completely
        self->pulse();  //cathodic
        self->swboard->setpol(0); //set pol anode for next pulse();

        self->pulse_rise = 0;
    }

    /*
        If stimulation duration is set >0, stop the stimulation after the duration has elapsed.
    */
    if (self->stim_duration > 0 && currentMicros - self->previousMicros_stim_duration >= self->stim_duration) {
        
        SPI.beginTransaction(SPISettings(50000000, MSBFIRST, SPI_MODE0));
        nrf_gpio_pin_clear(PIN_DAC_nCS);
        SPI.transfer16(0);
        nrf_gpio_pin_set  (PIN_DAC_nCS);
        SPI.endTransaction();

        self->stop();
        self->previousMicros_stim_duration = micros();

        if (Serial && WEMS_SERIAL_PRINT_DEBUG) {
            Serial.print("stop by time out (");
            Serial.print(self->stim_duration);
            Serial.println(" [us])");
        }
    }
    
    l_lock = false;
}


void EMSControl::pulse(void) {
    static int times_delay;
    static bool l_lock = false;
    if (EMSControl::self == NULL) return;
    if (l_lock) return;
    l_lock = true;
    /*
        Raise a pulse
        The rise time is about 40us
    */
    SPI.beginTransaction(SPISettings(50000000, MSBFIRST, SPI_MODE0));
    nrf_gpio_pin_clear(PIN_DAC_nCS);
    SPI.transfer16((short)(0xFFFF * self->val_intensity[self->swboard->getch() - 1] / 15));
    nrf_gpio_pin_set  (PIN_DAC_nCS);

    /*
        Wait for (times_delay * 7) + 70 us = val_pulseWidth us
    */
    
    if (self->val_pulseWidth >= 70)
        times_delay = (self->val_pulseWidth - 70) / 7;
    else
        times_delay = 0;
        
    while (times_delay > 0) {
        // Following procedures takes 7us
        digitalWrite(PIN_NC, LOW);    //takes 3.5us for each
        digitalWrite(PIN_NC, HIGH);
        times_delay --;
    }
    
    
    //takse 32us, the stimulation voltage is stored in the intensity_voltage.
    self->intensity_voltage[self->swboard->getch() - 1] = analogRead(PIN_VOL_CUR);  
    
    /*
        Lets fallen the pulse.
        The fallen time is about 40us
    */
    nrf_gpio_pin_clear(PIN_DAC_nCS);
    SPI.transfer16(0);
    nrf_gpio_pin_set  (PIN_DAC_nCS);
    
    SPI.endTransaction();
    l_lock = false;
}
