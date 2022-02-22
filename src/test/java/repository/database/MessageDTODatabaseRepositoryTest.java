package repository.database;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.*;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.repository.database.MessageDTODatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.repository.paging.PagingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageDTODatabaseRepositoryTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    MessageDTODatabaseRepository testRepository;

    public PagingRepository<Long, MessageDTO> getRepository() {
        if(testRepository == null)
            testRepository = new MessageDTODatabaseRepository(url, user, password);
        return testRepository;
    }

    public Long getMaximumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMaximumString = "select max(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMaximumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    public Long getMinimumId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMinimumString = "select min(id) from users";
            PreparedStatement findSql = connection.prepareStatement(findMinimumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }
    }

    public List<User> getUserTestData(){
        return Arrays.asList(
          new User("Gigi","Gigi","e1"),
          new User("Maria","Maria","e2"),
          new User("Bob","Bob","e3")

        );
    }

    public List<Message> getMessageTestData(){
        return Arrays.asList(

          new Message(new User(getMinimumId(),"Gigi","Gigi","e1"),
                  Arrays.asList(new User(getMinimumId() + 1,"Maria","Maria","e2"),
                          new User(getMinimumId() + 2,"Bob","Bob","e3")),
                  "Buna"
                  ),
                new Message(new User(getMinimumId() + 1,"Maria","Maria","e2"),
                        Arrays.asList(new User(getMinimumId(),"Gigi","Gigi","e1"),
                                new User(getMinimumId() + 2,"Bob","Bob","e3")),
                        "Salut"),
                new Message(new User(getMinimumId() + 2,"Bob","Bob","e3"),
                        Arrays.asList(new User(getMinimumId(),"Gigi","Gigi","e1"),
                                new User(getMinimumId() + 1,"Maria","Maria","e2")),
                        "Iondaime Hokage Sama"),
                new Message(new User(getMinimumId() + 2,"Bob","Bob","e3"),
                        Arrays.asList(new User(getMinimumId() + 1,"Maria","Maria","e2")),
                        "Fire Ball Tense")
        );
    }

    public void tearDown(){
        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            var deleteUsersStatement =
                    connection.prepareStatement("DELETE FROM users");
            deleteUsersStatement.executeUpdate();
            var deleteChatMessagesStatement =
                    connection.prepareStatement("DELETE FROM messages_id_correlation");
            deleteChatMessagesStatement.executeUpdate();
            var deleteMessagesStatement =
                    connection.prepareStatement("DELETE FROM messages");
            deleteMessagesStatement.executeUpdate();
            var deleteReplyMessagesStatement =
                    connection.prepareStatement("DELETE FROM replymessages");
            deleteReplyMessagesStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp(){
        tearDown();

        try(Connection connection = DriverManager.getConnection(url, user, password)) {
            String insertStatementString = "INSERT INTO users(first_name, last_name, username) VALUES (?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);

            for(User user : getUserTestData()){
                insertStatement.setString(1, user.getFirstName());
                insertStatement.setString(2, user.getLastName());
                insertStatement.setString(3, user.getUsername());
                insertStatement.executeUpdate();
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private MessageDTO constructDTOMessage(Message message){
        MessageDTO messageDTO = new MessageDTO(message);
        return messageDTO;
    }

    private MessageDTO constructDTOReplyMessage(Message message,Message replyMessage){
        MessageDTO messageDTO = new MessageDTO(message);
        messageDTO.setMessageToRespondTo(replyMessage);
        return messageDTO;
    }

    @Test
    void saveDtoMessagesTest(){
        List<Message> messageList = getMessageTestData();
        Message messageAtIndex0 = messageList.get(0); //1->2,3
        Message messageAtIndex1 = messageList.get(1); //2->1,3
        Message messageAtIndex2 = messageList.get(2); //3->1,2
        Message messageAtIndex3 = messageList.get(3); //3->2

        MessageDTO messageDTOForIndex0 = constructDTOMessage(messageAtIndex0);
        MessageDTO messageDTOForIndex1 = constructDTOMessage(messageAtIndex1);
        MessageDTO replyMessageDTOForIndex2 = constructDTOReplyMessage(messageAtIndex2,messageAtIndex0);
        MessageDTO messageDTOForIndex3 = constructDTOMessage(messageAtIndex3);

        Assertions.assertEquals(Optional.empty(),getRepository().save(messageDTOForIndex0));
        Assertions.assertEquals(Optional.empty(),getRepository().save(messageDTOForIndex1));
        Assertions.assertEquals(Optional.empty(),getRepository().save(replyMessageDTOForIndex2));
        Assertions.assertEquals(Optional.empty(),getRepository().save(messageDTOForIndex3));
    }

    @Test
    void findDTOMessagesTest(){
        List<Message> messageList = getMessageTestData();
        Message messageAtIndex0 = messageList.get(0); //1->2,3
        Message messageAtIndex1 = messageList.get(1); //2->1,3
        Message messageAtIndex2 = messageList.get(2); //3->1,2
        Message messageAtIndex3 = messageList.get(3); //3->2

        MessageDTO messageDTOForIndex0 = constructDTOMessage(messageAtIndex0);
        MessageDTO messageDTOForIndex1 = constructDTOMessage(messageAtIndex1);
        MessageDTO replyMessageDTOForIndex2 = constructDTOReplyMessage(messageAtIndex2,messageAtIndex0);
        MessageDTO messageDTOForIndex3 = constructDTOMessage(messageAtIndex3);
        getRepository().save(messageDTOForIndex0);
        getRepository().save(messageDTOForIndex1);
        getRepository().save(replyMessageDTOForIndex2);
        getRepository().save(messageDTOForIndex3);

        List<Long> allIDOFMessages = getRepository().getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        Long idOfFirstSavedDTOMessage = allIDOFMessages.get(0);
        Assertions.assertEquals(Optional.of (messageDTOForIndex0 ),
                getRepository().find(idOfFirstSavedDTOMessage));
        Long idOfSecondSavedDTOMessage = allIDOFMessages.get(1);
        Assertions.assertEquals(Optional.of( messageDTOForIndex1 ) ,
                getRepository().find(idOfSecondSavedDTOMessage));
        Long idOfThirdSavedDTOMessage = allIDOFMessages.get(2);
        Assertions.assertEquals(Optional.of( replyMessageDTOForIndex2 ) ,
                getRepository().find(idOfThirdSavedDTOMessage));
        Long idOfFourthSavedDToMessage = allIDOFMessages.get(3);
        Assertions.assertEquals( Optional.of( messageDTOForIndex3 ),
                getRepository().find(idOfFourthSavedDToMessage));

        //test the find method for a message that doesn't exist
        Assertions.assertEquals(Optional.empty() , getRepository().find(idOfFirstSavedDTOMessage-1));
    }

    @Test
    void removeMessageTest(){
        List<Message> messageList = getMessageTestData();
        Message messageAtIndex0 = messageList.get(0); //1->2,3
        Message messageAtIndex1 = messageList.get(1); //2->1,3
        Message messageAtIndex2 = messageList.get(2); //3->1,2
        Message messageAtIndex3 = messageList.get(3); //3->2

        MessageDTO messageDTOForIndex0 = constructDTOMessage(messageAtIndex0);
        MessageDTO messageDTOForIndex1 = constructDTOMessage(messageAtIndex1);
        MessageDTO replyMessageDTOForIndex2 = constructDTOReplyMessage(messageAtIndex2,messageAtIndex0);
        MessageDTO messageDTOForIndex3 = constructDTOMessage(messageAtIndex3);
        getRepository().save(messageDTOForIndex0);
        getRepository().save(messageDTOForIndex1);
        getRepository().save(replyMessageDTOForIndex2);
        getRepository().save(messageDTOForIndex3);

        List<Long> allIDOFMessages = getRepository().getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        var idMessage = allIDOFMessages.get(2);
        Assertions.assertEquals(getRepository().find(idMessage).get().getMainMessage(), messageAtIndex2);
        getRepository().remove(idMessage);
        Assertions.assertEquals(getRepository().find(idMessage), Optional.empty());
    }

}
