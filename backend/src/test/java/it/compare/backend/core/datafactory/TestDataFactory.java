package it.compare.backend.core.datafactory;

public interface TestDataFactory<T> {

    T createOne();

    Iterable<T> createMany(int count);
}
