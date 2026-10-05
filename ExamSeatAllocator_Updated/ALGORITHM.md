# Seat Allocation Algorithm

## Objective

Allocate students to classroom seats so that students from the same division are not placed in horizontally or vertically adjacent seats.

## Steps

1. Check that the student list is not empty.
2. Check that the classroom exists and has enough seats.
3. Validate student names, divisions and duplicate roll numbers.
4. Clear any previous classroom allocation.
5. Group students by division.
6. Generate a division pattern using backtracking.
7. For each seat, try a division that still has students and does not match the left or upper adjacent seat.
8. If a choice causes the remaining seats to become impossible, backtrack and try another division.
9. Assign the actual students to the generated division positions.
10. Perform final validation by checking duplicate roll numbers and all horizontal/vertical conflicts.
11. Return a successful allocation result.

## Why backtracking is used

A simple repeating pattern does not work for every possible combination of divisions. Backtracking allows the program to try another valid division when the current choice cannot complete the whole seating arrangement.

## Conflict rule

For every occupied seat, the student's division must be different from:

- the seat on the left,
- the seat on the right,
- the seat above,
- the seat below.

The final validation checks all four directions.

## Complexity note

The pattern search uses backtracking, so its worst-case time can grow quickly as the number of seats and divisions increases. For the intended microproject-sized classrooms, it provides a practical and easy-to-explain solution.
