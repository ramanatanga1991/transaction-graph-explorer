package com.example.graph.service;

import com.example.graph.model.NodeTransaction;
import java.util.List;

public interface ITransactionFilter {
    List<NodeTransaction> filter(List<NodeTransaction> transactions, TransactionFilterCriteria criteria);
    
    record TransactionFilterCriteria(Double minAmount, Double maxAmount, String txnType) {}
}
