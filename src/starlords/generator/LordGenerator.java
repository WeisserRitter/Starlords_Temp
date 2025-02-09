package starlords.generator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.combat.entities.Ship;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import starlords.generator.support.AvailableShipData;
import starlords.generator.support.ShipData;
import starlords.generator.types.flagship.LordFlagshipPickerBase;
import starlords.generator.types.fleet.LordFleetGeneratorBase;
import starlords.person.LordTemplate;
import starlords.util.Constants;
import starlords.util.WeightedRandom;

import java.util.*;

import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
public class LordGenerator {
    @Setter
    private static WeightedRandom[] sizeRatio = new WeightedRandom[4];
    @Setter
    private static WeightedRandom[] typeRatio = new WeightedRandom[4];
    @Setter
    private static WeightedRandom starlordLevelRatio;


    @Setter
    private static WeightedRandom maxShipRatio;
    @Setter
    private static WeightedRandom minShipRatio;

    private static LordFleetGeneratorBase[] fleetGeneratorTypes = {};
    private static double[] fleetGeneratorRatio = {};

    private static LordFlagshipPickerBase[] flagShipPickerTypes = {};
    private static double[] flagshipPickerRatio = {};

    @Setter
    private static double oddsOfNonePriorityShips;
    @Setter
    private static double oddsOfNoneSelectedFlagship;
    public static void tempTest(){
        Logger log = Global.getLogger(LordGenerator.class);;
        //log.info("DEBUG: got random of: "+maxShipRatio.getRandom());
        //log.info("DEBUG: got random of: "+minShipRatio.getRandom());
        log.info("DEBUG: got random of: "+starlordLevelRatio.getRandom());
        log.info("DEBUG: got random of: "+sizeRatio[0].getRandom());
        log.info("DEBUG: got random of: "+sizeRatio[1].getRandom());
        log.info("DEBUG: got random of: "+sizeRatio[2].getRandom());
        log.info("DEBUG: got random of: "+sizeRatio[3].getRandom());
    }
    @SneakyThrows
    public static LordTemplate generateStarlord(String factionID, MarketAPI StartingMarket){
        JSONObject json = Global.getSettings().getMergedJSONForMod("data/generator/blankLord.json", Constants.MOD_ID);
        int[] sizeratio = {
                sizeRatio[0].getRandom(),
                sizeRatio[1].getRandom(),
                sizeRatio[2].getRandom(),
                sizeRatio[3].getRandom()
        };
        int[] typeratio = {
                typeRatio[0].getRandom(),
                typeRatio[1].getRandom(),
                typeRatio[2].getRandom(),
                /*typeRatio[3].getRandom(),
                typeRatio[4].getRandom(),
                typeRatio[5].getRandom(),
                typeRatio[6].getRandom(),
                typeRatio[7].getRandom(),
                typeRatio[8].getRandom(),
                typeRatio[9].getRandom()*/
        };
        int maxShip = maxShipRatio.getRandom();
        int minShip = minShipRatio.getRandom();
        LordFleetGeneratorBase generator = fleetGeneratorTypes[getValueFromWeight(fleetGeneratorRatio)];
        LordFlagshipPickerBase flagShipPicker = flagShipPickerTypes[getValueFromWeight(flagshipPickerRatio)];
        int level = starlordLevelRatio.getRandom();
        boolean useAllShips = oddsOfNonePriorityShips > Math.random();
        boolean useSelectedShipsForFlagship = oddsOfNoneSelectedFlagship > Math.random();

        //I need to run, and rerun getShips repeatedly, untill ether the number of ships I have is equal to my target, or until I am out of possable ships.
        //note: this requires redrawing my generator, each time? or do I keep my generator, but have it repick its target? I think repick target, 3 times.

        /*json.getString("portrai");
        json.getString("");
        json.getString("");
        json.getString("");
        json.getString("");
        json.getString("");
        json.getString("");*/
        //LordTemplate lord = new LordTemplate("",json);
        //lord.name=name;
        //lord.lore=lore;
        //lord.battlePersonality=battlePersonality;
        //lord.factionId=factionID;
        //lord.flagShip=flahship;
        //lord.fleetName=fleetName;
        //lord.isMale=isMale;
        //lord.level=level;
        //lord.personality=personality;
        //lord.portrait=portrait;
        //lord.preferredItemId=prefurredITemId;
        //lord.ranking=ranking;
        //lord.shipPrefs=shipPrefs;
        return null;
    }
    private static ArrayList<ShipData> getShips(AvailableShipData ships,int[] sizeratio,int[] typeratio,int targetShips){
        ArrayList<ShipData> output = new ArrayList<>();
        String[] sizes = {
                AvailableShipData.HULLSIZE_FRIGATE,
                AvailableShipData.HULLSIZE_DESTROYER,
                AvailableShipData.HULLSIZE_CRUISER,
                AvailableShipData.HULLSIZE_CAPITALSHIP
        };
        String[] types = {
                AvailableShipData.HULLTYPE_CARRIER,
                AvailableShipData.HULLTYPE_WARSHIP,
                AvailableShipData.HULLTYPE_PHASE,
                /*AvailableShipData.HULLTYPE_CARGO,
                AvailableShipData.HULLTYPE_COMBATCIV,
                AvailableShipData.HULLTYPE_LINER,
                AvailableShipData.HULLTYPE_PERSONNEL,
                AvailableShipData.HULLTYPE_TANKER,
                AvailableShipData.HULLTYPE_TUG,
                AvailableShipData.HULLTYPE_UTILITY,*/
        };
        while(ships.getUnorganizedShips().size() > 0 && output.size() < targetShips){
            String type = sizes[getValueFromWeight(typeratio)];
            String size = types[getValueFromWeight(sizeratio)];
            Object[] a = ships.getOrganizedShips().get(type).get(size).values().toArray();
            if (a.length == 0) continue;
            ShipData b = (ShipData)a[(int)(Math.random() * a.length)];
            output.add(b);
            ships.getOrganizedShips().remove(b.getHullID());
            ships.getUnorganizedShips().remove(b.getHullID());
        }
        return output;
    }




    //gets availble ships to a faction (but only ones with default ship roles.). returns null if no matches
    public static AvailableShipData getAvailableShips(String factionID,boolean allShips){
        Set<String> a = null;
        if (!allShips) {
            a = Global.getSector().getFaction(factionID).getFactionSpec().getPriorityShips();
        }
        if (allShips || a == null || a.size() == 0){
            a = Global.getSector().getFaction(factionID).getFactionSpec().getKnownShips();
        }
        if (a == null){
            return null;
        }
        AvailableShipData b = AvailableShipData.getNewASD(a);
        if (b.getUnorganizedShips().size() == 0) return null;
        return b;
    }
    private static int getValueFromWeight(double[] weight){
        double totalValue = 0;
        double randomValue = Math.random();
        for (double a : weight){
            totalValue+=a;
        }
        totalValue*=randomValue;
        double currentValue = 0;
        for (int a = 0; a < weight.length; a++){
            currentValue+=weight[a];
            if (currentValue >= totalValue) return a;
        }
        return -1;//force a crash because I did something wrong and need to fix this.
    }
    private static int getValueFromWeight(int[] weight){
        double[] a = new double[weight.length];
        for (int b = 0; b < a.length; b++){
            a[b] = weight[b];
        }
        return getValueFromWeight(a);//force a crash because I did something wrong and need to fix this.
    }

}
