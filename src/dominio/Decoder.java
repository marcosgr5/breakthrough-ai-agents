package dominio;
import java.util.*;

public class Decoder {

    HashMap<String, Long> keyToLong;
    HashMap<Long, String> longToKey;

    public Decoder() {
        keyToLong = new HashMap<>();
        longToKey = new HashMap<>();
        long currentBit = 0x0000000000000001L;
        String key;
        for(int i = 1; i <= 8; i++) {
            for(int j = 'H'; j >= 'A'; j--) {
                key = (char)j + "" + i;
                keyToLong.put(key, currentBit);
                longToKey.put(currentBit, key);
                currentBit = currentBit<<1;
            }
        }
    }

    public long decodeKey (String s) {
        return keyToLong.get(s);
    }

    public String decodeLong (Long l) {
        return longToKey.get(l);
    }

    public boolean idValido(String s) {
        return keyToLong.containsKey(s);
    }
 
}
