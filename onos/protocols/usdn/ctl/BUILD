COMPILE_DEPS = CORE_DEPS + NETTY + [
    "@openflowj//jar",
    "//protocols/usdn/api:onos-protocols-usdn-api",
    "//protocols/usdn/driver:onos-protocols-usdn-driver",
    "//protocols/usdn/protocol:onos-protocols-usdn-protocol",
    "@io_netty_netty//jar",
    "//core/usdn:onos-usdn",
]

TEST_DEPS = TEST + [
    "//core/api:onos-api-tests",
]

osgi_jar_with_tests(
    name = "onos-protocols-usdn-ctl",
    test_deps = TEST_DEPS,
    visibility = ["//visibility:public"],
    deps = COMPILE_DEPS,
)
