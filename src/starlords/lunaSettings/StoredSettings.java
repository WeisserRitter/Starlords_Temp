package starlords.lunaSettings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import lombok.SneakyThrows;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import starlords.controllers.LordController;
import starlords.generator.LordGenerator;
import starlords.generator.support.AvailableShipData;
import starlords.person.Lord;
import starlords.util.Constants;
import starlords.util.WeightedRandom;

import java.io.IOException;

public class StoredSettings {
    public static void getSettings(){
        if (Global.getSettings().getModManager().isModEnabled("lunalib")){
            //getLunaSettings();
            //return;
        }
        getConfigSettings();
    }
    public static void attemptEnableLunalib(){
        if (!Global.getSettings().getModManager().isModEnabled("lunalib")) return;
        LunaSettings.addSettingsListener(new ApplySettingsOnChange());
    }
    private static void getLunaSettings(){
        Logger log = Global.getLogger(StoredSettings.class);;
        log.info("DEBUG: attempting to get lunaSettings");
        LordGenerator.setStarlordLevelRatio(getLunaWeightedRandom("generator_lordLevel"));
        LordGenerator.setSizeRatio(new WeightedRandom[]{
                getLunaWeightedRandom("generator_frigateRatio"),
                getLunaWeightedRandom("generator_destroyerRatio"),
                getLunaWeightedRandom("generator_cruiserRatio"),
                getLunaWeightedRandom("generator_captialRatio"),
        });
        //LordGenerator.setMinShipRatio(getLunaWeightedRandom("generator_lordLevel"));
        //LordGenerator.setMaxShipRatio(getLunaWeightedRandom("generator_lordLevel"));

        LordGenerator.tempTest();
        log.info("DEBUG: luna settings loaded successfully.");
    }
    @SneakyThrows
    private static void getConfigSettings(){
        AvailableShipData.startup();
        JSONObject json = Global.getSettings().getMergedJSONForMod("data/generator/starlord_generaterSettings.json",Constants.MOD_ID);
        //LordGenerator.set getConfigWeightedRandom("",json);
        Logger log = Global.getLogger(StoredSettings.class);;
        log.info("DEBUG: attempting to get normal config settings");
        LordGenerator.setStarlordLevelRatio(getConfigWeightedRandom("generator_lordLevel",json));
        LordGenerator.setSizeRatio(new WeightedRandom[]{
                getConfigWeightedRandom("generator_frigateRatio",json),
                getConfigWeightedRandom("generator_destroyerRatio",json),
                getConfigWeightedRandom("generator_cruiserRatio",json),
                getConfigWeightedRandom("generator_captialRatio",json),
        });
        //LordGenerator.setMinShipRatio(getConfigWeightedRandom("generator_lordLevel",json));
        //LordGenerator.setMaxShipRatio(getConfigWeightedRandom("generator_lordLevel",json));

        LordGenerator.tempTest();
        log.info("DEBUG: normal config settings loaded successfully.");
        for (FactionAPI a : Global.getSector().getAllFactions()) {
            LordGenerator.getAvailableShips(a.getId(),true);
        }
    }

    private static WeightedRandom getLunaWeightedRandom(String name){
        int min = LunaSettings.getInt(Constants.MOD_ID,name+"_min");
        int max = LunaSettings.getInt(Constants.MOD_ID,name+"_max");
        int target = LunaSettings.getInt(Constants.MOD_ID,name+"_target");
        double i = LunaSettings.getDouble(Constants.MOD_ID,name+"_i");
        return new WeightedRandom(max,min,i,target);
    }
    @SneakyThrows
    private static WeightedRandom getConfigWeightedRandom(String name, JSONObject object){
        //TODO test this please
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        object = object.getJSONObject(name);
        object.get("min");

        int min = object.getInt("min");
        int max = object.getInt("max");
        int target = object.getInt("target");
        double i = object.getDouble("i");
        return new WeightedRandom(max,min,i,target);
    }
}
