package sonar.logistics.core.tiles.displays.tiles.holographic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.NBTHelper;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenLook;

import javax.annotation.Nullable;

public class HolographicVectorHelper {

    public static final Vec3d X_VEC = new Vec3d(1, 0, 0);
    public static final Vec3d Y_VEC = new Vec3d(0, 1, 0);
    public static final Vec3d Z_VEC = new Vec3d(0, 0, 1);

    /**converts degrees to radians.
     * @param degrees value in degrees
     * @return value in radians */
    public static double toRadians(double degrees){
        return degrees * (Math.PI/180D);
    }

    /**converts radians to degrees.
     * @param radians value in radians
     * @return value in degrees */
    public static double toDegrees(double radians){
        return radians / (Math.PI/180D);
    }

    /** converts a integer vector to a double vector
     * @param vec vector in integers
     * @return vector in doubles */
    public static Vec3d convertVector(Vec3i vec){
        return new Vec3d(vec.getX(), vec.getY(), vec.getZ());
    }

    /** converts a integer vector to a double vector
     * @param vec vector in integers
     * @return vector in doubles */
    public static Vec3i convertVector(Vec3d vec){
        return new Vec3i(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z));
    }

    /** reads a {@link Vec3d} from a {@link NBTTagCompound}
     * @param tagName tag name to read the vector from
     * @param nbt the tag to read the vector from
     * @param type the sync type
     * @return the vector from the tag */
    public static Vec3d readVec3d(String tagName, NBTTagCompound nbt, NBTHelper.SyncType type) {
        NBTTagCompound vecTag = nbt.getCompoundTag(tagName);
        return new Vec3d(vecTag.getDouble("x"), vecTag.getDouble("y"), vecTag.getDouble("z"));
    }

    /** writes a {@link Vec3d} to an {@link NBTTagCompound}
     * @param tagName tag to save the vector under
     * @param nbt the tag to save the vector tag to
     * @param type the sync type
     * @return the given tag with the vector saved to it */
    public static NBTTagCompound writeVec3d(Vec3d vec, String tagName, NBTTagCompound nbt, NBTHelper.SyncType type){
        NBTTagCompound vecTag = new NBTTagCompound();
        vecTag.setDouble("x", vec.x);
        vecTag.setDouble("y", vec.y);
        vecTag.setDouble("z", vec.z);
        nbt.setTag(tagName, vecTag);
        return nbt;
    }

    /**
     * @param face the direction to offset
     * @return the offset vector */
    public static Vec3d getFaceOffset(EnumFacing face, double scale){
        return convertVector(face.getDirectionVec()).scale(scale);
    }

    /** returns the default rotation vector of a screen facing a particular direction
     * @param f the direction the screen is facing
     * @return the screens rotation, in the form of (pitch, yaw, roll)*/
    public static Vec3d getScreenRotation(EnumFacing f){
        double pitch = f.getFrontOffsetY()*90;
        double yaw = f.getAxis().isHorizontal() ? f.getOpposite().getHorizontalAngle() : (f==EnumFacing.UP ? EnumFacing.NORTH.getOpposite(): EnumFacing.NORTH).getHorizontalAngle();
        double roll = 0;

        return new Vec3d(pitch, yaw, roll);
    }

    /** returns a normalised look vector from the given rotations
     * @param pitch the pitch of the vector in degrees
     * @param yaw the yaw in degrees
     * @return the normalised vector */
    public static Vec3d getLookVector(double pitch, double yaw){
        return getLookVectorRadians(toRadians(pitch), -toRadians(yaw));
    }

    /** returns a normalised look vector from the given rotations
     * @param pitch the pitch of the vector in radians
     * @param yaw the yaw in radians
     * @return the normalised vector */
    private static Vec3d getLookVectorRadians(double pitch, double yaw){
        return new Vec3d(Math.cos(pitch) * Math.sin(yaw), -Math.sin(pitch), Math.cos(pitch) * Math.cos(yaw));
    }

    /** calculates if the player is facing the correct side of the screen
     * @param playerV the players look vector
     * @param screenV the screens look vector
     * @return if the player is facing the screen */
    public static boolean isFacingScreen(Vec3d playerV, Vec3d screenV){
        return playerV.dotProduct(screenV) > 0;
    }

    /** calculates the player's distance from the screen
     * @param lookOrigin the player's eye position, relative to the world
     * @param screenOrigin the centre of the screen, relative to the world
     * @param playerV the player's look vector
     * @param screenV the screens's rotation vector
     * @return the exact distance to the screen from the player */
    public static double getDistanceToScreen(Vec3d lookOrigin, Vec3d screenOrigin, Vec3d playerV, Vec3d screenV){
        return (-((lookOrigin.subtract(screenOrigin)).dotProduct(screenV)))/(playerV.dotProduct(screenV));
    }

    /** used to find the exact position of intersection in the world
     * @param lookOrigin the player's eye position
     * @param playerV the players look vector
     * @param distance the exact distance to the screen.
     * @return the point of intersection, the exact position in the world. */
    public static Vec3d getIntersection(Vec3d lookOrigin, Vec3d playerV, double distance){
        return lookOrigin.add(playerV.scale(distance));
    }

    /** calculates the screens horizontal vector
     * @param screenV the screens's rotation vector
     * @param rollV the screens's roll vector
     * @return the horizontal vector)*/
    public static Vec3d getHorizontalVector(Vec3d screenV, Vec3d rollV){
        return screenV.crossProduct(rollV).normalize();
    }

    /** calculates the screens vertical vector
     * @param screenV the screens's rotation vector
     * @param hozV the screen's horizontal vector
     * @return the vectical vector */
    public static Vec3d getVerticalVector(Vec3d screenV, Vec3d hozV){
        return screenV.crossProduct(hozV).normalize();
    }

    /** calculates the screens roll vector
     * @param screenV the screens's rotation vector
     * @param roll the screens roll in radians
     * @param pitch the screens pitch in radians
     * @return the screens roll vector*/
    public static Vec3d getRollVector(Vec3d screenV, double roll, double pitch){
        return Y_VEC.scale(Math.cos(roll)).subtract(screenV.scale(Math.sin(pitch)).scale(1-Math.cos(roll))).add(screenV.crossProduct(Y_VEC.scale(Math.sin(roll))));
    }

    /** calculates the screens horizontal vector if the pitch equals -90 or +90
     * @param yaw the screens yaw in radians
     * @return the screens horizontal vector*/
    public static Vec3d getHorizontalVectorSpecialCase(double yaw){
        return X_VEC.scale(Math.cos(yaw)).subtract(Z_VEC.scale(Math.sin(yaw)));
    }

    /** calculates the screens vertical vector if the pitch equals -90 or +90
     * or the horizontal vector is equivalent to Vec3d.ZERO
     * @param yaw the screens yaw in radians
     * @return the screens vertical vector*/
    public static Vec3d getVerticalVectorSpecialCase(double yaw){
        return X_VEC.scale(Math.sin(yaw)).add(Z_VEC.scale(Math.cos(yaw)));
    }

    /** calculates the screens horizontal and vertical vectors, taking into account the special case to avoid {@link ArithmeticException}
     * @param to the display
     * @param screenV the screen vector
     * @return returns the screen vectors in the form (horizontal, vectical)*/
    public static Vec3d[] getScreenVectors(IDisplay to, Vec3d screenV){
        Vec3d horizontal, vertical;
        if(to.getPitch() == 90 || to.getPitch() == -90){

            double actualYaw = -toRadians(to.getYaw() + (to.getPitch() == 90 ? + to.getRoll() : - to.getRoll()));

            horizontal = getHorizontalVectorSpecialCase(actualYaw);
            vertical = getVerticalVectorSpecialCase(actualYaw);
            if(to.getPitch() == -90){
               vertical = vertical.scale(-1);
            }
        }else{
            horizontal = getHorizontalVector(screenV, getRollVector(screenV, toRadians(to.getRoll()), toRadians(to.getPitch())).scale(-1));
            vertical = horizontal.equals(Vec3d.ZERO) ? getVerticalVectorSpecialCase(-toRadians(to.getYaw())) : getVerticalVector(screenV, horizontal);
        }
        return new Vec3d[]{horizontal, vertical};
    }

    /** calculates the exact position of the top left corner of the screen, relative to the world
     * @param origin the centre of the screen, relative to the world
     * @param horizontal the screens horizontal vector
     * @param vertical the screens vertical vector
     * @param screenWidth the exact screen width
     * @param screenHeight the exact screen height
     * @return the vector of the top left corner*/
    public static Vec3d getTopLeft(Vec3d origin, Vec3d horizontal, Vec3d vertical, double screenWidth, double screenHeight){
        return origin.subtract(horizontal.scale(screenWidth/2)).add(vertical.scale(screenHeight/2));
    }

    /** calculates the exact position of the top right corner of the screen, relative to the world
     * @param origin the centre of the screen, relative to the world
     * @param horizontal the screens horizontal vector
     * @param vertical the screens vertical vector
     * @param screenWidth the exact screen width
     * @param screenHeight the exact screen height
     * @return the vector of the top right corner*/
    public static Vec3d getTopRight(Vec3d origin, Vec3d horizontal, Vec3d vertical, double screenWidth, double screenHeight){
        return origin.add(horizontal.scale(screenWidth/2)).add(vertical.scale(screenHeight/2));
    }

    /** calculates the exact position of the bottom left corner of the screen, relative to the world
     * @param origin the centre of the screen, relative to the world
     * @param horizontal the screens horizontal vector
     * @param vertical the screens vertical vector
     * @param screenWidth the exact screen width
     * @param screenHeight the exact screen height
     * @return the vector of the bottom left corner*/
    public static Vec3d getBottomLeft(Vec3d origin, Vec3d horizontal, Vec3d vertical, double screenWidth, double screenHeight){
        return origin.subtract(horizontal.scale(screenWidth/2)).subtract(vertical.scale(screenHeight/2));
    }

    /** calculates the exact position of the bottom right corner of the screen, relative to the world
     * @param origin the centre of the screen, relative to the world
     * @param horizontal the screens horizontal vector
     * @param vertical the screens vertical vector
     * @param screenWidth the exact screen width
     * @param screenHeight the exact screen height
     * @return the vector of the bottom right corner*/
    public static Vec3d getBottomRight(Vec3d origin, Vec3d horizontal, Vec3d vertical, double screenWidth, double screenHeight){
        return origin.add(horizontal.scale(screenWidth/2)).subtract(vertical.scale(screenHeight/2));
    }

    /** calculates the exact position clicked on the display, can be null
     * @param to the screen
     * @param origin the centre of the screen, relative to the world
     * @param intersect the exact intersect of the players look vector on the screen, relative to the world
     * @param horizontal  the screens horizontal vector
     * @param vertical the screens vertical vector
     * @return the exact click position, taking into account the origin*/
    @Nullable
    public static double[] getClickedPosition(IDisplay to, Vec3d origin, Vec3d intersect, Vec3d horizontal, Vec3d vertical){
        Vec3d pos = intersect.subtract(origin);
        double intersect_hoz = pos.dotProduct(horizontal);
        double intersect_ver = pos.dotProduct(vertical);

        if(-to.getWidth()/2D < intersect_hoz && intersect_hoz < to.getWidth()/2D && -to.getHeight()/2D < intersect_ver && intersect_ver < to.getHeight()/2D){
            return new double[]{to.getWidth() - (pos.dotProduct(horizontal)+to.getWidth()/2D), to.getHeight() - (pos.dotProduct(vertical)+to.getHeight()/2D)};
        }
        return null;
    }

    private static double distance;
    private static Vec3d playerV, screenV, lookOrigin, origin, intersect, horizontal, vertical;
    /** calculates the exact position clicked on the display, can be null
     * @param from the player / or other entity
     * @param to the screen
     * @param maxDist the maximum block reach of the player
     * @return the exact click position, taking into account the origin*/
    @Nullable
    public static double[] getDisplayLook(Entity from, IDisplay to, double maxDist){
        if(from == null || to == null){
            return null;
        }
        playerV = getLookVector(from.rotationPitch, from.rotationYaw);
        screenV = getLookVector(to.getPitch(), to.getYaw());
        if(isFacingScreen(playerV, screenV)){
            lookOrigin = from.getPositionEyes(1);
            origin = to.getScreenOrigin();
            distance = getDistanceToScreen(lookOrigin, origin, playerV, screenV);
            if(Math.abs(distance) < maxDist){
               intersect = getIntersection(lookOrigin, playerV, distance);
               Vec3d[] vectors = getScreenVectors(to, screenV);
               horizontal = vectors[0]; vertical = vectors[1];
               return getClickedPosition(to, origin, intersect, horizontal, vertical);
            }
        }
        return null;
    }

    @Nullable
    public static DisplayScreenClick createClick(EntityPlayer player, IDisplay display, BlockInteractionType type){
        DisplayScreenClick position = new DisplayScreenClick();
        double[] clickPosition = getDisplayLook(player, display, 8);
        if(clickPosition != null) {
            position.identity = display.getInfoContainerID();
            position.setClickPosition(clickPosition);
            position.gsi = display.getGSI();
            position.type = type;
            position.intersect = intersect;
            return position;
        }
        return null;
    }

    @Nullable
    public static DisplayScreenLook createLook(EntityPlayer player, IDisplay display){
        DisplayScreenLook look = new DisplayScreenLook();
        double[] lookPosition = getDisplayLook(player, display, 8);
        if(lookPosition != null) {
            look.identity = display.getInfoContainerID();
            look.setLookPosition(lookPosition);
            return look;
        }
        return null;
    }

    public static DisplayScreenClick createFakeClick(DisplayGSI gsi, double clickX, double clickY, boolean doubleClick, int key) {
        DisplayScreenClick fakeClick = new DisplayScreenClick();
        fakeClick.gsi = gsi;
        fakeClick.type = key == 0 ? BlockInteractionType.LEFT : BlockInteractionType.RIGHT;
        fakeClick.clickX = clickX;
        fakeClick.clickY = clickY;
        fakeClick.intersect = convertVector(gsi.getDisplay().getActualDisplay().getCoords().getBlockPos());
        fakeClick.identity = gsi.getDisplayGSIIdentity();
        fakeClick.doubleClick = doubleClick;
        fakeClick.fakeGuiClick = true;
        return fakeClick;
    }
}