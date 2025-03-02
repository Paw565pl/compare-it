package it.compare.backend.core.datafactory;

import java.util.Collection;

public interface TestDataFactory<T> {

    T createOne();

    Iterable<T> createMany(int count);
    Collection<T> createMany(int count);

}
