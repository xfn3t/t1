package ru.t1.starter.model;



import java.util.Arrays;
import java.util.Objects;

public class CacheKey {

    private final String className;
    private final String methodName;
    private final Object[] args;

    public CacheKey(String className, String methodName, Object[] args) {
        this.className = className;
        this.methodName = methodName;
        this.args = args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheKey)) return false;
        CacheKey cacheKey = (CacheKey) o;
        return Objects.equals(className, cacheKey.className)
                && Objects.equals(methodName, cacheKey.methodName)
                && Arrays.deepEquals(args, cacheKey.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(className, methodName);
        result = 31 * result + Arrays.deepHashCode(args);
        return result;
    }
}