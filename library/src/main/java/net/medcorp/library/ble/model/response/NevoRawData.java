package net.medcorp.library.ble.model.response;

/*
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
public abstract class NevoRawData implements ResponseData {

	private static final long serialVersionUID = -2770351097320911999L;

	/** The TYPE of data, the getType function should return this value. */
	public final static String TYPE = "NevoTraining";

	@Override
	public String getType() {
		return TYPE;
	}
	
	public abstract byte[] getRawData();
	
}
