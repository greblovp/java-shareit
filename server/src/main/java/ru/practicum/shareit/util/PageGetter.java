package ru.practicum.shareit.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageGetter {

    public static Pageable getPageRequest(int from, int size, Sort sort) {
        if (from < 0) {
            throw new WrongPageParameterException("from — индекс первого элемента, не может быть отрицательным");
        }

        if (size < 1) {
            throw new WrongPageParameterException("size — количество элементов для отображения, не может быть меньше 0");
        }

        return PageRequest.of(from > 0 ? from / size : 0, size, sort);
    }
}
