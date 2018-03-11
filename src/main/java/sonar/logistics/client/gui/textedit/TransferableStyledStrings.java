package sonar.logistics.client.gui.textedit;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import sonar.logistics.api.displays.elements.text.StyledStringLine;

//W.I.P
public class TransferableStyledStrings implements Transferable  {

	public List<StyledStringLine> lines;
	
	public TransferableStyledStrings(StyledStringLine lines){
		
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
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
