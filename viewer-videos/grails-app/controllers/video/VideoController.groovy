package video

import attachement.AttachmentSecurityService
import attachment.Attachment
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.web.api.WebAttributes
import taack.domain.TaackAttachmentService

@GrailsCompileStatic
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class VideoController implements WebAttributes {

    TaackAttachmentService taackAttachmentService
    AttachmentSecurityService attachmentSecurityService

    def previewVideoFromShaOne(String shaOne) {
        Attachment a = Attachment.findByContentShaOne(shaOne)
        if (!a) {
            log.error "No attachment found.."
            return
        }
        a.contentType
        render(template: "/video/previewVideoFile", model: [shaOne: shaOne, contentType: a.contentType]) as String
    }

    def downloadBinVideo(String shaOne) {
        Attachment a = Attachment.findByContentShaOne(shaOne)
        if (!a) {
            log.error "No attachment found.."
            return
        }
        if (attachmentSecurityService.canDownloadFile(a)) {
            taackAttachmentService.downloadAttachment(Attachment.findByContentShaOne(shaOne), params.boolean('inline'))
        }
    }
}
