COMPILE_DEPS = CORE_DEPS + [
    "//protocols/sdnwise/protocol:onos-protocols-sdnwise-protocol",
    "@io_netty_netty//jar",
]

TEST_DEPS = TEST + [
    "//core/api:onos-api-tests",
]

osgi_jar_with_tests(
    name = "onos-protocols-sdnwise-api",
    test_deps = TEST_DEPS,
    visibility = ["//visibility:public"],
    deps = COMPILE_DEPS,
)
