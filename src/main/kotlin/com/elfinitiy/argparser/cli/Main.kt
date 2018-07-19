package com.elfinitiy.argparser.cli

import com.elfinitiy.parser.ArgContainer
import com.elfinitiy.parser.ArgParser

fun main(args: Array<String>) {
    val argsEx = arrayOf("C:\\FakePath\\FakeApp\\FakeExecutable", *args)
    val parser = ArgParser(argsEx)

    parser.addArgument("-f", "--file", type = String::class, help = "File path")
    parser.addArgument("-l", "--list", consumeArgCount = ArgContainer.ARGUMENT_COUNT_SPECIAL_ANY,
            type = Int::class, metaVar = "Value")

    val map = parser.parseArgs()

    System.out.println(map["list"]?.getValues<Int>())
}

