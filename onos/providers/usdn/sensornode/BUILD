COMPILE_DEPS = CORE_DEPS + [
    "@openflowj//jar",
    "//core/usdn:onos-usdn",
    "//protocols/usdn/api:onos-protocols-usdn-api",
    "//protocols/usdn/protocol:onos-protocols-usdn-protocol",
]

osgi_jar_with_tests(
    name = "onos-providers-usdn-sensornode",
    deps = COMPILE_DEPS,
)
