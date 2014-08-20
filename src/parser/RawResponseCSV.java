package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
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

		HSSFCellStyle redStyle = workbook.createCellStyle();
		redStyle.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
		redStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		// Create Flow time/count aggregation sheet
		HSSFSheet aggSheet = workbook.createSheet("Flows");

		// Setup Server hostname headers
		HSSFRow serverHeader = aggSheet.createRow(0);

		HSSFCell creationTime = serverHeader.createCell(0);
		creationTime.setCellValue("Report created: " + new Date(System.currentTimeMillis()).toString());

		// Setup item headers for aggregated columns
		HSSFRow aggHeader = aggSheet.createRow(1);
		HSSFCell timeHead = aggHeader.createCell(0);
		timeHead.setCellValue("Time");
		timeHead.setCellStyle(blueStyle);
		// Create a flow label count header for each enabled flow
		int col = 1;
		for (Flow f : Session.session) {
			if (f.isEnabled()) {
				// Only if this flow contains a "zgrep" or "cat" cmd
				isFlow: for (Server s : f.getServerList()) {
					for (CmdScript cs : s.getCmdScriptList()) {
						if (cs.isCmd() && (cs.getData().contains("zgrep ") || cs.getData().contains("cat "))) {
							HSSFCell flowHead = aggHeader.createCell(col);
							flowHead.setCellValue(f.getLabel());
							flowHead.setCellStyle(blueStyle);

							// Also populate respective server hostnames
							HSSFCell serverHead = serverHeader.createCell(col);
							serverHead.setCellValue(s.getServerName());
							serverHead.setCellStyle(goldStyle);
							col++;
							break isFlow;
						}
					}
				}
			}
		}

		int row = 2;
		col = 1;
		// Set granular time column from 00:00 to 23:59
		for (int hour = 0; hour <= 15; hour++) {
			for (int min = 0; min <= 59; min++) {
				String time = numToTime(hour, min);
				HSSFRow tempRow = aggSheet.createRow(row);
				HSSFCell timeCell = tempRow.createCell(0);
				timeCell.setCellValue(time);

				// Set alternating gray row theme
				if (row % 2 == 0)
					timeCell.setCellStyle(lightGreyStyle);

				for (Flow f : Session.session) {
					if (f.isEnabled()) {
						// Only if this flow contains a "zgrep" or "cat" cmd
						fillCount: for (Server s : f.getServerList()) {
							for (CmdScript cs : s.getCmdScriptList()) {
								if (cs.isCmd() && (cs.getData().contains("zgrep ") || cs.getData().contains("cat "))) {
									HSSFCell countCell = tempRow.createCell(col);
									// Find the count value equal to the current
									// time row being populated
									for (String[] logEntry : catLogParser(cs.getResponse())) {
										if (logEntry[0].equals(time)) {
											countCell.setCellValue(Integer.valueOf(logEntry[1]));
											if (row % 2 == 0)
												countCell.setCellStyle(lightGreyStyle);
											col++;
											break fillCount;
										}
									}
									// If the time entry doesn't exist
									countCell.setCellStyle(redStyle);
									col++;
									break fillCount;
								}
							}
						}
					}
				}
				row++;
				col = 1;
			}
		}
		// Auto size columns for aggregate sheet
		for (int i = 1; i < 10; i++) {
			aggSheet.autoSizeColumn(i);
		}

		writeCSVFile(filepath, workbook);
	}

	// Helper method to convert int hour and min to standard String
	static String numToTime(int hour, int min) {
		StringBuilder sb = new StringBuilder();

		String h = String.valueOf(hour);
		String m = String.valueOf(min);

		if (h.length() < 2)
			h = "0" + h;
		if (m.length() < 2)
			m = "0" + m;

		sb.append(h);
		sb.append(":");
		sb.append(m);

		return sb.toString();
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
			String temp = lineSplit[0];
			lineSplit[0] = lineSplit[1];
			lineSplit[1] = temp;

			catLog.add(lineSplit);
		}

		return catLog;
	}

}
