package SeatAllocationAlgorithm;
public class Seat {
 private final int row,column; private Student student;
 public Seat(int row,int column){this.row=row;this.column=column;}
 public boolean isEmpty(){return student==null;} public Student getStudent(){return student;} public void assignStudent(Student s){student=s;}
 public void clear(){student=null;}
}
