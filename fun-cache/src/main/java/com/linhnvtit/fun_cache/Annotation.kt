package com.linhnvtit.fun_cache

import com.linhnvtit.fun_cache.cache.CacheStrategy
import com.linhnvtit.fun_cache.utils.CACHE_DEFAULT_CAPACITY
import org.jetbrains.kotlin.backend.jvm.codegen.AnnotationCodegen.Companion.annotationClass
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization

@Target(AnnotationTarget.FUNCTION)
annotation class Cache(val capacity: Int = CACHE_DEFAULT_CAPACITY, val strategy: CacheStrategy = CacheStrategy.LRU)

fun List<IrConstructorCall>.hasAnnotation(annotationName: String?) =
    this.map { it.annotationClass.fqNameForIrSerialization.asString() }.contains(annotationName)

fun List<IrConstructorCall>.findAnnotation(annotationName: String?): IrConstructorCall? =
    this.find { it.annotationClass.fqNameForIrSerialization.asString().contains(annotationName.orEmpty()) }