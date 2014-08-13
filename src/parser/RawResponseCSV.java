package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import log.Alerts;
import log.Logger;
import model.CmdScript;
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

		// Create new workbook
		HSSFWorkbook workbook = new HSSFWorkbook();

		// Create some styles
		HSSFCellStyle goldStyle = workbook.createCellStyle();
		goldStyle.setFillForegroundColor(HSSFColor.GOLD.index);
		goldStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		HSSFCellStyle lightGreyStyle = workbook.createCellStyle();
		lightGreyStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		lightGreyStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		HSSFCellStyle blueStyle = workbook.createCellStyle();
		blueStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
		blueStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		for (Flow f : Session.session) {
			if (f.isEnabled()) {
				// Create a new sheet per enabled Flow
				HSSFSheet sheet = workbook.createSheet(f.getLabel());
				int rowIter = 0;

				// For each Server in this Flow, do special parsing behavior
				for (Server s : f.getServerList()) {
					// Header contains server, time, count
					HSSFRow headerRow = sheet.createRow(rowIter);
					HSSFCell serverHead = headerRow.createCell(0);
					serverHead.setCellValue(s.getServerName());
					serverHead.setCellStyle(goldStyle);
					HSSFCell timeHead = headerRow.createCell(1);
					timeHead.setCellValue("Time");
					timeHead.setCellStyle(blueStyle);
					HSSFCell countHead = headerRow.createCell(2);
					countHead.setCellValue("Count");
					countHead.setCellStyle(blueStyle);

					rowIter++;

					for (CmdScript cs : s.getCmdScriptList()) {

						// Do special parsing behavior for 'cat' results
						if (cs.isCmd() && cs.getData().contains("cat ")) {

							for (String[] logEntry : catLogParser(cs.getResponse())) {
								HSSFRow entryRow = sheet.createRow(rowIter);

								HSSFCell timeCell = entryRow.createCell(1);
								timeCell.setCellValue(logEntry[1]);

								HSSFCell countCell = entryRow.createCell(2);
								countCell.setCellValue(Integer.parseInt(logEntry[0]));

								if (rowIter % 2 == 0) {
									timeCell.setCellStyle(lightGreyStyle);
									countCell.setCellStyle(lightGreyStyle);
								}

								rowIter++;
							}
						}

						rowIter++;
					}
					rowIter++;
				}
				sheet.autoSizeColumn(0);
				sheet.autoSizeColumn(1);
				sheet.autoSizeColumn(2);
			}

		}

		writeCSVFile(filepath, workbook);

	}

	static void writeCSVFile(String filepath, HSSFWorkbook workbook) {
		// Write CSV file to user defined location
		try {
			File f = new File(filepath);
			FileOutputStream out = new FileOutputStream(f);
			workbook.write(out);

			Logger.log("Exported raw response spreadsheet to: " + filepath);

			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			Alerts.infoBox("Exported file currently in use by another process.", "File in Use");
			Logger.log("Could not export raw response spreadsheet, spreadsheet currently in use");
		} catch (IOException e) {
			e.printStackTrace();
			Logger.log("Error exporting raw response spreadsheet to: " + filepath);
		}
	}

	static LinkedList<String[]> catLogParser(String response) {
		LinkedList<String[]> catLog = new LinkedList<String[]>();

		for (String line : response.split("\n")) {
			String[] lineSplit = line.trim().split(" ");

			catLog.add(lineSplit);
		}

		return catLog;
	}

}
