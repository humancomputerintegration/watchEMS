/** Pin name aliases
 *  
 *      somwhow "int PIN_VOL_CUR = P0_2;" or so didn't work well. so here I defined them using #define macro. 
 *      if you find a better way, please modify it.
 */
#ifndef PIN_NAMES_H
#define PIN_NAMES_H

#define  PIN_VOL_CUR    P0_2    //A0; // swapped from D10 by manurally soldering jumber wire
#define  PIN_VOL_BAT    P0_3    //A1; //swapped from D8 by manually soldering jumber wire

#define  PIN_5V_EN      P1_12   //7;

#define  PIN_SR_CLR     P1_15   //10; // in circuitry of ver.2, it is conncected to D0, but vol_cur should be connected to D0(A0) so it was swapped.
#define  PIN_SR_SCK     P1_13   //8;  // in circuitry of ver.2, it is conneccted to D1, but vol_bat should be connected to D1(A1) so it was swapped.
#define  PIN_SR_RCK     P0_28   //2;
#define  PIN_SR_DIN     P0_29   //3;

#define  PIN_DAC_SDA    P0_4    //4;
#define  PIN_DAC_SCL    P0_5    //5;

#endif
