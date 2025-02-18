package stp

import attachment.Attachment
import grails.compiler.GrailsCompileStatic
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import taack.domain.TaackAttachmentService
import taack.ui.TaackUiConfiguration

@GrailsCompileStatic
@Secured(['ROLE_ADMIN', 'ROLE_STP_USER', 'ROLE_STP_DIRECTOR'])
class StpController {
    TaackAttachmentService taackAttachmentService

    PageRenderer groovyPageRenderer

    @Value('${intranet.root}')
    String intranetRoot

    String getGlbDir() {
        intranetRoot + "/stp/glb"
    }

    @Autowired
    TaackUiConfiguration taackUiConfiguration
    
    def index() {
        redirect action: "imports"
    }

    def previewStpFromShaOne(String shaOne) {
        render groovyPageRenderer.render(template: "/stp/previewGlbFile2", model: [shaOne: shaOne]) as String
    }

    def hdr() {
//        def hdrFile = taackUiConfiguration.root + "/stp/venice_sunset_1k.hdr"
//        def hdrFile = taackUiConfiguration.root + "/stp/small_harbour_sunset_4k.hdr"
        def hdrFile = taackUiConfiguration.root + "/stp/autumn_field_puresky_1k.hdr"
        response.setHeader("Cache-Control", "max-age=31536000")
        response.setHeader("Content-disposition", "attachment;filename=\"venice_sunset_1k.hdr\"")
        response.outputStream << new File(hdrFile).bytes
        return true
    }

    def stp3dFileContent(String shaOne) {
        def f = new File(glbDir + "/" + shaOne.strip() + ".glb")
        if (!f.exists()) {
            def a = Attachment.findByContentShaOne(shaOne)
            taackAttachmentService.attachmentPreview(a)
//            stpPreviewConverterService.createWebpPreview(a, glbDir + "/" + a.contentShaOne.strip() + ".webp")
        }
        //        response.setContentType("application/pdf")
        response.setHeader("Content-disposition", "attachment;filename=\"${shaOne.strip() + ".glb"}\"")
        response.outputStream << f.bytes
        response.outputStream.flush()
        response.outputStream.close()
    }

}
