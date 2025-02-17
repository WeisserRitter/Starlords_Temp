package starlords.generator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import starlords.controllers.FiefController;
import starlords.controllers.LordController;
import starlords.generator.support.LordGeneratorListinerTemp;
import starlords.listeners.LordGeneratorListener_base;
import starlords.person.Lord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class NewGameLordPicker {
    public static NewGameLordPicker instance;
    @Setter
    @Getter
    private static ArrayList<String> excludeFactions = new ArrayList<>();
    @Getter
    @Setter
    private static HashMap<String,Double> bonusFactionLordSize = new HashMap<>();
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
            log.info("DEBUG: checking a market of id, faction, size: "+market.getId()+", "+market.getFactionId()+", "+market.getSize());
            factionMarkets.putIfAbsent(faction,new ArrayList<>());
            if (FiefController.getOwner(market) != null){
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
                log.info("DEBUG: excluding faction of ID: "+remove);
                factionMarkets.remove(remove);
                factionSize.remove(remove);
            }
        }
        log.info("DEBUG: got "+factionMarkets.size()+" diffrent factions to add lords to...");
        Random ran = new Random();
        LordGeneratorListinerTemp listiner = new LordGeneratorListinerTemp();
        for (Object factionID : factionMarkets.keySet().toArray()){
            int size = factionSize.get((String)factionID);
            log.info("DEBUG: considering adding lords to faction of "+(String) factionID+"... (size of: "+size+")");
            addFaction((String) factionID, factionMarkets.get((String) factionID),factionLords.get((String)factionID) ,size,listiner,ran);
        }
        LordGeneratorListener_base.removeListener(listiner);
    }
    public void addFaction(String factionID,ArrayList<MarketAPI> markets,ArrayList<Lord> lords, int size,LordGeneratorListinerTemp listiner,Random ran){
        if (!allowAdditionalLords && lords.size() != 0) return;
        log.info("DEBUG: attempting to add lords to "+(String)factionID+" with a size of "+size+" and "+markets.size()+" number of markets.");
        int T0Lords = (int) ((size*T0PerSize)+T0Addition);
        int T1Lords = (int) ((size*T1PerSize)+T1Addition);
        int T2Lords = (int) ((size*T2PerSize)+T2Addition);
        double multi = 1;
        if (bonusFactionLordSize.get((String)factionID) != null)multi= bonusFactionLordSize.get((String) factionID);
        T0Lords*=multi;
        T1Lords*=multi;
        //T2Lords*=multi;
        log.info("DEBUG: got t0,t1,t2 target lords: "+T0Lords+", "+T1Lords+", "+T2Lords);
        if (lords == null) lords = new ArrayList<>();
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
            listiner.fief="null";
            if (markets.size() > 0 && T2oddsOfFief < ran.nextDouble()){
                int id = ran.nextInt(markets.size());
                listiner.fief = markets.get(id).getId();
                markets.remove(id);
            }
            LordGenerator.createStarlord(factionID);
        }

        log.info("DEBUG: adding T1 lords... ");
        listiner.tier = 1;
        for (int a = 0; a < T1Lords; a++) {
            listiner.fief="null";
            if (markets.size() > 0 && T1oddsOfFief < ran.nextDouble()){
                int id = ran.nextInt(markets.size());
                listiner.fief = markets.get(id).getId();
                markets.remove(id);
            }
            LordGenerator.createStarlord(factionID);
        }

        log.info("DEBUG: adding T0 lords... ");
        listiner.tier = 0;
        for (int a = 0; a < T0Lords; a++) {
            listiner.fief="null";
            if (markets.size() > 0 && T0oddsOfFief < ran.nextDouble()){
                int id = ran.nextInt(markets.size());
                listiner.fief = markets.get(id).getId();
                markets.remove(id);
            }
            LordGenerator.createStarlord(factionID);
        }
    }
}
