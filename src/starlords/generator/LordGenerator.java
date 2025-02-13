package starlords.generator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import starlords.generator.support.AvailableShipData;
import starlords.generator.support.ShipData;
import starlords.generator.types.flagship.LordFlagshipPickerBase;
import starlords.generator.types.fleet.LordFleetGeneratorBase;
import starlords.person.PosdoLordTemplate;
import starlords.util.WeightedRandom;

import java.util.*;

public class LordGenerator {
    @Setter
    private static WeightedRandom[] sizeRatio = new WeightedRandom[4];
    @Setter
    private static WeightedRandom[] typeRatio = new WeightedRandom[3];
    @Setter
    private static WeightedRandom starlordLevelRatio;
    @Setter
    private static String fleetAdjective;//NOTE: this can go into the 'strings' file? or should it. arg?!??
    @Setter
    private static double isMaleChance;
    @Setter
    private static double[] personalityRatio = {};
    private static final String[] personalities = {
            "Quarrelsome",
            "Calculating",
            "Martial",
            "Upstanding"
    };
    @Setter
    private static double[] battlePersonalityRatio = {};
    private static final String[] battlePersonalities = {
            "Timid",
            "Cautious",
            "Steady",
            "Aggressive",
            "Reckless"
    };
    @Setter
    private static WeightedRandom maxShipRatio;
    @Setter
    private static WeightedRandom minShipRatio;
    @Setter
    private static WeightedRandom shipSpawnRatio;
    private static ArrayList<LordFleetGeneratorBase> fleetGeneratorTypes = new ArrayList<>();
    private static ArrayList<Double> fleetGeneratorRatio = new ArrayList<>();
    @Setter
    @Getter
    private static LordFleetGeneratorBase fleetGeneratorBackup = new LordFleetGeneratorBase("base");

