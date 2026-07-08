package com.example.graph.service;

import com.example.graph.dto.ParentNodeDto;
import com.example.graph.model.GraphNode;
import java.util.List;

public interface IHierarchyResolver {
    HierarchyInfo resolveHierarchy(GraphNode node);
    
    record HierarchyInfo(int level, boolean isRoot, List<ParentNodeDto> parentChain) {}
}
