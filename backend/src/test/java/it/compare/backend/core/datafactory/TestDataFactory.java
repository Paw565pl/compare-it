package it.compare.backend.core.datafactory;

import java.util.List;

public interface TestDataFactory<T> {

    T generate();

    T createOne();

    List<T> createMany(int count);

    void clear();
}
