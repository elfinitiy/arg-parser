package com.elfinitiy.argparser.parser

import java.io.File

class ArgParser(private val args: Array<String>,
                private val prog: String? = null,
                private val usage: String? = null,
                private val description: String = "",
                private val epilog: String = "",
                private val firstIndexIsPath: Boolean = true
                ) {

    private val platformLineSeparator = System.lineSeparator()
    private val argContainerList = ArrayList<ArgContainer>()
    private val finalArgList = ArrayList<String>()
    private var isHelpRequired = false
    private val iterationStartIndex = if (firstIndexIsPath) 1 else 0

    private val argContainerMap = HashMap<String, ArgContainer>()

    init {
        finalArgList.addAll(args)
    }

    fun addArgument(vararg argNames: String,
                    consumeArgCount: String = ArgContainer.ARGUMENT_COUNT_SINGLE,
                    default: String? = null,
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
                help = help,
                metaVar = metaVar,
                destination = destination)

        argContainerList.add(argContainer)
    }

    fun parseArgs(vararg extraArgs: String): Map<String, ArgContainer> {
        mergeArgs(extraArgs)

        if(finalArgList.isEmpty()) {
            throw IllegalArgumentException("No arguments found")
        }

        iterateOverArgs()

        if(isHelpRequired) {
            print(generateHelpText())

            return argContainerMap
        }

        for(argContainer in argContainerList) {
            argContainerMap[argContainer.getDestinationName()] = argContainer
        }

        return argContainerMap
    }

    private fun print(data: String) {
        System.out.print(data)
    }

    private fun preProcessArgs() {
        for(index in iterationStartIndex until finalArgList.size) {
            val currentArgument = finalArgList[index]
            if(isHelpRequired(currentArgument)) {
                isHelpRequired = true
                break
            }
        }
    }

    private fun preProcessArgContainers() {
        val nextList = argContainerList.filter {
            it.isPositional && it.getConsumeCountType() == ArgContainer.ARGUMENT_COUNT_SPECIAL_ANY ||
                    it.getConsumeCountType() == ArgContainer.ARGUMENT_COUNT_SPECIAL_ONE_OR_MORE
        }

        if(nextList.size > 1) {
            throw IllegalArgumentException("Two positional arguments with wildcard parameter count is not supported")
        }
    }

    private fun iterateOverArgs() {
        if(finalArgList.size == 1) {
            return
        }

        preProcessArgs()
        if(isHelpRequired) {
            return
        }

        preProcessArgContainers()

        var previousArgContainer: ArgContainer? = null
        for(index in iterationStartIndex until finalArgList.size) {
            val currentArgument = finalArgList[index]
            var isArgMatched = false
            for(argContainer in argContainerList) {
                if(argContainer.isArgName(currentArgument)) {
                    previousArgContainer = argContainer
                    isArgMatched = true
                }
            }

            if(!isArgMatched && previousArgContainer != null) {
                if(!previousArgContainer.isFull()) {
                    previousArgContainer.addValue(currentArgument)
                }
                else {
                    previousArgContainer = null
                }
            }

            if(previousArgContainer == null) {
                val positionalItemToFill = getPositionalArgument()
                positionalItemToFill?.addValue(currentArgument)
            }
        }
    }

    private fun getPositionalArgument(): ArgContainer? {
        return argContainerList.find {
            it.isPositional && !it.isFull()
        }
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
            if(!firstIndexIsPath) {
                return DEFAULT_NAME_FOR_NON_EXISTING_PATH
            }
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
            |$platformLineSeparator
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

        const val DEFAULT_NAME_FOR_NON_EXISTING_PATH = "Undefined"
    }
}