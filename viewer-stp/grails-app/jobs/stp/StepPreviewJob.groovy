package stp

import grails.compiler.GrailsCompileStatic
import taack.domain.TaackAttachmentService
import taack.ui.TaackUiConfiguration

import java.nio.file.Files
import java.nio.file.Path


class StepPreviewJob {
    static triggers = {
        simple repeatInterval: 60_000, repeatCount: -1
    }

    final Boolean singleInstance = false

    final Boolean xvfbRun = false

    final Boolean useWeston = true

    final String freecadPath = TaackUiConfiguration.home + '/freecad-app-link'

    @GrailsCompileStatic
    void execute() {
        StepToPreview todo = null
        synchronized (StpPreviewConverterService.singleton) {
            if (StpPreviewConverterService.singleton.size() > 0)
                todo = StpPreviewConverterService.singleton.pop()
        }
        if (!todo || new File(todo.previewPath).exists()) return

        println("StepPreviewJob: ${todo.previewPath}")

        String filePath = todo.filePath
        String previewPath = todo.previewPath
        String glbPath = todo.glbPath
        TaackAttachmentService.PreviewFormat previewFormat = todo.format

        def m = new File('/tmp/model.stp').toPath()
        Files.deleteIfExists(m)
        Files.createSymbolicLink(m, new File(filePath).toPath())
        boolean doGlb = !new File(glbPath).exists()
        boolean doPreview = !new File(previewPath).exists()

        if (doGlb) log.info('Do Glb ' + glbPath)
        if (doPreview) log.info('Do preview ' + previewPath)

        String conv = """\
            import sys
            import ImportGui
            
            step = '/tmp/model.stp'
            webp = '${previewPath}'
            """.stripIndent()
        if (doGlb) {
            conv += """\
                glb = '${glbPath}'
                """.stripIndent()
        }
        conv += """\
            ImportGui.open(step)
            print('open: ', step)
            """.stripIndent()
        if (doGlb) {
            conv += """\
                ImportGui.export(FreeCAD.ActiveDocument.RootObjects, glb)
                print('export GLB: ', glb)
                """.stripIndent()
        }
        conv += """\
            Gui.activeDocument().activeView().viewIsometric()
            Gui.SendMsgToActiveView('ViewFit')
            Gui.ActiveDocument.ActiveView.saveImage(webp, ${previewFormat.pixelWidth}, ${previewFormat.pixelHeight}, 'Current')
            print('save webp: ', webp)
            App.closeDocument('Unnamed')
            print('close: Unnamed')
            mw=FreeCADGui.getMainWindow()
            mw.deleteLater()
            print('mw.deleteLater')
            """.stripIndent()

        Path convPath = Files.createTempFile('FreeCAD-Script', '.py')
        File convFile = convPath.toFile()
        convFile.append(conv)


        String cmd
        Process pWeston
        if (useWeston) {
            String pWestonCmd = '/usr/bin/weston --no-config --socket=wl-freecad --backend=headless'
            pWeston = pWestonCmd.execute()
//            cmd = "env WAYLAND_DISPLAY=wl-freecad QT_QPA_PLATFORM=wayland COIN_GL_NO_CURRENT_CONTEXT_CHECK=1 ${freecadPath}  --socket=wayland --env=QT_QPA_PLATFORM=wayland --env=WAYLAND_DISPLAY=wl-freecad ${singleInstance ? '--single-instance' : ''} ${convFile.path}"
            cmd = "${freecadPath} ${convFile.path}"
        } else if (xvfbRun) {
            cmd = "${xvfbRun ? '/usr/bin/xvfb-run ' : ''}${freecadPath} ${singleInstance ? '--single-instance' : ''} ${convFile.path}"
        } else {
            cmd = "env QT_QPA_PLATFORM=offscreen ${freecadPath} ${singleInstance ? '--single-instance' : ''} ${convFile.path}"
        }
        println "executing $cmd"
        Process p = cmd.execute()

        int occ = 0
        while (!new File(previewPath).exists() && occ++ < 50) {
            sleep(1000)
            println "Wait $occ ${new File(previewPath).exists()} ${filePath}"
        }

        println('killing Weston and FreeCAD')
        p.waitForProcessOutput(System.out, System.err)
        p.waitForOrKill(0)
        if (useWeston) pWeston.waitForOrKill(0)
        println "Deleting ${convPath.toString()}"
        Files.deleteIfExists(convPath)
    }

}