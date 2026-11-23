package com.jvmux.network.balancer;

import com.jvmux.network.balancer.connection.LeastConnectionBalancer;
import com.jvmux.network.balancer.connection.PowerOfTwoChoicesBalancer;
import com.jvmux.network.balancer.connection.WeightedLeastConnectionBalancer;
import com.jvmux.network.balancer.hash.ConsistentHashBalancer;
import com.jvmux.network.balancer.hash.IpHashBalancer;
import com.jvmux.network.balancer.latency.PeakEWMABalancer;
import com.jvmux.network.balancer.rand.RandomBalancer;
import com.jvmux.network.balancer.rand.RoundRobinBalancer;
import com.jvmux.network.balancer.rand.WeightedRandomBalancer;
import com.jvmux.network.balancer.rand.WeightedRoundRobinBalancer;

public final class LoadBalancerFactory {

    private LoadBalancerFactory() {}

    public static LoadBalancer create(BalancingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("BalancingStrategy cannot be null");
        }

        return switch (strategy) {
            case ROUND_ROBIN -> new RoundRobinBalancer();
            case WEIGHTED_ROUND_ROBIN -> new WeightedRoundRobinBalancer();
            case RANDOM -> new RandomBalancer();
            case WEIGHTED_RANDOM -> new WeightedRandomBalancer();
            case LEAST_CONNECTIONS -> new LeastConnectionBalancer();
            case WEIGHTED_LEAST_CONNECTIONS -> new WeightedLeastConnectionBalancer();
            case POWER_OF_TWO_CHOICES -> new PowerOfTwoChoicesBalancer();
            case CONSISTENT_HASH -> new ConsistentHashBalancer();
            case IP_HASH -> new IpHashBalancer();
            case PEAK_EWMA -> new PeakEWMABalancer();
            case LEAST_RESPONSE_TIME -> throw new UnsupportedOperationException(
                    "Balancing strategy not implemented: " + strategy
            );
        };
    }

    public static LoadBalancer create() {
        return create(BalancingStrategy.ROUND_ROBIN);
    }
}
