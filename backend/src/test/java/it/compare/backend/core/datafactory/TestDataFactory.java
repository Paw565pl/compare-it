package it.compare.backend.core.datafactory;

import java.util.Collection;

public interface TestDataFactory<T> {

    T generate();

    T createOne();

    Collection<T> createMany(int count);

    void clear();
}
