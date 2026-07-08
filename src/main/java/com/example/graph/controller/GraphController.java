package com.example.graph.controller;

import com.example.graph.dto.ChildrenTransactionsResponse;
import com.example.graph.dto.NodeResponse;
import com.example.graph.service.IGraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph")
public class GraphController {
    private final IGraphService graphService;

    public GraphController(IGraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/nodes/{id}")
    public NodeResponse getNode(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int maxDepth
    ) {
        return graphService.getNode(id, maxDepth);
    }

    @GetMapping("/nodes/{id}/children-transactions")
    public ChildrenTransactionsResponse getFilteredChildrenTransactions(
            @PathVariable String id,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String txnType
    ) {
        return graphService.getFilteredChildrenTransactions(id, minAmount, maxAmount, txnType);
    }
}
