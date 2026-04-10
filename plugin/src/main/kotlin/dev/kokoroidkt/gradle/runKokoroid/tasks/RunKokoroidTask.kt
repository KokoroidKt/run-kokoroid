package dev.kokoroidkt.gradle.runKokoroid.tasks

import dev.kokoroidkt.gradle.runKokoroid.config.RunKokoroidConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import sun.jvmstat.monitor.MonitoredVmUtil.commandLine
import javax.inject.Inject
import kotlin.io.path.createDirectories

abstract class RunKokoroidTask : JavaExec() {
    init {
        group = "kokoroid"
        description = "Run Kokoroid"
    }

    override fun exec() {
        val savePath = RunKokoroidConfig.kokoroidTempPath
        val actualSavePath = savePath.toAbsolutePath().normalize()
        actualSavePath.createDirectories()

        val jarFile = actualSavePath.resolve("kokoroid-core.jar")
        jarFile.parent?.createDirectories()

        mainClass.set("dev.kokoroidkt.core.MainKt")

        classpath = project.files(jarFile)

        if (RunKokoroidConfig.enableKokoroidDebug) {
            jvmArgs(
                "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005",
            )
        }

        if (RunKokoroidConfig.testExtensionType == null) {
            throw RuntimeException("testExtensionType is null, please set it: \"driver\" | \"adapter\" | \"plugin\"")
        }

        when (RunKokoroidConfig.testExtensionType) {
            "driver" -> {
                args("--with-driver-path", project.layout.buildDirectory
                    .dir(RunKokoroidConfig.libDir)
                    .get()
                    .asFile
                    .toPath()
                    .toString())
            }

            "adapter" -> {
                args(
                    "--with-adapter-path",
                    project.layout.buildDirectory
                        .dir(RunKokoroidConfig.libDir)
                        .get()
                        .asFile
                        .toPath()
                        .toString(),
                )
            }

            "plugin" -> {
                args(
                    "--with-plugin-path",
                    project.layout.buildDirectory
                        .dir(RunKokoroidConfig.libDir)
                        .get()
                        .asFile
                        .toPath()
                        .toString(),
                )
            }

            else -> {
                throw RuntimeException(
                    "Invalid testExtensionType: ${RunKokoroidConfig.testExtensionType}, " +
                        "please set it as: \"driver\" | \"adapter\" | \"plugin\"",
                )
            }
        }

        if (RunKokoroidConfig.enableKokoroidDebug) {
            args("--debug")
        }

        logger.lifecycle("Starting Kokoroid...")
        logger.lifecycle("Start Command: ${this.commandLine}")
        super.exec()
    }
}
