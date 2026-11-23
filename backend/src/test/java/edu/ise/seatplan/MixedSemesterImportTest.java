package edu.ise.seatplan;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MixedSemesterImportTest {
 @Test void acceptsExplicitLabelsRegardlessOfFileImportOrder()throws Exception {
  RoomRepository rooms=mock(RoomRepository.class);ExamRepository exams=mock(ExamRepository.class);Exam exam=new Exam();exam.id=8L;exam.seatingMode="MIXED";exam.semester="3rd Semester";exam.semesterB="5th Semester";when(exams.findById(8L)).thenReturn(Optional.of(exam));when(exams.save(any())).thenAnswer(call->call.getArgument(0));
  ApiController api=new ApiController(rooms,exams,new AllocationService(rooms,exams),new StudentImportService());
  List<String> firstOrder=List.of("1BI24IS001","1BI25IS001");Map<String,String> mapped=Map.of("1BI24IS001","3rd Semester","1BI25IS001","5th Semester");Map<String,Object> body=new HashMap<>();body.put("students",firstOrder);body.put("excluded",List.of());body.put("imported",List.of());body.put("groups",mapped);api.students(8L,body);
  assertEquals(mapped,Json.M.readValue(exam.studentGroupsJson,new com.fasterxml.jackson.core.type.TypeReference<Map<String,String>>(){}));
  body.put("students",List.of("1BI25IS001","1BI24IS001"));ApiController reversedApi=new ApiController(rooms,exams,new AllocationService(rooms,exams),new StudentImportService());reversedApi.students(8L,body);assertEquals(mapped,Json.M.readValue(exam.studentGroupsJson,new com.fasterxml.jackson.core.type.TypeReference<Map<String,String>>(){}));
  body.put("groups",Map.of("1BI24IS001","3rd Semester","1BI25IS001","3rd Semester"));assertThrows(IllegalArgumentException.class,()->api.students(8L,body));
 }
}
