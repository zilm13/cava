package net.consensys.cava.ssz;

import net.consensys.cava.bytes.Bytes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.consensys.cava.ssz.SSZSerializer.SSZType.Type.*;

public class SSZSerializerDecode {

    private static Map<SSZSerializer.SSZType.Type, Function<DecodeInputBox, Object>> decodeHandlers = new HashMap<>();

    static {
        decodeHandlers.put(HASH, c -> c.reader.readHash(c.field.sszType.size).toArrayUnsafe());
        decodeHandlers.put(BYTES,  c -> c.field.sszType.size == null ? c.reader.readBytes().toArrayUnsafe() :
                c.reader.readBytes(c.field.sszType.size).toArrayUnsafe());
        decodeHandlers.put(ADDRESS,  c -> c.reader.readAddress().toArrayUnsafe());
        decodeHandlers.put(STRING,  c -> c.reader.readString());
        decodeHandlers.put(BOOLEAN,  c -> c.reader.readBoolean());
        decodeHandlers.put(INT,  c -> c.reader.readInt(c.field.sszType.size));
        decodeHandlers.put(LONG,  c -> c.reader.readLong(c.field.sszType.size));
        decodeHandlers.put(BIGINT,  c -> c.reader.readUnsignedBigInteger(c.field.sszType.size));
        decodeHandlers.put(LIST, SSZSerializerDecode::decodeList);
        decodeHandlers.put(CONTAINER, SSZSerializerDecode::decodeContainer);
    }

    private static Object decodeContainer(DecodeInputBox c) {
        BytesSSZReader reader = c.reader;

        Bytes data = reader.readBytes();
        if (!c.field.skipContainer) {
            return SSZSerializer.decode(data.toArrayUnsafe(), c.field.type);
        } else {
            // FIXME: find a clear way
            return SSZSerializer.decode(SSZ.encodeBytes(data).toArrayUnsafe(), c.field.type); // Add length prefix
        }
    }

    private static Object decodeList(DecodeInputBox c) {
        BytesSSZReader reader = c.reader;

        // TODO: Other types
        switch (c.field.sszType.type) {
            case BYTES: {
                return reader.readByteArrayList();
            }
            case HASH: {
                return reader.readByteArrayList();
            }
            default: {
                throw new RuntimeException("Shouldn't be there!!!"); // TODO: normal exception
            }
        }
    }

    public static Object decodeNextField(SSZScheme.SSZField field, BytesSSZReader reader) {
        Function<DecodeInputBox, Object> handler;
        if (field.type.equals(List.class)) {
            handler = decodeHandlers.get(LIST);
        } else {
            if (field.skipContainer != null) {
                handler = decodeHandlers.get(CONTAINER);
            } else {
                handler = decodeHandlers.get(field.sszType.type);
            }
        }

        return handler.apply(new DecodeInputBox(field, reader));
    }

    static class DecodeInputBox {
        SSZScheme.SSZField field;
        BytesSSZReader reader;

        public DecodeInputBox(SSZScheme.SSZField field, BytesSSZReader reader) {
            this.field = field;
            this.reader = reader;
        }
    }
}
