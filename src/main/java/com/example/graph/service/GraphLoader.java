package com.example.graph.service;

import com.example.graph.model.GraphNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class GraphLoader implements IGraphLoader {
    private final Map<String, GraphNode> nodeById;
    private final Map<String, List<GraphNode>> childrenByParentId;

    public GraphLoader(ObjectMapper objectMapper) throws IOException {
        GraphData graphData = objectMapper.readValue(
                new ClassPathResource("transactions-graph.json").getInputStream(),
                GraphData.class
        );

        Map<String, GraphNode> nodes = new LinkedHashMap<>();
        Map<String, List<GraphNode>> children = new HashMap<>();

        for (GraphNode node : graphData.getNodes()) {
            nodes.put(node.getId(), node);
            if (node.getParentId() != null && !node.getParentId().isBlank()) {
                children.computeIfAbsent(node.getParentId(), ignored -> new ArrayList<>()).add(node);
            }
        }

        this.nodeById = Collections.unmodifiableMap(nodes);
        this.childrenByParentId = Collections.unmodifiableMap(children);
    }

    public Map<String, GraphNode> getNodeById() {
        return nodeById;
    }

    public List<GraphNode> getChildren(String parentId) {
        return childrenByParentId.getOrDefault(parentId, List.of());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GraphData {
        private List<GraphNode> nodes = List.of();

        public List<GraphNode> getNodes() {
            return nodes == null ? List.of() : nodes;
        }

        public void setNodes(List<GraphNode> nodes) {
            this.nodes = nodes == null ? List.of() : nodes;
        }
    }
}
