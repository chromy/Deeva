Feature: Deeva should be runnable
    In order to fix a Java program
    As a student
    I want to be able to run Deeva
 
    Scenario: Deeva can be started from the command line
      Given I have a simple Java program
       When I run the command "./run_deeva.py prog"
       Then I see deeva running

