package starlords.generator;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import starlords.generator.types.StarLordsFleetGeneratorBase;

public class StarLordsGenerator {
    public static double[][] sizeRatio = {
        {},
        {},
        {},
        {}
    };
    public static double[] maxShipRatio = {};
    public static double[] minShipRatio = {};

    public static StarLordsFleetGeneratorBase[] FleetGeneratorTypes = {};
    public static double[] fleetGeneratorRatio = {};

    public static double[] starlordLevelRatio = {};

    public static double oddsOfNonePriorityShips = 0;
    public static void generateStarlord(String factionID, MarketAPI StartingMarket){
        int[] sizeratio = {
                getValueFromWeight(sizeRatio[0]),
                getValueFromWeight(sizeRatio[1]),
                getValueFromWeight(sizeRatio[2]),
                getValueFromWeight(sizeRatio[3])
        };
        int maxShip = getValueFromWeight(maxShipRatio);
        int minShip = getValueFromWeight(minShipRatio);
        StarLordsFleetGeneratorBase generator = FleetGeneratorTypes[getValueFromWeight(fleetGeneratorRatio)];
        int level = getValueFromWeight(starlordLevelRatio);
        boolean useAllShips = false;
        if(oddsOfNonePriorityShips > Math.random()) useAllShips = true;
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
