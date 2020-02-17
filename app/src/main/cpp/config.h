
#ifndef __CONFIG_H 
#define __CONFIG_H

#ifdef __cplusplus
    extern "C" {
#endif

#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <time.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <semaphore.h>

typedef unsigned char  u8;                   /* defined for unsigned 8-bits integer variable 	无符号8位整型变量  */
typedef signed   char  s8;                    /* defined for signed 8-bits integer variable		有符号8位整型变量  */
typedef unsigned short u16;                  /* defined for unsigned 16-bits integer variable 	无符号16位整型变量 */
typedef signed   short s16;                   /* defined for signed 16-bits integer variable 		有符号16位整型变量 */
typedef unsigned int   u32;                  /* defined for unsigned 32-bits integer variable 	无符号32位整型变量 */
typedef signed   int   s32;                   /* defined for signed 32-bits integer variable 		有符号32位整型变量 */
typedef float          fp32;                    /* single precision floating point variable (32bits) 单精度浮点数（32位长度） */
typedef double         fp64;                    /* double precision floating point variable (64bits) 双精度浮点数（64位长度） */


#ifdef __cplusplus
    }
#endif

#endif

