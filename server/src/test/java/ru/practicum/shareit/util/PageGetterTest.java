package ru.practicum.shareit.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PageGetterTest {

    @Test
    void getPageRequest() {
        // given
        int from = 3;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // when
        Pageable result = PageGetter.getPageRequest(from, size, sort);

        // then
        assertThat(result.getPageNumber(), equalTo(from / size));
        assertThat(result.getPageSize(), equalTo(size));
        assertThat(result.getSort(), equalTo(sort));
    }

    @Test
    void getPageRequest_whenNegativeFrom() {
        // given
        int from = -1;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // when & then
        assertThatThrownBy(() -> PageGetter.getPageRequest(from, size, sort))
                .isInstanceOf(WrongPageParameterException.class);
    }

    @Test
    void getPageRequest_whenZeroSize() {
        // given
        int from = 0;
        int size = 0;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // when & then
        assertThatThrownBy(() -> PageGetter.getPageRequest(from, size, sort))
                .isInstanceOf(WrongPageParameterException.class);
    }
}