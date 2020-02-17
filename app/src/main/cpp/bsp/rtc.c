#include <stdio.h>
#include <stdlib.h>
#include <linux/rtc.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <sys/types.h>
#include <fcntl.h>
#include <unistd.h>
#include <time.h>
#include <string.h>

#include "rtc.h"

static const char *device = "/dev/rtc0";
static int rtc_fd;
char rtc_search_char[12] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '\n'};


int int4char(int num, char *str) {
    int data = num;
    if (num < 0 || num > 9999) {
        printf("num is too high or too low \n");
        return 1;
    }
    //printf("data4%d\n",data);
    str[0] = rtc_search_char[data / 1000];
    data = data % 1000;
    str[1] = rtc_search_char[data / 100];
    data = data % 100;
    str[2] = rtc_search_char[data / 10];
    str[3] = rtc_search_char[data % 10];
    //printf("char_distance4%s\n",str);
    return 0;
}

int int2char(int num, char *str) {
    int data = num;
    if (num < 0 || num > 99) {
        printf("num is too high or too low \n");
        return 1;
    }
    str[0] = rtc_search_char[data / 10];
    str[1] = rtc_search_char[data % 10];
    return 0;
}


//初始化RTC时钟
int InitRTC(void) {
    rtc_fd = open(device, O_RDWR);
    if (rtc_fd == -1) {
        printf("======InitRTC 打开RTC时钟设备失败=========\n");
        return 1;
    }
    return 0;
}

//关闭RTC时钟
int CloseRTC(void) {
    close(rtc_fd);
    return 0;
}

//设置RTC时钟
int SetRTC(struct rtc_time *rtc_tm) {
    int retval;
    //struct rtc_time rtc_tm;
    rtc_fd = open(device, O_RDWR);
    if (rtc_fd == -1) {
        printf("======SetRTC 打开RTC时钟设备失败=========\n");
        return 1;
    }

    /* write the RTC time/date */
    retval = ioctl(rtc_fd, RTC_SET_TIME, rtc_tm);
    if (retval == -1) {
        printf("======设置RTC时钟失败=========\n");
        close(rtc_fd);
        return 1;
    }
    close(rtc_fd);
    return 0;
}

//读RTC时钟
int ReadRTC(struct rtc_time *rtc_tm) {
    int retval;
    //struct rtc_time rtc_tm;

    rtc_fd = open(device, O_RDWR);
    if (rtc_fd == -1) {
        printf("======ReadRTC 打开RTC时钟设备失败=========\n");
        return 1;
    }

    /* Read the RTC time/date */
    retval = ioctl(rtc_fd, RTC_RD_TIME, rtc_tm);
    if (retval == -1) {
        printf("======读取RTC时钟失败=========\n");
        close(rtc_fd);
        return 1;
    }
    close(rtc_fd);
    return 0;
}

//获取rtc日期时间星期
int GetRTCDateTime(unsigned char *bCurrDateTime) {
    int retval;
    struct rtc_time p;
    retval = ReadRTC(&p);
    if (retval != 0) {
        printf("========ReadRTC ERR=======\n");
        return -1;
    }

//    printf("%d-%d-%d ", (1900+p.tm_year)-2000,(1+p.tm_mon), p.tm_mday, p.tm_wday);
//    printf("%d:%d:%d\n",p.tm_hour, p.tm_min, p.tm_sec);

    bCurrDateTime[0] = (1900 + p.tm_year) - 2000;
    bCurrDateTime[1] = (1 + p.tm_mon);
    bCurrDateTime[2] = p.tm_mday;
    bCurrDateTime[3] = p.tm_hour;
    bCurrDateTime[4] = p.tm_min;
    bCurrDateTime[5] = p.tm_sec;
    bCurrDateTime[6] = p.tm_wday;

    return 0;
}

