package org.jcm.model;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_text", length = 150)
    @NotNull
    private String text;

    @Column(name = "dataTime")
    @NotNull
    private LocalDateTime dataTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void prePersist(){
        setDataTime(LocalDateTime.now());
    }
}
