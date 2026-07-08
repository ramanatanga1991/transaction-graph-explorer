package com.example.graph.dto;

import com.example.graph.model.NodeTransaction;
import java.util.List;

public record ChildrenTransactionsResponse(
        String id,
        String parentId,
        String name,
        String accountNumber,
        int level,
        List<NodeTransaction> transactions
) {
}
