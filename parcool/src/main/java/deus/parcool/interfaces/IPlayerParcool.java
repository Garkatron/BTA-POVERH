package deus.parcool.interfaces;

import deus.momentum.interfaces.mixin.IPlayerMovementExtra;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.lwjgl.util.vector.Vector3f;

public interface IPlayerParcool {
	boolean parcool$canWallJump();
	Vector3f parcool$getWallPosition();
	boolean parcool$checkWallCollision(Player player, World world, float bbWidth, Vector3f outPosition);
}
