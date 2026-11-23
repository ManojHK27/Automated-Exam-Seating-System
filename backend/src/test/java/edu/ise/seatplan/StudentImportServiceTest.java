package edu.ise.seatplan;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StudentImportServiceTest {
 private MockMultipartFile workbook()throws Exception {
  try(XSSFWorkbook wb=new XSSFWorkbook();ByteArrayOutputStream out=new ByteArrayOutputStream()){
   var one=wb.createSheet("USNs");one.createRow(0).createCell(0).setCellValue("USN");one.createRow(1).createCell(0).setCellValue("000ABC001");one.createRow(2).createCell(0).setCellValue("1BI24IS002");
   var two=wb.createSheet("5th Semester");two.createRow(0).createCell(0).setCellValue("USN");two.createRow(1).createCell(0).setCellValue("1BI25IS001");
   wb.write(out);return new MockMultipartFile("file","students.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",out.toByteArray());
  }
 }
 @Test void readsTextUsnsAcrossSheetsAndSurfacesMissingSemesterMapping()throws Exception {
  var result=new StudentImportService().preview(workbook(),-1,-1,-1,true);var sheets=(List<Map<String,Object>>)result.get("sheets");assertEquals(2,sheets.size());
  var first=(List<ImportRow>)sheets.get(0).get("rows");assertEquals("000ABC001",first.get(0).usn());assertEquals("USNs",first.get(0).sheetName());assertEquals("",first.get(0).sourceSemester());assertEquals(false,sheets.get(0).get("hasSemesterColumn"));
  var second=(List<ImportRow>)sheets.get(1).get("rows");assertEquals("1BI25IS001",second.get(0).usn());assertEquals("5th Semester",second.get(0).sheetName());assertEquals("",second.get(0).sourceSemester());
 }
 @Test void detectsSemesterColumnAndReadsItsStudentLabels()throws Exception {
  MockMultipartFile file;try(XSSFWorkbook wb=new XSSFWorkbook();ByteArrayOutputStream out=new ByteArrayOutputStream()){
   var sheet=wb.createSheet("Students");var header=sheet.createRow(0);header.createCell(0).setCellValue("USN");header.createCell(1).setCellValue("Semester");var row=sheet.createRow(1);row.createCell(0).setCellValue("001BI24IS001");row.createCell(1).setCellValue("3rd Semester");wb.write(out);file=new MockMultipartFile("file","students.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",out.toByteArray());
  }
  var sheets=(List<Map<String,Object>>)new StudentImportService().preview(file,-1,-1,-1,true).get("sheets");var sheet=sheets.get(0);assertEquals(true,sheet.get("hasSemesterColumn"));var rows=(List<ImportRow>)sheet.get("rows");assertEquals("001BI24IS001",rows.get(0).usn());assertEquals("3rd Semester",rows.get(0).sourceSemester());
 }
 @Test void flagsBlankMalformedAndCrossSheetDuplicateRows()throws Exception {
  MockMultipartFile file;try(XSSFWorkbook wb=new XSSFWorkbook();ByteArrayOutputStream out=new ByteArrayOutputStream()){
   var first=wb.createSheet("First");first.createRow(0).createCell(0).setCellValue("USN");first.createRow(1).createCell(0).setCellValue("1BI24IS001");first.createRow(2).createCell(0).setCellValue("");first.createRow(3).createCell(0).setCellValue("bad!");
   var second=wb.createSheet("Second");second.createRow(0).createCell(0).setCellValue("USN");second.createRow(1).createCell(0).setCellValue("1BI24IS001");wb.write(out);file=new MockMultipartFile("file","students.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",out.toByteArray());
  }
  var sheets=(List<Map<String,Object>>)new StudentImportService().preview(file,-1,-1,-1,true).get("sheets");var first=(List<ImportRow>)sheets.get(0).get("rows");var second=(List<ImportRow>)sheets.get(1).get("rows");assertEquals("VALID",first.get(0).status());assertEquals("INVALID",first.get(1).status());assertEquals("INVALID",first.get(2).status());assertEquals("DUPLICATE",second.get(0).status());
 }
}
