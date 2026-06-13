package com.octane.shared.pagination;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void of_computesTotalPages() {
        var page = PageResponse.of(List.of("a", "b"), 0, 2, 5);

        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.content()).containsExactly("a", "b");
    }

    @Test
    void map_transformsContentKeepingMetadata() {
        var page = PageResponse.of(List.of(1, 2), 1, 2, 4);

        var mapped = page.map(String::valueOf);

        assertThat(mapped.content()).containsExactly("1", "2");
        assertThat(mapped.page()).isEqualTo(1);
        assertThat(mapped.totalElements()).isEqualTo(4);
        assertThat(mapped.totalPages()).isEqualTo(2);
    }
}
