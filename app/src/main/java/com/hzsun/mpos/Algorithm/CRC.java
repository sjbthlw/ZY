/**
 *
 */
package com.hzsun.mpos.Algorithm;

public class CRC {

    static byte[] crc8_tab = {(byte) 0X00, (byte) 0X5E, (byte) 0XBC, (byte) 0XE2, (byte) 0X61, (byte) 0X3F, (byte) 0XDD, (byte) 0X83, (byte) 0XC2,
            (byte) 0X9C, (byte) 0X7E, (byte) 0X20, (byte) 0XA3, (byte) 0XFD, (byte) 0X1F, (byte) 0X41, (byte) 0X9D, (byte) 0XC3, (byte) 0X21,
            (byte) 0X7F, (byte) 0XFC, (byte) 0XA2, (byte) 0X40, (byte) 0X1E, (byte) 0X5F, (byte) 0X01, (byte) 0XE3, (byte) 0XBD, (byte) 0X3E,
            (byte) 0X60, (byte) 0X82, (byte) 0XDC, (byte) 0X23, (byte) 0X7D, (byte) 0X9F, (byte) 0XC1, (byte) 0X42, (byte) 0X1C, (byte) 0XFE,
            (byte) 0XA0, (byte) 0XE1, (byte) 0XBF, (byte) 0X5D, (byte) 0X03, (byte) 0X80, (byte) 0XDE, (byte) 0X3C, (byte) 0X62, (byte) 0XBE,
            (byte) 0XE0, (byte) 0X02, (byte) 0X5C, (byte) 0XDF, (byte) 0X81, (byte) 0X63, (byte) 0X3D, (byte) 0X7C, (byte) 0X22, (byte) 0XC0,
            (byte) 0X9E, (byte) 0X1D, (byte) 0X43, (byte) 0XA1, (byte) 0XFF, (byte) 0X46, (byte) 0X18, (byte) 0XFA, (byte) 0XA4, (byte) 0X27,
            (byte) 0X79, (byte) 0X9B, (byte) 0XC5, (byte) 0X84, (byte) 0XDA, (byte) 0X38, (byte) 0X66, (byte) 0XE5, (byte) 0XBB, (byte) 0X59,
            (byte) 0X07, (byte) 0XDB, (byte) 0X85, (byte) 0X67, (byte) 0X39, (byte) 0XBA, (byte) 0XE4, (byte) 0X06, (byte) 0X58, (byte) 0X19,
            (byte) 0X47, (byte) 0XA5, (byte) 0XFB, (byte) 0X78, (byte) 0X26, (byte) 0XC4, (byte) 0X9A, (byte) 0X65, (byte) 0X3B, (byte) 0XD9,
            (byte) 0X87, (byte) 0X04, (byte) 0X5A, (byte) 0XB8, (byte) 0XE6, (byte) 0XA7, (byte) 0XF9, (byte) 0X1B, (byte) 0X45, (byte) 0XC6,
            (byte) 0X98, (byte) 0X7A, (byte) 0X24, (byte) 0XF8, (byte) 0XA6, (byte) 0X44, (byte) 0X1A, (byte) 0X99, (byte) 0XC7, (byte) 0X25,
            (byte) 0X7B, (byte) 0X3A, (byte) 0X64, (byte) 0X86, (byte) 0XD8, (byte) 0X5B, (byte) 0X05, (byte) 0XE7, (byte) 0XB9, (byte) 0X8C,
            (byte) 0XD2, (byte) 0X30, (byte) 0X6E, (byte) 0XED, (byte) 0XB3, (byte) 0X51, (byte) 0X0F, (byte) 0X4E, (byte) 0X10, (byte) 0XF2,
            (byte) 0XAC, (byte) 0X2F, (byte) 0X71, (byte) 0X93, (byte) 0XCD, (byte) 0X11, (byte) 0X4F, (byte) 0XAD, (byte) 0XF3, (byte) 0X70,
            (byte) 0X2E, (byte) 0XCC, (byte) 0X92, (byte) 0XD3, (byte) 0X8D, (byte) 0X6F, (byte) 0X31, (byte) 0XB2, (byte) 0XEC, (byte) 0X0E,
            (byte) 0X50, (byte) 0XAF, (byte) 0XF1, (byte) 0X13, (byte) 0X4D, (byte) 0XCE, (byte) 0X90, (byte) 0X72, (byte) 0X2C, (byte) 0X6D,
            (byte) 0X33, (byte) 0XD1, (byte) 0X8F, (byte) 0X0C, (byte) 0X52, (byte) 0XB0, (byte) 0XEE, (byte) 0X32, (byte) 0X6C, (byte) 0X8E,
            (byte) 0XD0, (byte) 0X53, (byte) 0X0D, (byte) 0XEF, (byte) 0XB1, (byte) 0XF0, (byte) 0XAE, (byte) 0X4C, (byte) 0X12, (byte) 0X91,
            (byte) 0XCF, (byte) 0X2D, (byte) 0X73, (byte) 0XCA, (byte) 0X94, (byte) 0X76, (byte) 0X28, (byte) 0XAB, (byte) 0XF5, (byte) 0X17,
            (byte) 0X49, (byte) 0X08, (byte) 0X56, (byte) 0XB4, (byte) 0XEA, (byte) 0X69, (byte) 0X37, (byte) 0XD5, (byte) 0X8B, (byte) 0X57,
            (byte) 0X09, (byte) 0XEB, (byte) 0XB5, (byte) 0X36, (byte) 0X68, (byte) 0X8A, (byte) 0XD4, (byte) 0X95, (byte) 0XCB, (byte) 0X29,
            (byte) 0X77, (byte) 0XF4, (byte) 0XAA, (byte) 0X48, (byte) 0X16, (byte) 0XE9, (byte) 0XB7, (byte) 0X55, (byte) 0X0B, (byte) 0X88,
            (byte) 0XD6, (byte) 0X34, (byte) 0X6A, (byte) 0X2B, (byte) 0X75, (byte) 0X97, (byte) 0XC9, (byte) 0X4A, (byte) 0X14, (byte) 0XF6,
            (byte) 0XA8, (byte) 0X74, (byte) 0X2A, (byte) 0XC8, (byte) 0X96, (byte) 0X15, (byte) 0X4B, (byte) 0XA9, (byte) 0XF7, (byte) 0XB6,
            (byte) 0XE8, (byte) 0X0A, (byte) 0X54, (byte) 0XD7, (byte) 0X89, (byte) 0X6B, (byte) 0X35};

