COMPILE_DEPS = CORE_DEPS + NETTY + [
    "@openflowj//jar",
    "//protocols/sdnwise/api:onos-protocols-sdnwise-api",
    "//protocols/sdnwise/driver:onos-protocols-sdnwise-driver",
    "//protocols/sdnwise/protocol:onos-protocols-sdnwise-protocol",
    "@io_netty_netty//jar",
    "//core/sdnwise:onos-sdnwise",
]

TEST_DEPS = TEST + [
    "//core/api:onos-api-tests",
]

osgi_jar_with_tests(
    name = "onos-protocols-sdnwise-ctl",
    test_deps = TEST_DEPS,
    visibility = ["//visibility:public"],
    deps = COMPILE_DEPS,
)
