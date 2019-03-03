package connectionIndependent.Scrawings;

import java.util.ArrayList;

public class ByteBuilder {
    ArrayList<Byte> array = new ArrayList<>();

    public ArrayList<Byte> getArray() {
        return array;
    }

    public void setArray(Byte aByte) {
        array.add(aByte);
    }


    public Byte[] build() {
        Byte[] bytes = new Byte[array.size()];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = array.get(i);
        }

        return bytes;
    }
}
