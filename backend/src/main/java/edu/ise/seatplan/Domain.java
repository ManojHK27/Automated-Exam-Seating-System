package edu.ise.seatplan;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;

@Entity @Table(name="rooms") class Room {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String name; public int floor=6; public String description="Regular classroom"; public String mode="DEFAULT"; public String orientation="COLUMNS"; public boolean enabled=true;
 @Lob public String layoutJson;
 public Room(){} public Room(String name,int columns,int benches,int positions){this.name=name; this.layoutJson=Layout.defaultLayout(columns,benches,positions);}
}
@Entity @Table(name="exams") class Exam {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String name, semester, semesterB, section, academicYear, term; public String seatingMode="SINGLE", mixPattern="B_A_B"; public LocalDate examDate; @Lob public String notes;
 @Lob public String studentsJson="[]"; @Lob public String studentGroupsJson="{}"; @Lob public String excludedJson="[]"; @Lob public String importedJson="[]"; @Lob public String roomIdsJson="[]";
 @Lob public String assignmentsJson="[]"; @Lob public String originalJson="[]"; @Lob public String rangeRulesJson="[]"; @Lob public String originalRangeRulesJson="[]"; public boolean saved=false; public long revision=0; public String grouping="RANGE";
 @Transient public String arrangementWarning;
 public Exam(){}
}
record Bench(int positions, List<Integer> blocked, Integer singlePositions) {
 Bench(int positions,List<Integer> blocked){this(positions,blocked,positions);}
}
record Column(List<Bench> benches) {}
record Layout(List<Column> columns,String orientation) {
 Layout(List<Column> columns){this(columns,"COLUMNS");}
 static String defaultLayout(int c,int b,int p){ try{return Json.write(new Layout(java.util.stream.IntStream.range(0,c).mapToObj(i->new Column(java.util.stream.IntStream.range(0,b).mapToObj(j->new Bench(p,List.of(),p)).toList())).toList()));}catch(Exception e){throw new RuntimeException(e);} }
}
record Seat(Long roomId,String room,String column,int bench,int position) {}
record Assignment(String usn,Seat seat) {}
record RoomRange(Long roomId,String startUsn,String endUsn) {}
record StudentRow(int row,String usn,String status,String message) {}
class Json {
 static final com.fasterxml.jackson.databind.ObjectMapper M=new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();
 static String write(Object o)throws Exception{return M.writeValueAsString(o);} static <T>T read(String s,com.fasterxml.jackson.core.type.TypeReference<T> t)throws Exception{return M.readValue(s,t);}
 static Layout layout(String s)throws Exception{return M.readValue(s,Layout.class);}
 static List<String> strings(String s)throws Exception{return M.readValue(s,new com.fasterxml.jackson.core.type.TypeReference<>(){});}
 static List<Assignment> assignments(String s)throws Exception{return M.readValue(s,new com.fasterxml.jackson.core.type.TypeReference<>(){});}
}
