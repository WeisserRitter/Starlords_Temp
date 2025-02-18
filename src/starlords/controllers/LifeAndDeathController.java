package starlords.controllers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import lombok.Getter;
import lombok.Setter;
import starlords.generator.LordGenerator;
import starlords.person.Lord;
import starlords.util.Constants;
import starlords.util.WeightedRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LifeAndDeathController extends BaseIntelPlugin{
    //no longer going to put this into the quest controler. it can stand on its own to feet. hopefully.
    private static LifeAndDeathController instance = null;
    private HashMap<MarketAPI,Double> markets = new HashMap<>();
    @Getter
    private ArrayList<String> excludedFaction = new ArrayList<>();
    @Getter
    private HashMap<String,Double> extremestFactions = new HashMap<>();
    @Setter
    private double gainPerSizeMulti;
    @Setter
    private double gainPerSizeExponent;
    @Setter
    private double stabilityLossMulti;

    @Setter
    private int maxLords;
    @Setter
    private int minLords;

    @Setter
    private int softMaxLords;
    @Setter
    private int softMinLords;
    @Setter
    private double slowDownPerExtraLord;
    @Setter
    private double speedUpPerExtraLord;

    @Setter
    private int stabilityReqForExtremists;
    @Setter
    private double oddsOfExtremistsPerStabilityLoss;

    private WeightedRandom pointsOnMarketCreation;

    @Setter
    private double oddsOfDeath;
    @Setter
    private int requiredPoints;
    private LifeAndDeathController(){
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()){
            String a = market.getId();
            addMarket(Global.getSector().getEconomy().getMarket(a));
        }
    }
    public boolean attemptToKillStalord(Lord lord){
        if (!Constants.ENABLE_LIFE_AND_DEATH_SYSTEM ||
            LordController.getLordsList().size() <= minLords ||
            LordGenerator.getRandom().nextDouble() < oddsOfDeath ||
            lord.getLordAPI().hasTag(Lord.TAG_IMMORTAL)) return false;
        //todo: make this so it puts out a intel saying they are dead.
        LordController.removeLordMidGame(lord);
        return true;
    }
    public void runMonth(){
        if (!Constants.ENABLE_LIFE_AND_DEATH_SYSTEM || LordController.getLordsList().size() >= maxLords) return;
        for(MarketAPI a : Global.getSector().getEconomy().getMarketsCopy()){
            MarketAPI market = Global.getSector().getEconomy().getMarket(a.getId());
            if (excludedFaction.contains(market.getFactionId())) continue;
            if (markets.get(market) == null) addMarket(market);
            addMarketsMonthlyPonits( market);
            attemptToSpawnLord( market);
        }
    }
    public void attemptToSpawnLord(MarketAPI market){
        while(markets.get(market) >= requiredPoints){
            markets.put(market,markets.get(market)-requiredPoints);
            LordGenerator.createStarlord(getSpawnedLordFaction(market),market);
            //todo: put the intell here to saw that a new lord has been spawned. how does that even work again?
        }
    }
    private String getSpawnedLordFaction(MarketAPI market){
        int stab = market.getStability().getModifiedInt();
        //short circlet this loop if lord is guaranteed to be part of the main faction.
        if (stab > stabilityReqForExtremists) return market.getFactionId();
        //get extremest faction if required
        if (LordGenerator.getRandom().nextDouble() > ((stabilityReqForExtremists+1) - stab) * oddsOfExtremistsPerStabilityLoss){
            double totalWeight = 0;
            for (String a : extremestFactions.keySet()){
                totalWeight+=extremestFactions.get(a);
            }
            double target = LordGenerator.getRandom().nextDouble()*totalWeight;
            totalWeight=0;
            for (String a : extremestFactions.keySet()){
                totalWeight+=extremestFactions.get(a);
                if (totalWeight >= target){
                    return a;
                }
            }
        }
        return market.getFactionId();
    }
    public void addMarketsMonthlyPonits(MarketAPI market){
        addPonits(market,getGainedPonits(market));
    }
    public double getGainedPonits(MarketAPI market){
        double multi = 1;
        if (LordController.getLordsList().size() > softMaxLords) multi -= (LordController.getLordsList().size()-softMaxLords)*slowDownPerExtraLord;
        if (LordController.getLordsList().size() < softMinLords) multi += (softMinLords-LordController.getLordsList().size())*speedUpPerExtraLord;
        int stab = market.getStability().getModifiedInt();
        int size = market.getSize();
        double output = (size*gainPerSizeMulti) + (Math.pow(gainPerSizeExponent,size))*((10-stab)*stabilityLossMulti);
        output*=multi;
        return output;
    }
    public void addMarket(MarketAPI market){
        markets.putIfAbsent(market,pointsOnMarketCreation.getRandom()*1d);
    }
    public void removeMarket(MarketAPI market){
        //todo: put this everywhere the fief controllers destroy market is.
        markets.remove(market);
    }
    public double getPonits(MarketAPI market){
        if (markets.get(market) == null) return 0;
        return markets.get(market);
    }
    public void setPonits(MarketAPI market, double ponits){
        markets.putIfAbsent(market,0d);
        markets.put(market,ponits);
    }
    public void addPonits(MarketAPI market, double ponits){
        markets.putIfAbsent(market,0d);
        setPonits(market,markets.get(market)+ponits);
    }
    public static LifeAndDeathController getInstance(boolean forceReset){
        if (forceReset || instance == null){
            List<IntelInfoPlugin> intel = Global.getSector().getIntelManager().getIntel(LifeAndDeathController.class);
            if (intel.isEmpty()) {
                instance = new LifeAndDeathController();
                Global.getSector().getIntelManager().addIntel(instance, true);
            } else {
                if (intel.size() > 1) {
                    throw new IllegalStateException("Should only be one LifeAndDeathController intel registered");
                }
                instance = (LifeAndDeathController) intel.get(0);
            }
        }
        return instance;
    }
    public static LifeAndDeathController getInstance(){
        return getInstance(false);
    }
}
