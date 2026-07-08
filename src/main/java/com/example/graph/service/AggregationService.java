package com.example.graph.service;

import com.example.graph.dto.AggregateLevelDto;
import com.example.graph.exception.CycleDetectedException;
import com.example.graph.model.GraphNode;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AggregationService implements IAggregationService {
    private final IGraphLoader graphLoader;

    public AggregationService(IGraphLoader graphLoader) {
        this.graphLoader = graphLoader;
    }

    @Override
    public List<AggregateLevelDto> aggregateByRelativeLevel(GraphNode root, int maxDepth) {
        Map<Integer, AggregateAccumulator> byLevel = new LinkedHashMap<>();
        ArrayDeque<NodeAtLevel> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new NodeAtLevel(root, 0));

        while (!queue.isEmpty()) {
            NodeAtLevel item = queue.removeFirst();
            GraphNode node = item.node();
            int level = item.level();

            if (!visited.add(node.getId())) {
                throw new CycleDetectedException("Cycle detected while aggregating subtree at node " + node.getId());
            }

            byLevel.computeIfAbsent(level, ignored -> new AggregateAccumulator()).add(node);
            if (level < maxDepth) {
                for (GraphNode child : graphLoader.getChildren(node.getId())) {
                    queue.addLast(new NodeAtLevel(child, level + 1));
                }
            }
        }

        return byLevel.entrySet().stream()
                .map(entry -> entry.getValue().toDto(entry.getKey()))
                .toList();
    }

    private record NodeAtLevel(GraphNode node, int level) {}

    private static class AggregateAccumulator {
        private int nodeCount;
        private int transactionCount;
        private double totalAmount;

        private void add(GraphNode node) {
            nodeCount++;
            transactionCount += node.getTransactions().size();
            totalAmount += node.getTransactions().stream()
                    .mapToDouble(txn -> Math.abs(txn.getAmount()))
                    .sum();
        }

        private AggregateLevelDto toDto(int level) {
            return new AggregateLevelDto(level, nodeCount, transactionCount, totalAmount);
        }
    }
}
