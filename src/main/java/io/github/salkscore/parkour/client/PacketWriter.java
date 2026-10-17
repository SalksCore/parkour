package io.github.salkscore.parkour.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

// Même format que FriendlyByteBuf côté client : varint, big endian, chaînes en UTF-8
final class PacketWriter {

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final DataOutputStream out = new DataOutputStream(bytes);

    PacketWriter(PacketType type) {
        writeByte(type.ordinal());
    }

    PacketWriter writeByte(int value) {
        try {
            out.writeByte(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    PacketWriter writeVarInt(int value) {
        while ((value & ~0x7F) != 0) {
            writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        return writeByte(value);
    }

    PacketWriter writeLong(long value) {
        try {
            out.writeLong(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    PacketWriter writeBoolean(boolean value) {
        return writeByte(value ? 1 : 0);
    }

    PacketWriter writeFloat(float value) {
        try {
            out.writeFloat(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    PacketWriter writeDouble(double value) {
        return writeLong(Double.doubleToRawLongBits(value));
    }

    PacketWriter writeString(String value) {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(data.length);
        bytes.writeBytes(data);
        return this;
    }

    byte[] toByteArray() {
        return bytes.toByteArray();
    }
}
