package ru.avesystems.maise.campaign.codec

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import java.io.*

/**
 * The codec allows to pass POJOs as messages through the event bus.
 */
class GenericCodec<T>(private val cls: Class<T>) : MessageCodec<T, T?> {
    override fun encodeToWire(buffer: Buffer, s: T) {
        val bos = ByteArrayOutputStream()

        try {
            val out = ObjectOutputStream(bos)
            out.writeObject(s)
            out.flush()

            val yourBytes = bos.toByteArray()
            buffer.appendInt(yourBytes.size)
            buffer.appendBytes(yourBytes)

            out.close()
        } catch (e: IOException) {
        } finally {
            try {
                bos.close()
            } catch (ex: IOException) {
            }
        }
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer): T? {
        val getIntLength = 4
        val dataStart = pos + getIntLength
        val dataLength = buffer.getInt(pos)

        val yourBytes = buffer.getBytes(dataStart, dataStart + dataLength)
        val bis = ByteArrayInputStream(yourBytes)

        try {
            val ois = ObjectInputStream(bis)
            val msg = ois.readObject() as T
            ois.close()
            return msg
        } catch (e: IOException) {
            println("Listen failed " + e.message)
        } catch (e: ClassNotFoundException) {
            println("Listen failed " + e.message)
        } finally {
            try {
                bis.close()
            } catch (e: IOException) {
            }
        }
        return null
    }

    override fun transform(customMessage: T): T? {
        return customMessage
    }

    override fun name(): String {
        return cls.simpleName + "Codec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }
}
