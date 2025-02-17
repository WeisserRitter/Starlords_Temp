package starlords.listeners;

import com.fs.starfarer.api.characters.PersonAPI;
import starlords.person.PosdoLordTemplate;

import java.util.ArrayList;

public class LordGeneratorListener_base {
    private static ArrayList<LordGeneratorListener_base> listiners = new ArrayList<>();
    public LordGeneratorListener_base(){
        listiners.add(this);
    }
    public void editLordPerson(PersonAPI lord){

    }
    public void editLord(PosdoLordTemplate lord){

    }

    public static void runEditLordPersons(PersonAPI lord){
        for (LordGeneratorListener_base a : listiners){
            a.editLordPerson(lord);
        }
    }
    public static void runEditLord(PosdoLordTemplate lord){
        for (LordGeneratorListener_base a : listiners){
            a.editLord(lord);
        }
    }
    public static void removeListener(LordGeneratorListener_base listener){
        listiners.remove(listener);
    }
}
