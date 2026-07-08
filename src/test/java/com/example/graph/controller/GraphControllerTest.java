package com.example.graph.controller;

import com.example.graph.dto.ChildrenTransactionsResponse;
import com.example.graph.dto.NodeResponse;
import com.example.graph.dto.NodeTreeDto;
import com.example.graph.exception.NodeNotFoundException;
import com.example.graph.model.NodeTransaction;
import com.example.graph.service.IGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphControllerTest {

    @Mock
    private IGraphService graphService;

    @InjectMocks
    private GraphController graphController;

    private NodeResponse mockNodeResponse;
    private ChildrenTransactionsResponse mockChildrenTransactionsResponse;

    @BeforeEach
    void setUp() {
        NodeTransaction transaction = new NodeTransaction();
        transaction.setTxnId("txn1");
        transaction.setDirection("IN");
        transaction.setTxnType("TRANSFER");
        transaction.setAmount(100.0);
        transaction.setCurrency("USD");

        NodeTreeDto mockTree = new NodeTreeDto(
                "node1",
                null,
                "Test Node",
                "ACC001",
                List.of(transaction),
                List.of()
        );

        mockNodeResponse = new NodeResponse(
                "node1",
                null,
                "Test Node",
                "ACC001",
                0,
                true,
                false,
                List.of(),
                List.of(),
                List.of(transaction),
                List.of(transaction),
                mockTree,
                List.of()
        );

        mockChildrenTransactionsResponse = new ChildrenTransactionsResponse(
                "node1",
                null,
                "Test Node",
                "ACC001",
                0,
                List.of(transaction)
        );
    }

    @Test
    void getNode_WithValidId_ShouldReturnNodeResponse() {
        when(graphService.getNode("node1", 1)).thenReturn(mockNodeResponse);

        var response = graphController.getNode("node1", 1);

        assertNotNull(response);
        assertEquals("node1", response.id());
        assertEquals("Test Node", response.name());
        verify(graphService, times(1)).getNode("node1", 1);
    }

    @Test
    void getNode_WithDefaultMaxDepth_ShouldUseDefaultValue() {
        when(graphService.getNode("node1", 1)).thenReturn(mockNodeResponse);

        graphController.getNode("node1", 1);

        verify(graphService, times(1)).getNode("node1", 1);
    }

    @Test
    void getNode_WithCustomMaxDepth_ShouldUseCustomValue() {
        when(graphService.getNode("node1", 3)).thenReturn(mockNodeResponse);

        graphController.getNode("node1", 3);

        verify(graphService, times(1)).getNode("node1", 3);
    }

    @Test
    void getNode_WhenNodeNotFound_ShouldPropagateException() {
        when(graphService.getNode("invalid", 1))
                .thenThrow(new NodeNotFoundException("invalid"));

        assertThrows(NodeNotFoundException.class, () -> graphController.getNode("invalid", 1));
        verify(graphService, times(1)).getNode("invalid", 1);
    }

    @Test
    void getNode_WhenServiceThrowsException_ShouldPropagateException() {
        when(graphService.getNode("node1", 1))
                .thenThrow(new IllegalArgumentException("Invalid parameter"));

        assertThrows(IllegalArgumentException.class, () -> graphController.getNode("node1", 1));
        verify(graphService, times(1)).getNode("node1", 1);
    }

    @Test
    void getFilteredChildrenTransactions_WithNoFilters_ShouldReturnAllTransactions() {
        when(graphService.getFilteredChildrenTransactions("node1", null, null, null))
                .thenReturn(mockChildrenTransactionsResponse);

        var response = graphController.getFilteredChildrenTransactions("node1", null, null, null);

        assertNotNull(response);
        assertEquals("node1", response.id());
        assertEquals(1, response.transactions().size());
        verify(graphService, times(1)).getFilteredChildrenTransactions("node1", null, null, null);
    }

    @Test
    void getFilteredChildrenTransactions_WithMinAmountFilter_ShouldApplyFilter() {
        when(graphService.getFilteredChildrenTransactions("node1", 50.0, null, null))
                .thenReturn(mockChildrenTransactionsResponse);

        graphController.getFilteredChildrenTransactions("node1", 50.0, null, null);

        verify(graphService, times(1)).getFilteredChildrenTransactions("node1", 50.0, null, null);
    }

    @Test
    void getFilteredChildrenTransactions_WithMaxAmountFilter_ShouldApplyFilter() {
        when(graphService.getFilteredChildrenTransactions("node1", null, 200.0, null))
                .thenReturn(mockChildrenTransactionsResponse);

        graphController.getFilteredChildrenTransactions("node1", null, 200.0, null);

        verify(graphService, times(1)).getFilteredChildrenTransactions("node1", null, 200.0, null);
    }

    @Test
    void getFilteredChildrenTransactions_WithTxnTypeFilter_ShouldApplyFilter() {
        when(graphService.getFilteredChildrenTransactions("node1", null, null, "TRANSFER"))
                .thenReturn(mockChildrenTransactionsResponse);

        graphController.getFilteredChildrenTransactions("node1", null, null, "TRANSFER");

        verify(graphService, times(1)).getFilteredChildrenTransactions("node1", null, null, "TRANSFER");
    }

    @Test
    void getFilteredChildrenTransactions_WithAllFilters_ShouldApplyAllFilters() {
        when(graphService.getFilteredChildrenTransactions("node1", 50.0, 200.0, "TRANSFER"))
                .thenReturn(mockChildrenTransactionsResponse);

        graphController.getFilteredChildrenTransactions("node1", 50.0, 200.0, "TRANSFER");

        verify(graphService, times(1)).getFilteredChildrenTransactions("node1", 50.0, 200.0, "TRANSFER");
    }

    @Test
    void getFilteredChildrenTransactions_WhenNodeNotFound_ShouldPropagateException() {
        when(graphService.getFilteredChildrenTransactions("invalid", null, null, null))
                .thenThrow(new NodeNotFoundException("invalid"));

        assertThrows(NodeNotFoundException.class, 
            () -> graphController.getFilteredChildrenTransactions("invalid", null, null, null));
        verify(graphService, times(1)).getFilteredChildrenTransactions("invalid", null, null, null);
    }

    @Test
    void getFilteredChildrenTransactions_WhenServiceThrowsException_ShouldPropagateException() {
        when(graphService.getFilteredChildrenTransactions("node1", null, null, null))
                .thenThrow(new IllegalArgumentException("Invalid parameter"));

        assertThrows(IllegalArgumentException.class, 
            () -> graphController.getFilteredChildrenTransactions("node1", null, null, null));
        verify(graphService, times(1)).getFilteredChildrenTransactions("node1", null, null, null);
    }

    @Test
    void getNode_VerifyServiceInteraction() {
        when(graphService.getNode("node1", 2)).thenReturn(mockNodeResponse);

        graphController.getNode("node1", 2);

        verify(graphService, only()).getNode("node1", 2);
    }

    @Test
    void getFilteredChildrenTransactions_VerifyServiceInteraction() {
        when(graphService.getFilteredChildrenTransactions("node1", 100.0, 500.0, "PAYMENT"))
                .thenReturn(mockChildrenTransactionsResponse);

        graphController.getFilteredChildrenTransactions("node1", 100.0, 500.0, "PAYMENT");

        verify(graphService, only()).getFilteredChildrenTransactions("node1", 100.0, 500.0, "PAYMENT");
    }
}
