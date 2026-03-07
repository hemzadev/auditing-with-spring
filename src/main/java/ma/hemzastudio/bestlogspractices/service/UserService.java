package ma.hemzastudio.bestlogspractices.service;

import ma.hemzastudio.bestlogspractices.dao.entity.User;

import java.util.UUID;

public interface UserService {

    public User create(String email, String username);

    public User updateEmail(UUID id, String newEmail);

    public void delete(UUID id);

}
