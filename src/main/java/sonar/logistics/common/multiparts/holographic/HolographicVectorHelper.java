package sonar.logistics.common.multiparts.holographic;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;

public class HolographicVectorHelper {

    public static final Vec3d X_VEC = new Vec3d(1, 0, 0);
    public static final Vec3d Y_VEC = new Vec3d(0, 1, 0);
    public static final Vec3d Z_VEC = new Vec3d(0, 0, 1);

    public static double toRadians(double degrees){
        return degrees * Math.PI/180D;
    }

    public static Vec3d convertVector(Vec3i vec){
        return new Vec3d(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vec3d getScreenOffset(EnumFacing face){
        return convertVector(face.getDirectionVec()).scale(0.5);
    }

    public static Vec3d getScreenRotation(EnumFacing f){
        double pitch = f.getFrontOffsetY()*90;
        double yaw = f.getAxis().isHorizontal() ? f.getOpposite().getHorizontalAngle() : (f==EnumFacing.UP ? EnumFacing.NORTH.getOpposite(): EnumFacing.NORTH).getHorizontalAngle();
        double roll = 0;

        return new Vec3d(pitch, yaw, roll);
    }


    /**returns a normalised look vector, pitch and yaw should be provided in radians.*/
    public static Vec3d getPlayerLookVector(double pitch, double yaw){
        return new Vec3d(Math.cos(pitch) * Math.sin(yaw), -Math.sin(pitch), Math.cos(pitch) * Math.cos(yaw));
    }

    /**returns a normalised look vector, pitch and yaw should be provided in radians.*/
    public static Vec3d getScreenLookVector(double pitch, double yaw){
        return getPlayerLookVector(pitch, yaw);
    }

    public static boolean isFacingScreen(Vec3d playerV, Vec3d screenV){
        return playerV.dotProduct(screenV) > 0;
    }

    public static double getDistanceToScreen(Vec3d point, Vec3d origin, Vec3d playerV, Vec3d screenV){
        return (-((point.subtract(origin)).dotProduct(screenV)))/(playerV.dotProduct(screenV));
    }

    public static Vec3d getIntersection(Vec3d point, Vec3d origin, Vec3d playerV, Vec3d screenV, double distance){
        return point.add(playerV.scale(distance));
    }

    public static Vec3d getHorizontalVector(Vec3d screenV, Vec3d rollV){
        return screenV.crossProduct(rollV).normalize();
    }

    public static Vec3d getVerticalVector(Vec3d screenV, Vec3d hozV){
        return screenV.crossProduct(hozV).normalize();
    }

    public static Vec3d getRollVector(Vec3d screenV, double roll, double pitch){
        //Vec3d Y_2 = screenV.scale(-1).scale(Math.sin(pitch));
       // return Y_VEC.scale(Math.cos(roll)).add(Y_2.scale(1-Math.cos(roll))).add(screenV.crossProduct(Y_VEC).scale(Math.sin(roll)));
        return Y_VEC.scale(Math.cos(roll))   .subtract(screenV.scale(Math.sin(pitch)).scale(1-Math.cos(roll)))     .add(screenV.crossProduct(Y_VEC.scale(Math.sin(roll))));
    }

    public static Vec3d getHorizontalVectorSpecialCase(double yaw){
        return X_VEC.scale(Math.cos(yaw)).subtract(Z_VEC.scale(Math.sin(yaw)));
    }

    public static Vec3d getVerticalVectorSpecialCase(double yaw){
        return X_VEC.scale(Math.sin(yaw)).add(Z_VEC.scale(Math.cos(yaw)));
    }

    public static Vec3d[] getScreenVectors(EntityHolographicDisplay to, Vec3d screenV){
        Vec3d horizontal, vertical;
        if(to.rotationPitch == 90 || to.rotationPitch == -90){

            double actualYaw = -toRadians(to.rotationYaw + (to.rotationPitch == 90 ? + to.rotationRoll : - to.rotationRoll));

            horizontal = getHorizontalVectorSpecialCase(actualYaw);
            vertical = getVerticalVectorSpecialCase(actualYaw);
            if(to.rotationPitch == -90){
               vertical = vertical.scale(-1);
            }
        }else{
            horizontal = getHorizontalVector(screenV, getRollVector(screenV, toRadians(to.rotationRoll), toRadians(to.rotationPitch)).scale(-1));
            vertical = horizontal.equals(Vec3d.ZERO) ? getVerticalVectorSpecialCase(-toRadians(to.rotationYaw)) : getVerticalVector(screenV, horizontal);
        }
        return new Vec3d[]{horizontal, vertical};
    }

    public static Vec3d getTopLeft(Vec3d origin, Vec3d horizontal, Vec3d vertical, double screenWidth, double screenHeight){
        return origin.subtract(horizontal.scale(screenWidth/2)).add(vertical.scale(screenHeight/2));
    }

    public static Vec3d getTopRight(Vec3d origin, Vec3d horizontal, Vec3d vertical, double screenWidth, double screenHeight){
        return origin.add(horizontal.scale(screenWidth/2)).add(vertical.scale(screenHeight/2));
    }

    public static Vec3d getBottomLeft(Vec3d origin, Vec3d horizontal, Vec3d vertical, double screenWidth, double screenHeight){
        return origin.subtract(horizontal.scale(screenWidth/2)).subtract(vertical.scale(screenHeight/2));
    }

    public static Vec3d getBottomRight(Vec3d origin, Vec3d horizontal, Vec3d vertical, double screenWidth, double screenHeight){
        return origin.add(horizontal.scale(screenWidth/2)).subtract(vertical.scale(screenHeight/2));
    }

    public static double[] getClickedPosition(Vec3d origin, Vec3d intersect, Vec3d horizontal, Vec3d vertical){
        Vec3d pos = intersect.subtract(origin);
        return new double[]{pos.dotProduct(horizontal), pos.dotProduct(vertical)};
    }

    @Nullable
    public static double[] getDisplayLook(Entity from, EntityHolographicDisplay to, double maxDist){
        Vec3d playerV = getPlayerLookVector(toRadians(from.rotationPitch), -toRadians(from.rotationYaw));
        Vec3d screenV = getScreenLookVector(toRadians(to.rotationPitch), -toRadians(to.rotationYaw));
        if(isFacingScreen(playerV, screenV)){
            Vec3d point = from.getPositionEyes(1);
            Vec3d origin = to.getPositionVector();
            double distance = getDistanceToScreen(point, origin, playerV, screenV);
           if(Math.abs(distance) < maxDist){
               Vec3d intersect = getIntersection(point, origin, playerV, screenV, distance);
               Vec3d[] vectors = getScreenVectors(to, screenV);

               Vec3d pos = intersect.subtract(origin);
               double intersect_hoz = pos.dotProduct(vectors[0]);
               double intersect_ver = pos.dotProduct(vectors[1]);

                if(-to.width/2 < intersect_hoz && intersect_hoz < to.width/2 && -to.height/2 < intersect_ver && intersect_ver < to.height/2){
                    return new double[]{to.width - (pos.dotProduct(vectors[0])+to.width/2D), to.height - (pos.dotProduct(vectors[1])+to.height/2D)};
                }
            }
        }
        return null;
    }

}
