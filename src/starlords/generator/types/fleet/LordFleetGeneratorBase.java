package starlords.generator.types.fleet;

import starlords.generator.support.AvailableShipData;
import starlords.generator.support.ShipData;

public class LordFleetGeneratorBase {
    public AvailableShipData skimPossibleShips(AvailableShipData input){
        AvailableShipData output = new AvailableShipData();
        for(ShipData a : input.getUnorganizedShips().values()){
            ShipData b = filterShipData(a);
            if (b != null){
                output.addShip(b);
            }
        }
        return output;
    }
    public ShipData filterShipData(ShipData data){
        return data;
    }
}
