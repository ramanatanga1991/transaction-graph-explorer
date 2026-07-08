package com.example.graph.service;

import com.example.graph.dto.ChildrenTransactionsResponse;
import com.example.graph.dto.NodeResponse;

public interface IGraphService {
    NodeResponse getNode(String id, int maxDepth);
    ChildrenTransactionsResponse getFilteredChildrenTransactions(String id, Double minAmount, Double maxAmount, String txnType);
}
