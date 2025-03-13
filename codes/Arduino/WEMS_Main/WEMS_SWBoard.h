#ifndef WEMS_SW_BOARD_H
#define WEMS_SW_BOARD_H

#include "Arduino.h"
#include "pinNames.h"

class SWBoard
{
    public:
        SWBoard();
        /** Reset the shift register; all cannels are changed to HiZ
         */
        void resetch();
        
        /** Change channel state
         *  
         *  For ch, an integer in [1,12] is acceptable
         *  When ch = 0, the channel is not changed.
         *  For pol, 0 = anodic, 1 = cathodic
         */
        void setch(int ch = 0, int pol = 0);
        int getch();
        /** Change only pol 
         */
        void setpol(int pol = 0);
        
    private:
        int channel;
        unsigned int ch_state[24];// first 12 are for anodic stimulation, latter 12 are for catodic stimulation 
        void update();
        void upload();
        void clockReset();
};

inline int SWBoard::getch()
{
    return this->channel;
}

#endif
