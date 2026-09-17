package stp

import groovy.transform.CompileStatic
import taack.domain.TaackAttachmentService.PreviewFormat

@CompileStatic
final class StepToPreview {
    final String filePath
    final String previewPath
    final String glbPath
    final PreviewFormat format

    StepToPreview(String filePath, String previewPath, String glbPath, PreviewFormat format) {
        this.filePath = filePath
        this.previewPath = previewPath
        this.glbPath = glbPath
        this.format = format
    }
}
