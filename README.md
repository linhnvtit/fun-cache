# FunCache
FunCache is a Kotlin library that provides easy-to-use annotation for caching function results. By leveraging Kotlin Compiler Plugin, it ensures that caching is seamlessly integrated into your Kotlin code, offering improved performance with minimal boilerplate.

## Features
- Simple Annotation: Just add `@Cache` annotation to your function, and the library handles the rest.
- Seamless Integration: Works with Kotlin's compiler plugin to automatically manage caching.
- Configurable: Customize cache behavior according to your needs.
- Thread-Safe: Ensures safe access to cache in multi-threaded environments.
- **Note**: Should only be used with "pure functions".

## Gradle
```kotlin
import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME

dependencies {
    implementation("com.github.linhnvtit:fun-cache:1.0.0")
    PLUGIN_CLASSPATH_CONFIGURATION_NAME("com.github.linhnvtit:fun-cache:1.0.0")
}
```

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

## Usage
- Annotate your functions with @Cache to enable caching:
```kotlin
@Cache
fun fibonacci(num: Int): Int {
    return when(num) {
        0 -> 0
        1 -> 1
        else -> fibonacci(num-1) + fibonacci(num-2)
    }
}

```
By adding the @Cache annotation, the fibonacci function's results will be cached, improving performance for repeated calls with the same parameters.
- FunCache also works fine with lambda functions too.
```kotlin
val lambdaFunc: (Int) -> Int = @Cache { TODO() }
```

## Configuration
- You can customize the cache behavior using optional parameters in the @Cache annotation:
```kotlin
@Cache(capacity= 1000, strategy= CacheStrategy.LRU)
fun fibonacci(num: Int): Int { TODO() }
```

- You can also clear the cache of an annotated function by passing its reflection to `FunCache.clearCache`
    - Reference: [https://kotlinlang.org/docs/reflection.html#function-references](https://kotlinlang.org/docs/reflection.html#function-references)
```kotlin
FunCache.clearCache(::fibonacci)
```
