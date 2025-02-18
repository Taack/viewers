package pdf

import attachment.Attachment
import grails.compiler.GrailsCompileStatic
import grails.gsp.PageRenderer
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.taack.IAttachmentConverter
import org.taack.IAttachmentPreviewConverter
import org.taack.IAttachmentShowIFrame
import taack.domain.TaackAttachmentService
import taack.domain.TaackAttachmentService.PreviewFormat

import javax.annotation.PostConstruct

@GrailsCompileStatic
final class UnoconvPreviewConverterService implements IAttachmentConverter, IAttachmentShowIFrame {

    static lazyInit = false

    static final singleton = new Object()

    @Value('${intranet.root}')
    String intranetRoot

    String getPdfDir() {
        intranetRoot + "/pdf"
    }

    TaackAttachmentService taackAttachmentService

    @PostConstruct
    void initTaackService() {
        TaackAttachmentService.registerConverter(this)
        TaackAttachmentService.registerAdditionalShow(this)
        FileUtils.forceMkdir(new File(pdfDir))
    }

    @Autowired
    PageRenderer g

    List<String> getPreviewManagedExtensions() {
        return getSupportedExtensionConversions().keySet().sort()
    }

    File generateOutput(Attachment attachment, String outputFormat) {
        String filePath = taackAttachmentService.attachmentPath(attachment)
        def pdf = new File(pdfDir + "/" + attachment.contentShaOne + ".$outputFormat").toPath()
        if (pdf.toFile().exists()) return pdf.toFile()
        String unoconvCmd = "unoconv -f $outputFormat -o $pdf ${filePath}"
        println unoconvCmd
        synchronized (singleton) {
            Process pPdf = unoconvCmd.execute()
            pPdf.consumeProcessOutput()
            pPdf.waitForOrKill(30 * 1000)
            pdf.toFile()
        }
    }

    @Override
    Map<String, List<String>> getSupportedExtensionConversions() {
        return [
                'doc' : ['pdf', 'odt'],
                'docx': ['pdf', 'odt'],
                'odt' : ['pdf'],
                'ppt' : ['pdf', 'odp'],
                'pptx': ['pdf', 'odp'],
                'odp' : ['pdf'],
                'xls' : ['pdf', 'ods'],
                'xlsx': ['pdf', 'ods'],
                'xlsm': ['pdf', 'ods'],
                'ods' : ['pdf']
        ]
    }

    @Override
    File convertTo(Attachment attachment, String extensionTo) {
        if (['pdf', 'odt', 'odp', 'ods'].contains(extensionTo))
            return generateOutput(attachment, extensionTo)
        return null
    }

    @Override
    List<String> getShowIFrameManagedExtensions() {
        return getPreviewManagedExtensions()
    }

    @Override
    String createShowIFrame(Attachment attachment) {
        g.render(template: "/pdf/previewAttachmentUnoFile", model: [shaOne: attachment.contentShaOne.strip()]) as String
    }
}
