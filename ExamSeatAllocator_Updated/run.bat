@echo off
if not exist bin mkdir bin
javac -d bin src\SeatAllocationAlgorithm\*.java src\Member3\ExamSeatAllocator.java src\Member3\ExamSeatGUI.java
if errorlevel 1 pause & exit /b 1
java -cp bin Member3.ExamSeatGUI
