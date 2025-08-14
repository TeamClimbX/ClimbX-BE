package com.climbx.climbx.common.util;

import java.util.Optional;
import java.util.function.Supplier;

public class OptionalUtil {

    public static <T> Optional<T> tryOf(Supplier<T> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