    static byte[] crc16_tab = {(byte) 0x0000, (byte) 0x1021, (byte) 0x2042, (byte) 0x3063, (byte) 0x4084, (byte) 0x50a5, (byte) 0x60c6, (byte) 0x70e7,
            (byte) 0x8108, (byte) 0x9129, (byte) 0xa14a, (byte) 0xb16b, (byte) 0xc18c, (byte) 0xd1ad, (byte) 0xe1ce, (byte) 0xf1ef,
            (byte) 0x1231, (byte) 0x0210, (byte) 0x3273, (byte) 0x2252, (byte) 0x52b5, (byte) 0x4294, (byte) 0x72f7, (byte) 0x62d6,
            (byte) 0x9339, (byte) 0x8318, (byte) 0xb37b, (byte) 0xa35a, (byte) 0xd3bd, (byte) 0xc39c, (byte) 0xf3ff, (byte) 0xe3de,
            (byte) 0x2462, (byte) 0x3443, (byte) 0x0420, (byte) 0x1401, (byte) 0x64e6, (byte) 0x74c7, (byte) 0x44a4, (byte) 0x5485,
            (byte) 0xa56a, (byte) 0xb54b, (byte) 0x8528, (byte) 0x9509, (byte) 0xe5ee, (byte) 0xf5cf, (byte) 0xc5ac, (byte) 0xd58d,
            (byte) 0x3653, (byte) 0x2672, (byte) 0x1611, (byte) 0x0630, (byte) 0x76d7, (byte) 0x66f6, (byte) 0x5695, (byte) 0x46b4,
            (byte) 0xb75b, (byte) 0xa77a, (byte) 0x9719, (byte) 0x8738, (byte) 0xf7df, (byte) 0xe7fe, (byte) 0xd79d, (byte) 0xc7bc,
            (byte) 0x48c4, (byte) 0x58e5, (byte) 0x6886, (byte) 0x78a7, (byte) 0x0840, (byte) 0x1861, (byte) 0x2802, (byte) 0x3823,
            (byte) 0xc9cc, (byte) 0xd9ed, (byte) 0xe98e, (byte) 0xf9af, (byte) 0x8948, (byte) 0x9969, (byte) 0xa90a, (byte) 0xb92b,
            (byte) 0x5af5, (byte) 0x4ad4, (byte) 0x7ab7, (byte) 0x6a96, (byte) 0x1a71, (byte) 0x0a50, (byte) 0x3a33, (byte) 0x2a12,
            (byte) 0xdbfd, (byte) 0xcbdc, (byte) 0xfbbf, (byte) 0xeb9e, (byte) 0x9b79, (byte) 0x8b58, (byte) 0xbb3b, (byte) 0xab1a,
            (byte) 0x6ca6, (byte) 0x7c87, (byte) 0x4ce4, (byte) 0x5cc5, (byte) 0x2c22, (byte) 0x3c03, (byte) 0x0c60, (byte) 0x1c41,
            (byte) 0xedae, (byte) 0xfd8f, (byte) 0xcdec, (byte) 0xddcd, (byte) 0xad2a, (byte) 0xbd0b, (byte) 0x8d68, (byte) 0x9d49,
            (byte) 0x7e97, (byte) 0x6eb6, (byte) 0x5ed5, (byte) 0x4ef4, (byte) 0x3e13, (byte) 0x2e32, (byte) 0x1e51, (byte) 0x0e70,
            (byte) 0xff9f, (byte) 0xefbe, (byte) 0xdfdd, (byte) 0xcffc, (byte) 0xbf1b, (byte) 0xaf3a, (byte) 0x9f59, (byte) 0x8f78,
            (byte) 0x9188, (byte) 0x81a9, (byte) 0xb1ca, (byte) 0xa1eb, (byte) 0xd10c, (byte) 0xc12d, (byte) 0xf14e, (byte) 0xe16f,
            (byte) 0x1080, (byte) 0x00a1, (byte) 0x30c2, (byte) 0x20e3, (byte) 0x5004, (byte) 0x4025, (byte) 0x7046, (byte) 0x6067,
            (byte) 0x83b9, (byte) 0x9398, (byte) 0xa3fb, (byte) 0xb3da, (byte) 0xc33d, (byte) 0xd31c, (byte) 0xe37f, (byte) 0xf35e,
            (byte) 0x02b1, (byte) 0x1290, (byte) 0x22f3, (byte) 0x32d2, (byte) 0x4235, (byte) 0x5214, (byte) 0x6277, (byte) 0x7256,
            (byte) 0xb5ea, (byte) 0xa5cb, (byte) 0x95a8, (byte) 0x8589, (byte) 0xf56e, (byte) 0xe54f, (byte) 0xd52c, (byte) 0xc50d,
            (byte) 0x34e2, (byte) 0x24c3, (byte) 0x14a0, (byte) 0x0481, (byte) 0x7466, (byte) 0x6447, (byte) 0x5424, (byte) 0x4405,
            (byte) 0xa7db, (byte) 0xb7fa, (byte) 0x8799, (byte) 0x97b8, (byte) 0xe75f, (byte) 0xf77e, (byte) 0xc71d, (byte) 0xd73c,
            (byte) 0x26d3, (byte) 0x36f2, (byte) 0x0691, (byte) 0x16b0, (byte) 0x6657, (byte) 0x7676, (byte) 0x4615, (byte) 0x5634,
            (byte) 0xd94c, (byte) 0xc96d, (byte) 0xf90e, (byte) 0xe92f, (byte) 0x99c8, (byte) 0x89e9, (byte) 0xb98a, (byte) 0xa9ab,
            (byte) 0x5844, (byte) 0x4865, (byte) 0x7806, (byte) 0x6827, (byte) 0x18c0, (byte) 0x08e1, (byte) 0x3882, (byte) 0x28a3,
            (byte) 0xcb7d, (byte) 0xdb5c, (byte) 0xeb3f, (byte) 0xfb1e, (byte) 0x8bf9, (byte) 0x9bd8, (byte) 0xabbb, (byte) 0xbb9a,
            (byte) 0x4a75, (byte) 0x5a54, (byte) 0x6a37, (byte) 0x7a16, (byte) 0x0af1, (byte) 0x1ad0, (byte) 0x2ab3, (byte) 0x3a92,
            (byte) 0xfd2e, (byte) 0xed0f, (byte) 0xdd6c, (byte) 0xcd4d, (byte) 0xbdaa, (byte) 0xad8b, (byte) 0x9de8, (byte) 0x8dc9,
            (byte) 0x7c26, (byte) 0x6c07, (byte) 0x5c64, (byte) 0x4c45, (byte) 0x3ca2, (byte) 0x2c83, (byte) 0x1ce0, (byte) 0x0cc1,
            (byte) 0xef1f, (byte) 0xff3e, (byte) 0xcf5d, (byte) 0xdf7c, (byte) 0xaf9b, (byte) 0xbfba, (byte) 0x8fd9, (byte) 0x9ff8,
            (byte) 0x6e17, (byte) 0x7e36, (byte) 0x4e55, (byte) 0x5e74, (byte) 0x2e93, (byte) 0x3eb2, (byte) 0x0ed1, (byte) 0x1ef0};

