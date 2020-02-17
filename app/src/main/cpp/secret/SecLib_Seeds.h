#ifndef HSECLIB_SEEDS
#define HSECLIB_SEEDS

#include "SecLIB_Define.h"

#define CoreCipherSeed_DWordL 0x48864307
#define CoreCipherSeed_DWordH 0x6E75537A

#define CardMakeSeed_SCARD 0x29886537
#define CardMakeSeed_FCARD 0x71921346

#define AuthenSeed_UCARD  0x63451897
#define AuthenSeed_SCARD  0x37852852

#define CoreTranKey_SECT   0x48864307
#define CoreTranKey_DWordH 0x89587634

#define LogonSeed_SETUP 0x2E12972B
#define LogonSeed_OPER  0x1F58763A

#endif
