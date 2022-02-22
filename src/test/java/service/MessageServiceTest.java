package service;

import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import socialNetwork.domain.models.*;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.exceptions.DatabaseException;
import socialNetwork.exceptions.EntityMissingValidationException;
import socialNetwork.repository.database.MessageDTODatabaseRepository;
import socialNetwork.repository.database.UserDatabaseRepository;
import socialNetwork.service.MessageService;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageServiceTest {
    String url = ApplicationContext.getProperty("network.database.url");
    String user = ApplicationContext.getProperty("network.database.user");
    String password = ApplicationContext.getProperty("network.database.password");
    MessageDTODatabaseRepository testMessageRepository;
    UserDatabaseRepository testUserRepository;
    MessageService testService;

    public MessageService getService() {
        if(testService == null) {
            testMessageRepository = new MessageDTODatabaseRepository(url, user, password);
            testUserRepository = new UserDatabaseRepository(url, user, password);
            testService = new MessageService(testUserRepository, testMessageRepository);
        }
        return testService;
    }

    public List<User> getUserData() {
        return new ArrayList<>(Arrays.asList(
                new User("Gigi","Gigi","g1"),
                new User("Maria","Maria","g2"),
                new User("Bob","Bob","g3"),
                new User("Johnny","Test","g4"),
                new User("Paul","Paul","g5"),
                new User("Andrei","Andrei","g6")
        ));
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

    public Long getMinimumMessageId(){
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            String findMinimumString = "select min(id) from messages";
            PreparedStatement findSql = connection.prepareStatement(findMinimumString);
            ResultSet resultSet = findSql.executeQuery();
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException exception){
            throw new DatabaseException(exception.getMessage());
        }

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

            String insertStatementString = "INSERT INTO users( first_name, last_name,username) VALUES (?,?,?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertStatementString);

            for(User user : getUserData()){
                insertStatement.setString(1, user.getFirstName());
                insertStatement.setString(2, user.getLastName());
                insertStatement.setString(3, user.getUsername());

                insertStatement.executeUpdate();
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    void sendMessagesTest(){
        getService().sendMessagesService(getMinimumId(),
                Arrays.asList(getMinimumId() + 1, getMinimumId() + 2), "Buna");
        getService().sendMessagesService(getMinimumId() + 2,
                Arrays.asList(getMinimumId() + 1), "Noapte buna");
        List<Long> listOfAllId = testMessageRepository.getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        Long idOfTheFirstMessageSend = listOfAllId.get(0);
        Assertions.assertEquals(Arrays.asList(testUserRepository.find(getMinimumId() + 1).get(),
                        testUserRepository.find(getMinimumId() + 2).get()),
                testMessageRepository.find(idOfTheFirstMessageSend).get().getMainMessage().getTo());
        Assertions.assertEquals(testUserRepository.find(getMinimumId()).get(),
                testMessageRepository.find(idOfTheFirstMessageSend).get().getMainMessage().getFrom());
        Assertions.assertEquals("Buna",
                testMessageRepository.find(idOfTheFirstMessageSend).get().getMainMessage().getText());

        Long idOfTheSecondMessageSend = listOfAllId.get(1);
        Assertions.assertEquals(Arrays.asList( testUserRepository.find(getMinimumId() + 1).get() ),
                testMessageRepository.find(idOfTheSecondMessageSend).get().getMainMessage().getTo());
        Assertions.assertEquals(testUserRepository.find(getMinimumId() + 2).get(),
                testMessageRepository.find(idOfTheSecondMessageSend).get().getMainMessage().getFrom());
        Assertions.assertEquals("Noapte buna",
                testMessageRepository.find(idOfTheSecondMessageSend).get().getMainMessage().getText());

//throw exception if the user that send the message doesn't exist
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendMessagesService(getMinimumId() - 1,Arrays.asList(getMinimumId() + 1,getMinimumId()),"gaga"));

//throw exception if one of the receivers doesn't exist
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().sendMessagesService(getMinimumId(),Arrays.asList(getMinimumId() + 1,getMinimumId() + 2,getMinimumId() - 1),"mama"));
    }

    @Test
    void replyMessagesTest(){
        getService().sendMessagesService(getMinimumId(),
                Arrays.asList(getMinimumId() + 1, getMinimumId() + 2), "Buna");
        getService().sendMessagesService(getMinimumId() + 2,
                Arrays.asList(getMinimumId() + 1), "Noapte buna");
        List<Long> listOfAllIdOfAllSentMessages = testMessageRepository.getAll()
                .stream()
                .map( messageDTO -> {
                    return messageDTO.getMainMessage().getId();
                } )
                .toList();

        Long idOfTheReplyMessage = listOfAllIdOfAllSentMessages.get(0);
        getService().respondMessageService(getMinimumId() + 2,idOfTheReplyMessage,"Wake up to reality");
        Optional< MessageDTO > responseMessage = testMessageRepository.getAll()
                .stream()
                .max( (MessageDTO x,MessageDTO y) ->{
                    return x.getMainMessage().getId().compareTo(y.getMainMessage().getId());
                } );
        Assertions.assertEquals(testUserRepository.find(getMinimumId() + 2).get(),
                responseMessage.get().getMainMessage().getFrom());
        Assertions.assertEquals(Arrays.asList( testUserRepository.find(getMinimumId()).get() ,
                testUserRepository.find(getMinimumId() + 1).get() ),
                responseMessage.get().getMainMessage().getTo());
        Assertions.assertEquals("Wake up to reality",
                responseMessage.get().getMainMessage().getText());
        Assertions.assertEquals("Buna",
                responseMessage.get().getMessageToRespondTo().getText());

        Long idOfSecondSentMessage = listOfAllIdOfAllSentMessages.get(1);
        getService().respondMessageService(getMinimumId() + 1, idOfSecondSentMessage,"Nothing ever goes as planned");
        Optional< MessageDTO > responseMessageToSecond = testMessageRepository.getAll()
                .stream()
                .max( (MessageDTO x,MessageDTO y) ->{
                    return x.getMainMessage().getId().compareTo(y.getMainMessage().getId());
                } );
        Assertions.assertEquals("Nothing ever goes as planned",
                responseMessageToSecond.get().getMainMessage().getText());
        Assertions.assertEquals("Noapte buna",
                responseMessageToSecond.get().getMessageToRespondTo().getText());

        //if user that reply doesn't exist
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().respondMessageService(getMinimumId() - 1,idOfTheReplyMessage,"kopac"));
        //if the message that I want to respond doesn't exist
//        Assertions.assertThrows(CorruptedDataException.class,
//                () -> getService().respondMessageService(getMinimumId(),-1L,"casa"));
    }

    @Test
    void conversationMessagesTest(){
        getService().sendMessagesService(getMinimumId(),
                Arrays.asList(getMinimumId() + 1, getMinimumId() + 2), "Buna"); //1 -> 2,3
        getService().sendMessagesService(getMinimumId(), // 1 -> 6
                Arrays.asList(getMaximumId()), "Noapte buna");
        getService().sendMessagesService(getMinimumId() + 1,
                Arrays.asList(getMinimumId(),getMinimumId() + 2), "HELLO"); //2 -> 1,3
        getService().sendMessagesService(getMinimumId() + 1,
                Arrays.asList(getMinimumId(),getMinimumId() + 2,getMaximumId()), "Let's go Kurama"); //2->1,3,6
        List<MessagesToRespondDTO> messagesToRespondDTOList =
                getService().getAllMessagesToRespondForUserService(getMinimumId() + 1);
        Assertions.assertEquals(messagesToRespondDTOList.get(0).getText(),"Buna");
        Assertions.assertEquals(messagesToRespondDTOList.size(),1);

        Long id = messagesToRespondDTOList.get(0).getId();
        getService().respondMessageService(getMinimumId() + 1, id, "Buna si tie");
        List < List<HistoryConversationDTO> > historyConversationDTO =
                getService().historyConversationService(getMinimumId(),getMinimumId() + 1);//1 2

        List<HistoryConversationDTO> chatHistoryConversationDTO = historyConversationDTO.get(1);
        System.out.println(chatHistoryConversationDTO);
        Assertions.assertEquals("Buna",chatHistoryConversationDTO.get(0).getText());
        Assertions.assertEquals("Gigi",chatHistoryConversationDTO.get(0).getFirstName());
        Assertions.assertEquals("HELLO",chatHistoryConversationDTO.get(1).getText());
        Assertions.assertEquals("Maria",chatHistoryConversationDTO.get(1).getFirstName());
        Assertions.assertEquals("Buna si tie",chatHistoryConversationDTO.get(2).getText());
        Assertions.assertEquals("Maria",chatHistoryConversationDTO.get(2).getFirstName());

        chatHistoryConversationDTO = historyConversationDTO.get(0);
        Assertions.assertEquals("Let's go Kurama",chatHistoryConversationDTO.get(0).getText());

        Assertions.assertEquals(2,historyConversationDTO.size());

        //if one of the users doesn't exist
        Assertions.assertThrows(EntityMissingValidationException.class,
                () -> getService().historyConversationService(getMinimumId() - 1,getMinimumId() + 1));
    }

    @Test
    void testGetAllChatForSpecifiedUser(){
        // 0 -> 1
        getService().sendMessagesService(getMinimumId(),
                Arrays.asList( getMinimumId() + 1), "Buna");
        getService().sendMessagesService(getMinimumId(),
                Arrays.asList( getMinimumId() + 1), "Cel fara de nume");
        getService().sendMessagesService(getMinimumId() + 1,
                Arrays.asList(  getMinimumId() ), "Se va ridica din nou");

        //0->1,2
        getService().sendMessagesService(getMinimumId(),
                Arrays.asList(getMinimumId() + 1 , getMinimumId() + 2), "Fara de neam");
        getService().sendMessagesService(getMinimumId() + 1,
                Arrays.asList(getMinimumId() , getMinimumId() + 2), "Fara de nume");
        Long idFirstMessageGroup = getMinimumMessageId() + 3;
        getService().respondMessageService(getMinimumId() + 2,
                idFirstMessageGroup,"Toata lumea asa imi spune");
        getService().respondMessageService(getMinimumId() + 1,
                idFirstMessageGroup,"Imi sta bine, imi sta bine");

        //6->5
        getService().sendMessagesService(getMaximumId(),
                Arrays.asList(getMinimumId() + 2), "Fara de neam");
        getService().sendMessagesService(getMaximumId(),
                Arrays.asList( getMinimumId() + 2), "Fara de nume");

        List<Chat> chatList = getService().getAllChatsSpecifiedUserMessageService(getMinimumId());
        Assertions.assertEquals(2,chatList.size());

        Chat privateChat = null;
        Chat groupChat = null;
        if( chatList.get(0).getMembers().size() == 2){
            privateChat = chatList.get(0);
            groupChat = chatList.get(1);
        }
        else{
            privateChat = chatList.get(1);
            groupChat = chatList.get(0);
        }
        //-------------privateChat
        Assertions.assertEquals(privateChat.getMembers().size(),2);
        Assertions.assertEquals(privateChat.getMessageList().size(),3);
        Assertions.assertEquals("Buna",privateChat.getMessageList().get(0).getText());
        Assertions.assertEquals("Cel fara de nume",privateChat.getMessageList().get(1).getText());
        Assertions.assertEquals("Se va ridica din nou",privateChat.getMessageList().get(2).getText());

        //-------------groupChat
        Assertions.assertEquals(groupChat.getMembers().size(),3);
        Assertions.assertEquals(groupChat.getMessageList().size(),2);
        Assertions.assertEquals("Fara de neam",groupChat.getMessageList().get(0).getText());
        Assertions.assertEquals("Fara de nume",groupChat.getMessageList().get(1).getText());
        //-------------groupChat----ReplyMessages
        Assertions.assertEquals(groupChat.getReplyMessageList().size(),2);
        Assertions.assertEquals("Toata lumea asa imi spune",
                groupChat.getReplyMessageList().get(0).getText());
        Assertions.assertEquals("Imi sta bine, imi sta bine",
                groupChat.getReplyMessageList().get(1).getText());

    }
}
