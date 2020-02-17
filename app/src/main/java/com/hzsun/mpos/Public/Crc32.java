package com.hzsun.mpos.Public;

public class Crc32 {


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


    public static int crc32(byte[] data, int offset, int len, int preval) {
        int crc32 = preval;

        for (int i = offset; i < len; i++) {
            crc32 = (crc32 >> 8) ^ crc32_tab[(crc32 ^ data[i]) & 0x00FF];
        }
        return (crc32 ^ (byte) 0xFFFFFFFF);
    }

    public static void main(String[] args) {
        System.out.println(crc32(new byte[]{(byte) 0xFC, 05, 02}, 0, 3, 1));
    }
}
