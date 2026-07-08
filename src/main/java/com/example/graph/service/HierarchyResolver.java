package com.example.graph.service;

import com.example.graph.dto.ParentNodeDto;
import com.example.graph.exception.CycleDetectedException;
import com.example.graph.model.GraphNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class HierarchyResolver implements IHierarchyResolver {
    private final IGraphLoader graphLoader;

    public HierarchyResolver(IGraphLoader graphLoader) {
        this.graphLoader = graphLoader;
    }

    @Override
    public IHierarchyResolver.HierarchyInfo resolveHierarchy(GraphNode node) {
        List<GraphNode> parents = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        GraphNode current = node;
        boolean directParentMissing = false;
        Map<String, GraphNode> nodeByIdMap = graphLoader.getNodeById();

        while (current.getParentId() != null && !current.getParentId().isBlank()) {
            if (!visited.add(current.getId())) {
                throw new CycleDetectedException("Cycle detected while resolving parent chain at node " + current.getId());
            }

            GraphNode parent = nodeByIdMap.get(current.getParentId());
            if (parent == null) {
                directParentMissing = current == node;
                break;
            }

            parents.add(parent);
            current = parent;
        }

        if (directParentMissing) {
            return new IHierarchyResolver.HierarchyInfo(0, true, List.of());
        }

        Collections.reverse(parents);
        List<ParentNodeDto> parentChain = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            GraphNode parent = parents.get(i);
            parentChain.add(new ParentNodeDto(
                    parent.getId(),
                    parent.getParentId(),
                    parent.getName(),
                    parent.getAccountNumber(),
                    i
            ));
        }

        return new IHierarchyResolver.HierarchyInfo(parentChain.size(), node.getParentId() == null || nodeByIdMap.get(node.getParentId()) == null, parentChain);
    }
}
