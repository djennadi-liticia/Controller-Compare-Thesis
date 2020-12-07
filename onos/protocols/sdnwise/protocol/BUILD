COMPILE_DEPS = CORE_DEPS + [
    "//core/sdnwise:onos-sdnwise",
]

TEST_DEPS = TEST + [
    "//core/api:onos-api-tests",
]

osgi_jar_with_tests(
    name = "onos-protocols-sdnwise-protocol",
    test_deps = TEST_DEPS,
    visibility = ["//visibility:public"],
    deps = COMPILE_DEPS,
)
