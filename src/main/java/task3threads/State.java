package task3threads;

import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@EqualsAndHashCode
public class State {
    // состояние - набор свойств (полей)
    private List<Object> lFields = new ArrayList<>();
    public State(Object o){
        Class cls = o.getClass();
        cls.getDeclaredFields();
        Arrays.stream(cls.getDeclaredFields()).forEach(x-> {
            try {
                x.setAccessible(true);
                lFields.add(x.get(o));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    };
}
