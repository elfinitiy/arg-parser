package com.elfinitiy.argparser.parser

@Suppress("UNCHECKED_CAST")
class ArgContainer(
        private val argNames: Array<out String>,
        private val consumeArgCount: String = ArgContainer.ARGUMENT_COUNT_SINGLE,
        private val default: String? = null,
        private val metaVar: String? = null,
        private val help: String = "",
        destination: String? = null
) {
    val isOptional: Boolean
    val isPositional: Boolean
    val positionalIndex: Int

    private val valueCollector = ArrayList<Any>()
    private val destinationName: String
    private val firstArgumentName: String
    private val upperCaseFirstName: String

    init {
        isOptional = argNames.all {
            it.contains(ARGUMENT_OPTIONAL_PREFIX)
        }
        isPositional = !isOptional


        positionalIndex = if(isPositional) {
            nextCounter()
        } else {
            -1
        }

        //Index, Len
        var topIndex = 0
        var topLength = 0
        for(index in 0 until argNames.size) {
            var item = argNames[index]
            val len = item.length
            if(len > topLength) {
                topIndex = index
                topLength = len
            }
        }

        firstArgumentName = argNames[0]
        upperCaseFirstName = firstArgumentName.substringAfterLast(ARGUMENT_OPTIONAL_PREFIX).toUpperCase()
        destinationName = destination ?: argNames[topIndex].replace("-", "")
    }

    fun addValue(value: Any) {
        valueCollector.add(value)
    }

    fun isArgName(argName: String): Boolean {
        return argNames.contains(argName)
    }

    fun <T> getValue(): T? {
        if(valueCollector.size <= 0) {
            return null
        }

        return valueCollector[0] as T
    }

    fun<T> getValues(): ArrayList<T> {
        return valueCollector as ArrayList<T>
    }

    fun isSingle(): Boolean {
        return consumeArgCount == ARGUMENT_COUNT_SINGLE
    }


    fun hasSingleValue(): Boolean {
        return valueCollector.isNotEmpty()
    }

    fun getDestinationName(): String {
        return destinationName
    }

    fun getUsageText(): String {
        val helpTextBuilder = StringBuilder()

        val metaVarToUse = metaVar ?: upperCaseFirstName

        for(index in 0 until argNames.size) {
            val arg = argNames[index]

            helpTextBuilder.append("$arg $metaVarToUse")
            if(index != argNames.lastIndex) {
                helpTextBuilder.append(", ")
            }
        }
        helpTextBuilder.append("  $help")

        return helpTextBuilder.toString()

    }

    fun isFull() = when(consumeArgCount) {
        ARGUMENT_COUNT_SINGLE -> valueCollector.size == 1
        ARGUMENT_COUNT_SPECIAL_ONE_OR_MORE, ARGUMENT_COUNT_SPECIAL_ANY -> false
        else -> {
            val count = consumeArgCount.toInt()
            valueCollector.size == count
        }
    }

    fun getConsumeCountType(): String {
        return consumeArgCount
    }

    fun isWildcardConsumeType(): Boolean {
        return consumeArgCount == ArgContainer.ARGUMENT_COUNT_SPECIAL_ANY ||
                consumeArgCount == ArgContainer.ARGUMENT_COUNT_SPECIAL_ONE_OR_MORE
    }

    companion object {
        const val ARGUMENT_COUNT_SINGLE = "?"
        const val ARGUMENT_COUNT_SPECIAL_ANY = "*"
        const val ARGUMENT_COUNT_SPECIAL_ONE_OR_MORE = "+"

        const val ARGUMENT_OPTIONAL_PREFIX = "-"

        var ARGUMENT_NOT_OPTIONAL_COUNTER = 0

        fun nextCounter(): Int {
            return ARGUMENT_NOT_OPTIONAL_COUNTER++
        }
    }
}