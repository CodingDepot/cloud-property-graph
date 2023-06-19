package io.clouditor.graph.passes.golang

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression
import de.fraunhofer.aisec.cpg.processing.IVisitor
import de.fraunhofer.aisec.cpg.processing.strategy.Strategy
import io.clouditor.graph.passes.LogPass

class GolangLogPass(ctx: TranslationContext) : LogPass(ctx) {
    override fun accept(result: TranslationResult) {
        for (tu in result.translationUnits) {
            tu.accept(
                Strategy::AST_FORWARD,
                object : IVisitor<Node>() {
                    override fun visit(t: Node) {
                        when (t) {
                            is MemberCallExpression -> handleMemeberCallExpression(result, tu, t)
                        }
                    }
                }
            )
        }
    }

    private fun handleMemeberCallExpression(
        result: TranslationResult,
        tu: TranslationUnitDeclaration,
        m: MemberCallExpression
    ) {
        val logMethods =
            arrayOf(
                "log.Info",
                "log.Debug",
                "log.Trace",
                "log.Warn",
                "log.Err",
            )
        // we are looking for calls to Msg or Msgf, which have a base of one of the
        // logging
        // specifiers above, e.g. log.Info().Msg("Hello")
        if ((m.name.localName == "Msg" || m.name.localName == "Msgf") &&
                (m.base as? CallExpression)?.toString() in logMethods
        ) {
            // the base name specifies the log severity, so we use this one as the
            // "name" of the log operation
            handleLog(result, m, m.base?.name?.localName ?: "missing", tu)
        }
    }
}
