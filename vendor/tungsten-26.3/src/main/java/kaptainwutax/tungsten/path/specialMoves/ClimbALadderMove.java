package kaptainwutax.tungsten.path.specialMoves;

import java.util.stream.Stream;

import com.google.common.collect.Streams;

import kaptainwutax.tungsten.TungstenMod;
import kaptainwutax.tungsten.TungstenModDataContainer;
import kaptainwutax.tungsten.agent.Agent;
import kaptainwutax.tungsten.helpers.DirectionHelper;
import kaptainwutax.tungsten.helpers.DistanceCalculator;
import kaptainwutax.tungsten.helpers.render.RenderHelper;
import kaptainwutax.tungsten.path.Node;
import kaptainwutax.tungsten.path.PathInput;
import kaptainwutax.tungsten.path.blockSpaceSearchAssist.BlockNode;
import kaptainwutax.tungsten.render.Color;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.LevelReader;

public class ClimbALadderMove {

	public static Node generateMove(Node parent, BlockNode nextBlockNode) {
		LevelReader world = TungstenModDataContainer.world;
		Agent agent = parent.agent;
	    Node newNode = new Node(parent, world, new PathInput(false, false, false, false, false, false, false, agent.pitch, agent.yaw),
	    				new Color(0, 255, 150), parent.cost + 0.002);
	    
        int limit = 0;
        while (limit < 8 && Math.abs(newNode.agent.getPos().y - nextBlockNode.getPos(true).y) > 0.2) {
        	limit++;
//        	RenderHelper.renderNode(newNode);
//        	try {
//				Thread.sleep(4);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

            newNode = new Node(newNode, world, new PathInput(false, false, false, false, true, false, false, agent.pitch, agent.yaw),
            		new Color(0, 255, 150), newNode.cost + 0.002);
        }
        
        return newNode;
	}
}
