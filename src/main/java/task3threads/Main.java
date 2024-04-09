package task3threads;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        Fraction fraction = new Fraction(2,3);
        Fractionable num = (Fractionable) Utils.cache(fraction);
        double d;
        num.doubleValue();// sout сработал
        num.doubleValue();// sout молчит
        System.out.println("=========");

        num.setNum(5);
        num.doubleValue();// sout сработал
        num.doubleValue();// sout молчит
        System.out.println("=========");

        num.setNum(2);
        num.doubleValue();// sout молчит
        num.doubleValue();// sout молчит
        System.out.println("=========");

        Thread.sleep(1500);
        num.doubleValue();// sout сработал
        num.doubleValue();// sout молчит

        System.out.println("=========");
        d = (double) num.doubleValue(); // sout молчит
        System.out.println("d = " + d);
    }
}
