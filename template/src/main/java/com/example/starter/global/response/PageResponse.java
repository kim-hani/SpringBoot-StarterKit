package com.example.starter.global.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 API 응답 래퍼
 * - Spring Data JPA의 Page<T>를 Controller 응답으로 직접 노출하지 않도록 변환한다.
 * - Page<T>를 직접 반환하면 pageable, sort 등 불필요한 내부 정보가 노출되므로
 *   필요한 페이징 메타데이터만 추려 응답 구조를 표준화한다.
 * - ApiResponse<PageResponse<T>> 형태로 ApiResponse에 감싸서 반환하는 것을 권장한다.
 */
@Getter
public class PageResponse<T> {

    /** 현재 페이지의 데이터 목록 */
    private final List<T> content;

    /** 현재 페이지 번호 (0-indexed: 첫 페이지는 0) */
    private final int page;

    /** 페이지 당 데이터 수 */
    private final int size;

    /** 전체 데이터 수 */
    private final long totalElements;

    /** 전체 페이지 수 */
    private final int totalPages;

    /** 다음 페이지 존재 여부 */
    private final boolean hasNext;

    /** 이전 페이지 존재 여부 */
    private final boolean hasPrevious;

    private PageResponse(List<T> content, int page, int size,
                         long totalElements, int totalPages,
                         boolean hasNext, boolean hasPrevious) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    /**
     * Spring Data JPA Page<T>를 PageResponse<T>로 변환
     * - Repository에서 반환한 Page 객체를 Controller에서 이 메서드로 변환 후 반환한다.
     * - 사용 예: return ResponseEntity.ok(ApiResponse.success(PageResponse.of(page)));
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
