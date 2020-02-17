package com.hzsun.mpos.Public;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import struct.JavaStruct;
import struct.StructClass;
import struct.StructException;
import struct.StructField;

public class StructTest {

    @StructClass
    public static class Foo {

        @StructField(order = 0)
        public byte b;
        @StructField(order = 2)
        public char c;

        @StructField(order = 3)
        public int i;

        @StructField(order = 4)
        public byte[] n = new byte[8];

    }

    public static void TestFoo() {
        try {

            List<Foo> list = new ArrayList<Foo>();

            // Pack the class as a byte buffer
            Foo f = new Foo();
            f.b = (byte) 1;
            f.c = 100;
            f.i = 9999;
            for (int i = 0; i < f.n.length; i++) {
                f.n[i] = (byte) (i + 0x30);
            }
            list.add(f);

            Foo f1 = new Foo();
            f1.b = (byte) 2;
            f1.i = 2;
            for (int i = 0; i < f1.n.length; i++) {
                f1.n[i] = (byte) (i + 0x40);
            }
            list.add(f1);

            //byte[] b = JavaStruct.pack(f);
            byte[] b = JavaStruct.pack(f, ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < b.length; i++) {
                System.out.printf("b[%d]: %d\n", i, b[i]);
            }

            Foo getF = list.get(1);
            byte[] b1 = JavaStruct.pack(getF, ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < b1.length; i++) {
                System.out.printf("b[%d]: %d\n", i, b1[i]);
            }


            // Unpack it into an object
            Foo f2 = new Foo();
            JavaStruct.unpack(f2, b, ByteOrder.LITTLE_ENDIAN);
            System.out.println("f2.b: " + f2.b);
            System.out.println("f2.i: " + f2.i);
            System.out.println("f2.n: " + f2.n);

        } catch (StructException e) {
            e.printStackTrace();
        }
    }

}
