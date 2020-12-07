COMPILE_DEPS = CORE_DEPS + [
    "//core/usdn:onos-usdn",
]

TEST_DEPS = TEST + [
    "//core/api:onos-api-tests",
]

osgi_jar_with_tests(
    name = "onos-protocols-usdn-protocol",
    test_deps = TEST_DEPS,
    visibility = ["//visibility:public"],
    deps = COMPILE_DEPS,
)
