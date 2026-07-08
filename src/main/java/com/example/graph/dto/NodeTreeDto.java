package com.example.graph.dto;

import com.example.graph.model.NodeTransaction;
import java.util.List;

public record NodeTreeDto(
        String id,
        String parentId,
        String name,
        String accountNumber,
        List<NodeTransaction> transactions,
        List<NodeTreeDto> children
) {
}
