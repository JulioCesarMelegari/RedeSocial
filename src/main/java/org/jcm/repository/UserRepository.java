package org.jcm.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.jcm.model.User;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

}
