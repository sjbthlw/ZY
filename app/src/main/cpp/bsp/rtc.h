#ifndef __RTC_H__
#define __RTC_H__

#ifdef __cplusplus
extern "C" {
#endif


extern int InitRTC(void);

extern int CloseRTC(void);

extern int SetRTC(struct rtc_time *rtc_tm);

extern int ReadRTC(struct rtc_time *rtc_tm);

extern int GetRTCDateTime(unsigned char *bCurrDateTime);

extern int SetDateTime(unsigned char *bCurrDateTime);

extern int RTC_test(void);

#ifdef __cplusplus
}
#endif

#endif
