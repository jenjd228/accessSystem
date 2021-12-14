package ru.sfedu.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.List;
import java.util.Objects;

@Root(name = "wrapper")
public class Wrapper<T> {

    @ElementListUnion({
            @ElementList(entry = "animal", type = Animal.class, inline = true),
            @ElementList(entry = "human", type = Human.class, inline = true),
            @ElementList(entry = "transport", type = Transport.class, inline = true)
    })
    private List<T> list;

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void addToList(T subject){
        list.add(subject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wrapper)) return false;
        Wrapper<?> wrapper = (Wrapper<?>) o;
        return Objects.equals(list, wrapper.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    @Override
    public String toString() {
        return "Wrapper{" +
                "list=" + list +
                '}';
    }
}
