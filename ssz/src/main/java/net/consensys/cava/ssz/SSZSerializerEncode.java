package net.consensys.cava.ssz;

import net.consensys.cava.bytes.Bytes;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.consensys.cava.ssz.SSZSerializer.*;
import static net.consensys.cava.ssz.SSZSerializer.SSZType.Type.*;

public class SSZSerializerEncode {

    private static Map<SSZSerializer.SSZType.Type, Consumer<EncodeInputBox>> encodeHandlers = new HashMap<>();

    static {
        encodeHandlers.put(LONG, SSZSerializerEncode::encodeLong);
        encodeHandlers.put(INT, SSZSerializerEncode::encodeInt);
        encodeHandlers.put(LIST, SSZSerializerEncode::encodeList);
        encodeHandlers.put(CONTAINER, SSZSerializerEncode::encodeContainer);
        encodeHandlers.put(BYTES, SSZSerializerEncode::encodeBytes);
        encodeHandlers.put(HASH, SSZSerializerEncode::encodeBytes);
        encodeHandlers.put(ADDRESS, SSZSerializerEncode::encodeBytes);
        encodeHandlers.put(BIGINT, SSZSerializerEncode::encodeBigInt);
        encodeHandlers.put(BOOLEAN, SSZSerializerEncode::encodeBoolean);
        encodeHandlers.put(STRING, SSZSerializerEncode::encodeString);
    }

    private static void encodeInt(EncodeInputBox input) {
        Integer bitLength = input.field.sszType.size;
        encodeLong((int) input.value, bitLength, input.result);
    }

    private static void encodeLong(EncodeInputBox input) {
        Integer bitLength = input.field.sszType.size;
        encodeLong((long) input.value, bitLength, input.result);
    }

