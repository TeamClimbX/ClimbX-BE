package com.climbx.climbx.common.converter;

import com.climbx.climbx.common.exception.InvalidEnumValueException;
import com.climbx.climbx.common.util.OptionalUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    @Override
    @NonNull
    public <T extends Enum<?>> Converter<String, T> getConverter(
        @NonNull
        Class<T> targetType
    ) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        Converter<String, T> converter = new StringToEnumConverter(targetType);
        return converter;
    }

    private static class StringToEnumConverter<T extends Enum<T>> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(
            @NonNull
            String source
        ) {
            return OptionalUtil.tryOf(() -> Enum.valueOf(this.enumType, source.toUpperCase()))
                .orElseThrow(() -> new InvalidEnumValueException(enumType.getName(), source));
        }
    }
}
