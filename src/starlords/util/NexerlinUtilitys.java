package starlords.util;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexUtils;
import exerelin.utilities.NexUtilsFaction;
import exerelin.utilities.NexUtilsMarket;

public class NexerlinUtilitys {
    public static boolean canBeAttacked(FactionAPI faction){
        if (NexConfig.getFactionConfig(faction.getId()).pirateFaction) return false;
        if (!NexConfig.getFactionConfig(faction.getId()).canInvade) return false;
        return true;
    }
    public static boolean canBeAttacked(MarketAPI market){
        return NexUtilsMarket.canBeInvaded(market,false);
    }
    public static boolean canChangeRelations(FactionAPI faction){
        //if (NexConfig.getFactionConfig(faction.getId()).hostileToAll || NexConfig.getFactionConfig(faction.getId()).playableFaction) return false;
        if (!NexConfig.getFactionConfig(faction.getId()).playableFaction) return false;
        if (NexConfig.getFactionConfig(faction.getId()).disableDiplomacy) return false;
        if (Misc.isPirateFaction(faction)) return false;
        return true;
    }
    public static boolean isMinorFaction(FactionAPI faction){
        //ok, so I have lined wether or not something is a minor faction and can be attacked together. this can be overwritten independently, but for now this makes sense I think?
        return !canBeAttacked(faction);
    }
}
