package com.example.graph.service;

import com.example.graph.dto.NodeTreeDto;
import com.example.graph.model.GraphNode;
import java.util.Set;

public interface ITreeBuilder {
    NodeTreeDto buildTree(GraphNode node, int maxDepth, Set<String> visitedPath);
}
