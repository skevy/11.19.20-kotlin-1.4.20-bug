package com.airbnb.viaduct.graphql

import com.airbnb.viaduct.utils.memoize
import com.github.benmanes.caffeine.cache.Caffeine
import graphql.language.AstPrinter
import graphql.language.AstSignature
import graphql.language.Definition
import graphql.language.Document
import graphql.language.Field
import graphql.language.FragmentDefinition
import graphql.language.InlineFragment
import graphql.language.OperationDefinition
import graphql.language.Selection
import graphql.language.SelectionSet
import graphql.language.SelectionSetContainer
import graphql.parser.Parser
import java.security.MessageDigest

typealias Sha256Hash = String

data class NormalizedQuery(
    val operationHash: Sha256Hash,
    val printedAst: String,
    val normalizedDocument: Document
)

object QueryNormalizer {
    private val parser = Parser()
    private val normalizedQueriesCache =
        Caffeine.newBuilder().maximumSize(1_000)
            .build<Pair<String?, String>, NormalizedQuery>()

    val normalizeGraphQLQuery =
        ::normalizeGraphQLQueryHelper.memoize(cache = normalizedQueriesCache.asMap())

    private fun normalizeGraphQLQueryHelper(
        operationName: String?,
        rawQuery: String
    ): NormalizedQuery {
        val rawDoc = parser.parseDocument(rawQuery)

        val normalizedDoc = rawDoc.transform { builder ->
            builder.definitions(
                rawDoc.definitions.map { def ->
                    if (def is SelectionSetContainer<*> && def.selectionSet != null) {
                        val newSet = removeTypenameFields(def.selectionSet)
                        when (def) {
                            is OperationDefinition -> def.transform { it.selectionSet(newSet) }
                            is FragmentDefinition -> def.transform { it.selectionSet(newSet) }
                            else -> def
                        }
                    } else {
                        def
                    } as Definition<*>
                }
            )
        }

        // get the operation from the document
        val operation = if (operationName == null) {
            normalizedDoc.getDefinitionsOfType(OperationDefinition::class.java).firstOrNull()
        } else {
            normalizedDoc.getDefinitionsOfType(OperationDefinition::class.java).firstOrNull { it.name == operationName }
        } ?: throw RuntimeException("Cannot parse operation from query document. Please check syntax.")

        // build a signature of the query
        val signatureDoc = AstSignature().signatureQuery(normalizedDoc, operation.name)
        val printedAst = AstPrinter.printAstCompact(signatureDoc)
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(printedAst.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })

        return NormalizedQuery(hash as Sha256Hash, printedAst, normalizedDoc)
    }

    private fun removeTypenameFields(selectionSet: SelectionSet): SelectionSet {
        val selections = selectionSet.selections.filter {
            (it as? Field)?.name != "__typename"
        }.map { sel ->
            if (sel is SelectionSetContainer<*> && sel.selectionSet != null) {
                val newSet = removeTypenameFields(sel.selectionSet)
                when (sel) {
                    is Field -> sel.transform { it.selectionSet(newSet) }
                    is InlineFragment -> sel.transform { it.selectionSet(newSet) }
                    else -> sel
                }
            } else {
                sel
            } as Selection<*>
        }
        return selectionSet.transform {
            it.selections(selections)
        }
    }
}
