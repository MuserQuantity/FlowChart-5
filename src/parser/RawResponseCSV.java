package parser;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

public class RawResponseCSV {

	public static void exportCSV(String filepath) {

		// Create new worksheet/book
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Flow Export");

		// Create column headers and styles
		HSSFRow headerRow = sheet.createRow(0);

		HSSFCellStyle flowStyle = workbook.createCellStyle();
		flowStyle.setFillForegroundColor(HSSFColor.GOLD.index);
		flowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		HSSFCellStyle serverStyle = workbook.createCellStyle();

		HSSFCellStyle responseStyle = workbook.createCellStyle();

		HSSFCell flowHead = headerRow.createCell(0);
		flowHead.setCellValue("Flow");
		flowHead.setCellStyle(flowStyle);

	}
}
