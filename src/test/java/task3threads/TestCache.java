package task3threads;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class TestCache {
    @Test
    @DisplayName("Проверка кэширования")
    public void tstCache() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        Fraction fr = new Fraction(57,1);
        Fractionable num = (Fractionable) Utils.cache(fr);
        Map<State, Map<Method, Result>> tstates;
        State tcurState;
        Map<Method, Result> tcurStateResults;
        // получаем Handler нашего Proxy
        CacheInvHadler numProxy = (CacheInvHadler) Proxy.getInvocationHandler(num);

        Class frcls = fr.getClass();
        Method m = frcls.getDeclaredMethod("doubleValue", null);
        long time = m.getAnnotation(Cache.class).time();

        Class cls = numProxy.getClass();
        Field fstates = cls.getDeclaredField("states");
        fstates.setAccessible(true);
        Field fcurState = cls.getDeclaredField("curState");
        fcurState.setAccessible(true);
        // ----------------
        num.setDenum(9); // Мутатор
        tcurState = (State)fcurState.get(numProxy);
        // Новое состояние должно появиться
        Assertions.assertNotNull(tcurState);
        // Но кэшированного результа по нему нет
        tstates = (Map<State, Map<Method, Result>>)fstates.get(numProxy);
        tcurStateResults = tstates.get(tcurState);
        Assertions.assertNull(tcurStateResults);
        // ----------------
        num.doubleValue(); // Вызвали Cache-метод
        // д.б. закэширован результат с ttl больше текущего (или 0)
        // и значением равным результату того же метода непроксируемого объекта
        tcurStateResults = tstates.get(tcurState);
        Result res = tcurStateResults.get(m);
        if (time == 0) {
            Assertions.assertEquals(res.ttl, 0L);
        } else {
            Assertions.assertTrue (res.ttl > System.currentTimeMillis());
        }
        Assertions.assertEquals(res.value, fr.doubleValue());
    }
}