    private static void encodeLong(long value, int bitLength, OutputStream result) {
        Bytes res = SSZ.encodeULong(value, bitLength);
        try {
            result.write(res.toArrayUnsafe());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write value \"%s\" to stream", value), e);
        }
    }

    private static void encodeBigInt(EncodeInputBox input) {
        Integer bitLength = input.field.sszType.size;
        BigInteger value = (BigInteger) input.value;
        Bytes res = SSZ.encodeBigInteger(value, bitLength);
        try {
            input.result.write(res.toArrayUnsafe());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write value \"%s\" to stream", value), e);
        }
    }

    private static void encodeList(EncodeInputBox input) {
        List value = (List) input.value;
        SSZSerializer.SSZType.Type internalType = null;
        if (input.field.sszType == null) {
            if (value.isEmpty()) {
                internalType = BYTES;
            } else {
                Object firstElem = value.get(0);
                internalType = SSZSerializer.extractType(firstElem.getClass(), null).type;
            }
        } else {
            internalType = input.field.sszType.type;
        }

        try {
            switch (internalType) {
                case HASH: {
                    Bytes[] data = repackBytesList((List<byte[]>) value);
                    input.result.write(SSZ.encodeHashList(data).toArrayUnsafe());
                    break;
                }
                case BYTES: {
                    Bytes[] data = repackBytesList((List<byte[]>) value);
                    input.result.write(SSZ.encodeBytesList(data).toArrayUnsafe());
                    break;
                }
                case ADDRESS: {
                    Bytes[] data = repackBytesList((List<byte[]>) value);
                    input.result.write(SSZ.encodeAddressList(data).toArrayUnsafe());
                    break;
                }
                case BOOLEAN: {
                    boolean[] data = new boolean[value.size()];
                    for(int i = 0; i < value.size(); ++i) {
                        data[i] = (boolean) value.get(i);
                    }
                    input.result.write(SSZ.encodeBooleanList(data).toArrayUnsafe());
                    break;
                }
                case BIGINT: {
                    BigInteger[] data = (BigInteger[]) value.toArray(new BigInteger[0]);
                    input.result.write(SSZ.encodeBigIntegerList(DEFAULT_BIGINT_SIZE, data).toArrayUnsafe());
                    break;
                }
                case INT: {
                    int[] data = new int[value.size()];
                    for(int i = 0; i < value.size(); ++i) {
                        data[i] = (int) value.get(i);
                    }
                    input.result.write(SSZ.encodeUIntList(DEFAULT_INT_SIZE, data).toArrayUnsafe());
                    break;
                }
                case LONG: {
                    long[] data = new long[value.size()];
                    for(int i = 0; i < value.size(); ++i) {
                        data[i] = (long) value.get(i);
                    }
                    input.result.write(SSZ.encodeLongIntList(DEFAULT_LONG_SIZE, data).toArrayUnsafe());
                    break;
                }
                case STRING: {
                    String[] data = (String[]) value.toArray(new String[0]);
                    input.result.write(SSZ.encodeStringList(data).toArrayUnsafe());
                    break;
                }
                case CONTAINER: {
                    Bytes[] data = packContainerList((List<Object>) value, input.field);
                    input.result.write(SSZ.encodeBytesList(data).toArrayUnsafe());
                    break;
                }
                default: {
                    throw new RuntimeException(String.format("Type %s encoding is not implemented yet", internalType));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static Bytes[] packContainerList(List<Object> values, SSZScheme.SSZField field) {
        Bytes[] res = new Bytes[values.size()];
        for (int i = 0; i < values.size(); ++i) {
            byte[] data = SSZSerializer.encode(values.get(i));
            Bytes curValue;
            if (!field.skipContainer) {
                Bytes prefix = SSZ.encodeInt32(data.length);
                Bytes payload = Bytes.of(data);
                curValue = Bytes.concatenate(prefix, payload);
            } else {
                curValue = Bytes.of(data);
            }
            res[i] = curValue;
        }

        return res;
    }

    private static Bytes[] repackBytesList(List<byte[]> list) {
        Bytes[] data = new Bytes[list.size()];
        for (int i = 0; i < list.size(); i++) {
            byte[] el = list.get(i);
            data[i] = Bytes.of(el);
        }

        return data;
    }

    private static void encodeContainer(EncodeInputBox input) {
        Object value = input.value;
        byte[] data = SSZSerializer.encode(value);

        if (!input.field.skipContainer) {
            try {
                // Prepend data with its length
                input.result.write(SSZ.encodeInt32(data.length).toArrayUnsafe());
            } catch (IOException e) {
                throw new RuntimeException("Failed to write data length to stream", e);
            }
        }

        try {
            input.result.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void encodeBytes(EncodeInputBox input) {
        byte[] data = (byte[]) input.value;
        Bytes res;

        SSZType.Type type = input.field.sszType.type;
        switch (type) {
            case HASH: {
                res = SSZ.encodeHash(Bytes.of(data));
                break;
            }
            case ADDRESS: {
                res = SSZ.encodeAddress(Bytes.of(data));
                break;
            }
            case BYTES: {
                res = SSZ.encodeByteArray(data);
                break;
            }
            default: {
                throw new RuntimeException(String.format("Type %s encoding is not implemented yet", type));
            }
        }
        try {
            input.result.write(res.toArrayUnsafe());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write data of type %s to stream", type), e);
        }
    }

    private static void encodeBoolean(EncodeInputBox input) {
        boolean value = (boolean) input.value;
        Bytes res = SSZ.encodeBoolean(value);
        try {
            input.result.write(res.toArrayUnsafe());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write boolean value \"%s\" to stream", value), e);
        }
    }

    private static void encodeString(EncodeInputBox input) {
        String value = (String) input.value;
        Bytes res = SSZ.encodeString(value);
        try {
            input.result.write(res.toArrayUnsafe());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write string value \"%s\" to stream", value), e);
        }
    }

    public static void encodeNextField(Object value, SSZScheme.SSZField field, OutputStream result) {
        if (field.type.equals(List.class)) {
            encodeHandlers.get(LIST).accept(new EncodeInputBox(value, field, result));
        } else {
            if (field.skipContainer != null) {
                encodeHandlers.get(CONTAINER).accept(new EncodeInputBox(value, field, result));
            } else {
                encodeHandlers.get(field.sszType.type).accept(new EncodeInputBox(value, field, result));
            }
        }
    }

    static class EncodeInputBox {
        Object value;
        SSZScheme.SSZField field;
        OutputStream result;

        public EncodeInputBox(Object value, SSZScheme.SSZField field, OutputStream result) {
            this.value = value;
            this.field = field;
            this.result = result;
        }
    }
}
