package com.linhnvtit.fun_cache.plugin

import com.linhnvtit.fun_cache.Cache
import com.linhnvtit.fun_cache.findAnnotation
import com.linhnvtit.fun_cache.utils.HASH_SEPARATOR
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irEqeqeq
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.builders.irTrue
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(FirIncompatiblePluginAPI::class)
internal class FunCacheIrVisitor(
    private val pluginContext: IrPluginContext,
    private val logger: MessageCollector,
) : IrElementTransformerVoid() {
    private var currentFunction: IrFunction? = null
    private var funcSignature: IrConstImpl<String>? = null
    private var concatenatedHashCodes: IrStringConcatenation? = null

    private val funCacheClass = pluginContext.referenceClass(FqName(FunCache::class.qualifiedName!!))
        ?: throw IllegalArgumentException("FunCache class not found")
    private val funCacheGetFunc = funCacheClass.functions.first { it.owner.name == Name.identifier("get") }
    private val funCacheHasFunc = funCacheClass.functions.first { it.owner.name == Name.identifier("has") }
    private val funCachePutFunc = funCacheClass.functions.first { it.owner.name == Name.identifier("put") }
    private val funCacheTryInitFunc = funCacheClass.functions.first { it.owner.name == Name.identifier("tryInitFunctionCache") }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        val cacheAnnotation = declaration.annotations.findAnnotation(Cache::class.qualifiedName)
        if (cacheAnnotation != null) {
            logger.report(CompilerMessageSeverity.INFO, "[FunCache] Detect function [${declaration.name}]")

            currentFunction = declaration
            val builder = IrBlockBodyBuilder(
                pluginContext, Scope(declaration.symbol), declaration.startOffset, declaration.endOffset
            )

            declaration.body = builder.irBlockBody {
                val hashCodeFunction = context.irBuiltIns.anyClass.functions.first { it.owner.name.asString() == "hashCode" }
                val parametersHashCodeVariable = buildList {
                    declaration.valueParameters.forEach {
                        add(
                            irTemporary(irCall(hashCodeFunction).apply {
                                dispatchReceiver = irGet(it)
                            })
                        )
                    }
                }

                val concatenatedHashCodes = irConcat().apply {
                    parametersHashCodeVariable.forEach {
                        addArgument(irGet(it))
                        addArgument(irString(HASH_SEPARATOR))
                    }
                }

                val funcSignature = irString(declaration.funCacheSignature)

                this@FunCacheIrVisitor.funcSignature = funcSignature
                this@FunCacheIrVisitor.concatenatedHashCodes = concatenatedHashCodes

                // Start travel to child nodes and modify them
                // We modify child nodes first to avoid modifying our own added codes
                super.visitFunction(declaration)

                val isKeyExist = builder.irTemporary(irCall(funCacheHasFunc).apply {
                    dispatchReceiver = null
                    putValueArgument(0, funcSignature)
                    putValueArgument(1, concatenatedHashCodes)
                })
                val capacity = cacheAnnotation.getValueArgument(0)
                val cacheStrategy = cacheAnnotation.getValueArgument(1)

                val tryInitFunctionCache =
                    irCall(funCacheTryInitFunc).apply {
                        dispatchReceiver = null
                        putValueArgument(0, funcSignature)
                        if (capacity != null) putValueArgument(1, capacity)
                        if (cacheStrategy != null) putValueArgument(2, cacheStrategy)
                    }

                val checkStatement = irIfThenElse(
                    declaration.returnType,
                    irEqeqeq(irGet(isKeyExist), irTrue()),
                    irBlock {
                        +irReturn(irGet(irTemporary(irCall(funCacheGetFunc).apply {
                            dispatchReceiver = null
                            putValueArgument(0, funcSignature)
                            putValueArgument(1, concatenatedHashCodes)
                        })))
                    },
                    irBlock {
                        declaration.body?.statements?.forEach { +it }
                    },
                )

                +tryInitFunctionCache
                +isKeyExist
                +checkStatement
            }
        }

        return declaration
    }

    override fun visitReturn(expression: IrReturn): IrExpression {
        if (currentFunction == null || funcSignature == null || concatenatedHashCodes == null) return expression

        val builder = IrBlockBuilder(
            pluginContext,
            Scope(expression.returnTargetSymbol),
            expression.startOffset,
            expression.endOffset
        )

        return builder.irBlock {
            val returnValue = builder.irTemporary(expression.value)
            +returnValue
            +irCall(funCachePutFunc).apply {
                dispatchReceiver = null
                putValueArgument(0, funcSignature)
                putValueArgument(1, concatenatedHashCodes)
                putValueArgument(2, irGet(returnValue))
            }
            +irReturn(irGet(returnValue))
        }
    }
}