    static byte[] crc32_tab = {(byte) 0x00000000, (byte) (byte) 0x77073096, (byte) (byte) 0xEE0E612C, (byte) (byte) 0x990951BA,
            (byte) (byte) 0x076DC419, (byte) (byte) 0x706AF48F, (byte) (byte) 0xE963A535, (byte) (byte) 0x9E6495A3, (byte) 0x0EDB8832,
            (byte) 0x79DCB8A4, (byte) 0xE0D5E91E, (byte) 0x97D2D988, (byte) 0x09B64C2B, (byte) 0x7EB17CBD, (byte) 0xE7B82D07, (byte) 0x90BF1D91,
            (byte) 0x1DB71064, (byte) 0x6AB020F2, (byte) 0xF3B97148, (byte) 0x84BE41DE, (byte) 0x1ADAD47D, (byte) 0x6DDDE4EB, (byte) 0xF4D4B551,
            (byte) 0x83D385C7, (byte) 0x136C9856, (byte) 0x646BA8C0, (byte) 0xFD62F97A, (byte) 0x8A65C9EC, (byte) 0x14015C4F, (byte) 0x63066CD9,
            (byte) 0xFA0F3D63, (byte) 0x8D080DF5, (byte) 0x3B6E20C8, (byte) 0x4C69105E, (byte) 0xD56041E4, (byte) 0xA2677172, (byte) 0x3C03E4D1,
            (byte) 0x4B04D447, (byte) 0xD20D85FD, (byte) 0xA50AB56B, (byte) 0x35B5A8FA, (byte) 0x42B2986C, (byte) 0xDBBBC9D6, (byte) 0xACBCF940,
            (byte) 0x32D86CE3, (byte) 0x45DF5C75, (byte) 0xDCD60DCF, (byte) 0xABD13D59, (byte) 0x26D930AC, (byte) 0x51DE003A, (byte) 0xC8D75180,
            (byte) 0xBFD06116, (byte) 0x21B4F4B5, (byte) 0x56B3C423, (byte) 0xCFBA9599, (byte) 0xB8BDA50F, (byte) 0x2802B89E, (byte) 0x5F058808,
            (byte) 0xC60CD9B2, (byte) 0xB10BE924, (byte) 0x2F6F7C87, (byte) 0x58684C11, (byte) 0xC1611DAB, (byte) 0xB6662D3D, (byte) 0x76DC4190,
            (byte) 0x01DB7106, (byte) 0x98D220BC, (byte) 0xEFD5102A, (byte) 0x71B18589, (byte) 0x06B6B51F, (byte) 0x9FBFE4A5, (byte) 0xE8B8D433,
            (byte) 0x7807C9A2, (byte) 0x0F00F934, (byte) 0x9609A88E, (byte) 0xE10E9818, (byte) 0x7F6A0DBB, (byte) 0x086D3D2D, (byte) 0x91646C97,
            (byte) 0xE6635C01, (byte) 0x6B6B51F4, (byte) 0x1C6C6162, (byte) 0x856530D8, (byte) 0xF262004E, (byte) 0x6C0695ED, (byte) 0x1B01A57B,
            (byte) 0x8208F4C1, (byte) 0xF50FC457, (byte) 0x65B0D9C6, (byte) 0x12B7E950, (byte) 0x8BBEB8EA, (byte) 0xFCB9887C, (byte) 0x62DD1DDF,
            (byte) 0x15DA2D49, (byte) 0x8CD37CF3, (byte) 0xFBD44C65, (byte) 0x4DB26158, (byte) 0x3AB551CE, (byte) 0xA3BC0074, (byte) 0xD4BB30E2,
            (byte) 0x4ADFA541, (byte) 0x3DD895D7, (byte) 0xA4D1C46D, (byte) 0xD3D6F4FB, (byte) 0x4369E96A, (byte) 0x346ED9FC, (byte) 0xAD678846,
            (byte) 0xDA60B8D0, (byte) 0x44042D73, (byte) 0x33031DE5, (byte) 0xAA0A4C5F, (byte) 0xDD0D7CC9, (byte) 0x5005713C, (byte) 0x270241AA,
            (byte) 0xBE0B1010, (byte) 0xC90C2086, (byte) 0x5768B525, (byte) 0x206F85B3, (byte) 0xB966D409, (byte) 0xCE61E49F, (byte) 0x5EDEF90E,
            (byte) 0x29D9C998, (byte) 0xB0D09822, (byte) 0xC7D7A8B4, (byte) 0x59B33D17, (byte) 0x2EB40D81, (byte) 0xB7BD5C3B, (byte) 0xC0BA6CAD,
            (byte) 0xEDB88320, (byte) 0x9ABFB3B6, (byte) 0x03B6E20C, (byte) 0x74B1D29A, (byte) 0xEAD54739, (byte) 0x9DD277AF, (byte) 0x04DB2615,
            (byte) 0x73DC1683, (byte) 0xE3630B12, (byte) 0x94643B84, (byte) 0x0D6D6A3E, (byte) 0x7A6A5AA8, (byte) 0xE40ECF0B, (byte) 0x9309FF9D,
            (byte) 0x0A00AE27, (byte) 0x7D079EB1, (byte) 0xF00F9344, (byte) 0x8708A3D2, (byte) 0x1E01F268, (byte) 0x6906C2FE, (byte) 0xF762575D,
            (byte) 0x806567CB, (byte) 0x196C3671, (byte) 0x6E6B06E7, (byte) 0xFED41B76, (byte) 0x89D32BE0, (byte) 0x10DA7A5A, (byte) 0x67DD4ACC,
            (byte) 0xF9B9DF6F, (byte) 0x8EBEEFF9, (byte) 0x17B7BE43, (byte) 0x60B08ED5, (byte) 0xD6D6A3E8, (byte) 0xA1D1937E, (byte) 0x38D8C2C4,
            (byte) 0x4FDFF252, (byte) 0xD1BB67F1, (byte) 0xA6BC5767, (byte) 0x3FB506DD, (byte) 0x48B2364B, (byte) 0xD80D2BDA, (byte) 0xAF0A1B4C,
            (byte) 0x36034AF6, (byte) 0x41047A60, (byte) 0xDF60EFC3, (byte) 0xA867DF55, (byte) 0x316E8EEF, (byte) 0x4669BE79, (byte) 0xCB61B38C,
            (byte) 0xBC66831A, (byte) 0x256FD2A0, (byte) 0x5268E236, (byte) 0xCC0C7795, (byte) 0xBB0B4703, (byte) 0x220216B9, (byte) 0x5505262F,
            (byte) 0xC5BA3BBE, (byte) 0xB2BD0B28, (byte) 0x2BB45A92, (byte) 0x5CB36A04, (byte) 0xC2D7FFA7, (byte) 0xB5D0CF31, (byte) 0x2CD99E8B,
            (byte) 0x5BDEAE1D, (byte) 0x9B64C2B0, (byte) 0xEC63F226, (byte) 0x756AA39C, (byte) 0x026D930A, (byte) 0x9C0906A9, (byte) 0xEB0E363F,
            (byte) 0x72076785, (byte) 0x05005713, (byte) 0x95BF4A82, (byte) 0xE2B87A14, (byte) 0x7BB12BAE, (byte) 0x0CB61B38, (byte) 0x92D28E9B,
            (byte) 0xE5D5BE0D, (byte) 0x7CDCEFB7, (byte) 0x0BDBDF21, (byte) 0x86D3D2D4, (byte) 0xF1D4E242, (byte) 0x68DDB3F8, (byte) 0x1FDA836E,
            (byte) 0x81BE16CD, (byte) 0xF6B9265B, (byte) 0x6FB077E1, (byte) 0x18B74777, (byte) 0x88085AE6, (byte) 0xFF0F6A70, (byte) 0x66063BCA,
            (byte) 0x11010B5C, (byte) 0x8F659EFF, (byte) 0xF862AE69, (byte) 0x616BFFD3, (byte) 0x166CCF45, (byte) 0xA00AE278, (byte) 0xD70DD2EE,
            (byte) 0x4E048354, (byte) 0x3903B3C2, (byte) 0xA7672661, (byte) 0xD06016F7, (byte) 0x4969474D, (byte) 0x3E6E77DB, (byte) 0xAED16A4A,
            (byte) 0xD9D65ADC, (byte) 0x40DF0B66, (byte) 0x37D83BF0, (byte) 0xA9BCAE53, (byte) 0xDEBB9EC5, (byte) 0x47B2CF7F, (byte) 0x30B5FFE9,
            (byte) 0xBDBDF21C, (byte) 0xCABAC28A, (byte) 0x53B39330, (byte) 0x24B4A3A6, (byte) 0xBAD03605, (byte) 0xCDD70693, (byte) 0x54DE5729,
            (byte) 0x23D967BF, (byte) 0xB3667A2E, (byte) 0xC4614AB8, (byte) 0x5D681B02, (byte) 0x2A6F2B94, (byte) 0xB40BBE37, (byte) 0xC30C8EA1,
            (byte) 0x5A05DF1B, (byte) 0x2D02EF8D};

