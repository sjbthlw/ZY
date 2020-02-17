/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：public.h
*
* 文件标识：public.h
* 摘    要：公共应用函数集合
*
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/12/3
* 编译环境：
*
* 历史信息：
*******************************************************************************/

#ifndef __PUBLIC_H
#define __PUBLIC_H

#ifdef __cplusplus
extern "C" {
#endif

static char Debug_Str[1024];
static const char *TAG = "JNI";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

////毫秒级 延时
//extern void Sleepms(int ms);
////微秒级 延时
//extern void Sleepus(int us);

//获取时间
extern long Get_SYSTimeUS(void);

//操作io口
extern int sysfs_write(const char *path, const char *val);

extern int LedShow(char color, int level);

#ifdef __cplusplus
}
#endif

#endif



