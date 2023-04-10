package ru.practicum.shareit.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.validation.ValidationException;

@Slf4j
public class PageRequestUtils {

    private static void validatePageRequestParams(Integer from, Integer size) {
        if (size != null && size <= 0) {
            log.warn("Параметр size должен быть больше 0 или равен null!");
            throw new ValidationException("Параметр size должен быть больше 0 или равен null!");
        }

        if (from != null && from < 0) {
            log.warn("Параметр from должен быть >= 0 или равен null!");
            throw new ValidationException("Параметр from должен быть >= 0 или равен null!");
        }
    }

    public static PageRequest getPageRequest(Integer from, Integer size, Sort sort) {
        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE, sort);

        validatePageRequestParams(from, size);
        if (from != null && size != null) {
            pageRequest = PageRequest.of(from / size, size, sort);
        }

        return pageRequest;
    }

    public static PageRequest getPageRequest(Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);

        if (from != null && size != null) {
            validatePageRequestParams(from, size);
            pageRequest = PageRequest.of(from / size, size);
        }

        return pageRequest;
    }
}