    private static ArrayList<LordFlagshipPickerBase> flagshipPickerTypes = new ArrayList<>();
    private static ArrayList<Double> flagshipPickerRatio = new ArrayList<>();
    @Setter
    @Getter
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
    public static PosdoLordTemplate generateStarlord(String factionID){
        PosdoLordTemplate lord = new PosdoLordTemplate();

        //I need to run, and rerun getShips repeatedly, untill ether the number of ships I have is equal to my target, or until I am out of possable ships.
        //note: this requires redrawing my generator, each time? or do I keep my generator, but have it repick its target? I think repick target, 3 times.


        lord.factionId=factionID;
        //lord.fleetName=fleetName;
        lord.isMale=isMaleChance < Math.random();
        lord.level=starlordLevelRatio.getRandom();
        lord.personality = personalities[getValueFromWeight(personalityRatio)];
        lord.battlePersonality = battlePersonalities[getValueFromWeight(battlePersonalityRatio)];
        //lord.portrait=portrait;
        //lord.preferredItemId=prefurredITemId;
        lord.ranking=0;
        generateAllShipsForLord(lord,factionID);
        return lord;
    }
    private static void generateAllShipsForLord(PosdoLordTemplate lord, String factionID){
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
        boolean useAllShips = oddsOfNonePriorityShips > Math.random();
        boolean useSelectedShipsForFlagship = oddsOfNoneSelectedFlagship > Math.random();
        //lord.shipPrefs=generateShips(factionID,useAllShips,minShip,maxShip,sizeratio,typeratio);
        generateShips(lord,factionID,useAllShips,minShip,maxShip,sizeratio,typeratio);
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
                AvailableShipData.HULLTYPE_WARSHIP,
                AvailableShipData.HULLTYPE_CARRIER,
                AvailableShipData.HULLTYPE_PHASE
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
    private static int getIdTypes(String type){
        String[] a = new String[]{
            AvailableShipData.HULLTYPE_WARSHIP,
            AvailableShipData.HULLTYPE_CARRIER,
            AvailableShipData.HULLTYPE_PHASE
        };
        for (int b = 0; b < a.length; b++){
            if (a[b].equals(type)) return b;
        }
        return -1;
    }
    private static int getIdSizes(String size){
        String[] a = new String[]{
            AvailableShipData.HULLSIZE_FRIGATE,
            AvailableShipData.HULLSIZE_DESTROYER,
            AvailableShipData.HULLSIZE_CRUISER,
            AvailableShipData.HULLSIZE_CAPITALSHIP
        };
        for (int b = 0; b < a.length; b++){
            if (a[b].equals(size)) return b;
        }
        return -1;
    }
    private static HashMap<String, Integer> assingFleetSpawnWeights(ArrayList<ShipData> shipDatas, int[] types, int[] sizes){
        //int[] output = new int[shipDatas.size()];
        HashMap<String,Integer> output = new HashMap<>();
        ArrayList<Double> shipOdds = new ArrayList<>();
        for(ShipData a : shipDatas) {
            for (int b = 0; b < a.getSpawnWeight().size(); b++) {
                //String vareantID = (String) a.getSpawnWeight().keySet().toArray()[b];
                float weight = (float) a.getSpawnWeight().values().toArray()[b];//get(vareantID);
                shipOdds.add((double) (weight*shipSpawnRatio.getRandom()));
            }
        }
        double[] outputtemp = new double[shipOdds.size()];
        double[] outputtemp2 = new double[shipOdds.size()];
        double[] totalS = new double[4];
        double[] totalT = new double[3];
        int allS=0;
        int allT=0;
        int d = 0;
        for(ShipData a : shipDatas){
            for(int b = 0; b < a.getSpawnWeight().size(); b++) {
                //String vareantID = (String) a.getSpawnWeight().keySet().toArray()[b];
                double weight = shipOdds.get(d);//(float)a.getSpawnWeight().values().toArray()[b];//get(vareantID);
                totalS[(int) getIdSizes(a.getHullSize())] += weight;
                totalT[(int) getIdTypes(a.getHullType())] += weight;
                allS++;
                allT++;
                d++;
            }
        }
        double typeSize = types[0]+types[1]+types[2];
        double sizeSize = sizes[0]+sizes[1]+sizes[2]+sizes[3];
        double[] sizesMulti = new double[sizes.length];
        for(int a = 0; a < sizes.length; a++){
            double b = totalS[a] / allS;
            double c = sizes[a] / sizeSize;
            sizesMulti[a] = c/b;
        }
        d = 0;
        for(ShipData a : shipDatas){
            for(int b = 0; b < a.getSpawnWeight().size(); b++) {
                double weight = shipOdds.get(d);//(float)a.getSpawnWeight().values().toArray()[b];//get(vareantID);
                double t = (weight * sizesMulti[getIdSizes(a.getHullSize())]) * 10;
                outputtemp[d] = t;
                d++;
            }
        }

        double[] typesMulti = new double[types.length];
        for(int a = 0; a < types.length; a++){
            double b = totalT[a] / allT;
            double c = types[a] / typeSize;
            typesMulti[a] = c/b;
        }
        d = 0;
        for(ShipData a : shipDatas){
            for(int b = 0; b < a.getSpawnWeight().size(); b++) {
                double weight = shipOdds.get(d);//(float)a.getSpawnWeight().values().toArray()[b];//get(vareantID);
                double t = (weight * typesMulti[getIdTypes(a.getHullType())]) * 10;
                outputtemp[d] = t;
                d++;
            }
        }
        d=0;

        for(ShipData a : shipDatas) {
            Object[] keys = a.getSpawnWeight().keySet().toArray();
            for (int b = 0; b < a.getSpawnWeight().size(); b++) {
                String key = (String)keys[b];
                output.put(key, (int)(outputtemp[d]+outputtemp2[d])/2);
            }
        }
        /*for(int a = 0; a < output.size(); a++){
            output[a] = (outputtemp[a]+outputtemp2[a])/2;
        }*/

        return output;
    }
    private static void generateShips(PosdoLordTemplate lord,String factionID,boolean useAllShips,int minShip,int maxShip,int[] sizeratio,int[] typeratio){
        HashMap<String,Integer> output = new HashMap<>();
        //generate possible ships
        AvailableShipData availableShipData = getAvailableShips(factionID,useAllShips);
        ArrayList<ShipData> ships = new ArrayList<>();
        int maxLoops = 5;
        maxLoops = fleetGeneratorTypes.size()!=0 ? maxLoops : 0;
        while (availableShipData.getUnorganizedShips().size() != 0 && ships.size() < maxShip && maxLoops > 0) {
            //so, since this is the stage were I get any ships I might want, lets ignore the max ship count here.
            fleetGeneratorTypes.get(getValueFromWeight(fleetGeneratorRatio)).skimPossibleShips(availableShipData);
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
        shipSpawnRatio.getRandom();//this will act as a modifier to the 'weight' of a given variant, in its type / size ratio
        /* OK: math issue time.
        I have 4 'weights' (marked a[]) and 3 'weights' (marked b[]).
        I also have x items (i). each i has 2 weights in total, one from each array.
        write an equation were I take x I's, and make it so the raitio of each respects -both- inserted weigts perfictly. the ratio of all item

         */
        lord.shipPrefs = assingFleetSpawnWeights(ships,sizeratio,typeratio);
        if (availableShipData.getUnorganizedShips().size() != 0 && oddsOfNonePriorityShips < Math.random()){
            ships = new ArrayList<>();
            for(Object a : availableShipData.getUnorganizedShips().values().toArray()){
                ships.add((ShipData) a);
            }

        }
        lord.flagShip = pickFlagship(ships);
    }
    private static String pickFlagship(ArrayList<ShipData> ships){
        if (flagshipPickerTypes.size() == 0){
            return flagshipGeneratorBackup.pickFlagship(ships);
        }
        return flagshipPickerTypes.get(getValueFromWeight(flagshipPickerRatio)).pickFlagship(ships);
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
    private static int getValueFromWeight(ArrayList<Double> weight){
        double totalValue = 0;
        double randomValue = Math.random();
        for (double a : weight){
            totalValue+=a;
        }
        totalValue*=randomValue;
        double currentValue = 0;
        for (int a = 0; a < weight.size(); a++){
            currentValue+=weight.get(a);
            if (currentValue >= totalValue) return a;
        }
        return -1;//force a crash because I did something wrong and need to fix this.
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

    public static void addFleetGenerator(LordFleetGeneratorBase generator, double weight){
        if (generator == null || weight == 0) return;
        for (int a = 0; a < fleetGeneratorTypes.size(); a++){
            if(fleetGeneratorTypes.get(a).getName().equals(generator.getName())){
                fleetGeneratorTypes.remove(a);
                fleetGeneratorRatio.remove(a);
                break;
            }
        }
        fleetGeneratorTypes.add(generator);
        fleetGeneratorRatio.add(weight);
    }
    public static void addFlagshipPicker(LordFlagshipPickerBase picker, double weight){
        if (picker == null || weight == 0) return;
        for (int a = 0; a < flagshipPickerTypes.size(); a++){
            if(flagshipPickerTypes.get(a).getName().equals(picker.getName())){
                flagshipPickerTypes.remove(a);
                flagshipPickerRatio.remove(a);
                break;
            }
        }
        flagshipPickerTypes.add(picker);
        flagshipPickerRatio.add(weight);
    }
}
