package com.example.graph.service;

import com.example.graph.dto.NodeTreeDto;
import com.example.graph.exception.CycleDetectedException;
import com.example.graph.model.GraphNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TreeBuilderService implements ITreeBuilder {
    private final IGraphLoader graphLoader;

    public TreeBuilderService(IGraphLoader graphLoader) {
        this.graphLoader = graphLoader;
    }

    @Override
    public NodeTreeDto buildTree(GraphNode node, int maxDepth, Set<String> visitedPath) {
        Set<String> path = new HashSet<>(visitedPath);
        if (!path.add(node.getId())) {
            throw new CycleDetectedException("Cycle detected while traversing children at node " + node.getId());
        }

        List<NodeTreeDto> childTrees = new ArrayList<>();
        if (maxDepth > 0) {
            for (GraphNode child : graphLoader.getChildren(node.getId())) {
                childTrees.add(buildTree(child, maxDepth - 1, path));
            }
        }

        return new NodeTreeDto(
                node.getId(),
                node.getParentId(),
                node.getName(),
                node.getAccountNumber(),
                node.getTransactions(),
                childTrees
        );
    }
}
