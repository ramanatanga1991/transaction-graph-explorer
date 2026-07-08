package com.example.graph.service;

import com.example.graph.exception.CycleDetectedException;
import com.example.graph.exception.NodeNotFoundException;
import com.example.graph.model.GraphNode;
import com.example.graph.model.NodeTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    @Mock
    private IGraphLoader graphLoader;
    @Mock
    private IHierarchyResolver hierarchyResolver;
    @Mock
    private ITreeBuilder treeBuilder;
    @Mock
    private IAggregationService aggregationService;
    @Mock
    private ITransactionFilter transactionFilter;

    @InjectMocks
    private GraphService graphService;

    private GraphNode rootNode;
    private GraphNode childNode1;
    private GraphNode childNode2;
    private GraphNode grandChildNode;
    private NodeTransaction transaction1;
    private NodeTransaction transaction2;

    @BeforeEach
    void setUp() {
        transaction1 = new NodeTransaction();
        transaction1.setTxnId("txn1");
        transaction1.setDirection("IN");
        transaction1.setTxnType("TRANSFER");
        transaction1.setAmount(100.0);
        transaction1.setCurrency("USD");

        transaction2 = new NodeTransaction();
        transaction2.setTxnId("txn2");
        transaction2.setDirection("OUT");
        transaction2.setTxnType("PAYMENT");
        transaction2.setAmount(200.0);
        transaction2.setCurrency("USD");

        grandChildNode = new GraphNode();
        grandChildNode.setId("grandchild1");
        grandChildNode.setParentId("child1");
        grandChildNode.setName("Grand Child 1");
        grandChildNode.setAccountNumber("ACC003");
        grandChildNode.setTransactions(List.of(transaction2));

        childNode1 = new GraphNode();
        childNode1.setId("child1");
        childNode1.setParentId("root");
        childNode1.setName("Child 1");
        childNode1.setAccountNumber("ACC002");
        childNode1.setTransactions(List.of(transaction1));

        childNode2 = new GraphNode();
        childNode2.setId("child2");
        childNode2.setParentId("root");
        childNode2.setName("Child 2");
        childNode2.setAccountNumber("ACC003");
        childNode2.setTransactions(List.of());

        rootNode = new GraphNode();
        rootNode.setId("root");
        rootNode.setParentId(null);
        rootNode.setName("Root Node");
        rootNode.setAccountNumber("ACC001");
        rootNode.setTransactions(List.of());

        Map<String, GraphNode> nodeById = Map.of(
                "root", rootNode,
                "child1", childNode1,
                "child2", childNode2,
                "grandchild1", grandChildNode
        );

        when(graphLoader.getNodeById()).thenReturn(nodeById);
        when(graphLoader.getChildren("root")).thenReturn(List.of(childNode1, childNode2));
        when(graphLoader.getChildren("child1")).thenReturn(List.of(grandChildNode));
        when(graphLoader.getChildren("child2")).thenReturn(List.of());
        when(graphLoader.getChildren("grandchild1")).thenReturn(List.of());
        
        when(hierarchyResolver.resolveHierarchy(any()))
                .thenReturn(new IHierarchyResolver.HierarchyInfo(0, true, List.of()));
        
        when(treeBuilder.buildTree(any(), anyInt(), any()))
                .thenReturn(new com.example.graph.dto.NodeTreeDto("root", null, "Root Node", "ACC001", List.of(), List.of()));
        
        when(aggregationService.aggregateByRelativeLevel(any(), anyInt()))
                .thenReturn(List.of());
        
        when(transactionFilter.filter(any(), any()))
                .thenReturn(List.of(transaction1));
    }

    @Test
    void getNode_WithValidId_ShouldReturnNodeResponse() {
        var response = graphService.getNode("root", 1);

        assertNotNull(response);
        assertEquals("root", response.id());
        assertEquals("Root Node", response.name());
        assertEquals("ACC001", response.accountNumber());
        assertEquals(0, response.level());
        assertTrue(response.isRoot());
        assertEquals(2, response.children().size());
    }

    @Test
    void getNode_WithInvalidId_ShouldThrowNodeNotFoundException() {
        when(graphLoader.getNodeById()).thenReturn(Map.of());

        assertThrows(NodeNotFoundException.class, () -> graphService.getNode("invalid", 1));
    }

    @Test
    void getNode_WithMaxDepthZero_ShouldReturnOnlyDirectChildren() {
        var response = graphService.getNode("root", 0);

        assertNotNull(response);
        verify(treeBuilder).buildTree(eq(rootNode), eq(0), any());
    }

    @Test
    void getNode_WithMaxDepthOne_ShouldReturnOneLevelOfChildren() {
        var response = graphService.getNode("root", 1);

        assertNotNull(response);
        verify(treeBuilder).buildTree(eq(rootNode), eq(1), any());
    }

    @Test
    void getNode_WithInvalidMaxDepth_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> graphService.getNode("root", -1));
        assertThrows(IllegalArgumentException.class, () -> graphService.getNode("root", 6));
    }

    @Test
    void getNode_WithCycle_ShouldThrowCycleDetectedException() {
        when(hierarchyResolver.resolveHierarchy(any()))
                .thenThrow(new CycleDetectedException("Cycle detected"));

        assertThrows(CycleDetectedException.class, () -> graphService.getNode("root", 1));
    }

    @Test
    void getFilteredChildrenTransactions_WithNoFilters_ShouldReturnAllTransactions() {
        when(transactionFilter.filter(any(), any()))
                .thenReturn(List.of(transaction1));

        var response = graphService.getFilteredChildrenTransactions("root", null, null, null);

        assertNotNull(response);
        assertEquals("root", response.id());
        verify(transactionFilter).filter(any(), eq(new ITransactionFilter.TransactionFilterCriteria(null, null, null)));
    }

    @Test
    void getFilteredChildrenTransactions_WithMinAmountFilter_ShouldFilterTransactions() {
        when(transactionFilter.filter(any(), any()))
                .thenReturn(List.of());

        var response = graphService.getFilteredChildrenTransactions("root", 150.0, null, null);

        assertNotNull(response);
        verify(transactionFilter).filter(any(), eq(new ITransactionFilter.TransactionFilterCriteria(150.0, null, null)));
    }

    @Test
    void getFilteredChildrenTransactions_WithMaxAmountFilter_ShouldFilterTransactions() {
        when(transactionFilter.filter(any(), any()))
                .thenReturn(List.of(transaction1));

        var response = graphService.getFilteredChildrenTransactions("root", null, 150.0, null);

        assertNotNull(response);
        verify(transactionFilter).filter(any(), eq(new ITransactionFilter.TransactionFilterCriteria(null, 150.0, null)));
    }

    @Test
    void getFilteredChildrenTransactions_WithTxnTypeFilter_ShouldFilterTransactions() {
        when(transactionFilter.filter(any(), any()))
                .thenReturn(List.of());

        var response = graphService.getFilteredChildrenTransactions("root", null, null, "PAYMENT");

        assertNotNull(response);
        verify(transactionFilter).filter(any(), eq(new ITransactionFilter.TransactionFilterCriteria(null, null, "PAYMENT")));
    }

    @Test
    void getFilteredChildrenTransactions_WithInvalidId_ShouldThrowNodeNotFoundException() {
        when(graphLoader.getNodeById()).thenReturn(Map.of());

        assertThrows(NodeNotFoundException.class, 
            () -> graphService.getFilteredChildrenTransactions("invalid", null, null, null));
    }

    @Test
    void getNode_WithMissingParent_ShouldTreatAsRoot() {
        GraphNode orphanNode = new GraphNode();
        orphanNode.setId("orphan");
        orphanNode.setParentId("nonexistent");
        orphanNode.setName("Orphan Node");
        orphanNode.setAccountNumber("ACC005");
        orphanNode.setTransactions(List.of());

        when(graphLoader.getNodeById()).thenReturn(Map.of("orphan", orphanNode));
        when(graphLoader.getChildren("orphan")).thenReturn(List.of());
        when(hierarchyResolver.resolveHierarchy(eq(orphanNode)))
                .thenReturn(new IHierarchyResolver.HierarchyInfo(0, true, List.of()));

        var response = graphService.getNode("orphan", 1);

        assertNotNull(response);
        assertEquals(0, response.level());
        assertTrue(response.isRoot());
        assertTrue(response.parentChain().isEmpty());
    }

    @Test
    void getNode_AggregateByLevel_ShouldCalculateCorrectAggregates() {
        when(aggregationService.aggregateByRelativeLevel(eq(rootNode), eq(2)))
                .thenReturn(List.of(
                        new com.example.graph.dto.AggregateLevelDto(0, 1, 0, 0.0),
                        new com.example.graph.dto.AggregateLevelDto(1, 2, 1, 100.0),
                        new com.example.graph.dto.AggregateLevelDto(2, 1, 1, 200.0)
                ));

        var response = graphService.getNode("root", 2);

        assertNotNull(response);
        assertNotNull(response.aggregates());
        assertEquals(3, response.aggregates().size());
        
        var level0 = response.aggregates().get(0);
        assertEquals(0, level0.level());
        assertEquals(1, level0.nodeCount());
        assertEquals(0, level0.transactionCount());
        assertEquals(0.0, level0.totalAmount());

        var level1 = response.aggregates().get(1);
        assertEquals(1, level1.level());
        assertEquals(2, level1.nodeCount());
        assertEquals(1, level1.transactionCount());
        assertEquals(100.0, level1.totalAmount());
    }
}
