package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import log.Alerts;
import model.Flow;
import model.Server;
import model.Session;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

public class RawResponseCSV {

	public static void exportCSV(String filepath) {
		// Qualify filepath string
		if (!filepath.substring(filepath.length() - 4).equalsIgnoreCase(".xls"))
			filepath += ".xls";

		// Create new worksheet/book
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Flow Export");

		// Create column headers and styles
		HSSFRow headerRow = sheet.createRow(0);

		HSSFCellStyle flowStyle = workbook.createCellStyle();
		flowStyle.setFillForegroundColor(HSSFColor.GOLD.index);
		flowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		HSSFCellStyle serverStyle = workbook.createCellStyle();
		serverStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		serverStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		HSSFCellStyle responseStyle = workbook.createCellStyle();
		responseStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
		responseStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		HSSFCell flowHead = headerRow.createCell(0);
		flowHead.setCellValue("Flow");
		flowHead.setCellStyle(flowStyle);

		HSSFCell serverHead = headerRow.createCell(1);
		serverHead.setCellValue("Server");
		serverHead.setCellStyle(serverStyle);

		HSSFCell responseHead = headerRow.createCell(2);
		responseHead.setCellValue("Response");
		responseHead.setCellStyle(responseStyle);

		// Populate contents of Session into worksheet
		int newRowIndex = 1;
		for (Flow f : Session.session) {
			if (f.isEnabled()) {
				for (Server s : f.getServerList()) {
					HSSFRow tempRow = sheet.createRow(newRowIndex);

					HSSFCell flowCell = tempRow.createCell(0);
					flowCell.setCellValue(f.getLabel());
					flowCell.setCellStyle(flowStyle);

					HSSFCell servCell = tempRow.createCell(1);
					servCell.setCellValue(s.getServerName());

					HSSFCell respCell = tempRow.createCell(2);
					respCell.setCellValue(s.collateResponses());

					if (newRowIndex % 2 == 0) {
						servCell.setCellStyle(serverStyle);
						respCell.setCellStyle(responseStyle);
					}

					newRowIndex++;
				}
			} else {
				HSSFRow tempRow = sheet.createRow(newRowIndex);
				HSSFCell flowCell = tempRow.createCell(0);
				flowCell.setCellValue(f.getLabel() + "<DISABLED>");
				flowCell.setCellStyle(serverStyle);

				HSSFCell servCell = tempRow.createCell(1);
				servCell.setCellValue("");
				servCell.setCellStyle(serverStyle);

				HSSFCell respCell = tempRow.createCell(2);
				respCell.setCellValue("");
				respCell.setCellStyle(serverStyle);

				newRowIndex++;
			}
		}

		// Fit data into columns
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);

		try {
			File f = new File(filepath);
			FileOutputStream out = new FileOutputStream(f);
			workbook.write(out);

			System.out.println("Exported CSV to\n'" + f.getAbsolutePath() + "'");

			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			Alerts.infoBox("Exported file currently in use by another process.", "File in Use");
		} catch (IOException e) {
			e.printStackTrace();
			// TODO logger
		}
	}
}
