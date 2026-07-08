# Design

## Loading

`GraphLoader` reads `transactions-graph-nodes.json` from the classpath with Jackson during startup. It builds:

- `nodeById`: lookup by graph node id
- `childrenByParentId`: direct child lookup by parent id

The service keeps everything in memory because the assignment data is static and small.

## Level and Parent Chain

`GraphService` walks the `parentId` chain from a requested node upward. If a parent is missing, the node is treated as root-like. The collected parent nodes are reversed so the response chain is ordered from root to direct parent.

Cycle detection uses a set of visited node ids. If a parent appears twice, `CycleDetectedException` is thrown and returned as HTTP 400.

## Children and Tree Traversal

Direct children come from `childrenByParentId`. For `childrenTree`, the service recursively visits child nodes until `maxDepth` is reached. Each branch carries its own visited path so a cycle is reported clearly without blocking valid sibling branches.

## Transactions

Node transactions are returned from the requested node. Next-level transactions are the concatenated transactions from direct child nodes.

The optional `children-transactions` endpoint filters only direct child transactions by:

- `minAmount`
- `maxAmount`
- `txnType`

## Aggregates

Aggregates are computed for the subtree rooted at the requested node. Level `0` means the requested node, level `1` means direct children, and so on. `totalAmount` is the absolute sum of transaction amounts.

## Scaling Ideas

For a larger dataset, the same model could be moved to a database table keyed by node id with an index on `parent_id`. Frequently requested parent chains or subtree aggregates could be cached. For very deep or broad graphs, iterative BFS traversal would avoid recursion limits and make pagination easier.
