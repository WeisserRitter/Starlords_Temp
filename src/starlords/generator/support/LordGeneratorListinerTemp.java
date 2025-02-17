package starlords.generator.support;

import starlords.listeners.LordGeneratorListener;
import starlords.person.PosdoLordTemplate;

public class LordGeneratorListinerTemp extends LordGeneratorListener {
    public int tier = 0;
    public String fief=null;
    @Override
    public void editLord(PosdoLordTemplate lord) {
        lord.ranking=tier;
        lord.fief=fief;
    }
}
