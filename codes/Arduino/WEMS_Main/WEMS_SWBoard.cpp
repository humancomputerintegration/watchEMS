#include "WEMS_SWBoard.h"

SWBoard::SWBoard()
    : channel(1)
    , ch_state{
    0b100000010101010101010000,//ch1    anodic
    0b001000000101010101010100,//ch2    anodic
    0b000010000001010101010101,//ch3    anodic
    0b010000100000010101010101,//ch4    anodic
    0b010100001000000101010101,//ch5    anodic
    0b010101000010000001010101,//ch6    anodic
    0b010101010000100000010101,//ch7    anodic
    0b010101010100001000000101,//ch8    anodic
    0b010101010101000010000001,//ch9    anodic
    0b010101010101010000100000,//ch10   anodic
    0b000101010101010100001000,//ch11   anodic
    0b000001010101010101000010,//ch12   anodic
    0b010000101010101010100000,//ch1    cathodic
    0b000100001010101010101000,//ch2    cathodic
    0b000001000010101010101010,//ch3    cathodic
    0b100000010000101010101010,//ch4    cathodic
    0b101000000100001010101010,//ch5    cathodic
    0b101010000001000010101010,//ch6    cathodic
    0b101010100000010000101010,//ch7    cathodic
    0b101010101000000100001010,//ch8    cathodic
    0b101010101010000001000010,//ch9    cathodic
    0b101010101010100000010000,//ch10   cathodic
    0b001010101010101000000100,//ch11   cathodic
    0b000010101010101010000001 //ch12   cathodic
}
{
    pinMode(PIN_SR_CLR, OUTPUT);
    pinMode(PIN_SR_SCK, OUTPUT);
    pinMode(PIN_SR_RCK, OUTPUT);
    pinMode(PIN_SR_DIN, OUTPUT);
}

void SWBoard::update() {
    nrf_gpio_pin_set(PIN_SR_SCK); //digitalWrite(PIN_SR_SCK, HIGH);
    nrf_gpio_pin_set(PIN_SR_SCK); //digitalWrite(PIN_SR_SCK, HIGH);
    nrf_gpio_pin_clear(PIN_SR_SCK); //digitalWrite(PIN_SR_SCK, LOW);
    nrf_gpio_pin_clear(PIN_SR_SCK); //digitalWrite(PIN_SR_SCK, LOW);
}

void SWBoard::upload() {
    nrf_gpio_pin_set(PIN_SR_RCK); //digitalWrite(PIN_SR_RCK, HIGH);
    nrf_gpio_pin_set(PIN_SR_RCK); //digitalWrite(PIN_SR_RCK, HIGH);
    nrf_gpio_pin_clear(PIN_SR_RCK); //digitalWrite(PIN_SR_RCK, LOW);
    nrf_gpio_pin_clear(PIN_SR_RCK); //digitalWrite(PIN_SR_RCK, LOW);
}

void SWBoard::clockReset() {
    digitalWrite(PIN_SR_RCK, LOW);
    digitalWrite(PIN_SR_SCK, LOW);
}

void SWBoard::resetch() {
    clockReset();
    // set all HiZ
    digitalWrite(PIN_SR_CLR, LOW);
    update();
    upload();
    // enable insertion of data to the shift registor
    digitalWrite(PIN_SR_CLR, HIGH);
}

void SWBoard::setpol(int pol) {
    this->setch(this->channel, pol);
}

void SWBoard::setch(int ch, int pol)
{
    if (ch < 0 || 12 < ch) return;
    if (ch != 0) this->channel = ch;
    unsigned int buf = ch_state[this->channel - 1 + pol * 12];
    clockReset();

    for (int i = 0; i < 12; i++) {
        ((buf >> (2 * i + 0)) & 0b01) > 0 ? nrf_gpio_pin_set(PIN_SR_DIN) : nrf_gpio_pin_clear(PIN_SR_DIN); //digitalWrite(PIN_SR_DIN, ((buf >> (2 * i + 0)) & 0b01) > 0 ? HIGH : LOW);
        update();
        ((buf >> (2 * i + 1)) & 0b01) > 0 ? nrf_gpio_pin_set(PIN_SR_DIN) : nrf_gpio_pin_clear(PIN_SR_DIN); //digitalWrite(PIN_SR_DIN, ((buf >> (2 * i + 1)) & 0b01) > 0 ? HIGH : LOW);
        update();
    }
    upload();
}
