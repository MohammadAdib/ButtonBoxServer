package mohammad.adib.buttonbox.server

import java.awt.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException
import java.util.Random
import kotlin.system.exitProcess

object Main {

    private var debug = false
    private const val isRunning = true
    private var robot: Robot? = null
    private var computerName: String? = null

    @JvmStatic
    fun main(args: Array<String>) {
        computerName = getComputerName()
        debug = args.isNotEmpty() && args[0] == "--debug"
        println("______ _   _ _____ _____ _____ _   _ ______  _______   __\n" +
                "| ___ \\ | | |_   _|_   _|  _  | \\ | || ___ \\|  _  \\ \\ / /\n" +
                "| |_/ / | | | | |   | | | | | |  \\| || |_/ /| | | |\\ V / \n" +
                "| ___ \\ | | | | |   | | | | | | . ` || ___ \\| | | |/   \\ \n" +
                "| |_/ / |_| | | |   | | \\ \\_/ / |\\  || |_/ /\\ \\_/ / /^\\ \\\n" +
                "\\____/ \\___/  \\_/   \\_/  \\___/\\_| \\_/\\____/  \\___/\\/   \\/")
        Thread(Runnable { startUDPServer() }).start()
        try {
            robot = Robot()
        } catch (e: AWTException) {
            e.printStackTrace()
        }

    }

    private fun getComputerName(): String? {
        val env = System.getenv()
        return when {
            env.containsKey("COMPUTERNAME") -> env["COMPUTERNAME"]
            env.containsKey("HOSTNAME") -> env["HOSTNAME"]
            else -> "BUTTONBOX-" + Random().nextInt(500)
        }
    }

    private fun startUDPServer() {
        try {
            val socket = DatagramSocket(18250)
            println("\nServer started. Please connect your phone to the same Wi-Fi network and enjoy")
            while (isRunning) {
                try {
                    val buffer = ByteArray(2048)
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    handlePacket(packet, buffer)
                } catch (e: Exception) {
                    println(e.message)
                }

            }
        } catch (e: SocketException) {
            println("Error: \n" + e.message)
            exitProcess(1)
        }

    }

    @Throws(Exception::class)
    private fun handlePacket(p: DatagramPacket, b: ByteArray) {
        val rawMessage = String(b).trim { it <= ' ' }
        if (debug) println(rawMessage + " from " + p.address + ":" + p.port)
        if (rawMessage.startsWith("buttonbox-ack")) {
            val socket = DatagramSocket()
            val packet = DatagramPacket(computerName!!.toByteArray(), computerName!!.length, p.address, 18250)
            socket.send(packet)
        } else {
            val keyCode = Integer.parseInt(String(b).toLowerCase().trim { it <= ' ' })
            println("Button Press: " + keyCode.toChar())
            simulateKeyPress(keyCode)
        }
    }

    private fun simulateKeyPress(key: Int) {
        Thread {
            robot!!.keyPress(key)
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
            }
            robot!!.keyRelease(key)
        }.start()
    }
}