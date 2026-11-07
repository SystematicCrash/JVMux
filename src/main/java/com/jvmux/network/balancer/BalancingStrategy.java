package com.jvmux.network.balancer;

public enum BalancingStrategy {
    /**
     * Cycles sequentially through all healthy servers in the cluster.
     */
    ROUND_ROBIN,

    /**
     * Selects servers in a cycle based on assigned static server weights
     * (higher capacity servers handle proportionally more requests).
     */
    WEIGHTED_ROUND_ROBIN,

    /**
     * Selects an arbitrary healthy server randomly with uniform probability.
     */
    RANDOM,

    /**
     * Selects a server randomly with selection probability proportional to static weight.
     */
    WEIGHTED_RANDOM,

    /**
     * Routes traffic to the backend node with the lowest number of concurrent active connections.
     */
    LEAST_CONNECTIONS,

    /**
     * Routes traffic to the server with the lowest ratio of active connections to assigned weight.
     */
    WEIGHTED_LEAST_CONNECTIONS,

    /**
     * Routes to the server with the lowest response latency / RTT
     * (often combined with active connections).
     */
    LEAST_RESPONSE_TIME,

    /**
     * Peak Exponentially Weighted Moving Average: dynamic scoring based on
     * (activeConnections + 1) * EWMA(latency), avoiding thundering herds.
     */
    PEAK_EWMA,

    /**
     * Two Random Choices (Power of Two Choices): picks two random servers
     * and selects the one with the lower load/active connections.
     */
    POWER_OF_TWO_CHOICES,

    /**
     * Consistent hashing based on a request property (e.g. client IP, header, cookie)
     * to maintain session affinity across node additions/removals.
     */
    CONSISTENT_HASH,

    /**
     * Deterministic hashing on the client's remote IP address.
     */
    IP_HASH
}