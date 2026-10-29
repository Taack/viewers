package stp

import grails.compiler.GrailsCompileStatic

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class StepPreviewJob {
    static triggers = {
        simple repeatInterval: 60_000, repeatCount: -1
    }

    static concurrent = false

    StpPreviewConverterService stpPreviewConverterService

    @GrailsCompileStatic
    void execute() {
        StepToPreview todo
        while ((todo = nextTodo()) != null) {
            try {
                convert(todo)
            } catch (e) {
                log.error "StepPreviewJob: ${todo.filePath}: ${e.message}", e
            }
        }
    }

    @GrailsCompileStatic
    private static StepToPreview nextTodo() {
        synchronized (StpPreviewConverterService.singleton) {
            StpPreviewConverterService.singleton.empty ? null : StpPreviewConverterService.singleton.pop()
        }
    }

    @GrailsCompileStatic
    private void convert(StepToPreview todo) {
        File glb = new File(todo.glbPath)
        File preview = new File(todo.previewPath)
        boolean doGlb = !glb.exists()
        boolean doPreview = !preview.exists()
        if (!doGlb && !doPreview) return

        // cad-dsl renders the image at its own size, convert resizes it to the preview format
        Path image = Files.createTempFile('stp-preview', '.png')
        try {
            List<String> command = [StpConfiguration.cadDslPath, stpPreviewConverterService.scriptPath, todo.filePath]
            if (doGlb) command << glb.path
            if (doPreview) command << image.toString()
            String output = run(command, [CAD_DSL_OPTS: "-Djava.library.path=${StpConfiguration.cadDslNativeDir}".toString()])

            if (doPreview && Files.size(image) > 0) {
                String size = "${todo.format.pixelWidth}x${todo.format.pixelHeight}"
                run(['convert', image.toString(), '-resize', size, preview.path])
            }
            if (!glb.exists() || !preview.exists()) log.warn "StepPreviewJob: ${todo.filePath} not converted: ${output}"
        } finally {
            Files.deleteIfExists(image)
        }
    }

    @GrailsCompileStatic
    private String run(List<String> command, Map<String, String> environment = [:]) {
        log.info "StepPreviewJob: ${command.join(' ')}"
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true)
        builder.environment().putAll(environment)
        Process process = builder.start()
        StringBuilder output = new StringBuilder()
        Thread reader = process.consumeProcessOutputStream(output)
        if (!process.waitFor(StpConfiguration.conversionTimeoutMinutes, TimeUnit.MINUTES)) {
            process.destroyForcibly()
            log.error "StepPreviewJob: timeout: ${command.join(' ')}"
        }
        reader.join()
        output.toString()
    }
}
