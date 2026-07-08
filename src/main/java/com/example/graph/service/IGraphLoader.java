package com.example.graph.service;

import com.example.graph.model.GraphNode;
import java.util.List;
import java.util.Map;

public interface IGraphLoader {
    Map<String, GraphNode> getNodeById();
    List<GraphNode> getChildren(String parentId);
}