    /**
     * 计算数组的CRC8校验值
     *
     * @param data 需要计算的数组
     * @return CRC8校验值
     */
    public static byte calcCrc8(byte[] data) {
        return Crc8(data, 0, data.length, (byte) 0);
    }

    /**
     * 计算CRC8校验值
     *
     * @param data   数据
     * @param offset 起始位置
     * @param len    长度
     * @return 校验值
     */
    public static byte calcCrc8(byte[] data, int offset, int len) {
        return Crc8(data, offset, len, (byte) 0);
    }

    //计算CRC8校验值
    public static byte Crc8(byte[] data, int offset, int len, byte preval) {
        byte ret = preval;
        for (int i = offset; i < (offset + len); ++i) {
            ret = crc8_tab[(0X00FF & (ret ^ data[i]))];
        }
        return ret;
    }

    //计算CRC16校验值
    public static int Crc16(byte[] data, int offset, int len, int preval) {
        int crc16 = preval;

        for (int i = offset; i < len; i++) {
            crc16 = (crc16 << 8) ^ crc16_tab[((crc16 >> 8) ^ data[i]) & 0x00FF];
        }
        return (crc16 ^ (byte) 0xFFFFFFFF);
    }

    //计算CRC32校验值
    public static int Crc32(byte[] data, int offset, int len, int preval) {
        int crc32 = preval;

        for (int i = offset; i < len; i++) {
            crc32 = (crc32 >> 8) ^ crc32_tab[(crc32 ^ data[i]) & 0x00FF];
        }
        return (crc32 ^ (byte) 0xFFFFFFFF);
    }

    //CRC和校验
    public static short CRC_Plus(byte[] buf, long len) {
        short crc = 0;
        long lngCheckSum = 0;

        //校验码
        for (int i = 0; i < len; i++) {
            lngCheckSum = lngCheckSum + buf[i];
        }
        crc = (short) (lngCheckSum & 0xFFFF);

        return crc;
    }


    // 测试
    public static void main(String[] args) {
        byte crc = CRC.calcCrc8(new byte[]{1, 1, 1, 1, 1, 0, 0, 2, 10, 11, 12, 1, 0, 1, 4});
        System.out.println("" + Integer.toHexString(0x00ff & crc));
        System.out.println(Crc16(new byte[]{(byte) 0xFC, 05, 02}, 0, 3, 1));
        System.out.println(Crc32(new byte[]{(byte) 0xFC, 05, 02}, 0, 3, 1));
    }
}