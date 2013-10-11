package edu.oregonstate.cope.clientRecorder;

import org.json.simple.JSONObject;

/**
 * Records text changes and test runs from the IDE. Encodes changes in JSON
 * format.
 */

public class ClientRecorder {

	private String IDE;

	protected enum EventType {
		debugLaunch, normalLaunch, fileOpen, fileClose, textChange
	};

	/**
	 * Parameter values are not checked for consistency. Fully qualified names
	 * include the workspace of the file.
	 * 
	 * @param text
	 *            This is the text that was added to the document
	 * @param offset
	 *            This is the location that the text was added in the doc
	 * @param length
	 *            length of the text that was removed
	 * @param sourceFile
	 *            fully qualified name of the file
	 * @param changeOrigin
	 *            who originated the change, ie user, refactoring engine, source
	 *            control
	 */
	public void recordTextChange(String text, int offset, int length, String sourceFile, String changeOrigin) {
		ChangePersister.instance().persist(buildJSONTextChange(text, offset, length, sourceFile, changeOrigin));
	}

	protected JSONObject buildJSONTextChange(String text, int offset, int length, String sourceFile, String changeOrigin) {
		if (text == null || sourceFile == null || changeOrigin == null) {
			throw new RuntimeException("Change parameters cannot be null");
		}
		if (sourceFile.isEmpty())
			throw new RuntimeException("Source File cannot be empty");
		if (changeOrigin.isEmpty())
			throw new RuntimeException("Change Origin cannot be empty");

		JSONObject obj = buildCommonJSONObj(EventType.textChange);
		obj.put("text", text);
		obj.put("offset", offset);
		obj.put("len", length);
		obj.put("sourceFile", sourceFile);
		obj.put("changeOrigin", changeOrigin);
		return obj;
	}

	public void recordDebugLaunch(String fullyQualifiedMainFunction) {
		ChangePersister.instance().persist(buildIDEFileEventJSON(EventType.debugLaunch, fullyQualifiedMainFunction));
	}

	public void recordNormalLaunch(String fullyQualifiedMainFunction) {
		ChangePersister.instance().persist(buildIDEFileEventJSON(EventType.normalLaunch, fullyQualifiedMainFunction));
	}

	public void recordFileOpen(String fullyQualifiedMainFunction) {
		ChangePersister.instance().persist(buildIDEFileEventJSON(EventType.fileOpen, fullyQualifiedMainFunction));
	}

	public void recordFileClose(String fullyQualifiedMainFunction) {
		ChangePersister.instance().persist(buildIDEFileEventJSON(EventType.fileClose, fullyQualifiedMainFunction));
	}

	protected JSONObject buildIDEFileEventJSON(Enum EventType, String fullyQualifiedMainFunction) {
		if (fullyQualifiedMainFunction == null) {
			throw new RuntimeException("Fully Qualified Main Function cannot be null");
		}
		JSONObject obj;
		obj = buildCommonJSONObj(EventType);
		obj.put("fullyQualifiedMain", fullyQualifiedMainFunction);
		return obj;
	}

	public String getIDE() {
		return IDE;
	}

	public void setIDE(String IDE) {
		this.IDE = IDE;
	}

	protected JSONObject buildCommonJSONObj(Enum eventType) {
		JSONObject obj;
		obj = new JSONObject();
		obj.put("IDE", this.getIDE());
		obj.put("eventType", eventType);
		return obj;
	}
}
