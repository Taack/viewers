package stp

import groovy.transform.CompileStatic
import taack.ui.TaackUiConfiguration

@CompileStatic
class StpConfiguration {
    static String freecadPath = TaackUiConfiguration.home + '/freecad-link'
    static Boolean xvfbRun = false
    static Boolean singleInstance = true
    static Boolean offscreen = true
    static Boolean useWeston = true
}
