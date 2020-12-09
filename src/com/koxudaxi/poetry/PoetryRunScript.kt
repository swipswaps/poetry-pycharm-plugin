package com.koxudaxi.poetry

import com.intellij.execution.ExecutionManager
import com.intellij.execution.Location
import com.intellij.execution.RunManager
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.execution.util.ExecutionErrorDialog
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.jetbrains.extensions.python.toPsi
import com.jetbrains.python.packaging.PyExecutionException
import com.jetbrains.python.run.PythonRunConfigurationProducer
import com.jetbrains.python.sdk.pythonSdk
import org.toml.lang.psi.*


class PoetryRunScript : AnAction() {
    private fun runScriptFromRunConfiguration(project: Project, file: PsiFile) {
        val configurationFromContext = RunConfigurationProducer
                .getInstance(PythonRunConfigurationProducer::class.java)
                .createConfigurationFromContext(ConfigurationContext(file)) ?: return
        val settings = configurationFromContext.configurationSettings
        val runManager = RunManager.getInstance(project)
        runManager.addConfiguration(settings)
        runManager.selectedConfiguration = settings
        val builder = ExecutionEnvironmentBuilder.createOrNull(DefaultRunExecutor.getRunExecutorInstance(), settings)
                ?: return
        ExecutionManager.getInstance(project).restartRunProfile(builder.build())
    }

    override fun actionPerformed(e: AnActionEvent) {
        val tomlKey = e.dataContext.getData(Location.DATA_KEY)?.psiElement as? TomlKey ?: return
        val project = e.project ?: return
        val scriptPath = project.pythonSdk?.homeDirectory?.parent?.findChild(tomlKey.text) ?: return
        val scriptFile = scriptPath.toPsi(project) ?: return ExecutionErrorDialog.show(PyExecutionException("Cannot find a script file\nPlease run 'poetry install' before executing scripts", "poetry", emptyList()), "Poetry Plugin", project)
        runScriptFromRunConfiguration(project, scriptFile.containingFile)

    }

    init {
        templatePresentation.icon = AllIcons.Actions.Execute
    }

    companion object {
        const val actionID = "poetryRunScript"
    }
}