import com.elfinitiy.parser.ArgContainer
import com.elfinitiy.parser.ArgParser
import org.junit.Test
import kotlin.test.assertTrue


class SimpleTest {

    @Test
    fun testArgs(){
        val args = arrayOf("-f", "text.txt", "--list", "1 2 3 4 5")
        val parser = ArgParser(args)

        parser.addArgument("-f")
        parser.addArgument("--list", consumeArgCount = ArgContainer.ARGUMENT_COUNT_SPECIAL_ANY)

        val argMap = parser.parseArgs()

        assertTrue(argMap.containsKey("F"))
        assertTrue(argMap.containsKey("LIST"))
    }
}