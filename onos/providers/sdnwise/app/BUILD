BUNDLES = [
    "@openflowj//jar",
    "//core/sdnwise:onos-sdnwise",
    "//protocols/sdnwise/api:onos-protocols-sdnwise-api",
    "//protocols/sdnwise/ctl:onos-protocols-sdnwise-ctl",
    "//protocols/sdnwise/driver:onos-protocols-sdnwise-driver",
    "//protocols/sdnwise/protocol:onos-protocols-sdnwise-protocol",
    "//providers/sdnwise/device:onos-providers-sdnwise-device",
    "//providers/sdnwise/devicecontrol:onos-providers-sdnwise-devicecontrol",
    "//providers/sdnwise/sensornode:onos-providers-sdnwise-sensornode",
    "//providers/sdnwise/packet:onos-providers-sdnwise-packet",
    "//providers/sdnwise/flow:onos-providers-sdnwise-flow",
]

APPS = [
    "org.onosproject.hostprovider",
    "org.onosproject.lldpprovider",
]

onos_app(
    app_name = "org.onosproject.sdnwise",
    category = "Provider",
    description = "SDN-WISE southbound meta application.",
    included_bundles = BUNDLES,
    required_apps = APPS,
    title = "SDN-WISE Provider",
    url = "http://sdn-wise.dieei.unict.it",
)
