package com.example.graph.exception;

public class NodeNotFoundException extends RuntimeException {
    public NodeNotFoundException(String nodeId) {
        super("Graph node " + nodeId + " does not exist");
    }
}
