package starlords.generator.types.flagship;

import lombok.Getter;
import starlords.generator.support.ShipData;

import java.util.ArrayList;

public class LordFlagshipPickerBase {
    @Getter
    private String name = "";
    public LordFlagshipPickerBase(String name){
        this.name = name;
    }
    public String pickFlagship(ArrayList<ShipData> ships){
        Object[] a = (ships.get((int)(Math.random()*ships.size()))).getSpawnWeight().keySet().toArray();
        return (String)a[(int) (Math.random()*a.length)];

    }
}
