# Workblehat code quality demo

The worblehat code quality demo application retrieves all borrowed books from
a (fake) database and checks if the books exceed the allowed borrowing duration.

- In case a book is borrowed for more than three weeks (22 - 28 days), the 
application sends an email to the borrower with a reminder that there's only one 
week left to return the book .
- After the fourth week (29 - 35 days) the borrower is informed that he has 
to pay a 1 € surcharge. 
- Every week after that (>=36 days) the borrower a reminder is sent and his 
surcharge is raised by 2€.

The application is designed to only have a small boilerplate-footprint so changes
have a huge effect on the metrics in sonar.
It can be used to demonstrate how to fix code smells (e.g. code duplication, 
cyclomatic redundancy, etc.) and refactor it according to one's taste.

## Prerequisites

### Sonarqube (https://www.sonarqube.org)

The included sonar is configured to use an embedded database which stores its 
data to `sonar/data/`.

1. startup:

    ```bash
    $ ./sonar/bin/<select-your-os-here>/sonar.sh start
    ```    
    
2. open your browser and navigate to [http://localhost:9000](http://localhost:9000)

3. build application including sonar analysis

    ```bash
    $ mvn clean build sonar:sonar -Psonar
    ```

In order to delete all analysis data, just delete all files and subfolders in 
 directory `sonar/data/`.


### SMTP Mail Relay

The application tries to send mails via a SMTP mail relay server. 
For the demo it is not a good idea to  use a _real_ SMTP service. 
Alternativly you should use a fake service catching all mails that are 
send by the application.

An easy-to-use fake service is the [MailCatcher](https://mailcatcher.me).

#### Installing MailCatcher

In order to install MailCatcher, you need to have 
[ruby](https://www.ruby-lang.org) and [rubygems](https://rubygems.org/) 
installed on your machine.

1. installation

    ```bash
    $ gem install mailcatcher
    ```
    
2. start MailCatcher

    ```bash
    # in daemon mode
    $ mailcatcher
    # in foreground with verbose output
    $ mailcatcher -fv
    ```
    
    The service listens on port `1025` for incoming mails.
  
3. on the MailCatchers webservice at [http://localhost:1080](http://localhost:1080)
you'll find all catched mails.
    
Note: A restart of the mailcatcher erases all mails that have been received before.
