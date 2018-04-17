package sonar.logistics.api.displays.elements;

public interface IDisplayRenderable {

	/**updates any render properties, before render() is called*/
	void updateRender();

	/**renders the object*/
	void render();

	/** set the maximum scaling for the object*/
	double[] setMaxScaling(double[] scaling);

}
