package starlords.controllers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import lombok.Getter;
import lombok.Setter;
import starlords.generator.LordGenerator;
import starlords.person.Lord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LifeAndDeathControler extends BaseIntelPlugin{
    //no longer going to put this into the quest controler. it can stand on its own to feet. hopefully.
    private static LifeAndDeathControler instance = null;
    private HashMap<MarketAPI,Double> markets = new HashMap<>();
    private ArrayList<String> exscludedFaction = new ArrayList<>();
    private HashMap<String,Double> extremestFactions = new HashMap<>();
    private double gainPerSizeMulti;
    private double gainPerSizeExponent;

    @Setter
    @Getter
    private double oddsOfDeath;
    private int requiredPoints;
    private LifeAndDeathControler(){
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()){
            String a = market.getId();
            addMarket(Global.getSector().getEconomy().getMarket(a));
        }
    }
    public boolean attemptToKillStalord(Lord lord){
        //todo: make this so it randomly kills the starlord with out the immortal tag, returns whether it did or not, and then puts out a intel saying they are dead.
        //todo: make it so this always returns false when this is disabled.
        return false;
    }
    public void runMonth(){
        //todo: make it so this never runs when this is disabled.
        for (Object market : markets.keySet().toArray()){
            addMarketsMonthlyPonits((MarketAPI) market);
            attemptToSpawnLord((MarketAPI) market);
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
        //todo: make this allow for extremest factions to spawn at low stab.
        return market.getFactionId();
    }
    public void addMarketsMonthlyPonits(MarketAPI market){
        addPonits(market,getGainedPonits(market));
    }
    public double getGainedPonits(MarketAPI market){
        //todo: make this return the currect amount of ponits per a month of starlords being cool.
        return 0d;
    }
    public void addMarket(MarketAPI market){
        //todo: make this have the desred weighted random value.
        markets.putIfAbsent(market,0d);
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
    public static LifeAndDeathControler getInstance(){
        if (instance == null){
            List<IntelInfoPlugin> intel = Global.getSector().getIntelManager().getIntel(LifeAndDeathControler.class);
            if (intel.isEmpty()) {
                instance = new LifeAndDeathControler();
                Global.getSector().getIntelManager().addIntel(instance, true);
            } else {
                if (intel.size() > 1) {
                    throw new IllegalStateException("Should only be one QuestController intel registered");
                }
                instance = (LifeAndDeathControler) intel.get(0);
            }
        }
        return instance;
    }
}
