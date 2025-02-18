package pdf

import attachement.AttachmentSecurityService
import attachment.Attachment
import grails.compiler.GrailsCompileStatic
import grails.gsp.PageRenderer
import org.codehaus.groovy.runtime.MethodClosure
import org.springframework.beans.factory.annotation.Autowired
import org.taack.IAttachmentShowIFrame
import taack.domain.TaackAttachmentService
import taack.render.TaackUiEnablerService

import javax.annotation.PostConstruct

@GrailsCompileStatic
final class PdfPreviewConverterService implements IAttachmentShowIFrame {

    static lazyInit = false

    AttachmentSecurityService attachmentSecurityService

    private securityClosure(Long id, Map p) {
        if (!id && !p) return true
        if (!id) return true
        attachmentSecurityService.canDownloadFile(Attachment.get(id))
    }


    @Autowired
    PageRenderer g

    @PostConstruct
    void initTaackService() {
        TaackUiEnablerService.securityClosure(
                this.&securityClosure,
                PdfController.&downloadBinPdf as MethodClosure,
                PdfController.&downloadBinUnoPdf as MethodClosure)

        TaackAttachmentService.registerAdditionalShow(this)
    }


    @Override
    List<String> getShowIFrameManagedExtensions() {
        return ['pdf', 'PDF']
    }

    @Override
    String createShowIFrame(Attachment attachment) {
        g.render(template: "/pdf/previewAttachmentPdfFile", model: [shaOne: attachment.contentShaOne.strip()]) as String
    }
}
