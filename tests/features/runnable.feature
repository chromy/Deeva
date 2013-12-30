Feature: Deeva should be runnable
    In order to fix a Java program
    As a student
    I want to be able to run Deeva

    Scenario: Deeva can be started from the command line
      Given I have a "helloworld" Java program
       When I run the command "run_deeva.py examples.helloworld.HelloWorld"
       Then I see deeva running

    Scenario: Deeva displays code
       Given I have a "helloworld" Java program
        When I run the command "run_deeva.py examples.helloworld.HelloWorld"
        Then I see deeva running
         And I see "class HelloWorld"

    Scenario: Deeva gives useful error message
        When I run the command "run_deeva.py"
        Then I see "usage:" on stderr
