package me.ildarorama.modbuscollector.support;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;

public class MappedProperty<T> extends ReadOnlyStringWrapper {
    private SimpleObjectProperty<T> property;
    public MappedProperty(SimpleObjectProperty<T> property, String field) {
        this.property = property;
    }


}
