package stp

import groovy.transform.CompileStatic
import taack.ui.TaackUiConfiguration

@CompileStatic
class StpConfiguration {
    // cad-dsl launcher, from `./gradlew :cad-dsl-occt:installDist` in cad-builder
    static String cadDslPath = TaackUiConfiguration.home + '/cad-dsl/bin/cad-dsl'
    // libcad-dsl-native.so linked against an OCCT built without Xlib, to render with no display
    // (cad-builder docs/building.adoc#headless)
    static String cadDslNativeDir = TaackUiConfiguration.home + '/cad-dsl-native'
    static long conversionTimeoutMinutes = 5
}
