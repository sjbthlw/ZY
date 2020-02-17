#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <termios.h>
#include <sys/time.h>

#include "uart.h"

//串口对应关系
//打印小票-->ttysWK0 (232) （RJ11）
//psam-->ttysWK1 (ttl)
//（未知）-->ttysWK2 (ttl) （RJ11）
//qrcode-->ttysWK3 (ttl)

struct termios opt;

int Uart_Init(int fd, int BaudID, char nEvent) {
    int err;

    memset(&opt, 0x00, sizeof(opt));

    // 设置波特率
    switch (BaudID) {
        case 0:
            //set baud rate
            cfsetispeed(&opt, B9600);
            cfsetospeed(&opt, B9600);
            break;
        case 1:
            cfsetispeed(&opt, B38400);
            cfsetospeed(&opt, B38400);
            break;
        case 2:
            cfsetispeed(&opt, B57600);
            cfsetospeed(&opt, B57600);
            break;
        case 3:
            cfsetispeed(&opt, B115200);
            cfsetospeed(&opt, B115200);
            break;

        default:
            cfsetispeed(&opt, B9600);
            cfsetospeed(&opt, B9600);
            break;
    }
    opt.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
    opt.c_oflag &= ~OPOST;//原始数据输出
    //opt.c_oflag  &= ~ONLCR;
    //opt.c_oflag  &= ~OCRNL;

    opt.c_iflag &= ~(IXON | IXOFF | IXANY);
    opt.c_iflag &= ~(INPCK | ICRNL | BRKINT);

    //设置奇偶校验位
    switch (nEvent) {
        case 'O': //奇数
            opt.c_cflag |= PARODD;
            opt.c_cflag |= PARENB;
            opt.c_iflag |= (INPCK | ISTRIP);
            break;
        case 'E': //偶数
            opt.c_cflag |= PARENB;
            opt.c_cflag &= ~PARODD;
            //opt.c_iflag |= (INPCK | ISTRIP);
            opt.c_iflag |= INPCK;
            break;
        case 'N': //无奇偶校验位
            opt.c_cflag &= ~PARENB;//无校验
            break;
    }

    opt.c_cflag &= ~CRTSCTS;//不使用流控制
    opt.c_cflag |= (CLOCAL | CREAD);//激活项，用于本地连接和接收

    opt.c_cflag &= ~CSIZE;
    //opt.c_cflag |= CS8; //8位字符长度
    opt.c_cflag |= CS8; //8位字符长度

    opt.c_cflag &= ~CSTOPB;//一个停止位
    //opt.c_cflag |= CSTOPB;//二个停止位
    opt.c_cc[VTIME] = 0;//读取一个字符等待1*(1/10)s
    opt.c_cc[VMIN] = 0;//读取字符的最少个数

    opt.c_cflag |= (CLOCAL | CREAD);
    opt.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
    opt.c_oflag &= ~OPOST;
    opt.c_oflag &= ~(ONLCR | OCRNL);    //添加的
    opt.c_iflag &= ~(ICRNL | INLCR);
    opt.c_iflag &= ~(IXON | IXOFF | IXANY);    //添加的

    tcflush(fd, TCIFLUSH);
    err = tcsetattr(fd, TCSANOW, &opt);
    if (err != 0) {
        printf("tcsetattr error , %d\n", err);
        return -1;
    }
    return 0;
}

