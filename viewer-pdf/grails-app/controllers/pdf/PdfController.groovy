package pdf

import attachement.AttachmentSecurityService
import attachment.Attachment
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.web.api.WebAttributes
import taack.domain.TaackAttachmentService

@GrailsCompileStatic
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class PdfController implements WebAttributes {

    TaackAttachmentService taackAttachmentService
    UnoconvPreviewConverterService unoconvPreviewConverterService
    AttachmentSecurityService attachmentSecurityService

    def previewPdfFromShaOne(String shaOne) {
        render(view: 'previewPdfFile', model: [shaOne: shaOne])
    }

    def downloadBinPdf(String shaOne) {
        Attachment a = Attachment.findByContentShaOne(shaOne)
        if (!a) {
            log.error "No attachment found.."
            return
        }
        if (attachmentSecurityService.canDownloadFile(a)) {
            if (a.originalName.endsWith('pdf') || a.originalName.endsWith('PDF')) {
                taackAttachmentService.downloadAttachment(Attachment.findByContentShaOne(shaOne), params.boolean('inline'))
            } else {
                log.error "$a not a PDF"
            }
        }
    }

    def downloadBinUnoPdf(String shaOne) {
        def f = new File(unoconvPreviewConverterService.pdfDir + "/" + shaOne.strip() + ".pdf")
        def a = Attachment.findByContentShaOne(shaOne)
        if (attachmentSecurityService.canDownloadFile(a)) {
            if (!f.exists()) {
                f = unoconvPreviewConverterService.generateOutput(a, 'pdf')
            }
            response.setContentType("application/pdf")
            response.setHeader("Content-disposition", "inline;filename=${URLEncoder.encode(a.originalName, 'utf-8')}.pdf")
            response.outputStream << f.bytes
            response.outputStream.flush()
            response.outputStream.close()
        }
    }
}
