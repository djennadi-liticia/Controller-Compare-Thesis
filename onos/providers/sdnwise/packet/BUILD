COMPILE_DEPS = CORE_DEPS + [
    "@openflowj//jar",
    "//core/sdnwise:onos-sdnwise",
    "//protocols/sdnwise/api:onos-protocols-sdnwise-api",
    "//protocols/sdnwise/protocol:onos-protocols-sdnwise-protocol",
]

osgi_jar_with_tests(
    name = "onos-providers-sdnwise-packet",
    deps = COMPILE_DEPS,
)
