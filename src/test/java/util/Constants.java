package util;

public class Constants {

	public static final String sqlFilePath="./SQLFiles";
	public static final String propertyFilePath="./propertyFile/PropertyFile.properties";
	public static final String metaDataFilePath="./TableMetadata/Metadata.xlsx";
	public static final String metaDataFilePath_mysql="./TableMetadata/Metadata_MySQL.xlsx";
	public static final String metaDataQuery="SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = ";
	public static final String metaDataQuery_mysql="SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_NAME = ";
}
