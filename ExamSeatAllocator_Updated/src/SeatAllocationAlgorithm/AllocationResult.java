package SeatAllocationAlgorithm;
public class AllocationResult {
 private final boolean success; private final String message; private final Classroom classroom;
 public AllocationResult(boolean success,String message,Classroom classroom){this.success=success;this.message=message;this.classroom=classroom;}
 public boolean isSuccess(){return success;} public String getMessage(){return message;} public Classroom getClassroom(){return classroom;}
}
