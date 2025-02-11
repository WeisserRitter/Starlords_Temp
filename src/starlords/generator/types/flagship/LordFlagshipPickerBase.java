package starlords.generator.types.flagship;

import lombok.Getter;

public class LordFlagshipPickerBase {
    @Getter
    private String name = "";
    public LordFlagshipPickerBase(String name){
        this.name = name;
    }
}
