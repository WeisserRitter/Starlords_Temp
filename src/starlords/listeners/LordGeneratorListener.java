package starlords.listeners;

import com.fs.starfarer.api.characters.PersonAPI;
import starlords.person.PosdoLordTemplate;

import java.util.ArrayList;

public class LordGeneratorListener {
    private static ArrayList<LordGeneratorListener> listiners = new ArrayList<>();
    public LordGeneratorListener(){
        listiners.add(this);
    }
    public void editLordPerson(PersonAPI lord){

    }
    public void editLord(PosdoLordTemplate lord){

    }

    public static void runEditLordPersons(PersonAPI lord){
        for (LordGeneratorListener a : listiners){
            a.editLordPerson(lord);
        }
    }
    public static void runEditLord(PosdoLordTemplate lord){
        for (LordGeneratorListener a : listiners){
            a.editLord(lord);
        }
    }
    public static void removeListener(LordGeneratorListener listener){
        listiners.remove(listener);
    }
}
