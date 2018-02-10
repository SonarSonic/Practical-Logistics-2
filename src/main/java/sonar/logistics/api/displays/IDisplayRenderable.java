package sonar.logistics.api.displays;

public interface IDisplayRenderable {

	void updateRender();

	void render();

	double[] setMaxScaling(double[] scaling);

}
