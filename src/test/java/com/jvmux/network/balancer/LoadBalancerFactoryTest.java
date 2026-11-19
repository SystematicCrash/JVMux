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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancerFactoryTest {

    @ParameterizedTest
    @MethodSource("strategyToClassProvider")
    void shouldCreateCorrectLoadBalancerInstance(BalancingStrategy strategy, Class<? extends LoadBalancer> expectedClass) {
        LoadBalancer balancer = LoadBalancerFactory.create(strategy);
        assertNotNull(balancer);
        assertInstanceOf(expectedClass, balancer);
    }

    static Stream<Arguments> strategyToClassProvider() {
        return Stream.of(
                Arguments.of(BalancingStrategy.ROUND_ROBIN, RoundRobinBalancer.class),
                Arguments.of(BalancingStrategy.WEIGHTED_ROUND_ROBIN, WeightedRoundRobinBalancer.class),
                Arguments.of(BalancingStrategy.RANDOM, RandomBalancer.class),
                Arguments.of(BalancingStrategy.WEIGHTED_RANDOM, WeightedRandomBalancer.class),
                Arguments.of(BalancingStrategy.LEAST_CONNECTIONS, LeastConnectionBalancer.class),
                Arguments.of(BalancingStrategy.WEIGHTED_LEAST_CONNECTIONS, WeightedLeastConnectionBalancer.class),
                Arguments.of(BalancingStrategy.POWER_OF_TWO_CHOICES, PowerOfTwoChoicesBalancer.class),
                Arguments.of(BalancingStrategy.CONSISTENT_HASH, ConsistentHashBalancer.class),
                Arguments.of(BalancingStrategy.IP_HASH, IpHashBalancer.class),
                Arguments.of(BalancingStrategy.PEAK_EWMA, PeakEWMABalancer.class)
        );
    }

    @Test
    void shouldThrowUnsupportedOperationExceptionForLeastResponseTime() {
        assertThrows(UnsupportedOperationException.class, () ->
                LoadBalancerFactory.create(BalancingStrategy.LEAST_RESPONSE_TIME)
        );
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStrategyIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                LoadBalancerFactory.create(null)
        );
    }

    @Test
    void shouldCreateDefaultRoundRobinBalancerWhenNoArgProvided() {
        LoadBalancer balancer = LoadBalancerFactory.create();
        assertNotNull(balancer);
        assertInstanceOf(RoundRobinBalancer.class, balancer);
    }
}
