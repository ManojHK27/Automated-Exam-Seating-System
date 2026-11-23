package edu.ise.seatplan;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service class StudentImportService {
 Map<String,Object> preview(MultipartFile file,int sheetIndex,int usnColumn,int semesterColumn,boolean detect)throws Exception {
  try(Workbook workbook=WorkbookFactory.create(file.getInputStream())){
   if(workbook.getNumberOfSheets()==0)throw new IllegalArgumentException("The workbook has no worksheets.");
   List<Map<String,Object>> sheets=new ArrayList<>();Set<String> seen=new HashSet<>();
   if(sheetIndex>=0){if(sheetIndex>=workbook.getNumberOfSheets())throw new IllegalArgumentException("The selected worksheet no longer exists in this workbook.");sheets.add(readSheet(workbook.getSheetAt(sheetIndex),sheetIndex,usnColumn,semesterColumn,detect,seen));}
   else for(int i=0;i<workbook.getNumberOfSheets();i++)sheets.add(readSheet(workbook.getSheetAt(i),i,-1,-1,true,seen));
   return Map.of("fileName",Optional.ofNullable(file.getOriginalFilename()).orElse("Imported workbook"),"sheets",sheets);
  }
 }
 private Map<String,Object> readSheet(Sheet sheet,int index,int requestedUsn,int requestedSemester,boolean detect,Set<String> seen){
  DataFormatter formatter=new DataFormatter();int headerIndex=sheet.getFirstRowNum();Row header=sheet.getRow(headerIndex);int columns=header==null?0:Math.max(0,header.getLastCellNum());List<String> headers=new ArrayList<>();for(int c=0;c<columns;c++)headers.add(formatter.formatCellValue(header.getCell(c)).trim());
  int usnColumn=requestedUsn>=0?requestedUsn:0;if(requestedUsn<0&&detect)for(int c=0;c<headers.size();c++)if(headers.get(c).toLowerCase(Locale.ROOT).contains("usn")){usnColumn=c;break;}
  int semesterColumn=requestedSemester;if(requestedSemester==-1){semesterColumn=-1;for(int c=0;c<headers.size();c++){String h=headers.get(c).trim().toLowerCase(Locale.ROOT);if(h.equals("sem")||h.contains("semester")){semesterColumn=c;break;}}}
  boolean hasHeader=headers.stream().anyMatch(h->h.toLowerCase(Locale.ROOT).contains("usn"));int start=hasHeader?headerIndex+1:headerIndex;List<ImportRow> rows=new ArrayList<>();
  for(int i=start;i<=sheet.getLastRowNum();i++){Row row=sheet.getRow(i);String raw=row==null||usnColumn<0?"":formatter.formatCellValue(row.getCell(usnColumn)).trim();String usn=raw.toUpperCase(Locale.ROOT);String status="VALID",message="";if(usn.isBlank()){status="INVALID";message="Blank USN";}else if(!usn.matches("[A-Z0-9]{8,20}")){status="INVALID";message="Expected 8–20 letters or digits";}else if(!seen.add(usn)){status="DUPLICATE";message="Duplicate USN in this import";}String sourceSemester=row==null||semesterColumn<0?"":formatter.formatCellValue(row.getCell(semesterColumn)).trim();rows.add(new ImportRow(i+1,usn,status,message,index,sheet.getSheetName(),sourceSemester));}
  return Map.of("sheetIndex",index,"sheetName",sheet.getSheetName(),"headers",headers,"detectedColumn",usnColumn,"semesterColumn",semesterColumn,"hasSemesterColumn",semesterColumn>=0,"rows",rows);
 }
}

record ImportRow(int row,String usn,String status,String message,int sheetIndex,String sheetName,String sourceSemester){}
