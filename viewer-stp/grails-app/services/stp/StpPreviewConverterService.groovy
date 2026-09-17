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
    final Boolean singleInstance = true
    final Boolean xvfbRun = false
    final Boolean useWeston = true
    final Boolean offscreen = true

    String getGlbDir() {
        intranetRoot + '/stp/glb'
    }

    TaackAttachmentService taackAttachmentService

    @PostConstruct
    void initTaackService() {
        log.info "singleInstance = ${singleInstance}, xvfbRun = ${xvfbRun}, useWeston = ${useWeston}, offscreen = ${offscreen}"
        TaackAttachmentService.registerPreviewConverter(this)
        new File(glbDir).mkdirs()
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
//            def m = new File('/tmp/model.stp').toPath()
//            Files.deleteIfExists(m)
//            Files.createSymbolicLink(m, new File(filePath).toPath())
//            String conv = """\
//            import sys
//            import ImportGui
//
//            step = '/tmp/model.stp'
//            webp = '${previewPath}'
//            glb = '${glbDir + '/' + attachment.contentShaOne + '.glb'}'
//
//            ImportGui.open(step)
//            ImportGui.export(FreeCAD.ActiveDocument.RootObjects, glb)
//            Gui.activeDocument().activeView().viewIsometric()
//            Gui.SendMsgToActiveView('ViewFit')
//            Gui.ActiveDocument.ActiveView.saveImage(webp, ${previewFormat.pixelWidth}, ${previewFormat.pixelHeight}, 'Current')
//            App.closeDocument('Unnamed')
//            mw=FreeCADGui.getMainWindow()
//            mw.deleteLater()
//        """.stripIndent()
//
//            Path convPath = Files.createTempFile('FreeCAD-Script', '.py')
//            File convFile = convPath.toFile()
//            convFile.append(conv)
//
//
//            String cmd
//            Process pWeston
//            if (stpConfiguration.useWeston) {
//                String pWestonCmd = '/usr/bin/weston --no-config --socket=wl-freecad --backend=headless'
//                log.info "$pWestonCmd"
//                pWeston = pWestonCmd.execute()
//                cmd = "env WAYLAND_DISPLAY=wl-freecad ${stpConfiguration.freecadPath}  QT_QPA_PLATFORM=wayland ${stpConfiguration.singleInstance ? '--single-instance' : ''} ${convFile.path}"
//            } else if (stpConfiguration.xvfbRun) {
//                cmd = "${stpConfiguration.xvfbRun ? '/usr/bin/xvfb-run ' : ''}${stpConfiguration.freecadPath} ${stpConfiguration.singleInstance ? '--single-instance' : ''} ${convFile.path}"
//            } else if (stpConfiguration.offscreen) {
//                cmd = "env QT_QPA_PLATFORM=offscreen ${stpConfiguration.freecadPath} ${stpConfiguration.singleInstance ? '--single-instance' : ''} ${convFile.path}"
//            }
//            log.info "executing $cmd"
//            Process p = cmd.execute()
//            //p.consumeProcessOutput()
//
//            int occ = 0
//            while(!new File(previewPath).exists() && occ++ < 50) {
//                sleep(1000)
//                println "Wait $occ ${new File(previewPath).exists()} ${filePath}"
//            }
//            //log.info "${p.text}"
////            if (!new File(previewPath).exists()) {
//                println('killing Weston and FreeCAD')
////                if (pWeston) pWeston.waitForOrKill(0)
//                p.waitForOrKill(0)
////            }
//            println "Deleting ${convPath.toString()}"
//            Files.deleteIfExists(convPath)
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
