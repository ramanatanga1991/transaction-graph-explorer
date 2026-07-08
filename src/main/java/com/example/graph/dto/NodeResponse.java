package com.example.graph.dto;

import com.example.graph.model.NodeTransaction;
import java.util.List;

public record NodeResponse(
        String id,
        String parentId,
        String name,
        String accountNumber,
        int level,
        boolean isRoot,
        boolean isLeaf,
        List<ParentNodeDto> parentChain,
        List<ChildNodeDto> children,
        List<NodeTransaction> transactions,
        List<NodeTransaction> nextLevelTransactions,
        NodeTreeDto childrenTree,
        List<AggregateLevelDto> aggregates
) {
}
