/*
 [A "BSD license"]
 Copyright (c) 2015      Rainer Mueller
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


#include "Lpc17xxDigitalOut.h"
#include "Log.h"
#include "Signals.h"
#include "chip.h"

/**
 \brief Implementation of Lpc17xxDigitalOut Systemdation


*/
namespace pearlrt {

   Lpc17xxDigitalOut::Lpc17xxDigitalOut(int port, int start, int width) :
      port(port), start(start), width(width) {
      static const unsigned int Lpc17xxBits[5] = {
         0x7fff8fff, // P0: 0-11, 15-30
         0xffffc713, // P1: 0-1,4, 8-10, 14-31
         0x00001fff, // P2: 0-13
         0x06000000, // P3: 25-26
         0x30000000
      }; // P4: 28-29

      unsigned int bit;

      dationStatus = CLOSED;

      if (port < 0 || port > 4) {
         Log::error("Lpc17xxDigitalOut: illegal port number (%d)", port);
         throw theInternalDationSignal;
      }

      if (start < 0 || start > 31) {
         Log::error("Lpc17xxDigitalOut: illegal start bit number (%d)", start);
         throw theInternalDationSignal;
      }

      if (width < 1 || width > 32) {
         Log::error("Lpc17xxDigitalOut: illegal width (%d)", width);
         throw theInternalDationSignal;
      }

      if (start - width + 1 < 0) {
         Log::error("Lpc17xxDigitalOut: width too large (start=%d, width=%d)",
                    start, width);
         throw theInternalDationSignal;
      }

      // create mask of used bits for this device
      bit = 1 << start;
      mask = 0;

      while (width > 0) {
         mask |= bit;
         bit >>= 1;
         width--;
      }

      if ((Lpc17xxBits[port] & mask) != mask) {
         Log::error("Lpc17xxDigitalOut: not (all) requested bits are "
                    "available (%x)",
                    (unsigned int)(Lpc17xxBits[port] & mask));
         throw theDationParamSignal;
      }


      // set the GPIO bits to digital input mode
      LPC_GPIO[port].MASK = ~mask;
      LPC_GPIO[port].DIR  = mask;
   }

   SystemDationB* Lpc17xxDigitalOut::dationOpen(const char* idf,
         int openParam) {
      if (idf != 0) {
         Log::error("IDF not allowed for Lpc17xxDigitalOut device");
         throw theDationParamSignal;
      }

      if (openParam != OUT) {
         Log::error("No open parameters except OUT allowed for Lpc17xxDigitalOut device");
         throw theDationParamSignal;
      }

      if (dationStatus != CLOSED) {
         Log::error("Lpc17xxDigitalOut: Dation already open");
         throw theOpenFailedSignal;
      }

      dationStatus = OPENED;
      return this;
   }
   void Lpc17xxDigitalOut::dationClose(int closeParam) {
      if (closeParam != 0) {
         Log::error("No close parameters allowed for "
                    "Lpc17xxDigitalOut device");
         throw theDationParamSignal;
      }

      if (dationStatus != OPENED) {
         Log::error("Lpc17xxDigitalOut: Dation not open");
         throw theDationNotOpenSignal;
      }

      dationStatus = CLOSED;
   }

   void Lpc17xxDigitalOut::dationRead(void* data, size_t size) {
      throw theInternalDationSignal;
   }

   void Lpc17xxDigitalOut::dationWrite(void* data, size_t size) {
      int d;

      if (dationStatus != OPENED) {
         Log::error("Lpc17xxDigitalOut: Dation not open");
         throw theDationNotOpenSignal;
      }

      // write data to application memory
      switch (size) {
      case 1:
         d = (*(uint8_t*)data) >> (8 - width);
         break;

      case 2:
         d = (*(uint16_t*)data) >> (16 - width);
         break;

      case 4:
         d = (*(uint32_t*)data) >> (32 - width);
         break;

      default:
         Log::error("Lpc17xxDigitalOut: illegal size (%d) ", size);
         throw theInternalDationSignal;
      }

      d <<= (start - width + 1);   // shift to correct position
      LPC_GPIO[port].MASK = ~mask;
      LPC_GPIO[port].PIN = d;
   }

   int Lpc17xxDigitalOut::capabilities() {
      return OUT;
   }
}

