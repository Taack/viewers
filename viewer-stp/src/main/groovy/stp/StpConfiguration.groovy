package stp

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.ConfigurationProperties

@CompileStatic
@ConfigurationProperties('stp')
class StpConfiguration {
    String freecadPath
    Boolean xvfbRun
    Boolean singleInstance
    Boolean offscreen
    Boolean useWeston
}
