package com.jvmux;

import com.jvmux.core.http.HttpMethod;
import com.jvmux.core.http.routing.Router;
import com.jvmux.core.http.routing.model.route.Handler;

import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        var router = new Router();

        router.add(Set.of(HttpMethod.GET), "/users/profile", new Handler());
        router.add(Set.of(HttpMethod.POST), "/users/profile", new Handler());
        router.add(Set.of(HttpMethod.DELETE), "/users/profile", new Handler());

        router.build();
    }
}
