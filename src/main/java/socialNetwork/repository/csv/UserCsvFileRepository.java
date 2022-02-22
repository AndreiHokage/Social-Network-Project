package socialNetwork.repository.csv;

import socialNetwork.domain.models.User;
import socialNetwork.exceptions.CorruptedDataException;
import socialNetwork.exceptions.IOFileException;

/**
 * implementation for AbstractCsvFileRepository for User model
 */
public class UserCsvFileRepository extends AbstractCsvFileRepository<Long, User> {
    /**
     * constructor
     * @param filePath - String - absolute path
     * @throws IOFileException - cannot open file
     */
    public UserCsvFileRepository(String filePath) {
        super(filePath);
    }

    @Override
    public User stringToEntity(String entityAsString) {
        String[] userAttributes = entityAsString.split(",");
        if(userAttributes.length != 4)
            throw new CorruptedDataException("user csv file is corrupted");
        Long id;
        try{
            id = Long.parseLong(userAttributes[0].stripLeading().stripTrailing());
        } catch (NumberFormatException exception){
            throw new CorruptedDataException("user csv file is corrupted");
        }
        String firstName = userAttributes[1].stripLeading().stripTrailing();
        String lastName = userAttributes[2].stripLeading().stripTrailing();
        String username = userAttributes[3].stripLeading().stripTrailing();
        return new User(id, firstName, lastName ,username);
    }

    @Override
    public String entityToString(User entity) {
        return "" +
                entity.getId() + "," +
                entity.getFirstName().stripTrailing().stripLeading() + "," +
                entity.getLastName().stripTrailing().stripLeading() + "," +
                entity.getUsername().stripTrailing().stripLeading();
    }
}