//设置时间
int SetDateTime(unsigned char *bCurrDateTime) {
    struct timeval tv;
    time_t t;
    struct tm stm;
    struct rtc_time rtc_tm;
    int rec;

    int i;
    unsigned char bReadDateTime[8];

    stm.tm_year = 2000 + bCurrDateTime[0] - 1900;
    stm.tm_mon = bCurrDateTime[1] - 1;
    stm.tm_mday = bCurrDateTime[2];
    stm.tm_hour = bCurrDateTime[3];
    stm.tm_min = bCurrDateTime[4];
    stm.tm_sec = bCurrDateTime[5];
    //stm.tm_wday =bCurrDateTime[6];

    rtc_tm.tm_year = 2000 + bCurrDateTime[0] - 1900;
    rtc_tm.tm_mon = bCurrDateTime[1] - 1;
    rtc_tm.tm_mday = bCurrDateTime[2];
    rtc_tm.tm_hour = bCurrDateTime[3];
    rtc_tm.tm_min = bCurrDateTime[4];
    rtc_tm.tm_sec = bCurrDateTime[5];

    t = mktime(&stm);
    tv.tv_sec = t;
    tv.tv_usec = 0;

    rec = settimeofday(&tv, NULL);
    if (rec == 0) {
        rec = SetRTC(&rtc_tm);
        if (rec != 0) {
            return -1;
        }
    }
    return 0;
}


int RTC_test(void) {
    int fd, retval;
    struct rtc_time rtc_tm;
    time_t timep;
    struct tm *p;
    printf("======Start RTC test========\n");
    fd = open(device, O_RDWR);
    if (fd == -1) {
        printf("======打开RTC设备失败=========\n");
        return 1;
    }
#if 1
    /* Read the RTC time/date */
    retval = ioctl(fd, RTC_RD_TIME, &rtc_tm);
    if (retval == -1) {
        printf("========ioctl=======\n");
        return 1;
    }

    printf("RTC date/time: %d/%d/%d %02d:%02d:%02d\n",
           rtc_tm.tm_mday, rtc_tm.tm_mon + 1, rtc_tm.tm_year + 1900,
           rtc_tm.tm_hour, rtc_tm.tm_min, rtc_tm.tm_sec);

    time(&timep);
    p = gmtime(&timep);
    printf("OS date/time(UTC): %d/%d/%d %02d:%02d:%02d\n",
           p->tm_mday, p->tm_mon + 1, p->tm_year + 1900,
           p->tm_hour, p->tm_min, p->tm_sec);

    p = localtime(&timep);
    printf("OS date/time(Local): %d/%d/%d %02d:%02d:%02d\n",
           p->tm_mday, p->tm_mon + 1, p->tm_year + 1900,
           p->tm_hour, p->tm_min, p->tm_sec);
#else
    ///////////////////// read/wirte the rtc//////////////////
            rtc_tm.tm_mday = 12;
            rtc_tm.tm_mon = 8 -1;//8月
            rtc_tm.tm_year = 2015 -1900;//2015年

            rtc_tm.tm_hour = 16;
            rtc_tm.tm_min = 41;
            rtc_tm.tm_sec = 30;

            rtc_tm_temp = rtc_tm;

            /* write the RTC time/date */
            retval = ioctl(fd, RTC_SET_TIME, &rtc_tm);
            if (retval == -1)
            {
                    printf("ioctl err \n");
                    return 1;
            }

            /* Read the RTC time/date */
            retval = ioctl(fd, RTC_RD_TIME, &rtc_tm);
            if (retval == -1)
            {
                    printf("ioctl err \n");
                    return 1;
            }

            if( (rtc_tm_temp.tm_mday != rtc_tm.tm_mday)|| (rtc_tm_temp.tm_mon != rtc_tm.tm_mon)||
                (rtc_tm_temp.tm_year != rtc_tm.tm_year)|| (rtc_tm_temp.tm_hour != rtc_tm.tm_hour)||
                (rtc_tm_temp.tm_min != rtc_tm.tm_min))
            {
                printf("datetime err \n");
                return 1;
            }
#endif
    close(fd);

    return 0;
}  
