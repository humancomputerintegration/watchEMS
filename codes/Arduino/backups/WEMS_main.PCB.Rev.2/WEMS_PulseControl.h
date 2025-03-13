#ifndef WEMS_PULSE_CONTROL_H
#define WEMS_PULSE_CONTROL_H

#ifndef WEMS_SERIAL_PRINT_DEBUG
#define WEMS_SERIAL_PRINT_DEBUG true
#endif

#include "Arduino.h"
#include "pinNames.h"


/*
    For DAC
*/
#include "Wire.h"
#include "MCP4725.h"

#include "WEMS_SWBoard.h"

class EMSControl
{
    public:
        SWBoard* swboard;
        int pulse_rise;
        int is_stimulating;

        static EMSControl* self;
        EMSControl(float intensity = 5, unsigned long pulsewidth = 200, unsigned long frequency = 200);

        static void pulse_rise_timing(void);
        static void pulse(void);
        static void task_pulse(void);

        void start(void);
        void stop(void);
        void setStimDuration_us(unsigned long);
        void setStimDuration_inf(void);

        void setIntensity(float);
        void setPulseWidth(unsigned long);
        void setFrequency(unsigned long);
        void setPeriod(unsigned long);

        float getIntensity(void);
        float getVoltage(void);
        unsigned long getPulseWidth(void);
        unsigned long getFrequency(void);
        unsigned long getPeriod(void);

    private:
        MCP4725* DAC_MCP;
        float val_intensity[12];
        float const RATE_DAC2VOL = (1.0 / 4095.0) * 3.6 * (1030.0 / 30.0);
        float intensity_voltage[12];
        unsigned long val_frequency;    //Hz
        unsigned long val_period;       //microsec
        unsigned long val_pulseWidth;   //microsec
        unsigned long stim_duration;    //microseconds
        unsigned long stim_duration_tmp;    //Backup to restore stim_duration after stimulation with infinite duration by command "a"
        unsigned long previousMicros_stim_duration; //to stop stimulation after the duration defined

};


inline void EMSControl::start(void) {

    digitalWrite(PIN_5V_EN, HIGH);
    this->swboard->setch();
    this->previousMicros_stim_duration = micros();
    this->is_stimulating = 1;

    if (Serial && WEMS_SERIAL_PRINT_DEBUG) Serial.println("start");
}

inline void EMSControl::stop(void) {
    digitalWrite(PIN_5V_EN, LOW);
    this->is_stimulating = 0;

    if (Serial && WEMS_SERIAL_PRINT_DEBUG) Serial.println("stop");
    this->setStimDuration_us(this->stim_duration_tmp);
}


inline void EMSControl::setStimDuration_us(unsigned long arg_duration) {
    if (0 < arg_duration && arg_duration <= 10000000) {
        this->stim_duration = arg_duration;
        this->stim_duration_tmp = arg_duration;
    }
    if (Serial && WEMS_SERIAL_PRINT_DEBUG) {
        Serial.print("stim duration: ");
        Serial.print(this->stim_duration);
        Serial.println(" [us]");
    }
}

inline void EMSControl::setStimDuration_inf(void) {
    this->stim_duration_tmp = this->stim_duration;
    this->stim_duration = 0;
    if (Serial && WEMS_SERIAL_PRINT_DEBUG) {
        Serial.println("stim duration: inf");
    }
}

inline void EMSControl::setIntensity(float arg_intensity)
{
    if (0 <= arg_intensity && arg_intensity <= 15 ) {
        this->val_intensity[this->swboard->getch() - 1] = arg_intensity;
    }

    if (Serial && WEMS_SERIAL_PRINT_DEBUG) {
        Serial.print("intensity: ");
        Serial.print(this->val_intensity[this->swboard->getch() - 1]);
        Serial.println(" [mA]");
    }

}
inline void EMSControl::setPulseWidth(unsigned long arg_pulsewidth)
{

    if (150 < arg_pulsewidth && arg_pulsewidth < 500) {
        this->val_pulseWidth = arg_pulsewidth;
    }
    if (Serial && WEMS_SERIAL_PRINT_DEBUG) {
        Serial.print("pulse width: ");
        Serial.print(this->val_pulseWidth);
        Serial.println(" [us]");
    }
}
inline void EMSControl::setFrequency(unsigned long arg_freq)
{
    if (1 <= arg_freq && arg_freq <= 400 ) {
        this->val_frequency = arg_freq;
        this->val_period = 1000000 / this->val_frequency;
    }

    if (Serial && WEMS_SERIAL_PRINT_DEBUG) {
        Serial.print("frequency: ");
        Serial.print(this->val_frequency);
        Serial.println(" [Hz]");
        Serial.print("period: ");
        Serial.print(this->val_period);
        Serial.println(" [us]");
    }
}
inline void EMSControl::setPeriod(unsigned long arg_period)
{
    if (2500 <= arg_period && arg_period <= 1000000) {
        this->val_period = arg_period;
        this->val_frequency = 1000000 / this->val_period;
    }

    if (Serial && WEMS_SERIAL_PRINT_DEBUG) {
        Serial.print("frequency: ");
        Serial.print(this->val_frequency);
        Serial.println(" [Hz]");
        Serial.print("period: ");
        Serial.print(this->val_period);
        Serial.println(" [us]");
    }
}


inline float EMSControl::getIntensity(void)
{
    return this->val_intensity[this->swboard->getch() - 1];
}

inline float EMSControl::getVoltage(void)
{
    return this->intensity_voltage[this->swboard->getch() - 1] * this->RATE_DAC2VOL;
}
inline unsigned long EMSControl::getPulseWidth(void)
{
    return this->val_pulseWidth;
}
inline unsigned long EMSControl::getFrequency(void)
{
    return this->val_frequency;
}
inline unsigned long EMSControl::getPeriod(void)
{
    return this->val_period;
}

#endif
