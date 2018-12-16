<?xml version="1.0"?>
<recipe>

    <instantiate from="root/src/app_package/ViewModel.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${viewModelName}.kt" />

    <open file="${escapeXmlAttribute(srcOut)}/${viewModelName}.kt" />

    <instantiate from="root/src/app_package/ViewState.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${viewStateName}.kt" />

    <instantiate from="root/src/app_package/Event.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${eventName}.kt" />

    <instantiate from="root/src/app_package/Effect.kt.ftl"
                   to="${escapeXmlAttribute(srcOut)}/${effectName}.kt" />

</recipe>
