package com.jvmux.network.balancer;

import com.jvmux.network.model.Cluster;
import com.jvmux.network.model.Server;

public interface LoadBalancer {

    Server select(Cluster cluster);
}
