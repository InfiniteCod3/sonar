/*
 * Copyright (C) 2023 Sonar Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.jonesdev.sonar.common.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.EncoderException;
import kotlin.text.Charsets;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;

import static xyz.jonesdev.sonar.common.protocol.VarIntUtil.readVarInt;
import static xyz.jonesdev.sonar.common.protocol.VarIntUtil.writeVarInt;

// Mostly taken from Velocity
@UtilityClass
public class ProtocolUtil {
  public @NotNull String readString(final ByteBuf buf) throws CorruptedFrameException {
    return readString(buf, Short.MAX_VALUE);
  }

  public @NotNull String readString(final ByteBuf buf,
                                    final int cap) throws CorruptedFrameException {
    return readString(buf, cap, false);
  }

  public @NotNull String readString(final ByteBuf buf,
                                    final int cap,
                                    final boolean legacy) throws CorruptedFrameException {
    final int length = readVarInt(buf);

    checkFrame(length >= 0, "Got a negative-length string");
    checkFrame(length <= cap * 3, "Bad string size");
    String str;
    if (legacy) {
      // TODO: length checking?
      str = buf.toString(Charsets.UTF_8);
      buf.skipBytes(buf.readableBytes());
    } else {
      checkFrame(buf.isReadable(length), "Got an invalid string length");
      str = buf.toString(buf.readerIndex(), length, Charsets.UTF_8);
      buf.skipBytes(length);
    }
    checkFrame(str.length() <= cap, "Got a too-long string");
    return str;
  }

  public void writeString(final ByteBuf buf, final CharSequence str) {
    final int size = ByteBufUtil.utf8Bytes(str);
    writeVarInt(buf, size);
    buf.writeCharSequence(str, Charsets.UTF_8);
  }

  public static void writeArray(final ByteBuf byteBuf, final byte[] bytes) {
    checkFrame(bytes.length < Short.MAX_VALUE, "Too long array");
    writeVarInt(byteBuf, bytes.length);
    byteBuf.writeBytes(bytes);
  }

  public static void writeStringArray(final ByteBuf byteBuf, final String[] stringArray) {
    writeVarInt(byteBuf, stringArray.length);
    for (final String s : stringArray) {
      writeString(byteBuf, s);
    }
  }

  public void writeCompoundTag(final ByteBuf byteBuf, final CompoundBinaryTag compoundTag) {
    try {
      BinaryTagIO.writer().write(compoundTag, (DataOutput) new ByteBufOutputStream(byteBuf));
    } catch (IOException e) {
      throw new EncoderException("Unable to encode NBT CompoundTag");
    }
  }

  private void checkFrame(final boolean expression, final String message) {
    if (!expression) {
      throw new CorruptedFrameException(message);
    }
  }
}
