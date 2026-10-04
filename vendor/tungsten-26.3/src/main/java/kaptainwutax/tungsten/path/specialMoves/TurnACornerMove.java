package kaptainwutax.tungsten.path.specialMoves;

import java.util.stream.Stream;

import com.google.common.collect.Streams;

import kaptainwutax.tungsten.TungstenMod;
import kaptainwutax.tungsten.TungstenModDataContainer;
import kaptainwutax.tungsten.agent.Agent;
import kaptainwutax.tungsten.helpers.DirectionHelper;
import kaptainwutax.tungsten.helpers.render.RenderHelper;
import kaptainwutax.tungsten.path.Node;
import kaptainwutax.tungsten.path.PathInput;
import kaptainwutax.tungsten.path.blockSpaceSearchAssist.BlockNode;
import kaptainwutax.tungsten.render.Color;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.LevelReader;

public class TurnACornerMove {

	public static Node generateMove(Node parent, BlockNode nextBlockNode, boolean reverse) {
		LevelReader world = TungstenModDataContainer.world;
		Agent agent = parent.agent;

		float desiredYaw = (float) DirectionHelper.calcYawFromVec3d(agent.getPos(), nextBlockNode.getPos(true));
	    Node newNode = new Node(parent, world, new PathInput(false, false, false, false, false, false, false, agent.pitch, desiredYaw),
	    				new Color(0, 255, 150), parent.cost + 5);
	    
	    boolean jump = false;
        int limit = 0;
        desiredYaw -= reverse ? -90f : 90f;
        while (limit < 8 && !newNode.agent.horizontalCollision) {
        	limit++;
        	RenderHelper.renderNode(newNode);
        	try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            newNode = new Node(newNode, world, new PathInput(true, false, false, false, jump, false, true, agent.pitch, desiredYaw),
            		new Color(0, 255, 150), newNode.cost + 5);
        }
        limit = 0;
		desiredYaw = (float) DirectionHelper.calcYawFromVec3d(agent.getPos(), nextBlockNode.getPos(true));
        while (limit < 8 && !newNode.agent.horizontalCollision) {
        	limit++;
        	RenderHelper.renderNode(newNode);
        	try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            newNode = new Node(newNode, world, new PathInput(true, false, false, false, jump, false, true, agent.pitch, desiredYaw),
            		new Color(0, 255, 150), newNode.cost + 5);
        }
        
        return newNode;
	}
}
