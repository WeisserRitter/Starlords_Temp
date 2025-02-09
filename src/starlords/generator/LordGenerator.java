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
import starlords.person.PosdoLordTemplate;
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
    private static String fleetName;//NOTE: this can go into the 'strings' file? or should it. arg?!??
    @Setter
    private static double isMaleChance;
    @Setter
    private static double[] personalityRatio = {};
    private static String[] personalities = {};
    @Setter
    private static double[] battlePersonalityRatio = {};
    private static String[] battlePersonalities = {};
    @Setter
    private static WeightedRandom maxShipRatio;
    @Setter
    private static WeightedRandom minShipRatio;

    private static LordFleetGeneratorBase[] fleetGeneratorTypes = {};
    private static double[] fleetGeneratorRatio = {};
    private static LordFleetGeneratorBase fleetGeneratorBackup;

    private static LordFlagshipPickerBase[] flagShipPickerTypes = {};
    private static double[] flagshipPickerRatio = {};
    private static LordFlagshipPickerBase flagshipGeneratorBackup;

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
    public static PosdoLordTemplate generateStarlord(String factionID, MarketAPI StartingMarket){
        PosdoLordTemplate lord = new PosdoLordTemplate();
        int[] sizeratio = {
                sizeRatio[0].getRandom(),
                sizeRatio[1].getRandom(),
                sizeRatio[2].getRandom(),
                sizeRatio[3].getRandom()
        };
        if (sizeratio[0] == 0 && sizeratio[1] == 0 && sizeratio[2] == 0 && sizeratio[3] == 0){
            sizeratio[(int)(Math.random() * 4)] = 1;
        }
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
        if (typeratio[0] == 0 && typeratio[1] == 0 && typeratio[2] == 0){
            typeratio[(int)(Math.random() * 3)] = 1;
        }
        int maxShip = maxShipRatio.getRandom();
        int minShip = minShipRatio.getRandom();
        LordFleetGeneratorBase generator = fleetGeneratorTypes[getValueFromWeight(fleetGeneratorRatio)];
        LordFlagshipPickerBase flagShipPicker = flagShipPickerTypes[getValueFromWeight(flagshipPickerRatio)];
        boolean useAllShips = oddsOfNonePriorityShips > Math.random();
        boolean useSelectedShipsForFlagship = oddsOfNoneSelectedFlagship > Math.random();

        //I need to run, and rerun getShips repeatedly, untill ether the number of ships I have is equal to my target, or until I am out of possable ships.
        //note: this requires redrawing my generator, each time? or do I keep my generator, but have it repick its target? I think repick target, 3 times.


        lord.factionId=factionID;
        lord.shipPrefs=generateShips(factionID,useAllShips,minShip,maxShip,sizeratio,typeratio);
        //lord.flagShip=flahship;
        //lord.fleetName=fleetName;
        lord.isMale=isMaleChance < Math.random();
        lord.level=starlordLevelRatio.getRandom();
        lord.personality = personalities[getValueFromWeight(personalityRatio)];
        lord.battlePersonality = battlePersonalities[getValueFromWeight(battlePersonalityRatio)];
        //lord.portrait=portrait;
        //lord.preferredItemId=prefurredITemId;
        lord.ranking=0;
        return null;
    }
    private static ArrayList<ShipData> getShips(AvailableShipData ships,int[] typeratio,int[] sizeratio,int targetShips){
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
        int[] tempSize = new int[sizeratio.length];
        int[] tempType = new int[typeratio.length];
        int maxLoops = targetShips * 5;
        while(ships.getUnorganizedShips().size() > 0 && output.size() < targetShips && maxLoops > 0){
            int s = getLowestNumberID(sizeratio,tempSize);
            int t = getLowestNumberID(typeratio,tempType);
            ShipData ship = ships.getRandomShip(types[t],sizes[s]);
            if (ship == null) {
                maxLoops--;
                continue;
            }
            output.add(ship);
            ships.removeShip(ship.getHullID());
            maxLoops--;
        }
        //a backup, do to the possibility of 'missing' elegable ships in the past while loop (if both counters end up in sync). this does provide an incorrect ratio though.
        while (ships.getOrganizedShips().size() > 0 && output.size() < targetShips){
            boolean emergencyBreak = true;
            for (int a = 0; a < typeratio.length; a++){
                if (typeratio[a] == 0){
                    continue;
                }
                for (int b = 0; b < sizeratio.length; b++){
                    if (sizeratio[b] == 0){
                        continue;
                    }
                    ShipData ship = ships.getRandomShip(types[a],sizes[b]);
                    if (ship == null) {
                        continue;
                    }
                    output.add(ship);
                    ships.removeShip(ship.getHullID());
                    emergencyBreak = false;
                }
            }
            if (emergencyBreak) break;
        }
        return output;
    }

    private static HashMap<String,Integer> generateShips(String factionID,boolean useAllShips,int minShip,int maxShip,int[] sizeratio,int[] typeratio){
        HashMap<String,Integer> output = new HashMap<>();
        //generate possible ships
        AvailableShipData availableShipData = getAvailableShips(factionID,useAllShips);
        ArrayList<ShipData> ships = new ArrayList<>();
        int maxLoops = 5;
        maxLoops = fleetGeneratorTypes.length!=0 ? maxLoops : 0;
        while (availableShipData.getUnorganizedShips().size() != 0 && ships.size() < maxShip && maxLoops > 0) {
            //so, since this is the stage were I get any ships I might want, lets ignore the max ship count here.
            fleetGeneratorTypes[getValueFromWeight(fleetGeneratorRatio)].skimPossibleShips(availableShipData);
            ArrayList<ShipData> newShips = getShips(availableShipData,sizeratio,typeratio,maxShip);
            for (ShipData a : newShips){
                ships.add(a);
                availableShipData.removeShip(a.getHullID());
            }
            maxLoops--;
        }
        if(ships.size() < minShip){
            fleetGeneratorBackup.skimPossibleShips(availableShipData);
            ArrayList<ShipData> newShips = getShips(availableShipData,sizeratio,typeratio,maxShip);
            for (ShipData a : newShips){
                ships.add(a);
                availableShipData.removeShip(a.getHullID());
            }
        }

        //generate ship ratio


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
    private static int getLowestNumberID(int[] list,int[] tempData){
        int out = 0;
        while(true) {
            for (int a = 0; a < tempData.length; a++) {
                if (tempData[a] > out) out = tempData[a];
            }
            if (out != 0) {
                tempData[out]--;
                return out;
            }
            for (int a = 0; a < tempData.length; a++) {
                tempData[a] = list[a];
            }
        }
    }
    public static int[][] targetNumbers(int[] listA,int[] listB,int target){
        ///is this even required? yes it is. it will remove I cant od this this way!
        /*IF sucsesfull, this would in fact remove the issue of having the wrong 'raitio' of ships, BUT
        * 1) there is no garenty the wanted raitio is even truely possable????
        *   -yes there is. getting the right number of different ships might be impossible, but getting the right number of ships 100% is possible. because I can just modify the frequancy they spawn at
        * 2) there is no garenty I have enouth ships for each type anyways. but is that a issue?
        * ...
        * ok solution time:
        * make it like... I get one ship of one type / size, the another of a diffrent type. but I then change the numbers so like...
        * so like, if I wanted a raitio of 5/4/2/1, I would have a total of 11, were I got in an order that would result in a equal distribution. so..
        * I would get 1,1,2,1,2,1,2,3,1,2,3,1,reset because everything is zero.*/
        int[][] output = new int[listA.length][listB.length];
        int totala = 0;
        int totalb = 0;
        for (int a = 0; a < listA.length; a++){
            totala+=listA[a];
        }
        for (int b = 0; b < listB.length; b++){
            totalb+=listB[b];
        }
        int totalWantedShips = totala*totalb;

        return output;
    }
}
