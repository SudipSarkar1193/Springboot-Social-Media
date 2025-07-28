package com.SSarkar.Xplore.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDTO<T> {

    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean isLast;

}