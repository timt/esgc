package io.shaka.esgc

fun getEnvProperty(name: String): String = (System.getenv(name)
    ?: throw IllegalStateException("$name environment variable is required"))