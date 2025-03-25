package starlords.util.crossmod;

import com.fs.starfarer.api.util.WeightedRandomPicker;
import second_in_command.SCData;
import second_in_command.SCUtils;
import second_in_command.misc.PotentialPick;
import second_in_command.specs.*;
import starlords.person.Lord;

import java.util.List;
import java.util.Objects;

public class SCLordsFactory {
    public static void populateExecutiveOfficers(Lord lord) {
        SCData scData = SCUtils.getFleetData(lord.getFleet());

        int currentSlot = 0;
        for (String aptitudeId : lord.getTemplate().executiveOfficers.keySet() ) {
            SCOfficer officer = new SCOfficer(lord.getFaction().createRandomPerson(), aptitudeId);
            List<String> lordExecutiveOfficerSkills = lord.getTemplate().executiveOfficers.get(aptitudeId);
            int currentSkill = 0;
            for (String skillId : lordExecutiveOfficerSkills) {
                officer.addSkill(skillId);
                currentSkill++;
            }
            while (currentSkill < 5) {
                WeightedRandomPicker<PotentialPick> unlockableSkills = new WeightedRandomPicker<>();
                SCBaseAptitudePlugin aptitude = officer.getAptitudePlugin();
                List<SCAptitudeSection> sections = aptitude.getSections();
                for (SCAptitudeSection section : sections) {
                    for (String skillId : section.getSkills()) {
                        if (!officer.getActiveSkillIDs().contains(skillId)) {
                            SCBaseSkillPlugin skillPlugin = Objects.requireNonNull(SCSpecStore.getSkillSpec(skillId)).getPlugin();
                            unlockableSkills.add(new PotentialPick(officer, skillPlugin), skillPlugin.getNPCSpawnWeight(lord.getFleet()));
                        }
                    }
                }
                PotentialPick newSkill = unlockableSkills.pick();
                if (newSkill != null) {
                    newSkill.getOfficer().addSkill(newSkill.getSkill().getId());
                }
                currentSkill++;
            }
            scData.setOfficerInSlot(currentSlot, officer);
            currentSlot++;
        }
    }
}
