package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class magellan_BonesawOnHitTest {

    private MockedStatic<Global> globalMock;
    private CombatEngineAPI engineMock;
    private SoundPlayerAPI soundMock;

    @BeforeEach
    public void setUp() {
        globalMock = mockStatic(Global.class);
        engineMock = mock(CombatEngineAPI.class);
        soundMock = mock(SoundPlayerAPI.class);

        globalMock.when(Global::getCombatEngine).thenReturn(engineMock);
        globalMock.when(Global::getSoundPlayer).thenReturn(soundMock);
    }

    @AfterEach
    public void tearDown() {
        if (globalMock != null) {
            globalMock.close();
        }
    }

    @Test
    public void testOnHit_MechShot_DealsSpallingArmorDamage() {
        magellan_BonesawOnHit onHit = new magellan_BonesawOnHit();

        DamagingProjectileAPI projectile = mock(DamagingProjectileAPI.class);
        ShipAPI target = mock(ShipAPI.class);
        ArmorGridAPI armorGrid = mock(ArmorGridAPI.class);
        ApplyDamageResultAPI damageResult = mock(ApplyDamageResultAPI.class);

        Vector2f point = new Vector2f(100f, 100f);
        Vector2f locTarget = new Vector2f(100f, 100f);
        Vector2f vTarget = new Vector2f(0f, 0f);

        when(projectile.getProjectileSpecId()).thenReturn("magellan_bonesaw_mech_shot");
        when(projectile.isFading()).thenReturn(false);
        when(target.getLocation()).thenReturn(locTarget);
        when(target.getVelocity()).thenReturn(vTarget);
        when(target.getArmorGrid()).thenReturn(armorGrid);

        float[][] grid = new float[5][5];
        when(armorGrid.getGrid()).thenReturn(grid);
        when(armorGrid.getCellAtLocation(point)).thenReturn(new int[]{2, 2});
        when(armorGrid.getArmorValue(anyInt(), anyInt())).thenReturn(100f);

        onHit.onHit(projectile, target, point, false, damageResult, engineMock);

        verify(armorGrid, atLeastOnce()).setArmorValue(anyInt(), anyInt(), anyFloat());
        verify(target).syncWithArmorGridState();
        verify(soundMock).playSound(eq("magellan_bonesaw_ftr_crit"), eq(1.0f), eq(1.0f), any(), any());
    }

    @Test
    public void testOnHit_StandardBonesaw_DoesNotDealSpallingArmorDamage() {
        magellan_BonesawOnHit onHit = new magellan_BonesawOnHit();

        DamagingProjectileAPI projectile = mock(DamagingProjectileAPI.class);
        ShipAPI target = mock(ShipAPI.class);
        ArmorGridAPI armorGrid = mock(ArmorGridAPI.class);
        ApplyDamageResultAPI damageResult = mock(ApplyDamageResultAPI.class);

        Vector2f point = new Vector2f(100f, 100f);
        Vector2f locTarget = new Vector2f(100f, 100f);
        Vector2f vTarget = new Vector2f(0f, 0f);

        when(projectile.getProjectileSpecId()).thenReturn("magellan_bonesaw_shot");
        when(projectile.getWeapon()).thenReturn(null);
        when(projectile.isFading()).thenReturn(false);
        when(target.getLocation()).thenReturn(locTarget);
        when(target.getVelocity()).thenReturn(vTarget);
        when(target.getArmorGrid()).thenReturn(armorGrid);

        onHit.onHit(projectile, target, point, false, damageResult, engineMock);

        verify(armorGrid, never()).setArmorValue(anyInt(), anyInt(), anyFloat());
        verify(target, never()).syncWithArmorGridState();
        verify(soundMock).playSound(eq("magellan_bonesaw_ftr_crit"), eq(1.0f), eq(1.0f), any(), any());
    }
}
