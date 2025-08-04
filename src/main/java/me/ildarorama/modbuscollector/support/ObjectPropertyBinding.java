package me.ildarorama.modbuscollector.support;

import javafx.beans.property.ReadOnlyObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

public class ObjectPropertyBinding<V> implements Callable<String> {
    private static final Logger log = LoggerFactory.getLogger(ObjectPropertyBinding.class);
    private final ReadOnlyObjectProperty<V> object;
    private final String property;
    private Field field = null;

    public ObjectPropertyBinding(ReadOnlyObjectProperty<V> object, String property) {
        this.object = object;
        this.property = property;
    }

    @Override
    public String call() throws Exception {
        V val = object.getValue();
        if (val != null) {
            try {
                if (field == null) {
                    initField(val);
                }
                return String.valueOf(field.get(val));
            } catch (Exception e) {
                log.info("Can not get field", e);
            }
        }
        return "";
    }

    private synchronized void initField(V val) throws NoSuchFieldException {
        if (field == null) {
            field = val.getClass().getDeclaredField(property);
            field.setAccessible(true);
        }
    }
}
