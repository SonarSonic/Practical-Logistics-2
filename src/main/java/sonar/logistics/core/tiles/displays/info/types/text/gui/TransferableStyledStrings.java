package sonar.logistics.core.tiles.displays.info.types.text.gui;

import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledStringLine;

import javax.annotation.Nonnull;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;

//W.I.P
public class TransferableStyledStrings implements Transferable  {

	public List<StyledStringLine> lines;
	
	public TransferableStyledStrings(StyledStringLine lines){
		
	}
	
	@Nonnull
    @Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if(flavor==DataFlavor.stringFlavor){
			return "";
		}
		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{DataFlavor.stringFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {		
		return flavor==DataFlavor.stringFlavor;
	}

}
