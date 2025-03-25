package starlords.util.crossmod;

import com.fs.starfarer.api.util.WeightedRandomPicker;
import second_in_command.SCData;
import second_in_command.SCUtils;
import second_in_command.specs.*;
import starlords.person.Lord;
import starlords.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class SCLordsFactory {
    public static void populateExecutiveOfficers(Lord lord) {
        SCData scData = SCUtils.getFleetData(lord.getFleet());

        int currentSlot = 0;
        for (String aptitudeId : lord.getTemplate().executiveOfficers.keySet() ) {
            SCOfficer officer = new SCOfficer(lord.getFaction().createRandomPerson(), aptitudeId);
            List<String> lordExecutiveOfficerSkills = lord.getTemplate().executiveOfficers.get(aptitudeId);
            int currentSkill = 0;
            List<String> unlockedSkills = new ArrayList<>();
            for (String skillId : lordExecutiveOfficerSkills) {
                officer.addSkill(skillId);
                unlockedSkills.add(skillId);
                currentSkill++;
            }
            for (int i = currentSkill; i < 6; i++) {
                String newSkill = pickRandomSkill(officer, unlockedSkills);
                if (newSkill != null) {
                    officer.addSkill(newSkill);
                }
            }
            scData.setOfficerInSlot(currentSlot, officer);

            if (lord.getTemplate().portraitGroup != null) {
                Utils.setPortraitFromGroup(officer.getPerson(), lord.getTemplate().portraitGroup);
            }

            currentSlot++;
        }
    }

    private static String pickRandomSkill(SCOfficer officer, List<String> unlockedSkills) {
        WeightedRandomPicker<String> unlockableSkills = new WeightedRandomPicker<>();
        SCBaseAptitudePlugin aptitude = officer.getAptitudePlugin();
        aptitude.clearSections();
        aptitude.createSections();
        List<SCAptitudeSection> sections = aptitude.getSections();
        for (SCAptitudeSection section : sections) {
            for (String skillId : section.getSkills()) {
                if (!unlockedSkills.contains(skillId)) {
                    SCSkillSpec skillSpec = SCSpecStore.getSkillSpec(skillId);
                    if (skillSpec != null) {
                        unlockableSkills.add(skillId, skillSpec.getNpcSpawnWeight() + 0.01f);
                    } else {
                        unlockableSkills.add(skillId);
                    }
                }
            }
        }
        return unlockableSkills.pick();
    }
}
