package com.example.graph.dto;

import com.example.graph.model.NodeTransaction;
import java.util.List;

public record ChildNodeDto(
        String id,
        String parentId,
        String name,
        String accountNumber,
        List<NodeTransaction> transactions
) {
}
