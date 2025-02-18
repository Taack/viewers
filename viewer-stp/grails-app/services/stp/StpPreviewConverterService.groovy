package stp

import attachment.Attachment
import grails.compiler.GrailsCompileStatic
import grails.gsp.PageRenderer
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.taack.IAttachmentPreviewConverter
import org.taack.IAttachmentShowIFrame
import taack.domain.TaackAttachmentService
import taack.domain.TaackAttachmentService.PreviewFormat

import javax.annotation.PostConstruct
import java.nio.file.Files
import java.nio.file.Path

@GrailsCompileStatic
class StpPreviewConverterService implements IAttachmentPreviewConverter, IAttachmentShowIFrame {

    static lazyInit = false

    static final singleton = new Object()

    @Value('${intranet.root}')
    String intranetRoot

    @Autowired
    StpConfiguration stpConfiguration

    String getGlbDir() {
        intranetRoot + "/stp/glb"
    }

    TaackAttachmentService taackAttachmentService

    @PostConstruct
    void initTaackService() {
        log.info "singleInstance = ${stpConfiguration.singleInstance}, xvfbRun = ${stpConfiguration.xvfbRun}, useWeston = ${stpConfiguration.useWeston}, offscreen = ${stpConfiguration.offscreen}"
        TaackAttachmentService.registerPreviewConverter(this)
        FileUtils.forceMkdir(new File(glbDir))
        TaackAttachmentService.registerAdditionalShow(this)
    }

    @Autowired
    PageRenderer g

    @Override
    List<String> getPreviewManagedExtensions() {
        return ["stp", "STEP"]
    }

    @Override
    void createWebpPreview(Attachment attachment, String previewPath, PreviewFormat previewFormat) {
        String filePath = taackAttachmentService.attachmentPath(attachment)
        if (new File(previewPath).exists() || !new File(filePath).exists()) return
        synchronized (singleton) {
            def m = new File("/tmp/model.stp").toPath()
            Files.deleteIfExists(m)
            Files.createSymbolicLink(m, new File(filePath).toPath())
            String conv = """\
            import sys
            import ImportGui
            
            step = '/tmp/model.stp'
            webp = '${previewPath}'
            glb = '${glbDir + '/' + attachment.contentShaOne + ".glb"}'
            
            ImportGui.open(step)
            ImportGui.export(FreeCAD.ActiveDocument.RootObjects, glb)
            Gui.activeDocument().activeView().viewIsometric()
            Gui.SendMsgToActiveView("ViewFit")
            Gui.ActiveDocument.ActiveView.saveImage(webp, ${previewFormat.pixelWidth}, ${previewFormat.pixelHeight}, 'Current')
            App.closeDocument("Unnamed")
            mw=FreeCADGui.getMainWindow()
            mw.deleteLater()
        """.stripIndent()

            Path convPath = Files.createTempFile("FreeCAD-Script", ".py")
            File convFile = convPath.toFile()
            convFile.append(conv)


            String cmd
            Process pWeston
            if (stpConfiguration.useWeston) {
                String pWestonCmd = "/usr/bin/weston --no-config --socket=wl-freecad --backend=headless"
                log.info "$pWestonCmd"
                pWeston = pWestonCmd.execute()
                cmd = "env WAYLAND_DISPLAY=wl-freecad ${stpConfiguration.freecadPath}  QT_QPA_PLATFORM=wayland ${stpConfiguration.singleInstance ? '--single-instance' : ''} ${convFile.path}"
            } else if (stpConfiguration.xvfbRun) {
                cmd = "${stpConfiguration.xvfbRun ? "/usr/bin/xvfb-run " : ""}${stpConfiguration.freecadPath} ${stpConfiguration.singleInstance ? '--single-instance' : ''} ${convFile.path}"
            } else if (stpConfiguration.offscreen) {
                cmd = "env QT_QPA_PLATFORM=offscreen ${stpConfiguration.freecadPath} ${stpConfiguration.singleInstance ? '--single-instance' : ''} ${convFile.path}"
            }
            log.info "executing $cmd"
            Process p = cmd.execute()
            //p.consumeProcessOutput()

            int occ = 0
            while(!new File(previewPath).exists() && occ++ < 40) {
                sleep(1000)
                println "Wait $occ ${new File(previewPath).exists()} ${filePath}"
            }
            //log.info "${p.text}"
            println "Deleting ${convPath.toString()}"
            Files.deleteIfExists(convPath)
        }
    }

    @Override
    List<String> getShowIFrameManagedExtensions() {
        return getPreviewManagedExtensions()
    }

    @Override
    String createShowIFrame(Attachment attachment) {
        g.render(template: "/stp/previewAttachmentGlbFile", model: [shaOne: attachment.contentShaOne.strip()]) as String
    }
}
