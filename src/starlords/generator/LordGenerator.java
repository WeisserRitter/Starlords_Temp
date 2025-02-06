package starlords.generator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.util.ListMap;
import lombok.Setter;
import org.apache.log4j.Logger;
import starlords.generator.types.flagship.LordFlagshipPickerBase;
import starlords.generator.types.fleet.LordFleetGeneratorBase;
import starlords.util.WeightedRandom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
public class LordGenerator {
    @Setter
    private static WeightedRandom[] sizeRatio = new WeightedRandom[4];
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

    public static void generateStarlord(String factionID, MarketAPI StartingMarket){
        int[] sizeratio = {
                sizeRatio[0].getRandom(),
                sizeRatio[1].getRandom(),
                sizeRatio[2].getRandom(),
                sizeRatio[3].getRandom()
        };
        int maxShip = maxShipRatio.getRandom();
        int minShip = minShipRatio.getRandom();
        LordFleetGeneratorBase generator = fleetGeneratorTypes[getValueFromWeight(fleetGeneratorRatio)];
        LordFlagshipPickerBase flagShipPicker = flagShipPickerTypes[getValueFromWeight(flagshipPickerRatio)];
        int level = starlordLevelRatio.getRandom();
        boolean useAllShips = oddsOfNonePriorityShips > Math.random();
        boolean useSelectedShipsForFlagship = oddsOfNoneSelectedFlagship > Math.random();
    }

    public static void getAvailableShips(String factionID,boolean allShips){
        /*Notes:
            1) isCivilan, isCombat, isCarrer appear to do nothing at all.
         */
        Logger log = Global.getLogger(LordGenerator.class);;
        Set<String> a = null;
        if (!allShips) {
            a = Global.getSector().getFaction(factionID).getFactionSpec().getPriorityShips();
        }
        if (allShips || a == null || a.size() == 0){
            a = Global.getSector().getFaction(factionID).getFactionSpec().getKnownShips();
        }
        Set<String> carriers = Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.CARRIER_LARGE);
        carriers.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.CARRIER_MEDIUM));
        carriers.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.CARRIER_SMALL));

        Set<String> warships = Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.COMBAT_CAPITAL);
        warships.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.COMBAT_LARGE));
        warships.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.COMBAT_MEDIUM));
        warships.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.COMBAT_SMALL));
        warships.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.COMBAT_SMALL_FOR_SMALL_FLEET));

        Set<String> phase = Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.PHASE_CAPITAL);
        phase.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.PHASE_LARGE));
        phase.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.PHASE_MEDIUM));
        phase.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.PHASE_SMALL));

        Set<String> combatFrigeters = Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.COMBAT_FREIGHTER_LARGE);
        combatFrigeters.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.COMBAT_FREIGHTER_MEDIUM));
        combatFrigeters.addAll(Global.getSector().getFaction(factionID).getVariantsForRole(ShipRoles.COMBAT_FREIGHTER_SMALL));

        log.info("runing data for faction of name: "+ Global.getSector().getFaction(factionID).getDisplayName());
        for (Object d : phase.toArray()){
            log.info("TESTING: getting a phase ship: "+ d);
        }
        //Global.getSector().getAutofitVariants().getTargetVariants()//this is for filling weapon slots I think???
        //Global.getSector().getAutofitVariants().getTargetVariants()//or maybe its not!?!?! I dont knowwwwwwww
        //filter out all ships without both a goal variant, and spawning conditions for a variant.
        //also filter out all ships that are only used for civilian operations.
        //Global.getFactory().createEmptyFleet()
        //Global.getSector().getFaction(factionID).pick
        //ok, so I dont know what im even looking at. like, how do I determin if a ship is phase, carrier, or not at all???
        Set<ShipVariantAPI> resultVariants = new HashSet<>();
        ListMap<String> allVariants = Global.getSettings().getHullIdToVariantListMap();
        //Global.getFactory().//getAllShipHullSpecs()
        for (String blueprintId : a) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(blueprintId);
            List<String> hullVariants = allVariants.getList(spec.getHullId());
            ShipVariantAPI targetVariant = null;
            for (String variantId : hullVariants) {
                ShipVariantAPI checked = Global.getSettings().getVariant(variantId);
                if (checked.isGoalVariant()) {
                    targetVariant = checked;
                }
                if (checked.isCivilian()){

                }
                boolean temp = true;
                if (phase.contains(variantId)){
                    log.info("DEBUG: determining phase ships stats; name: "+checked.getHullSpec().getHullName()+", isPhase: "+checked.getHullSpec().isPhase()+", size: "+checked.getHullSpec().getHullSize());
                    temp = false;
                }
                if (checked.getHullSpec().isPhase() && temp){
                    log.info("DEBUG: FAILED to get phase ship; name: "+checked.getHullSpec().getHullName()+", isPhase: "+checked.getHullSpec().isPhase()+", size: "+checked.getHullSpec().getHullSize());
                }
            }
            if (targetVariant != null) {
                resultVariants.add(targetVariant);
                targetVariant.getDesignation();//.getSource().//.getDesignation()
            }
        }
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

}
