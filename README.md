# Transaction Graph Explorer

Spring Boot 3 / Java 17 REST service for exploring hierarchical graph nodes and their transactions.

## Run

```bash
mvn spring-boot:run
```

The service loads `src/main/resources/transactions-graph-nodes.json` on startup.

## Endpoints

### Get a graph node

```bash
curl "http://localhost:8080/api/graph/nodes/N1"
```

Optional depth-limited tree:

```bash
curl "http://localhost:8080/api/graph/nodes/N1?maxDepth=3"
```

`maxDepth` defaults to `1` and must be between `0` and `5`.

### Filter direct child transactions

```bash
curl "http://localhost:8080/api/graph/nodes/N1/children-transactions?txnType=POS&minAmount=100&maxAmount=5000"
```

## Response Coverage

`GET /api/graph/nodes/{id}` returns:

- Node details
- Computed hierarchy level
- `isRoot` and `isLeaf`
- Parent chain ordered root to direct parent
- Direct children
- Node transactions
- Direct child transactions
- Depth-limited `childrenTree`
- Subtree aggregates by relative depth

Aggregate totals use the absolute transaction amount for both debit and credit transactions.

## Errors

Missing nodes return HTTP 404:

```json
{
  "error": "NODE_NOT_FOUND",
  "message": "Graph node N999 does not exist"
}
```

Cycles and invalid query parameters return HTTP 400 with a JSON error body.

## Assumptions

- A node with `parentId == null` is a root.
- A node whose parent is missing is treated as root-like: level `0`, empty parent chain, `isRoot = true`.
- Direct children are indexed from the loaded dataset and are not assumed to be sorted in the source JSON.
- Traversal uses visited sets to prevent infinite loops.
