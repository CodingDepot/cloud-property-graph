package io.clouditor.graph.passes.python

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.*
import io.clouditor.graph.passes.LogPass

class PythonLogPass(ctx: TranslationContext) : LogPass(ctx) {
    override fun accept(t: TranslationResult) {
        // if (this.lang is PythonLanguageFrontend) {
        for (tu in t.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    fun visit(m: MemberCallExpression) {
                        if (m.name.localName == "info" && m.base?.name?.localName == "logging") {
                            handleLog(t, m, m.name.localName, tu)
                        }
                    }
                }
            )
        }
    }
}
