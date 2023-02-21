package com.theplug.kotori.kotoriutils.libs;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.HeadIcon;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;

import javax.inject.Inject;
import java.lang.reflect.Field;

@Slf4j
public class NPCsLibrary {

    @Inject
    private Client client;

    @Setter
    private NPC npc;
    @Setter
    private NPCComposition npcComposition;
    @Setter
    private String actorClassName;
    @Setter
    private String sequenceFieldName;
    @Setter
    private int sequenceGetterMultiplier;
    @Setter
    private String npcCompositionClassName;
    @Setter
    private String overheadIconFieldName;

    private int animationId;
    private HeadIcon headIcon;

    public NPCsLibrary(Client client) {
        this.client = client;
    }

    public int getNPCAnimationID() {
        try {
            Field sequence = client.getClass().getClassLoader().loadClass(actorClassName).getDeclaredField(sequenceFieldName);
            sequence.setAccessible(true);
            int obfuscatedSequenceValue = sequence.getInt(npc);
            sequence.setAccessible(false);
            animationId = obfuscatedSequenceValue * sequenceGetterMultiplier;
        } catch (Exception e) {
            log.error("Failed to get NPC animation id.", e);
        }

        return animationId;
    }

    public HeadIcon getNPCHeadIcon() {
        try {
            Field headIconSpriteIndexes = client.getClass().getClassLoader().loadClass(npcCompositionClassName).getDeclaredField(overheadIconFieldName);
            headIconSpriteIndexes.setAccessible(true);
            short[] headIconSpritesValues = short[].class.cast(headIconSpriteIndexes.get(npcComposition));
            headIconSpriteIndexes.setAccessible(false);
            switch (headIconSpritesValues[0]) {
                case 0:
                    headIcon = HeadIcon.MELEE;
                    break;
                case 1:
                    headIcon = HeadIcon.RANGED;
                    break;
                case 2:
                    headIcon = HeadIcon.MAGIC;
                    break;
                case 3:
                    headIcon = HeadIcon.RETRIBUTION;
                    break;
                case 4:
                    headIcon = HeadIcon.SMITE;
                    break;
                case 5:
                    headIcon = HeadIcon.REDEMPTION;
                    break;
                case 6:
                    headIcon = HeadIcon.RANGE_MAGE;
                    break;
                case 7:
                    headIcon = HeadIcon.RANGE_MELEE;
                    break;
                case 8:
                    headIcon = HeadIcon.MAGE_MELEE;
                    break;
                case 9:
                    headIcon = HeadIcon.RANGE_MAGE_MELEE;
                    break;
                case 10:
                    headIcon = HeadIcon.WRATH;
                    break;
                case 11:
                    headIcon = HeadIcon.SOUL_SPLIT;
                    break;
                case 12:
                    headIcon = HeadIcon.DEFLECT_MELEE;
                    break;
                case 13:
                    headIcon = HeadIcon.DEFLECT_RANGE;
                    break;
                case 14:
                    headIcon = HeadIcon.DEFLECT_MAGE;
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to get NPC overhead icon.", e);
        }

        return headIcon;
    }
}
