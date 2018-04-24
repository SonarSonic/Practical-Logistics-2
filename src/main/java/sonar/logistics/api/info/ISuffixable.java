package sonar.logistics.api.info;

/**implemented on {@link IInfo} which can have adjustments added to the info*/
public interface ISuffixable {

	/**gets the suffix for the info*/
    String getSuffix();

	/**gets the prefix for the info*/
    String getPrefix();

	/**gets the raw unedited data from the info*/
    String getRawData();

}
