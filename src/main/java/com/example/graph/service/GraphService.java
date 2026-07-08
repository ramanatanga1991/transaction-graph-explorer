package com.example.graph.service;

import com.example.graph.dto.AggregateLevelDto;
import com.example.graph.dto.ChildNodeDto;
import com.example.graph.dto.ChildrenTransactionsResponse;
import com.example.graph.dto.NodeResponse;
import com.example.graph.dto.NodeTreeDto;
import com.example.graph.exception.NodeNotFoundException;
import com.example.graph.model.GraphNode;
import com.example.graph.model.NodeTransaction;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class GraphService implements IGraphService {
    private static final int MAX_ALLOWED_DEPTH = 5;

    private final IGraphLoader graphLoader;
    private final IHierarchyResolver hierarchyResolver;
    private final ITreeBuilder treeBuilder;
    private final IAggregationService aggregationService;
    private final ITransactionFilter transactionFilter;
    private final Map<String, GraphNode> nodeById;

    public GraphService(
            IGraphLoader graphLoader,
            IHierarchyResolver hierarchyResolver,
            ITreeBuilder treeBuilder,
            IAggregationService aggregationService,
            ITransactionFilter transactionFilter
    ) {
        this.graphLoader = graphLoader;
        this.hierarchyResolver = hierarchyResolver;
        this.treeBuilder = treeBuilder;
        this.aggregationService = aggregationService;
        this.transactionFilter = transactionFilter;
        this.nodeById = graphLoader.getNodeById();
    }

    @Override
    public NodeResponse getNode(String id, int maxDepth) {
        validateMaxDepth(maxDepth);
        GraphNode node = findNode(id);
        IHierarchyResolver.HierarchyInfo hierarchy = hierarchyResolver.resolveHierarchy(node);

        List<ChildNodeDto> children = directChildren(node).stream()
                .map(this::toChildDto)
                .toList();

        List<NodeTransaction> nextLevelTransactions = directChildren(node).stream()
                .flatMap(child -> child.getTransactions().stream())
                .toList();

        return new NodeResponse(
                node.getId(),
                node.getParentId(),
                node.getName(),
                node.getAccountNumber(),
                hierarchy.level(),
                hierarchy.isRoot(),
                children.isEmpty(),
                hierarchy.parentChain(),
                children,
                node.getTransactions(),
                nextLevelTransactions,
                treeBuilder.buildTree(node, maxDepth, new HashSet<>()),
                aggregationService.aggregateByRelativeLevel(node, maxDepth)
        );
    }

    @Override
    public ChildrenTransactionsResponse getFilteredChildrenTransactions(
            String id,
            Double minAmount,
            Double maxAmount,
            String txnType
    ) {
        GraphNode node = findNode(id);
        int level = hierarchyResolver.resolveHierarchy(node).level();
        
        List<NodeTransaction> allTransactions = directChildren(node).stream()
                .flatMap(child -> child.getTransactions().stream())
                .toList();

        List<NodeTransaction> filteredTransactions = transactionFilter.filter(
                allTransactions,
                new ITransactionFilter.TransactionFilterCriteria(minAmount, maxAmount, txnType)
        );

        return new ChildrenTransactionsResponse(
                node.getId(),
                node.getParentId(),
                node.getName(),
                node.getAccountNumber(),
                level,
                filteredTransactions
        );
    }

    private GraphNode findNode(String id) {
        GraphNode node = nodeById.get(id);
        if (node == null) {
            throw new NodeNotFoundException(id);
        }
        return node;
    }

    private List<GraphNode> directChildren(GraphNode node) {
        return graphLoader.getChildren(node.getId());
    }

    private ChildNodeDto toChildDto(GraphNode node) {
        return new ChildNodeDto(
                node.getId(),
                node.getParentId(),
                node.getName(),
                node.getAccountNumber(),
                node.getTransactions()
        );
    }

    private void validateMaxDepth(int maxDepth) {
        if (maxDepth < 0 || maxDepth > MAX_ALLOWED_DEPTH) {
            throw new IllegalArgumentException("maxDepth must be between 0 and " + MAX_ALLOWED_DEPTH);
        }
    }
}
