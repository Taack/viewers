package stp

import attachment.Attachment
import grails.compiler.GrailsCompileStatic
import grails.gsp.PageRenderer
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.taack.IAttachmentPreviewConverter
import org.taack.IAttachmentShowIFrame
import taack.domain.TaackAttachmentService
import taack.domain.TaackAttachmentService.PreviewFormat
import taack.ui.TaackUiConfiguration

@GrailsCompileStatic
class StpPreviewConverterService implements IAttachmentPreviewConverter, IAttachmentShowIFrame {

    static lazyInit = false

    static final List<StepToPreview> singleton = []

    final String intranetRoot = TaackUiConfiguration.root

    String getGlbDir() {
        intranetRoot + '/stp/glb'
    }

    // cad-dsl runs a script from a file, not from the classpath
    String getScriptPath() {
        intranetRoot + '/stp/StepPreview.groovy'
    }

    TaackAttachmentService taackAttachmentService

    @PostConstruct
    void initTaackService() {
        TaackAttachmentService.registerPreviewConverter(this)
        new File(glbDir).mkdirs()
        new File(scriptPath).bytes = getClass().getResourceAsStream('/stp/StepPreview.groovy').bytes
        TaackAttachmentService.registerAdditionalShow(this)
    }

    @Autowired
    PageRenderer g

    @Override
    List<String> getPreviewManagedExtensions() {
        return ['stp', 'step', 'STEP']
    }

    @Override
    void createWebpPreview(Attachment attachment, String previewPath, PreviewFormat previewFormat) {
        String filePath = taackAttachmentService.attachmentPath(attachment)
        if (new File(previewPath).exists() || !new File(filePath).exists()) return
        synchronized (singleton) {
            singleton.push(new StepToPreview(filePath, previewPath, glbDir + '/' + attachment.contentShaOne + '.glb', previewFormat))
        }
    }

    @Override
    List<String> getShowIFrameManagedExtensions() {
        return getPreviewManagedExtensions()
    }

    @Override
    String createShowIFrame(Attachment attachment) {
        g.render(template: '/stp/previewAttachmentGlbFile', model: [shaOne: attachment.contentShaOne.strip()]) as String
    }
}
