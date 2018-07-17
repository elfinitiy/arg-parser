package com.elfinitiy.parser

import java.io.File
import kotlin.reflect.KClass

class ArgParser(private val args: Array<String>,
                private val prog: String? = null,
                private val usage: String? = null,
                private val description: String = "",
                private val epilog: String = ""
                ) {

    private val outputDataCollection = ArrayList<String>()
    private val platformLineSeparator = System.lineSeparator()
    private val argContainerList = ArrayList<ArgContainer>()
    private val finalArgList = ArrayList<String>()
    private var isHelpRequired = false

    private val argMap = HashMap<String, Any>()

    init {
        finalArgList.addAll(args)
    }

    fun addArgument(vararg argNames: String,
                    consumeArgCount: String = ArgContainer.ARGUMENT_COUNT_SINGLE,
                    default: String? = null,
                    type: KClass<out Any> = Any::class,
                    metaVar: String? = null,
                    help: String = "",
                    destination: String? = null
                    ) {

        if(argNames.isEmpty()) {
            throw IllegalArgumentException("argNames can not be empty")
        }

        val argContainer = ArgContainer(argNames,
                consumeArgCount = consumeArgCount,
                default=default,
                type = type,
                help = help,
                metaVar = metaVar,
                destination = destination)

        argContainerList.add(argContainer)
    }

    fun parseArgs(vararg extraArgs: String): Map<String, Any?> {
        mergeArgs(extraArgs)

        if(finalArgList.isEmpty()) {
            throw IllegalArgumentException("No arguments found")
        }

        iterateOverArgs()
        val map = HashMap<String, Any?>()

        if(isHelpRequired) {
            print(generateHelpText())

            return map
        }

        for(argContainer in argContainerList) {
            map[argContainer.getDestinationName()] = if(argContainer.isSingle()) {
                argContainer.getValue()
            } else {
                argContainer.getValues()
            }
        }

        return map
    }

    private fun print(data: String) {
        System.out.print(data)
    }

    private fun iterateOverArgs() {
        if(finalArgList.size == 1) {
            return
        }

        val startIndex = if (prog != null) 1 else 0
        var previousArgContainer: ArgContainer? = null
        for(index in startIndex until finalArgList.size) {
            val currentArgument = finalArgList[index]

            if(isHelpRequired(currentArgument)) {
                isHelpRequired = true
                break
            }

            var isArgMatched = false
            for(argContainer in argContainerList) {
                if(argContainer.isArgName(currentArgument)) {
                    previousArgContainer = argContainer
                    isArgMatched = true
                }
            }

            if(!isArgMatched) {
                if(previousArgContainer == null) {
                    continue
                }

                if(previousArgContainer.isSingle() && previousArgContainer.hasSingleValue()) {
                    continue
                }

                previousArgContainer.addValue(currentArgument)
            }
        }
    }

    private fun handleArgumentCollection(argName: String) {

    }


    private fun isHelpRequired(argumentParam: String): Boolean {
        return argumentParam == ARGUMENT_PARAM_HELP || argumentParam == ARGUMENT_PARAM_HELP_SHORT
    }

    private fun generateHelpText(): String {
        val usageText = generateUsageText()
        val argumentUsageText = generateArgumentUsageText()
        return """
            $usageText
            $argumentUsageText
        """.trimIndent().trimMargin()
    }

    private fun generateUsageText(): String {

        return """|
            |usage: ${getProgramName()} [-h]
            """.trimIndent().trimMargin()
    }

    private fun getProgramName(): String {
        return if(prog != null) {
            prog
        }
        else {
            val path = finalArgList[0]

            val lastIndex = path.lastIndexOf(File.separator)
            path.substring(lastIndex + 1)
        }
    }

    private fun generateArgumentUsageText(): String {
        var argumentUsageTexts = ""
        for(arg in argContainerList) {
            argumentUsageTexts += "  |   ${arg.getUsageText()}$platformLineSeparator"
        }
        return """|
        |optional arguments:
            |   -h, --help  $ARGUMENT_PARAM_HELP_DESCRIPTION
            |$argumentUsageTexts
        """.trimIndent().trimMargin()
    }

    private fun mergeArgs(extraArgs: Array<out String>) {
        for(arg in extraArgs) {
            finalArgList.add(arg)
        }
    }

    companion object {
        const val ARGUMENT_PARAM_HELP = "--help"
        const val ARGUMENT_PARAM_HELP_SHORT = "-h"

        const val ARGUMENT_PARAM_HELP_DESCRIPTION = "show this help message and exit"
    }
}