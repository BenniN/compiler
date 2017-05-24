/*
 [A "BSD license"]
 Copyright (c) 2017 Rainer Mueller
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

#include "Signals.h"
#include "Log.h"

/**
\file
*/

namespace pearlrt {
   /**
   \addtogroup datatypes
   @{
   */

   /**

   Dereference of a REF variable

   The OpenPEARL system translates REF into native pointers, which are
   initialized with NULL. There is not explicit initialisation enforced
   by the language definition.
   Thus each access to the object via the  reference must check, if the
   pointer is valid. This test is done in this template method, which
   works for all kinds of references (except REF CHAR()) of the PEARL
   application.

   Usage in the C++ code (without explicit namespace):
  
   \code
   Fixed<15> x;
   Fixed<15> * y;
   ...
   y = & x;           // assignment to the reference variable
   x = Cont(y) + x;   // usage in an expression
   Cont(y) = x;       // Usage in an assignment
   \endcode 

   \tparam C the class of the referenced variable. This may be any 
             kind of class.
   \param x the reference variable. If the value is not NULL, the referenced
            object is returned as reference type of C++.
           
    \throws   RefNotInitializedSignal if the parameter x ist NULL 
   */
   template<class C> C& cont(C* x) {
      if (x) {
         return *x;
      }
      Log::error("use of uninitialized reference");
      throw theRefNotInitializedSignal; 
   }

   /**
   depricated version of cont
   */
   template<class C> C& Cont(C* x) {
      if (x) {
         return *x;
      }
      Log::error("use of uninitialized reference");
      throw theRefNotInitializedSignal; 
   }
   /** @} */
}

