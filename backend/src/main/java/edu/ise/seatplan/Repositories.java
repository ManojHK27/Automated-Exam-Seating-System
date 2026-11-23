package edu.ise.seatplan;
import org.springframework.data.jpa.repository.JpaRepository;
interface RoomRepository extends JpaRepository<Room,Long> {}
interface ExamRepository extends JpaRepository<Exam,Long> {}
