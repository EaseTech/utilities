A simple Java class to test sending of emails.
This client can be used to test whether the emails are being sent or not.

To use this client, you need to have an SMTP server running somewhere and you have to provide the correct values for "mail.smtp.host" and "mail.smtp.port" properties.

We can use fake mail server to test it as well (http://sourceforge.net/projects/fakemail/)

To use fake mail follow the following steps :

1) Download fakemail server from http://sourceforge.net/projects/fakemail/

2) Unzip it in a directory of your choice. Lets assume that directory is "/home/fakemail-python-1.0"

3) Run the following command :
 python /home/fakemail-python-1.0/fakemail.py --host=0.0.0.0 --port=22225 --path=/home/fakemail-python-1.0/mail --log=//home/fakemail-python-1.0/log --background
 
4) Make sure you have "mail" and "log" directory created. If they are not created then create one yourself. Else the mails will not be delivered.

5) Once you have the fake mail server running, simply run SendMail.java class and you should recieve an email in your mail folder.
