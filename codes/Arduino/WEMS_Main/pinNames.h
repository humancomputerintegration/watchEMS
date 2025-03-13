/** Pin name aliases
 *  
 *      somwhow "int PIN_VOL_CUR = P0_2;" or so didn't work well. so here I defined them using #define macro. 
 *      if you find a better way, please modify it.
 */
#ifndef PIN_NAMES_H
#define PIN_NAMES_H

#define  PIN_VOL_CUR    P0_2    //A0;
#define  PIN_VOL_BAT    P0_3    //A1;

#define  PIN_SR_RCK     P0_28   //D2;
#define  PIN_SR_DIN     P0_29   //D3;
#define  PIN_SR_SCK     P0_4    //D4;
#define  PIN_SR_CLR     P0_5    //D5;

#define  PIN_HiV_EN     P1_11   //D6;

//#define  PIN_DAC_SDA    P0_4    //D4;
//#define  PIN_DAC_SCL    P0_5    //D5;

#define  PIN_DAC_MOSI   P1_15    //D10;
#define  PIN_DAC_MISO   P1_14    //D9;
#define  PIN_DAC_SCK    P1_13    //D8;
#define  PIN_DAC_nCS    P1_12    //D7;

//#define  PIN_NC P1_14
#define  PIN_NC P0_5

#endif
