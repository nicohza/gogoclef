package kaptainwutax.tungsten.render;

import kaptainwutax.tungsten.TungstenModDataContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class Line extends Renderer {

    public Vec3 start;
    public Vec3 end;
    public Color color;

    public Line() {
        this(Vec3.ZERO, Vec3.ZERO, Color.WHITE);
    }

    public Line(Vec3 start, Vec3 end) {
        this(start, end, Color.WHITE);
    }

    public Line(Vec3 start, Vec3 end, Color color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }

    @Override
    public void render() {
        if (this.start == null || this.end == null || this.color == null) return;
        int argb = 0xff000000 | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        net.minecraft.gizmos.Gizmos.line(this.start, this.end, argb, 2.0F);
    }

    @Override
    public BlockPos getPos() {
        double x = (this.end.x() - this.start.x()) / 2 + this.start.x();
        double y = (this.end.y() - this.start.y()) / 2 + this.start.y();
        double z = (this.end.z() - this.start.z()) / 2 + this.start.z();
        return new BlockPos((int) x, (int) y, (int) z);
    }

}
