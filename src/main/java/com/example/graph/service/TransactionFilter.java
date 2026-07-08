package com.example.graph.service;

import com.example.graph.model.NodeTransaction;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TransactionFilter implements ITransactionFilter {

    @Override
    public List<NodeTransaction> filter(List<NodeTransaction> transactions, ITransactionFilter.TransactionFilterCriteria criteria) {
        return transactions.stream()
                .filter(txn -> criteria.minAmount() == null || txn.getAmount() >= criteria.minAmount())
                .filter(txn -> criteria.maxAmount() == null || txn.getAmount() <= criteria.maxAmount())
                .filter(txn -> criteria.txnType() == null || criteria.txnType().equalsIgnoreCase(txn.getTxnType()))
                .collect(Collectors.toList());
    }
}
