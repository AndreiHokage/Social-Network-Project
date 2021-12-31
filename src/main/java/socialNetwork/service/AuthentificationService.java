package socialNetwork.service;

import socialNetwork.domain.models.Autentification;
import socialNetwork.domain.validators.AuthentificationValidator;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.repository.paging.Page;
import socialNetwork.repository.paging.Pageable;
import socialNetwork.repository.paging.PageableImplementation;
import socialNetwork.repository.paging.PagingRepository;

import java.net.Authenticator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthentificationService {

    PagingRepository<String, Autentification> repoAuthentification;
    EntityValidatorInterface<String,Autentification> authentificationValidator;

    public AuthentificationService(PagingRepository<String, Autentification> repoAuthentification, EntityValidatorInterface<String,Autentification> authentificationValidator) {
        this.repoAuthentification = repoAuthentification;
        this.authentificationValidator = authentificationValidator;
    }

    public Optional<Autentification> saveAuthentificationService(String username,String password){
        Autentification autentification = new Autentification(username, password);
        authentificationValidator.validate(autentification);
        Optional<Autentification> saveAutentification = repoAuthentification.save(autentification);
        return saveAutentification;
    }

    public Optional<Autentification> findAuthentificationService(String username){
        Optional<Autentification> findAuthentification = repoAuthentification.find(username);
        return findAuthentification;
    }

    public List<Autentification> getAllAuthentificationService(){
        return repoAuthentification.getAll();
    }

    private int pageNumber = 0;
    private int pageSize = 1;

    private Pageable pageable;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageable(Pageable pageable){
        this.pageable = pageable;
    }

    public Set<Autentification> getNextAutentifications(){
        this.pageNumber++;
        return getAutentificationsOnPage(this.pageNumber);
    }

    public Set<Autentification> getAutentificationsOnPage(int pageNumber){
        this.pageNumber = pageNumber;
        Pageable pageable = new PageableImplementation(pageNumber,this.pageSize);
        Page<Autentification> autentificationPage = repoAuthentification.getAll(pageable);
        return autentificationPage.getContent().collect(Collectors.toSet());
    }

}
