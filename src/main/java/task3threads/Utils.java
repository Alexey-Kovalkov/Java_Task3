package task3threads;

import java.lang.reflect.Proxy;

public class Utils {
    public static Object cache (Object o) {
        Class cls = o.getClass();
        return Proxy.newProxyInstance(cls.getClassLoader(),
                                        cls.getInterfaces(),
                                        new CacheInvHadler(o));
    }
}
