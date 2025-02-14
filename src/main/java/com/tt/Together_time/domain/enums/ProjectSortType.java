package com.tt.Together_time.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ProjectSortType {
    CREATED_DESC(Sort.by(Sort.Order.desc("createdAt"))),
    VIEWS_DESC(Sort.by(Sort.Order.desc("views")));

    private final Sort sort;
}
