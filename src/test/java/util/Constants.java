package util;

public class Constants {
	
	public static final int THREAD_POOL_SIZE=5;

	public static final String sqlFilePath="./SQLFiles";
	public static final String propertyFilePath="./propertyFile/PropertyFile.properties";
	
	public static final String mappingSheetPath_oracle="./MappingSheet/MapptingSheet_Oracle.xlsx";
	public static final String mappingSheetPath_mysql="./MappingSheet/MapptingSheet_MySQL.xlsx";
	
	public static final String metaDataFilePath="./TableMetadata/metadata_excel.xlsx";
	public static final String metaDataFilePath_oracle="./TableMetadata/Metadata_Oracle.xlsx";
	public static final String metaDataFilePath_mysql="./TableMetadata/Metadata_MySQL.xlsx";
	
	public static final String metaDataQuery_oracle="SELECT COLUMN_NAME, DATA_TYPE, CHAR_LENGTH,DATA_LENGTH,DATA_PRECISION, DATA_SCALE FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = ";
	public static final String metaDataQuery_mysql="SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_NAME = ";
}
