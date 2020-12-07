BUNDLES = [
    "@openflowj//jar",
    "//core/usdn:onos-usdn",
    "//protocols/usdn/api:onos-protocols-usdn-api",
    "//protocols/usdn/ctl:onos-protocols-usdn-ctl",
    "//protocols/usdn/driver:onos-protocols-usdn-driver",
    "//protocols/usdn/protocol:onos-protocols-usdn-protocol",
    "//providers/usdn/device:onos-providers-usdn-device",
    "//providers/usdn/devicecontrol:onos-providers-usdn-devicecontrol",
    "//providers/usdn/sensornode:onos-providers-usdn-sensornode",
    "//providers/usdn/packet:onos-providers-usdn-packet",
    "//providers/usdn/flow:onos-providers-usdn-flow",
]

APPS = [
    "org.onosproject.hostprovider",
    "org.onosproject.lldpprovider",
]

onos_app(
    app_name = "org.onosproject.usdn",
    category = "Provider",
    description = "USDN Adapter application.",
    included_bundles = BUNDLES,
    required_apps = APPS,
    title = "USDN Adapter",
    url = "https://github.com/mbaddeley/usdn",
)
