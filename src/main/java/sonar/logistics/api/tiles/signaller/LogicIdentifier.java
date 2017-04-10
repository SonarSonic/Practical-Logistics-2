package sonar.logistics.api.tiles.signaller;

public class LogicIdentifier {

	public String identifier;
	public String parent;
	
	public LogicIdentifier(String identifier,String parent){
		this.identifier=identifier;
		this.parent=parent;
	}
	
}
