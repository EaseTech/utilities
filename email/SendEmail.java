public class SendEmail {

    public static void main(String[] args) {
        
//        final String username = "jamest";
//        final String password = "oclc*5789";
 
        Properties props = new Properties();
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host", "0.0.0.0");
        props.put("mail.smtp.port", "22225");
 
        Session session = Session.getInstance(props);
 
        try {
 
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply@oclc.org"));
            message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("bhuinyas@oclc.org"));
            message.setSubject("Testing Subject");
            message.setText("Dear Mail Crawler,"
                + "\n\n No spam to my email, please!");
 
            Transport.send(message);
 
            System.out.println("Done");
 
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
