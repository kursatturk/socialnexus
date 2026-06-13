package com.socialnexus.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnexus.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthorUsernameOrderByCreatedAtDesc(String authorUsername);

    List<Post> findByAuthorUsernameInOrderByCreatedAtDesc(Collection<String> authorUsernames);
}
