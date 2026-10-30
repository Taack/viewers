package stp

import attachment.Attachment
import grails.compiler.GrailsCompileStatic
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.annotation.Secured
import taack.domain.TaackAttachmentService
import taack.ui.TaackUiConfiguration

@GrailsCompileStatic
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class StpController {
    TaackAttachmentService taackAttachmentService

    PageRenderer groovyPageRenderer

    final String intranetRoot = TaackUiConfiguration.root

    String getGlbDir() {
        intranetRoot + '/stp/glb'
    }

    final String intranetTaack = TaackUiConfiguration.taack

    def index() {
        redirect action: 'imports'
    }

    def previewStpFromShaOne(String shaOne) {
        render groovyPageRenderer.render(template: '/stp/previewGlbFile2', model: [shaOne: shaOne]) as String
    }

    def hdr() {
//        def hdrFile = taackUiConfiguration.root + '/stp/venice_sunset_1k.hdr'
//        def hdrFile = taackUiConfiguration.root + '/stp/small_harbour_sunset_4k.hdr'
        def hdrFile = intranetTaack + '/stp/autumn_field_puresky_1k.hdr'
        response.setHeader('Cache-Control', 'max-age=31536000')
        response.setHeader('Content-disposition', 'attachment;filename=\'venice_sunset_1k.hdr\'')
        response.outputStream << new File(hdrFile).bytes
        return true
    }

    def stp3dFileContent(String shaOne) {
        def f = new File(glbDir + '/' + shaOne.strip() + '.glb')
        if (!f.exists()) {
            def a = Attachment.findByContentShaOne(shaOne)
            taackAttachmentService.attachmentPreview(a)
//            stpPreviewConverterService.createWebpPreview(a, glbDir + '/' + a.contentShaOne.strip() + '.webp')
        }
        response.setHeader('Content-disposition', "attachment;filename=${shaOne.strip() + '.glb'}")
        response.outputStream << f.bytes
        response.outputStream.flush()
        response.outputStream.close()
    }

}
