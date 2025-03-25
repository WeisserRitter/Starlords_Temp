package starlords.util.crossmod;

import second_in_command.SCData;
import second_in_command.SCUtils;
import second_in_command.specs.SCOfficer;
import starlords.person.Lord;

import java.util.List;

public class SCLordsFactory {
    public static void populateExecutiveOfficers(Lord lord) {
        SCData scData = SCUtils.getFleetData(lord.getFleet());

        int currentSlot = 0;
        for (String aptitudeId : lord.getTemplate().executiveOfficers.keySet() ) {
            SCOfficer officer = new SCOfficer(lord.getFaction().createRandomPerson(), aptitudeId);
            List<String> lordExecutiveOfficerSkills = lord.getTemplate().executiveOfficers.get(aptitudeId);
            for (String skillId : lordExecutiveOfficerSkills) {
                officer.addSkill(skillId);
            }
            scData.setOfficerInSlot(currentSlot, officer);
            currentSlot++;
        }
    }
}
