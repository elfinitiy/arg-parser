package com.elfinitiy.argparser.cli

import com.elfinitiy.argparser.parser.ArgContainer
import com.elfinitiy.argparser.parser.ArgParser

fun main(args: Array<String>) {
    val parser = ArgParser(args, prog="Test", firstIndexIsPath = false)

    parser.addArgument("values", consumeArgCount = ArgContainer.ARGUMENT_COUNT_SPECIAL_ANY)
    parser.addArgument("second", consumeArgCount = ArgContainer.ARGUMENT_COUNT_SPECIAL_ANY)
    parser.addArgument("-f", "--file", help = "File path")
    parser.addArgument("-l", "--list", consumeArgCount = ArgContainer.ARGUMENT_COUNT_SPECIAL_ANY,
            metaVar = "Value", destination = "v")

    val map = parser.parseArgs("test", "mest", "best", "-f", "file.txt", "--list", "1", "2", "3", "8")

    System.out.println(map["values"]?.getValues<Int>())
}

