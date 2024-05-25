package com.linhnvtit.fun_cache.plugin

import com.linhnvtit.fun_cache.utils.EMPTY
import com.linhnvtit.fun_cache.utils.HASH_SEPARATOR
import com.linhnvtit.fun_cache.utils.PIPE_SEPARATOR
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

fun buildSignatureName(p1: String, p2: String): String = "[fq]$p1[p]$p2"
fun buildParameters(p1: String, p2: String): String = "[pX]$p1$PIPE_SEPARATOR$p2"

val KFunction<*>.funCacheSignature: String
    get() {
        val fqName = "${this.javaMethod?.declaringClass?.`package`?.name}.${this.name}"
        val parameters = this.parameters.joinToString(HASH_SEPARATOR) { buildParameters(it.name.orEmpty(), it.type.toString()) }

        return buildSignatureName(fqName, parameters)
    }

@OptIn(ObsoleteDescriptorBasedAPI::class)
val IrFunction.funCacheSignature: String
    get() {
        val qualifiedName = this.descriptor.fqNameSafe.asString()
        val parameters = this.valueParameters.joinToString(HASH_SEPARATOR) { it.typeString }
        return buildSignatureName(qualifiedName, parameters)
    }

val IrValueParameter.typeString: String
    get() {
        val name = this.name.asString()
        val type = this.type.classFqName?.asString() ?: EMPTY
        return buildParameters(name, type)
    }