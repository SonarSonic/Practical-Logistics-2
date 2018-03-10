package sonar.logistics.api.displays.elements;

public interface IDisplayRenderable {

	void updateRender();

	void render();

	double[] setMaxScaling(double[] scaling);

}
