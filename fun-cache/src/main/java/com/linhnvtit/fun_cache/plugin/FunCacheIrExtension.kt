package com.linhnvtit.fun_cache.plugin

import com.linhnvtit.fun_cache.plugin.FunCacheIrVisitor
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class FunCacheIrExtension(
    private val logger: MessageCollector
) : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment, pluginContext: IrPluginContext
    ) {
        moduleFragment.accept(FunCacheIrVisitor(pluginContext, logger), null)
    }
}