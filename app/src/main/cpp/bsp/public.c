#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/time.h>

#include "public.h"


////毫秒级 延时
//void Sleepms(int ms)
//{
//    struct timeval delay;
//    delay.tv_sec = 0;
//    delay.tv_usec = ms * 1000;
//    select(0, NULL, NULL, NULL, &delay);
//}
//
////微秒级 延时
//void Sleepus(int us)
//{
//    struct timeval delay;
//    delay.tv_sec = 0;
//    delay.tv_usec = us ; //  us
//    select(0, NULL, NULL, NULL, &delay);
//}

//获取时间
long Get_SYSTimeUS(void) {
    long lngtimeus;
    struct timeval tv;
    struct timezone tz;

    gettimeofday(&tv, &tz);

//    LOGD("tv_sec; %d \n", tv.tv_sec);
//    LOGD("tv_usec; %d \n",tv.tv_usec);

    lngtimeus = ((tv.tv_sec * 1000 * 1000 + tv.tv_usec) / 1000);
    //LOGD("lngtimeus; %d \n",lngtimeus);

    return lngtimeus;

}

//操作io口
int sysfs_write(const char *path, const char *val) {
    int fd;
    int bytes;
    fd = open(path, O_CREAT | O_RDWR | O_TRUNC, 0644);
    if (fd < 0) {
        //ALOGE("unable to open file %s,err: %s", path, strerror(errno));
        return -1;
    }
    bytes = write(fd, val, strlen(val));
    close(fd);
    return bytes;
}

//LED 1:Red 2:Blue 3:Green
//LDE灯
//echo 0 > /sys/class/leds/blue/brightness	//灭
//echo 1 > /sys/class/leds/blue/brightness	/亮
//
//echo 0 > /sys/class/leds/red/brightness	//灭
//echo 1 > /sys/class/leds/red/brightness	/亮
//
//echo 0 > /sys/class/leds/green/brightness	//灭
//echo 1 > /sys/class/leds/green/brightness	/亮
int LedShow(char color, int level) {
    int fd;
    int bytes = 0;
    char val[1];

    memset(val, 0x00, sizeof(val));

    switch (color) {
        case 1:
            if (level == 0)
                sysfs_write("/sys/class/leds/red/brightness", "0");
            else
                sysfs_write("/sys/class/leds/red/brightness", "1");
            break;

        case 2:
            if (level == 0)
                sysfs_write("/sys/class/leds/blue/brightness", "0");
            else
                sysfs_write("/sys/class/leds/blue/brightness", "1");
            break;

        case 3:
            if (level == 0)
                sysfs_write("/sys/class/leds/green/brightness", "0");
            else
                sysfs_write("/sys/class/leds/green/brightness", "1");
            break;
    }
    return bytes;
}