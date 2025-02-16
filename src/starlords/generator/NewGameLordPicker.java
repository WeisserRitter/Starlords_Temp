package starlords.generator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import starlords.controllers.FiefController;
import starlords.controllers.LordController;
import starlords.generator.support.LordGeneratorListinerTemp;
import starlords.listeners.LordGeneratorListener;
import starlords.lunaSettings.StoredSettings;
import starlords.person.Lord;
import starlords.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewGameLordPicker {
    public static NewGameLordPicker instance;
    @Setter
    @Getter
    private static ArrayList<String> excludeFactions = new ArrayList<>();
    @Setter
    private double T0PerSize;
    @Setter
    private double T1PerSize;
    @Setter
    private double T2PerSize;
    @Setter
    private double T0Addition;
    @Setter
    private double T1Addition;
    @Setter
    private double T2Addition;

    @Setter
    private double T0oddsOfFief;
    @Setter
    private double T1oddsOfFief;
    @Setter
    private double T2oddsOfFief;

    @Setter
    private boolean allowAdditionalLords;
    Logger log = Global.getLogger(NewGameLordPicker.class);;
    public void addAll(){
        //lord generator settings
        log.info("DEBUG: attempting to add new game lords");
        HashMap<String,ArrayList<MarketAPI>> factionMarkets = new HashMap<>();
        HashMap<String,Integer> factionSize = new HashMap<>();
        HashMap<String,ArrayList<Lord>> factionLords = new HashMap<>();
        List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
        //getting faction sizes and possible fiefs.
        for (MarketAPI market : markets){
            String faction = market.getFactionId();
            if (FiefController.getOwner(market) != null){
                factionMarkets.putIfAbsent(faction,new ArrayList<>());
                factionMarkets.get(faction).add(market);
            }
            factionSize.putIfAbsent(faction, 0);
            factionSize.put(faction,factionSize.get(faction)+market.getSize());
        }
        //getting all already created lords
        for(Lord lord : LordController.getLordsList()){
            String factionID = lord.getFaction().getId();
            factionLords.putIfAbsent(factionID,new ArrayList<>());
            factionLords.get(factionID).add(lord);
        }
        //removing excluded factions
        for (String remove : excludeFactions){
            if (factionMarkets.get(remove) != null){
                factionMarkets.remove(remove);
                factionSize.remove(remove);
            }
        }

        log.info("DEBUG: got "+factionMarkets.size()+" diffrent factions to add lords to...");
        LordGeneratorListinerTemp listiner = new LordGeneratorListinerTemp();
        for (Object factionID : factionMarkets.keySet().toArray()){
            addFaction((String) factionID, factionMarkets.get((String) factionID),factionLords.get((String)factionID) ,factionSize.get((String)factionID),listiner);
        }
        LordGeneratorListener.removeListener(listiner);
    }
    public void addFaction(String factionID,ArrayList<MarketAPI> markets,ArrayList<Lord> lords, int size,LordGeneratorListinerTemp listiner){
        if (!allowAdditionalLords && lords.size() != 0) return;
        log.info("DEBUG: attempting to add lords to "+(String)factionID+" with a size of "+size+" and "+markets.size()+" number of markets.");
        int T0Lords = (int) ((size*T0PerSize)+T0Addition);
        int T1Lords = (int) ((size*T1PerSize)+T1Addition);
        int T2Lords = (int) ((size*T2PerSize)+T2Addition);
        log.info("DEBUG: got t0,t1,t2 target lords: "+T0Lords+", "+T1Lords+", "+T2Lords);
        for (Lord a: lords){
            switch (a.getRanking()){
                case 0:
                    T0Lords--;
                    break;
                case 1:
                    T1Lords--;
                    break;
                case 2:
                    T2Lords--;
                    break;
            }
        }
        log.info("DEBUG: after pruning the remaining lords to add are: t0,t1,t2: "+T0Lords+", "+T1Lords+", "+T2Lords);
        log.info("DEBUG: adding T2 lords... ");
        listiner.tier = 2;
        for (int a = 0; a < T2Lords; a++) {
            listiner.fief=null;
            if (markets.size() != 0 && T2oddsOfFief < Math.random()){
                int id = (int) (Math.random()*markets.size());
                listiner.fief = markets.get(id).getId();
                markets.remove(id);
            }
            LordGenerator.createStarlord(factionID);
        }

        log.info("DEBUG: adding T1 lords... ");
        listiner.tier = 1;
        for (int a = 0; a < T1Lords; a++) {
            listiner.fief=null;
            if (markets.size() != 0 && T1oddsOfFief < Math.random()){
                int id = (int) (Math.random()*markets.size());
                listiner.fief = markets.get(id).getId();
                markets.remove(id);
            }
            LordGenerator.createStarlord(factionID);
        }

        log.info("DEBUG: adding T0 lords... ");
        listiner.tier = 0;
        for (int a = 0; a < T0Lords; a++) {
            listiner.fief=null;
            if (markets.size() != 0 && T0oddsOfFief < Math.random()){
                int id = (int) (Math.random()*markets.size());
                listiner.fief = markets.get(id).getId();
                markets.remove(id);
            }
            LordGenerator.createStarlord(factionID);
        }
    }
}
