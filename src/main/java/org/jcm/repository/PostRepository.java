package org.jcm.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.jcm.model.Post;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PostRepository implements PanacheRepository<Post> {

}
