package deus.parkube.interfaces;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.lwjgl.util.vector.Vector3f;

public interface IPlayerParkube {
	boolean parkube$isCollidingWithWall();
	Vector3f parkube$getWallPosition();
	boolean parkube$checkWallCollision(Player player, World world, float bbWidth, Vector3f outPosition);
	boolean parkube$isWallSliding();

}
