package com.koxudaxi.poetry

import com.intellij.codeInspection.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.jetbrains.python.sdk.*
import org.toml.lang.psi.*

class PoetryVersionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean,
                              session: LocalInspectionToolSession): PsiElementVisitor {
        return PoetryFileVisitor(holder, session)
    }

    class PoetryFileVisitor(val holder: ProblemsHolder,
                            session: LocalInspectionToolSession) : PsiElementVisitor() {
        private fun guessModule(element: PsiElement): Module? {
            return ModuleUtilCore.findModuleForPsiElement(element)
                    ?: ModuleManager.getInstance(element.project).modules.let { if (it.size != 1) null else it[0] }
        }

        override fun visitFile(file: PsiFile) {
            val module = guessModule(file) ?: return
            val sdk = PythonSdkUtil.findPythonSdk(module) ?: return
            if (!isPoetry(file.project, sdk)) return
            if (file.virtualFile != module.pyProjectToml) return
            file.children
                    .filter { element ->
                        (element as? TomlTable)?.header?.names?.joinToString(".") { it.text } in listOf("tool.poetry.dependencies", "tool.poetry.dev-dependencies")
                    }.flatMap {
                        it.children.mapNotNull { line -> line as? TomlKeyValue }
                    }.forEach { keyValue ->
                        val packageName = keyValue.key.text
                        val outdatedVersion = PyPoetryPackageManager.getInstance(sdk).getOutdatedPackages()[packageName]
                        if (outdatedVersion is PoetryOutdatedVersion) {
                            val message = "'${packageName}' version ${outdatedVersion.currentVersion} is outdated (latest: ${outdatedVersion.latestVersion})"
                            holder.registerProblem(keyValue, message, ProblemHighlightType.WARNING)
                        }
                    }
        }
    }
}