package task3threads;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheInvHadler implements InvocationHandler {
    private Object obj;

    CacheInvHadler(Object obj) {
        this.obj = obj;
    }

    private Map<State, Map<Method, Result>> states = new HashMap<>();
    private State curState;
    private Map<Method, Result> curStateResults;
    private Object objectResult;
    private GarbCleaner garbCleaner;

    private class GarbCleaner extends Thread{
        public void run(){
            Map<task3threads.State, Map<Method, Result>> statesForClear = new HashMap<>(states);
            Map<task3threads.State, Map<Method, Result>> statesNew = new HashMap<>();
            states = statesNew;
            if (!statesForClear.isEmpty()) {
                for (Map<Method, Result> map : statesForClear.values()){
                    for (Method met : map.keySet()){
                        Result result = map.get(met);
                        if (result.ttl < System.currentTimeMillis() && result.ttl != 0){
                            map.remove(met);
                        }
                    }
                }
            }
            Map<task3threads.State, Map<Method, Result>> statesMerge = new HashMap<>(states);
            statesMerge.putAll(statesForClear);
            states = statesMerge;
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method m = obj.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (m.isAnnotationPresent(Cache.class)) {
            // Закэшированные значения для состояний и методов лежат в states, текущее состояние - в curState.
            // Ищем по состоянию и методу - если нашлось и не просрочено - возвращаем.
            long time = m.getAnnotation(Cache.class).time();
            curStateResults = states.get(curState);
            if (curStateResults != null) {
                Result res = curStateResults.get(m);
                if (res != null) { // если нащли
                    // надо проверить "просрочку" - если не просрочено или 0 - возвращаем и всё на этом
                    if (res.ttl > System.currentTimeMillis() || res.ttl == 0) {
                        objectResult = res.value;
                        return objectResult;
                    } else{ // если наткнулись на просрочку - запускаем сборщик мусора (если только он уже не работает)
                        if ((garbCleaner == null) || (!garbCleaner.isAlive())) {
                            garbCleaner = new GarbCleaner();
                            garbCleaner.start();
                            System.out.println("Start cleaner");
                        }
                    }
                }
            }
            // если нет подходящего состояния - вычислить состояние, посчитать метод и кэшировать
            objectResult = method.invoke(obj, args);
            if (curStateResults == null) {
                curState = new State(obj);
                curStateResults = new HashMap<>();
            }
            Result oRes = new Result(System.currentTimeMillis() + time, objectResult);
            if (time == 0) {
                oRes.ttl = 0l;
            }
            curStateResults.put(m, oRes);
            states.put(curState, curStateResults);
            return objectResult; // возвращаем результат
        }

        objectResult = method.invoke(obj, args);
        // Если пришёл "мутатор" и создал новое состояние (вторая пара в примере) - запись с таким состоянием
        // в map-у надо добавить:
        // Сперва исполнить метод-мутатор. Потом пересчитать curState.
        if (m.isAnnotationPresent(Mutator.class)) {
            // считаем состояние
            curState = new State(obj);
            if (!states.containsKey(curState)) {
                states.put(curState, null); // если нет - добавляем.
            }
        }
        return objectResult;
    }
}
