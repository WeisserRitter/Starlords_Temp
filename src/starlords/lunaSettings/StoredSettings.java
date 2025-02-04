package starlords.lunaSettings;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;

public class StoredSettings {
    public static void getSettings(){
        if (Global.getSettings().getModManager().isModEnabled("lunalib")){
            getLunaSettings();
            return;
        }
        getConfigSettings();
    }
    public static void attemptEnableLunalib(){
        if (!Global.getSettings().getModManager().isModEnabled("lunalib")) return;
        LunaSettings.addSettingsListener(new ApplySettingsOnChange());
    }
    private static void getLunaSettings(){
        String modName = "starlords";
        //LunaSettings.getString(modName, "FieldID");
    }
    private static void getConfigSettings(){
        Global.getSettings().getString("");

    }
}
