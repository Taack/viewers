package video

import attachement.AttachmentSecurityService
import attachment.Attachment
import grails.compiler.GrailsCompileStatic
import grails.gsp.PageRenderer
import org.codehaus.groovy.runtime.MethodClosure
import org.springframework.beans.factory.annotation.Autowired
import org.taack.IAttachmentPreviewConverter
import org.taack.IAttachmentShowIFrame
import taack.domain.TaackAttachmentService
import taack.render.TaackUiEnablerService

import javax.annotation.PostConstruct

@GrailsCompileStatic
final class VideoPreviewConverterService implements IAttachmentPreviewConverter, IAttachmentShowIFrame {

    static lazyInit = false

    AttachmentSecurityService attachmentSecurityService
    TaackAttachmentService taackAttachmentService

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
                VideoController.&downloadBinVideo as MethodClosure)

        TaackAttachmentService.registerAdditionalShow(this)
        TaackAttachmentService.registerPreviewConverter(this)
    }


    @Override
    List<String> getShowIFrameManagedExtensions() {
        return ['mp4', 'MP4', 'mpeg4', 'webm', '3gpp', 'mov', 'MOV']
    }

    @Override
    String createShowIFrame(Attachment attachment) {

        println "createShowIFrame $attachment"

        g.render(template: "/video/previewAttachmentVideoFile", model: [shaOne: attachment.contentShaOne.strip()]) as String
    }

    @Override
    List<String> getPreviewManagedExtensions() {
        return getShowIFrameManagedExtensions()
    }

    @Override
    void createWebpPreview(Attachment attachment, String previewPath, TaackAttachmentService.PreviewFormat format) {
        String filePath = taackAttachmentService.attachmentPath(attachment)
        def webp = new File(taackAttachmentService.attachmentPreviewPath(format, attachment))
        if (webp.exists())
            return
        String ffmpegCmd = "ffmpeg -i ${filePath} -ss 00:00:01.000 -vframes 1 $webp"
        println ffmpegCmd
        Process pPdf = ffmpegCmd.execute()
        pPdf.consumeProcessOutput()
        pPdf.waitForOrKill(3 * 1000)
    }

}
