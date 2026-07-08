package com.example.graph.service;

import com.example.graph.dto.AggregateLevelDto;
import com.example.graph.model.GraphNode;
import java.util.List;

public interface IAggregationService {
    List<AggregateLevelDto> aggregateByRelativeLevel(GraphNode root, int maxDepth);
}
