package starlords.lunaSettings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import lombok.SneakyThrows;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import starlords.generator.LordGenerator;
import starlords.generator.support.AvailableShipData;
import starlords.generator.types.flagship.LordFlagshipPickerBase;
import starlords.generator.types.flagship.LordFlagshipPicker_Cost;
import starlords.generator.types.flagship.LordFlagshipPicker_DP;
import starlords.generator.types.flagship.LordFlagshipPicker_HP;
import starlords.generator.types.fleet.LordFleetGeneratorBase;
import starlords.generator.types.fleet.LordFleetGenerator_Desing;
import starlords.generator.types.fleet.LordFleetGenerator_Hullmod;
import starlords.generator.types.fleet.LordFleetGenerator_System;
import starlords.util.Constants;
import starlords.util.WeightedRandom;

public class StoredSettings {
    public static void getSettings(){
        AvailableShipData.startup();
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
        Logger log = Global.getLogger(StoredSettings.class);;
        log.info("DEBUG: attempting to get lunaSettings");
        LordGenerator.setStarlordLevelRatio(getLunaWeightedRandom("generator_lordLevel"));
        LordGenerator.setSizeRatio(new WeightedRandom[]{
                getLunaWeightedRandom("generator_frigateRatio"),
                getLunaWeightedRandom("generator_destroyerRatio"),
                getLunaWeightedRandom("generator_cruiserRatio"),
                getLunaWeightedRandom("generator_captialRatio")
        });
        LordGenerator.setTypeRatio(new WeightedRandom[]{
                getLunaWeightedRandom("generator_warshipRatio"),
                getLunaWeightedRandom("generator_carrierRatio"),
                getLunaWeightedRandom("generator_phaseRatio")
        });
        LordGenerator.setPersonalityRatio(new double[]{
                LunaSettings.getDouble(Constants.MOD_ID,"generator_personalityRatio_Quarrelsome"),
                LunaSettings.getDouble(Constants.MOD_ID,"generator_personalityRatio_Calculating"),
                LunaSettings.getDouble(Constants.MOD_ID,"generator_personalityRatio_Martial"),
                LunaSettings.getDouble(Constants.MOD_ID,"generator_personalityRatio_Upstanding")
        });
        LordGenerator.setBattlePersonalityRatio(new double[]{
                LunaSettings.getDouble(Constants.MOD_ID,"generator_battlePersonalityRatio_Timid"),
                LunaSettings.getDouble(Constants.MOD_ID,"generator_battlePersonalityRatio_Cautious"),
                LunaSettings.getDouble(Constants.MOD_ID,"generator_battlePersonalityRatio_Steady"),
                LunaSettings.getDouble(Constants.MOD_ID,"generator_battlePersonalityRatio_Aggressive"),
                LunaSettings.getDouble(Constants.MOD_ID,"generator_battlePersonalityRatio_Reckless")
        });
        LordGenerator.setIsMaleChance(LunaSettings.getDouble(Constants.MOD_ID,"generator_isMaleChance"));
        String temp = LunaSettings.getString(Constants.MOD_ID,"generator_fleetAdjective");
        if (temp == null) temp = "";
        LordGenerator.setFleetAdjective(temp);

        LordGenerator.setOddsOfNonePriorityShips(LunaSettings.getDouble(Constants.MOD_ID,"generator_oddsOfNonePriorityShips"));
        LordGenerator.setOddsOfNoneSelectedFlagship(LunaSettings.getDouble(Constants.MOD_ID,"generator_oddsOfNoneSelectedFlagship"));

        LordGenerator.addFleetGenerator(new LordFleetGenerator_System("system"),LunaSettings.getDouble(Constants.MOD_ID,"generator_fleetTemplates_system"));
        LordGenerator.addFleetGenerator(new LordFleetGenerator_Desing("Desing"),LunaSettings.getDouble(Constants.MOD_ID,"generator_fleetTemplates_hullmod"));
        LordGenerator.addFleetGenerator(new LordFleetGenerator_Hullmod("Hullmod"),LunaSettings.getDouble(Constants.MOD_ID,"generator_fleetTemplates_manufacture"));
        LordGenerator.addFleetGenerator(new LordFleetGeneratorBase("random"),LunaSettings.getDouble(Constants.MOD_ID,"generator_fleetTemplates_random"));

        LordGenerator.addFlagshipPicker(new LordFlagshipPicker_Cost("cost"),LunaSettings.getDouble(Constants.MOD_ID,"generator_flagshipPicker_Cost"));
        LordGenerator.addFlagshipPicker(new LordFlagshipPickerBase("random"),LunaSettings.getDouble(Constants.MOD_ID,"generator_flagshipPicker_Random"));
        LordGenerator.addFlagshipPicker(new LordFlagshipPicker_HP("hp"),LunaSettings.getDouble(Constants.MOD_ID,"generator_flagshipPicker_HP"));


        LordGenerator.setShipSpawnRatio(getLunaWeightedRandom("generator_shipSpawnRatio"));
        LordGenerator.setMaxShipRatio(getLunaWeightedRandom("generator_maxShipSpawnRatio"));
        LordGenerator.setMinShipRatio(getLunaWeightedRandom("generator_minShipSpawnRatio"));
        //LordGenerator.setMinShipRatio(getLunaWeightedRandom("generator_lordLevel"));
        //LordGenerator.setMaxShipRatio(getLunaWeightedRandom("generator_lordLevel"));

        log.info("DEBUG: luna settings loaded successfully.");
    }
    @SneakyThrows
    private static void getConfigSettings(){
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
        LordGenerator.setTypeRatio(new WeightedRandom[]{
                getConfigWeightedRandom("generator_warshipRatio",json),
                getConfigWeightedRandom("generator_carrierRatio",json),
                getConfigWeightedRandom("generator_phaseRatio",json)
        });
        LordGenerator.setPersonalityRatio(new double[]{
                json.getDouble("generator_personalityRatio_Quarrelsome"),
                json.getDouble("generator_personalityRatio_Calculating"),
                json.getDouble("generator_personalityRatio_Martial"),
                json.getDouble("generator_personalityRatio_Upstanding")
        });
        LordGenerator.setBattlePersonalityRatio(new double[]{
                json.getDouble("generator_battlePersonalityRatio_Timid"),
                json.getDouble("generator_battlePersonalityRatio_Cautious"),
                json.getDouble("generator_battlePersonalityRatio_Steady"),
                json.getDouble("generator_battlePersonalityRatio_Aggressive"),
                json.getDouble("generator_battlePersonalityRatio_Reckless")
        });
        LordGenerator.setIsMaleChance(json.getDouble("generator_isMaleChance"));
        String temp = json.getString("generator_fleetAdjective");
        if (temp == null) temp = "";
        LordGenerator.setFleetAdjective(temp);

        LordGenerator.setOddsOfNonePriorityShips(json.getDouble("generator_oddsOfNonePriorityShips"));
        LordGenerator.setOddsOfNoneSelectedFlagship(json.getDouble("generator_oddsOfNoneSelectedFlagship"));

        LordGenerator.addFleetGenerator(new LordFleetGenerator_System("system"),json.getDouble("generator_fleetTemplates_system"));
        LordGenerator.addFleetGenerator(new LordFleetGenerator_Desing("Desing"),json.getDouble("generator_fleetTemplates_hullmod"));
        LordGenerator.addFleetGenerator(new LordFleetGenerator_Hullmod("Hullmod"),json.getDouble("generator_fleetTemplates_manufacture"));
        LordGenerator.addFleetGenerator(new LordFleetGeneratorBase("random"),json.getDouble("generator_fleetTemplates_random"));

        LordGenerator.addFlagshipPicker(new LordFlagshipPicker_Cost("cost"),json.getDouble("generator_flagshipPicker_Cost"));
        LordGenerator.addFlagshipPicker(new LordFlagshipPicker_DP("random"),json.getDouble("generator_flagshipPicker_Random"));
        LordGenerator.addFlagshipPicker(new LordFlagshipPicker_HP("hp"),json.getDouble("generator_flagshipPicker_HP"));

        LordGenerator.setMaxShipRatio(getConfigWeightedRandom("generator_maxShipSpawnRatio",json));
        LordGenerator.setMinShipRatio(getConfigWeightedRandom("generator_minShipSpawnRatio",json));

        LordGenerator.setShipSpawnRatio(getConfigWeightedRandom("generator_shipSpawnRatio",json));
        log.info("DEBUG: normal config settings loaded successfully.");
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
