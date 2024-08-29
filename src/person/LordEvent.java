package person;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import controllers.LordController;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static util.Constants.DEBUG_MODE;

@Getter
public class LordEvent {
    public enum OffensiveType {
        HARASS(0, DEBUG_MODE ? 1 : 7),
        RAID_GENERIC(1, DEBUG_MODE ? 1 : 7),
        RAID_INDUSTRY(1, DEBUG_MODE ? 1 : 7),
        BOMBARD_TACTICAL(4, DEBUG_MODE ? 2 : 14),
        BOMBARD_SATURATION(6, DEBUG_MODE ? 2 : 14),
        NEX_GROUND_BATTLE(6, DEBUG_MODE ? 1 : 7);

        public final int violence;
        public final int chargeTime; // how long a lord has to hover around a target to initiate offensive

        OffensiveType(int violence, int chargeTime) {
            this.violence = violence;
            this.chargeTime = chargeTime;
        }
    }

    public static final String FEAST = "feast";
    public static final String RAID = "raid";
    public static final String CAMPAIGN = "campaign";

    @Setter
    private long offenseTimestamp;  // When an offensive action's charge time started
    @Setter
    private int totalViolence; // if a raid/campaign causes enough damage, it will end
    @Setter
    private OffensiveType offensiveType;
    @Setter
    private Object battle;
    private final long start;
    private final String type;
    private Lord originator;
    @Setter
    private boolean alive;
    @Setter
    private SectorEntityToken target;
    private List<Lord> participants;
    private List<Lord> opposition; // defenders in a raid/campaign, unused for feast

    public LordEvent(String type, Lord origin) {
        this(type, origin, null);
    }

    public LordEvent(String type, Lord origin, SectorEntityToken target) {
        originator = origin;
        this.type = type;
        this.target = target;
        alive = true;
        participants = new ArrayList<>();
        opposition = new ArrayList<>();
        start = Global.getSector().getClock().getTimestamp();
    }

    public LordAction getAction() {
        switch(type) {
            case FEAST:
                return LordAction.FEAST;
            case RAID:
                return LordAction.RAID;
            case CAMPAIGN:
                return LordAction.CAMPAIGN;
        }
        return null;
    }

    // used on save load to remove outdated lord references
    public void updateReferences() {
        originator = LordController.getLordOrPlayerById(originator.getLordAPI().getId());
    }

    public float getTotalMarines() {
        float marines = originator.getFleet().getCargo().getMarines();
        for (Lord supporter : participants) {
            if (originator.getFleet().getContainingLocation().equals(
                    supporter.getFleet().getContainingLocation())) {
                marines += supporter.getFleet().getCargo().getMarines();
            }
        }
        return marines;
    }

    public float getTotalFuel() {
        float fuel = originator.getFleet().getCargo().getFuel();
        for (Lord supporter : participants) {
            if (originator.getFleet().getContainingLocation().equals(
                    supporter.getFleet().getContainingLocation())) {
                fuel += supporter.getFleet().getCargo().getFuel();
            }
        }
        return fuel;
    }

    public float getTotalArms() {
        float arms = originator.getFleet().getCargo().getCommodityQuantity(Commodities.HAND_WEAPONS);
        for (Lord supporter : participants) {
            if (originator.getFleet().getContainingLocation().equals(
                    supporter.getFleet().getContainingLocation())) {
                arms += supporter.getFleet().getCargo().getCommodityQuantity(Commodities.HAND_WEAPONS);
            }
        }
        return arms;
    }
}
