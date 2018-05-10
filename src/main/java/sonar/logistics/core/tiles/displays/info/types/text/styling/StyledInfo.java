package sonar.logistics.core.tiles.displays.info.types.text.styling;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMStyledString;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.core.tiles.displays.info.references.ReferenceType;

import javax.xml.ws.Holder;
import java.util.List;

@ASMStyledString(id = StyledInfo.REGISTRY_NAME, modid = PL2Constants.MODID)
public class StyledInfo implements IStyledString, INBTSyncable {

	public InfoUUID uuid;
	public ReferenceType refType;
	public SonarStyling style;
	private static final char REPRESENTIVE = '^';
	
	private String formattingString;
	private StyledStringLine line;

	public StyledInfo() {}

	public StyledInfo(InfoUUID uuid, ReferenceType type) {
		this(uuid, type, new SonarStyling());
	}

	public StyledInfo(InfoUUID uuid, ReferenceType refType, SonarStyling style) {
		this.uuid = uuid;
		this.refType = refType;
		this.style = style;
	}

	public IStyledString setLine(StyledStringLine line){
		this.line = line;
		return this;
	}
	
	public StyledStringLine getLine(){
		return line;
	}

	public InfoUUID getInfoUUID() {
		return uuid;
	}

	public InfoUUID setInfoUUID(InfoUUID uuid) {
		return this.uuid = uuid;
	}

	public ReferenceType getReferenceType() {
		return this.refType;
	}

	public ReferenceType setReferenceType(ReferenceType refType) {
		return this.refType = refType;
	}

	public String setUnformattedString(String s) {
		//InfoUUID uuid = InfoUUID.fromString(s);
		//this.updateTextContents();
		return s;
	}

	public String getUnformattedString() {
		return String.valueOf(REPRESENTIVE);
	}

	public String getTextFormattingStyle() {
		return style.getTextFormattingString();
	}
	
	private String cachedFormattedString = null;

	@Override
	public String getFormattedString() {
		if(cachedFormattedString == null){
			cachedFormattedString = getTextFormattingStyle() + refType.getRefString(ClientInfoHandler.instance().getInfoMap().get(uuid));
		}
		return cachedFormattedString;
	}

	@Override
	public SonarStyling setStyle(SonarStyling f) {
		style = f;
		onStyleChanged();
		return getStyle();
	}

	@Override
	public SonarStyling getStyle() {
		return style;
	}

	@Override
	public void onStyleChanged() {
		this.updateTextContents();
	}

	public void updateTextContents() {
		this.cachedWidth = -1;
		this.cachedFormattedString = null;
		if (this.getLine() != null) {
			this.getLine().updateTextContents();
		}
	}

	public void updateTextScaling() {
		if (this.getLine() != null) {
			this.getLine().updateTextScaling();
		}
	}


	@Override
	public String toString() {
		return getFormattedString();
	}

	@Override
	public int getStringLength() {
		return getUnformattedString().length();
	}

	private int cachedWidth = -1;

	@Override
	public int getStringWidth() {
		if (cachedWidth == -1) {
			cachedWidth = RenderHelper.fontRenderer.getStringWidth(getFormattedString());
		}
		return cachedWidth;
	}

	@Override
	public IStyledString copy() {
		return new StyledInfo(uuid, refType, style.copy());
	}

	@Override
	public Tuple<Character, Integer> getCharClicked(int yPos, Holder<Double> subClickX, Holder<Double> subClickY) {
		return new Tuple(REPRESENTIVE, 0);
	}

	@Override
	public List<InfoUUID> getInfoReferences(){
		return Lists.newArrayList(uuid);
	}
	
	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		(uuid = new InfoUUID()).readData(nbt, type);
		(style = new SonarStyling()).readData(nbt, type);
		refType = ReferenceType.values()[nbt.getInteger("rt")];
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		uuid.writeData(nbt, type);
		style.writeData(nbt, type);
		nbt.setInteger("rt", refType.ordinal());
		return nbt;
	}

	@Override
	public boolean canCombine(IStyledString ss) {
		//return ss instanceof StyledInfo && ss.getStyle().matching(style);
		return false;
	}

	public void combine(IStyledString ss) {
		//int previousLength = getStringLength();
		//this.setUnformattedString(this.getUnformattedString() + ss.getUnformattedString());
	}

	public static final String REGISTRY_NAME = "s_i";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
}
