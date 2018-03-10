package sonar.logistics.client.gui.textedit;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextFormatting;

public enum HotKeyFunctions {

	//cursor move
	HOME((c, i) -> i==Keyboard.KEY_HOME && !GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> gui.cursorPosition.setXToFirst()),//
	END((c, i) -> i==Keyboard.KEY_END && !GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> gui.cursorPosition.setXToLast(gui)),//
	UP((c, i) -> i==Keyboard.KEY_UP && !GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> gui.cursorPosition.moveY(gui, -1)),//
	DOWN((c, i) -> i==Keyboard.KEY_DOWN && !GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> gui.cursorPosition.moveY(gui, -1)),//
	LEFT((c, i) -> i==Keyboard.KEY_LEFT && !GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> gui.cursorPosition.moveX(gui, -1)),//
	RIGHT((c, i) -> i==Keyboard.KEY_RIGHT && !GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> gui.cursorPosition.moveX(gui, 1)),//

	//selection move
	HOME_SHIFT((c, i) -> i==Keyboard.KEY_HOME && GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> {if(gui.checkAndCreateSelection()){gui.selectPosition.setXToFirst();}}),//
	END_SHIFT((c, i) -> i==Keyboard.KEY_END && GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> {if(gui.checkAndCreateSelection()){gui.selectPosition.setXToLast(gui);}}),//	
	UP_SHIFT((c, i) -> i==Keyboard.KEY_UP && GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> {if(gui.checkAndCreateSelection()){gui.selectPosition.moveY(gui, -1);}}),//
	DOWN_SHIFT((c, i) -> i==Keyboard.KEY_DOWN && GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> {if(gui.checkAndCreateSelection()){gui.selectPosition.moveY(gui, -1);}}),//
	LEFT_SHIFT((c, i) -> i==Keyboard.KEY_LEFT && GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> {if(gui.checkAndCreateSelection()){gui.selectPosition.moveX(gui, -1);}}),//
	RIGHT_SHIFT((c, i) -> i==Keyboard.KEY_RIGHT && GuiScreen.isShiftKeyDown(), (gui, string, chr, key) -> {if(gui.checkAndCreateSelection()){gui.selectPosition.moveX(gui, 1);}}),//
	
	//format changes
	BOLD((c, i) -> i == Keyboard.KEY_N && GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown() && !GuiScreen.isAltKeyDown(), (gui, string, chr, key) -> gui.toggleSpecialFormatting(TextFormatting.BOLD)),//
	ITALIC((c, i) -> i == Keyboard.KEY_I && GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown() && !GuiScreen.isAltKeyDown(), (gui, string, chr, key) -> gui.toggleSpecialFormatting(TextFormatting.ITALIC)),//
	UNDERLINE((c, i) -> i == Keyboard.KEY_U && GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown() && !GuiScreen.isAltKeyDown(), (gui, string, chr, key) -> gui.toggleSpecialFormatting(TextFormatting.UNDERLINE)),//

	ENTER((c, i) -> i==Keyboard.KEY_RETURN || i==Keyboard.KEY_NUMPADENTER, (gui, string, chr, key) -> gui.onCarriageReturn()),//
	COPY((c, i) -> GuiScreen.isKeyComboCtrlC(i), (gui, string, chr, key) -> gui.copy()),//
	PASTE((c, i) -> GuiScreen.isKeyComboCtrlV(i), (gui, string, chr, key) -> gui.paste()),//
	CUT((c, i) -> GuiScreen.isKeyComboCtrlX(i), (gui, string, chr, key) -> gui.cut()),//
	BACKSPACE((c, i) -> i==Keyboard.KEY_BACK, (gui, string, chr, key) -> gui.removeText(key)),//
	DEL((c, i) -> i==Keyboard.KEY_DELETE, (gui, string, chr, key) -> gui.removeText(key)),//
	
	//no line required
	SAVE((c, i) -> i == Keyboard.KEY_S && GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown() && !GuiScreen.isAltKeyDown(), (gui, chr, key) -> gui.save()),//
	SELECT_ALL((c, i) -> GuiScreen.isKeyComboCtrlA(i), (gui, chr, key) -> GuiActions.SELECT_ALL.trigger(gui)), // 
	DESELECT_ALL((c, i) -> c == 4, (gui, chr, key) -> GuiActions.DESELECT_ALL.trigger(gui)), // 

	//if no other hot key is activated, type into the line
	TYPE((c, i) -> ChatAllowedCharacters.isAllowedCharacter(c), (gui, string, chr, key) -> gui.addText(Character.toString(chr))); // 
		
	public IKeyMatch key;
	public ITypingAction typeAction;
	public IHotKeyAction hotKeyAction;
	public boolean requiresLine;

	HotKeyFunctions(IKeyMatch key, ITypingAction action) {
		this.key = key;
		this.typeAction = action;
		this.requiresLine= true;
	}
	
	HotKeyFunctions(IKeyMatch key, IHotKeyAction action) {
		this.key = key;
		this.hotKeyAction = action;
		this.requiresLine= false;
	}

	public static boolean checkFunction(GuiEditStyledStrings gui, StyledStringLine string, char c, int i) {
		for (HotKeyFunctions func : HotKeyFunctions.values()) {
			if ((!func.requiresLine|| string!=null) && func.key.canTriggerFunction(c, i)) {				
				if(func.requiresLine){
					func.typeAction.trigger(gui, string, c, i);							
				}else{		
					func.hotKeyAction.trigger(gui, c, i);			
				}
				return true;
			}
		}
		return false;
	}
}
